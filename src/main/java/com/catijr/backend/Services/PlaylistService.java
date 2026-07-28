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

    public void deleteMusicById(UUID playlistId, UUID musicId) {
        var playlist = EntityLookup.getOr404(playlistRepository, playlistId);

        if (playlistRepository.musicExistsById(playlistId, musicId)) {
            var music = EntityLookup.getOr404(musicRepository, musicId);
            List<Music> musics = new ArrayList<>(playlist.getSongs());

            musics.removeIf(tgt_music -> tgt_music.getId().equals(musicId));

            playlist.setMusicQtd(playlist.getMusicQtd() - 1);
            playlist.setDuration(playlist.getDuration() - music.getDuration());

            playlist.setSongs(musics);

            playlistRepository.save(playlist);
        } else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
