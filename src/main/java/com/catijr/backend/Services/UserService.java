package com.catijr.backend.Services;

import java.time.Instant;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;
import java.util.function.BiFunction;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.catijr.backend.DTOs.Album.GetAlbumNoMusicsDTO;
import com.catijr.backend.DTOs.Artist.GetArtistDTO;
import com.catijr.backend.DTOs.Music.GetMusicDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;
import com.catijr.backend.Entities.Album;
import com.catijr.backend.Entities.Artist;
import com.catijr.backend.Entities.FollowedArtist;
import com.catijr.backend.Entities.LibraryItem;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Play;
import com.catijr.backend.Entities.PlayKind;
import com.catijr.backend.Entities.Playlist;
import com.catijr.backend.Entities.SavedAlbum;
import com.catijr.backend.Entities.SavedMusic;
import com.catijr.backend.Repositories.AlbumRepository;
import com.catijr.backend.Repositories.ArtistRepository;
import com.catijr.backend.Repositories.FollowedArtistRepository;
import com.catijr.backend.Repositories.MusicRepository;
import com.catijr.backend.Repositories.PlayRepository;
import com.catijr.backend.Repositories.PlaylistRepository;
import com.catijr.backend.Repositories.SavedAlbumRepository;
import com.catijr.backend.Repositories.SavedMusicRepository;
import com.catijr.backend.Mappers.AlbumMapper;
import com.catijr.backend.Mappers.ArtistMapper;
import com.catijr.backend.Mappers.MusicMapper;
import com.catijr.backend.Mappers.PlaylistMapper;
import com.catijr.backend.utils.EntityLookup;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final AlbumRepository       albumRepository;
    private final MusicRepository       musicRepository;
    private final PlaylistRepository    playlistRepository;
    private final ArtistRepository      artistRepository;

    private final SavedMusicRepository      savedMusicRepository;
    private final SavedAlbumRepository      savedAlbumRepository;
    private final FollowedArtistRepository  followedArtistRepository;

    private final PlayRepository        playRepository;

    private final AlbumMapper           albumMapper;
    private final PlaylistMapper        playlistMapper;
    private final ArtistMapper          artistMapper;
    private final MusicMapper           musicMapper;

    // GET das coleções de biblioteca: mais recentemente adicionado primeiro.
    private static final Sort ADDED_AT_DESC = Sort.by(Sort.Direction.DESC, "addedAt");

    // Quantos itens distintos os GET de "recentes" devolvem (derivados de tb_plays).
    private static final int RECENT_LIMIT = 8;


    public List<GetPlaylistNoMusicDTO> getUserPlaylists(){
        // Ordem estável entre mutações: createdAt ASC (playlists antigas ficam no
        // topo, novas entram no fim). NÃO ordenar por updatedAt — reordenar faixas
        // bumpa updatedAt e jogaria a playlist recém-editada pro fim da sidebar.
        // Desempate por id garante ordem total mesmo se dois createdAt coincidirem.
        List<Playlist> playlists = playlistRepository.findAll(
                Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));

        return playlists.stream().map(playlistMapper::toDTO).toList();
    }

    public List<GetArtistDTO> getUserRecentArtists(){
        return recentByKind(PlayKind.ARTIST, artistRepository, Artist::getId, artistMapper::toDTO);
    }

    public List<GetArtistDTO> getUserMostPlayedArtists(){
        List<Artist> artists = artistRepository.findTop5ByOrderByListenersDesc();
        
        return artists.stream().map(artistMapper::toDTO).toList();
    }

    public List<GetMusicDTO> getUserRecentMusics(){
        return recentByKind(PlayKind.MUSIC, musicRepository, Music::getId, musicMapper::toDTO);
    }

    public List<GetMusicDTO> getUserMostPlayedMusics(){
        List<Music> musics = musicRepository.findTop5ByOrderByTimesListenDesc();

        return musics.stream().map(musicMapper::toDTO).toList();
    }

    public List<GetAlbumNoMusicsDTO> getUserRecentAlbums(){
        return recentByKind(PlayKind.ALBUM, albumRepository, Album::getId, albumMapper::toNoMusicsDTO);
    }

    // ------------------------------------------------------------------
    // Reproduções (tb_plays) — fonte de verdade dos GET de "recentes" acima.
    //
    // O frontend só faz POST depois de min(30s, duração/2) de escuta, então não
    // há threshold aqui: toda chamada vira uma linha. A deduplicação ("N itens
    // distintos, mais recente primeiro") mora na LEITURA (recentByKind), não na
    // escrita. "playlist" é aceito e armazenado mesmo sem endpoint de leitura.
    // ------------------------------------------------------------------

    /** POST /user/plays: registra uma reprodução. 400 se kind/id forem inválidos. */
    @Transactional
    public void recordPlay(String kindRaw, String idRaw){
        PlayKind kind = parsePlayKind(kindRaw);
        UUID entityId = parseEntityId(idRaw);

        Instant now = Instant.now();
        playRepository.save(Play.builder()
                .kind(kind)
                .entityId(entityId)
                .playedAt(now)
                .build());

        // Denormaliza lastPlayedAt na entidade de catálogo correspondente ao kind,
        // para o frontend ordenar a biblioteca por recência sem consultar tb_plays.
        // Se a entidade não existir (id desconhecido/apagado), o UPDATE afeta 0 linhas
        // — sem erro, igual à tolerância do insert em tb_plays.
        stampLastPlayed(kind, entityId, now);
    }

    /** Carimba lastPlayedAt na tabela de catálogo do {@code kind} (UPDATE tolerante). */
    private void stampLastPlayed(PlayKind kind, UUID entityId, Instant when){
        switch (kind) {
            case MUSIC    -> musicRepository.touchLastPlayedAt(entityId, when);
            case ALBUM    -> albumRepository.touchLastPlayedAt(entityId, when);
            case ARTIST   -> artistRepository.touchLastPlayedAt(entityId, when);
            case PLAYLIST -> playlistRepository.touchLastPlayedAt(entityId, when);
        }
    }

    /**
     * Fonte única dos três GET de "recentes": pega os ids das últimas
     * {@code RECENT_LIMIT} entidades tocadas (distintas, mais recente primeiro),
     * busca-as no catálogo e as remapeia de volta para a ORDEM de reprodução.
     * Entidades apagadas somem naturalmente (findAllById não as devolve) e uma
     * biblioteca sem plays vira lista vazia — nunca null.
     */
    private <T, D> List<D> recentByKind(
            PlayKind kind,
            JpaRepository<T, UUID> catalogRepo,
            Function<T, UUID> idOf,
            Function<T, D> toDTO) {
        Pageable limit = PageRequest.of(0, RECENT_LIMIT);
        List<UUID> recentIds = playRepository.findRecentEntityIds(kind, limit);

        Map<UUID, T> byId = catalogRepo.findAllById(recentIds).stream()
                .collect(Collectors.toMap(idOf, Function.identity()));

        return recentIds.stream()
                .map(byId::get)              // null se a entidade foi apagada -> filtrada abaixo
                .filter(Objects::nonNull)
                .map(toDTO)
                .toList();
    }

    private PlayKind parsePlayKind(String raw){
        if (raw == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kind ausente");
        }
        try {
            return PlayKind.valueOf(raw.trim().toUpperCase(Locale.ROOT));
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "kind inválido");
        }
    }

    private UUID parseEntityId(String raw){
        if (raw == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id ausente");
        }
        try {
            return UUID.fromString(raw.trim());
        } catch (IllegalArgumentException ex) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "id inválido");
        }
    }

    // ------------------------------------------------------------------
    // Biblioteca: músicas salvas, álbuns salvos e artistas seguidos.
    //
    // POST/DELETE são idempotentes (a PK compartilhada com o id do item já
    // impede duplicatas): retornam sempre 204, tanto se a linha já existia
    // quanto se já estava ausente. O 404 é reservado para quando a própria
    // música/álbum/artista referenciada não existe no catálogo — por isso a
    // checagem de existência é feita no repositório do CATÁLOGO, não no de
    // biblioteca. Um re-POST NÃO atualiza o addedAt (preserva a ordem original).
    // ------------------------------------------------------------------

    public List<GetMusicDTO> getSavedMusics(){
        return listLibrary(savedMusicRepository, SavedMusic::getMusic, musicMapper::toDTO);
    }

    @Transactional
    public void saveMusic(UUID musicId){
        addToLibrary(musicRepository, savedMusicRepository, musicId,
                (music, addedAt) -> SavedMusic.builder().music(music).addedAt(addedAt).build());
    }

    @Transactional
    public void unsaveMusic(UUID musicId){
        removeFromLibrary(musicRepository, savedMusicRepository, musicId);
    }

    public List<GetAlbumNoMusicsDTO> getSavedAlbums(){
        return listLibrary(savedAlbumRepository, SavedAlbum::getAlbum, albumMapper::toNoMusicsDTO);
    }

    @Transactional
    public void saveAlbum(UUID albumId){
        addToLibrary(albumRepository, savedAlbumRepository, albumId,
                (album, addedAt) -> SavedAlbum.builder().album(album).addedAt(addedAt).build());
    }

    @Transactional
    public void unsaveAlbum(UUID albumId){
        removeFromLibrary(albumRepository, savedAlbumRepository, albumId);
    }

    public List<GetArtistDTO> getFollowedArtists(){
        return listLibrary(followedArtistRepository, FollowedArtist::getArtist, artistMapper::toDTO);
    }

    @Transactional
    public void followArtist(UUID artistId){
        addToLibrary(artistRepository, followedArtistRepository, artistId,
                (artist, addedAt) -> FollowedArtist.builder().artist(artist).addedAt(addedAt).build());
    }

    @Transactional
    public void unfollowArtist(UUID artistId){
        removeFromLibrary(artistRepository, followedArtistRepository, artistId);
    }

    // ------------------------------------------------------------------
    // Helpers genéricos das coleções de biblioteca. As três coleções só diferem
    // em: (1) o repositório do catálogo, (2) o repositório da coleção e (3) como
    // extrair/reconstruir a linha da coleção a partir do item do catálogo. Todo
    // o fluxo — ordenação do GET, idempotência do POST/DELETE e o 404 apoiado no
    // CATÁLOGO — é idêntico entre as três e mora aqui.
    // ------------------------------------------------------------------

    /** GET: item do catálogo de cada linha, mapeado para DTO, do mais recente ao mais antigo. */
    private <C, L extends LibraryItem, D> List<D> listLibrary(
            JpaRepository<L, UUID> libraryRepo,
            Function<L, C> toCatalogItem,
            Function<C, D> toDTO) {
        return libraryRepo.findAll(ADDED_AT_DESC).stream()
                .map(toCatalogItem)
                .map(toDTO)
                .toList();
    }

    /**
     * POST idempotente: 404 se o item não existir no catálogo; se já estiver na
     * coleção não faz nada (preserva o addedAt original).
     */
    private <C, L extends LibraryItem> void addToLibrary(
            JpaRepository<C, UUID> catalogRepo,
            JpaRepository<L, UUID> libraryRepo,
            UUID id,
            BiFunction<C, Instant, L> toLibraryRow) {
        C item = EntityLookup.getOr404(catalogRepo, id);
        if (!libraryRepo.existsById(id)) {
            libraryRepo.save(toLibraryRow.apply(item, Instant.now()));
        }
    }

    /** DELETE idempotente: 404 se o item não existir no catálogo; remove da coleção se presente. */
    private void removeFromLibrary(
            JpaRepository<?, UUID> catalogRepo,
            JpaRepository<?, UUID> libraryRepo,
            UUID id) {
        EntityLookup.existsOr404(catalogRepo, id);
        if (libraryRepo.existsById(id)) {
            libraryRepo.deleteById(id);
        }
    }








}
