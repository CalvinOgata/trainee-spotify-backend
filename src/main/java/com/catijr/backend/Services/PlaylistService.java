package com.catijr.backend.Services;


import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;
import com.catijr.backend.DTOs.Playlist.PutPlaylistDTO;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Playlist;
import com.catijr.backend.Repositories.MusicRepository;
import com.catijr.backend.DTOs.Playlist.CreatePlaylistDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistDTO;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Playlist;
import com.catijr.backend.Mappers.PlaylistMapper;
import com.catijr.backend.Repositories.PlaylistRepository;
import com.catijr.backend.utils.EntityLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class PlaylistService {

    private final PlaylistRepository playlistRepository;
    private final MusicRepository musicRepository;
    private final PlaylistMapper playlistMapper;

    // Playlist de sistema "Curtidas": única por usuário e SEMPRE privada — nunca
    // pode virar pública (ver setPrivacy). Identificada pelo nome, como a semeia o DataSeeder.
    private static final String LIKED_SONGS_NAME = "Músicas Curtidas";

    public Playlist getPlaylistById(UUID playlistId) {
        var playlist = EntityLookup.getOr404(playlistRepository, playlistId);

        return playlist;
    }

    public Playlist editPlaylistAttributes(UUID playlistId, PutPlaylistDTO changesDTO) {
        var playlist = EntityLookup.getOr404(playlistRepository, playlistId);

        if (changesDTO.name() != null) {
            playlist.setName(changesDTO.name());
        }

        if (changesDTO.description() != null) {
            playlist.setDescription(changesDTO.description());
        }

        var edited = playlistRepository.save(playlist);

        return edited;
    }

    /**
     * PATCH /playlist/{id}/private — define (set EXPLÍCITO, não toggle) se a playlist
     * é privada. Devolve o summary atualizado (mesmo shape do PUT /attributes) para o
     * frontend atualizar o estado local sem um GET extra.
     *
     * <p>400 se {@code isPrivate} ausente; 404 se a playlist não existir; 409 se for a
     * "Músicas Curtidas" (playlist de sistema, SEMPRE privada — não pode virar pública).
     *
     * <p><b>Autorização (hoje no-op):</b> só o dono deveria poder alterar (403 para os
     * demais). Não há auth nem campo de dono neste projeto single-user — igual ao
     * reorder ({@link PlaylistReorderService}). Quando houver auth, checar o dono AQUI,
     * antes de qualquer escrita, e lançar 403 para não-donos.
     */
    public GetPlaylistNoMusicDTO setPrivacy(UUID playlistId, Boolean isPrivate) {
        if (isPrivate == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "isPrivate ausente");
        }

        Playlist playlist = EntityLookup.getOr404(playlistRepository, playlistId);

        if (LIKED_SONGS_NAME.equals(playlist.getName())) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "A playlist 'Músicas Curtidas' é sempre privada e não pode ser alterada");
        }

        playlist.setPrivate(isPrivate);
        Playlist saved = playlistRepository.save(playlist);

        return playlistMapper.toDTO(saved);
    }

    public Playlist addMusicToPlaylist(UUID playlistId, UUID musicId) {
        var playlist = EntityLookup.getOr404(playlistRepository, playlistId);

        if (!playlistRepository.musicExistsById(playlistId, musicId)) {
            var music = EntityLookup.getOr404(musicRepository, musicId);

            List<Music> musics = new ArrayList<>(playlist.getSongs());

            musics.add(music);

            playlist.setSongs(musics);
            playlist.setMusicQtd(playlist.getMusicQtd() + 1);
            playlist.setDuration(playlist.getDuration() + music.getDuration());

            var updated = playlistRepository.save(playlist);

            return updated;
        } else {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST);
        }
    }

    /**
     * POST /playlist/{playlistId}/musics/{musicId}: adiciona a música na playlist
     * INCONDICIONALMENTE — permite duplicatas (o "tem certeza?" já foi confirmado no
     * frontend). Diferente do PATCH {@link #addMusicToPlaylist}, NÃO checa se a música
     * já está presente: cada chamada acrescenta uma nova ocorrência no fim.
     *
     * <p>Usa o MESMO lock pessimista do reorder ({@link PlaylistRepository#findByIdForUpdate},
     * SELECT ... FOR UPDATE na linha da playlist) para serializar adds concorrentes:
     * dois POSTs simultâneos na mesma playlist executam em sequência, então não
     * disputam a próxima posição (o Hibernate atribui a posição via {@code @OrderColumn}
     * a partir da lista que já contém o item do add anterior). Sem o lock, um dos dois
     * adds seria perdido.
     *
     * <p>{@code musicQtd} e {@code duration} contam OCORRÊNCIAS (somam a cada add), não
     * músicas distintas.
     */
    @Transactional
    public GetPlaylistDTO appendMusicToPlaylist(UUID playlistId, UUID musicId) {
        Playlist playlist = playlistRepository.findByIdForUpdate(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        Music music = EntityLookup.getOr404(musicRepository, musicId);

        List<Music> musics = playlist.getSongs() == null
                ? new ArrayList<>()
                : new ArrayList<>(playlist.getSongs());
        musics.add(music);

        playlist.setSongs(musics);
        playlist.setMusicQtd(playlist.getMusicQtd() + 1);
        playlist.setDuration(playlist.getDuration() + music.getDuration());

        Playlist saved = playlistRepository.save(playlist);

        return playlistMapper.toFullDTO(saved);
    }

    public GetPlaylistNoMusicDTO createPlaylist(CreatePlaylistDTO playlist){
        Playlist playlistEntity = playlistMapper.toEntity(playlist);
        Playlist savedEntity = playlistRepository.save(playlistEntity);

        return playlistMapper.toDTO(savedEntity);
    }


    public void deletePlaylistById(UUID playlistId) {
        EntityLookup.existsOr404(playlistRepository, playlistId);
        playlistRepository.deleteById(playlistId);
    }

    /**
     * DELETE /playlist/{playlistId}/positions/{position}: remove a ocorrência na
     * POSIÇÃO informada (índice 0-based na ordem da tracklist). Substitui o antigo
     * "remove por musicId" (que apagava TODAS as ocorrências) e suporta playlists
     * com duplicatas: apaga exatamente uma linha.
     *
     * <p>Compactação das posições é AUTOMÁTICA: o relacionamento é {@code @OrderColumn}
     * ({@code song_position}), então ao remover o elemento do índice {@code position}
     * da lista e regravá-la, o Hibernate reescreve as posições restantes de forma
     * contígua (0..N-2) — não é preciso um UPDATE manual de "position - 1".
     *
     * <p>Tudo numa transação com o MESMO lock pessimista do reorder/append
     * ({@link PlaylistRepository#findByIdForUpdate}): deletes concorrentes na mesma
     * playlist serializam, sem estado parcial que corromperia a matemática de posição.
     * Posição fora do intervalo (inclusive uma linha que outro delete já removeu) → 404.
     */
    @Transactional
    public GetPlaylistDTO removeMusicAtPosition(UUID playlistId, int position) {
        Playlist playlist = playlistRepository.findByIdForUpdate(playlistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Music> musics = playlist.getSongs() == null
                ? new ArrayList<>()
                : new ArrayList<>(playlist.getSongs());

        if (position < 0 || position >= musics.size()) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }

        Music removed = musics.remove(position);

        playlist.setSongs(musics); // @OrderColumn recompacta song_position (0..N-2)
        playlist.setMusicQtd(playlist.getMusicQtd() - 1);
        playlist.setDuration(playlist.getDuration() - removed.getDuration());

        Playlist saved = playlistRepository.save(playlist);

        return playlistMapper.toFullDTO(saved);
    }
}
