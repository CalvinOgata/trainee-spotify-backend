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
import com.catijr.backend.Entities.LibraryItem;
import com.catijr.backend.Entities.LibraryKind;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Play;
import com.catijr.backend.Entities.PlayKind;
import com.catijr.backend.Entities.Playlist;
import com.catijr.backend.Repositories.AlbumRepository;
import com.catijr.backend.Repositories.ArtistRepository;
import com.catijr.backend.Repositories.LibraryItemRepository;
import com.catijr.backend.Repositories.MusicRepository;
import com.catijr.backend.Repositories.PlayRepository;
import com.catijr.backend.Repositories.PlaylistRepository;
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

    // Biblioteca unificada: músicas salvas, álbuns salvos e artistas seguidos moram
    // numa única tabela polimórfica (library_items), discriminada por kind.
    private final LibraryItemRepository libraryItemRepository;

    private final PlayRepository        playRepository;

    private final AlbumMapper           albumMapper;
    private final PlaylistMapper        playlistMapper;
    private final ArtistMapper          artistMapper;
    private final MusicMapper           musicMapper;

    // Quantos itens distintos os GET de "recentes" devolvem (derivados de tb_plays).
    private static final int RECENT_LIMIT = 8;


    public List<GetPlaylistNoMusicDTO> getUserPlaylists(){
        // Ordem estável entre mutações: createdAt ASC (playlists antigas ficam no
        // topo, novas entram no fim). NÃO ordenar por updatedAt — reordenar faixas
        // bumpa updatedAt e jogaria a playlist recém-editada pro fim da sidebar.
        // Desempate por id garante ordem total mesmo se dois createdAt coincidirem.
        List<Playlist> playlists = playlistRepository.findAll(
                Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));

        // lastPlayedAt derivado de tb_plays (não é mais coluna): o front pode reordenar
        // a sidebar/biblioteca por "tocada recentemente" sem que o backend guarde a coluna.
        List<UUID> ids = playlists.stream().map(Playlist::getId).toList();
        Map<UUID, Instant> recency = recencyMap(PlayKind.PLAYLIST, ids);

        return playlists.stream()
                .map(p -> withLastPlayed(playlistMapper.toDTO(p), recency.get(p.getId())))
                .toList();
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
    // Reproduções (tb_plays) — fonte ÚNICA de recência.
    //
    // O frontend só faz POST depois de min(30s, duração/2) de escuta, então não
    // há threshold aqui: toda chamada vira UMA linha em tb_plays e nada mais (não
    // há mais coluna last_played_at no catálogo para carimbar). Tanto os GET de
    // "recentes" (recentByKind) quanto o lastPlayedAt da biblioteca (recencyMap)
    // derivam desta tabela na leitura. "playlist" é aceito e armazenado.
    // ------------------------------------------------------------------

    /** POST /user/plays: registra uma reprodução. 400 se kind/id forem inválidos. */
    @Transactional
    public void recordPlay(String kindRaw, String idRaw){
        PlayKind kind = parsePlayKind(kindRaw);
        UUID entityId = parseEntityId(idRaw);

        playRepository.save(Play.builder()
                .kind(kind)
                .entityId(entityId)
                .playedAt(Instant.now())
                .build());
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

    /**
     * Última reprodução (lastPlayedAt) de cada id do {@code kind}, derivada de
     * tb_plays. Substitui a antiga coluna denormalizada do catálogo. Ids sem
     * nenhum play não aparecem no mapa (lastPlayedAt fica null no DTO). Lista
     * vazia curto-circuita o IN (:ids) que alguns bancos rejeitam.
     */
    private Map<UUID, Instant> recencyMap(PlayKind kind, List<UUID> ids) {
        if (ids.isEmpty()) {
            return Map.of();
        }
        return playRepository.findLastPlayedByKind(kind, ids).stream()
                .collect(Collectors.toMap(
                        PlayRepository.LastPlayedRow::getEntityId,
                        PlayRepository.LastPlayedRow::getLastPlayedAt));
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
    // Biblioteca: músicas salvas, álbuns salvos e artistas seguidos — todas na
    // tabela library_items, distinguidas pelo kind.
    //
    // POST/DELETE são idempotentes (a PK item_id impede duplicatas): retornam
    // sempre 204, tanto se a linha já existia quanto se já estava ausente. O 404
    // é reservado para quando a própria música/álbum/artista referenciada não
    // existe no CATÁLOGO — por isso a checagem de existência é feita no
    // repositório do catálogo, não no da biblioteca. Um re-POST NÃO atualiza o
    // addedAt (preserva a ordem original).
    // ------------------------------------------------------------------

    public List<GetMusicDTO> getSavedMusics(){
        return listLibrary(LibraryKind.MUSIC, PlayKind.MUSIC, musicRepository,
                Music::getId, musicMapper::toDTO, this::withLastPlayed);
    }

    /** Salva a música e DEVOLVE o DTO (com lastPlayedAt derivado) para o front
     *  inserir o item já na posição de recência correta, sem um GET extra. */
    @Transactional
    public GetMusicDTO saveMusic(UUID musicId){
        Music music = addToLibrary(musicRepository, LibraryKind.MUSIC, musicId);
        return withLastPlayed(musicMapper.toDTO(music), lastPlayedOf(PlayKind.MUSIC, musicId));
    }

    @Transactional
    public void unsaveMusic(UUID musicId){
        removeFromLibrary(musicRepository, musicId);
    }

    public List<GetAlbumNoMusicsDTO> getSavedAlbums(){
        return listLibrary(LibraryKind.ALBUM, PlayKind.ALBUM, albumRepository,
                Album::getId, albumMapper::toNoMusicsDTO, this::withLastPlayed);
    }

    @Transactional
    public GetAlbumNoMusicsDTO saveAlbum(UUID albumId){
        Album album = addToLibrary(albumRepository, LibraryKind.ALBUM, albumId);
        return withLastPlayed(albumMapper.toNoMusicsDTO(album), lastPlayedOf(PlayKind.ALBUM, albumId));
    }

    @Transactional
    public void unsaveAlbum(UUID albumId){
        removeFromLibrary(albumRepository, albumId);
    }

    public List<GetArtistDTO> getFollowedArtists(){
        return listLibrary(LibraryKind.ARTIST, PlayKind.ARTIST, artistRepository,
                Artist::getId, artistMapper::toDTO, this::withLastPlayed);
    }

    @Transactional
    public GetArtistDTO followArtist(UUID artistId){
        Artist artist = addToLibrary(artistRepository, LibraryKind.ARTIST, artistId);
        return withLastPlayed(artistMapper.toDTO(artist), lastPlayedOf(PlayKind.ARTIST, artistId));
    }

    @Transactional
    public void unfollowArtist(UUID artistId){
        removeFromLibrary(artistRepository, artistId);
    }

    // ------------------------------------------------------------------
    // Helpers genéricos da biblioteca. As três coleções só diferem em: (1) o
    // kind (na library e na de plays), (2) o repositório do catálogo, (3) como
    // extrair o id e mapear para DTO, e (4) como carimbar o lastPlayedAt no DTO.
    // O fluxo — ordenar por addedAt, tolerar item apagado, derivar recência de
    // tb_plays, idempotência do POST/DELETE e o 404 apoiado no catálogo — mora aqui.
    // ------------------------------------------------------------------

    /**
     * GET de uma coleção: ids do library_items daquele kind (mais recente primeiro),
     * catálogo buscado por esses ids, remapeado de volta para a ordem de addedAt
     * (itens apagados filtrados), com lastPlayedAt derivado de tb_plays.
     */
    private <C, D> List<D> listLibrary(
            LibraryKind libKind,
            PlayKind playKind,
            JpaRepository<C, UUID> catalogRepo,
            Function<C, UUID> idOf,
            Function<C, D> toDTO,
            BiFunction<D, Instant, D> withLastPlayed) {
        List<UUID> ids = libraryItemRepository.findByKindOrderByAddedAtDesc(libKind).stream()
                .map(LibraryItem::getItemId)
                .toList();

        Map<UUID, C> byId = catalogRepo.findAllById(ids).stream()
                .collect(Collectors.toMap(idOf, Function.identity()));

        Map<UUID, Instant> recency = recencyMap(playKind, ids);

        return ids.stream()
                .map(byId::get)             // null se o item foi apagado do catálogo -> filtrado
                .filter(Objects::nonNull)
                .map(c -> withLastPlayed.apply(toDTO.apply(c), recency.get(idOf.apply(c))))
                .toList();
    }

    /**
     * POST idempotente: 404 se o item não existir no catálogo; se já estiver na
     * biblioteca não faz nada (preserva o addedAt original). Devolve a entidade do
     * catálogo para o caller montar o DTO de resposta.
     */
    private <C> C addToLibrary(JpaRepository<C, UUID> catalogRepo, LibraryKind kind, UUID id) {
        C item = EntityLookup.getOr404(catalogRepo, id);
        if (!libraryItemRepository.existsById(id)) {
            libraryItemRepository.save(LibraryItem.builder()
                    .itemId(id)
                    .kind(kind)
                    .addedAt(Instant.now())
                    .build());
        }
        return item;
    }

    /** lastPlayedAt de um único item (derivado de tb_plays), ou null se nunca tocado. */
    private Instant lastPlayedOf(PlayKind kind, UUID id) {
        return recencyMap(kind, List.of(id)).get(id);
    }

    /** DELETE idempotente: 404 se o item não existir no catálogo; remove da biblioteca se presente. */
    private void removeFromLibrary(JpaRepository<?, UUID> catalogRepo, UUID id) {
        EntityLookup.existsOr404(catalogRepo, id);
        if (libraryItemRepository.existsById(id)) {
            libraryItemRepository.deleteById(id);
        }
    }

    // ------------------------------------------------------------------
    // Overlays de lastPlayedAt: os DTOs são records imutáveis e o MapStruct não
    // preenche mais lastPlayedAt (deixou de ser coluna). Reconstruímos o record
    // trocando só esse campo pelo valor derivado de tb_plays. Um por shape de DTO.
    // ------------------------------------------------------------------

    private GetMusicDTO withLastPlayed(GetMusicDTO d, Instant lastPlayedAt) {
        return new GetMusicDTO(d.id(), d.title(), d.artistId(), d.albumId(), d.playlistsId(),
                d.duration(), d.releaseDate(), d.timesListen(), d.explicit(),
                d.createdAt(), d.updatedAt(), lastPlayedAt, d.imageUrl());
    }

    private GetAlbumNoMusicsDTO withLastPlayed(GetAlbumNoMusicsDTO d, Instant lastPlayedAt) {
        return new GetAlbumNoMusicsDTO(d.id(), d.title(), d.year(), d.artistId(), d.artistName(),
                d.createdAt(), d.updatedAt(), lastPlayedAt, d.imageUrl());
    }

    private GetArtistDTO withLastPlayed(GetArtistDTO d, Instant lastPlayedAt) {
        return new GetArtistDTO(d.id(), d.name(), d.listeners(), d.about(),
                d.createdAt(), d.updatedAt(), lastPlayedAt, d.imageUrl());
    }

    private GetPlaylistNoMusicDTO withLastPlayed(GetPlaylistNoMusicDTO d, Instant lastPlayedAt) {
        return new GetPlaylistNoMusicDTO(d.id(), d.name(), d.description(), d.musicQtd(), d.duration(),
                d.createdAt(), d.updatedAt(), lastPlayedAt, d.imageUrl(), d.isPrivate());
    }
}
