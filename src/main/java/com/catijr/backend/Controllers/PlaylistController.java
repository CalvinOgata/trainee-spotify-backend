package com.catijr.backend.Controllers;


import com.catijr.backend.DTOs.Playlist.GetPlaylistDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;
import com.catijr.backend.DTOs.Playlist.PutPlaylistDTO;
import com.catijr.backend.DTOs.Playlist.SetPlaylistPrivacyDTO;
import com.catijr.backend.DTOs.Playlist.CreatePlaylistDTO;
import com.catijr.backend.Mappers.PlaylistMapper;
import com.catijr.backend.Services.PlaylistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;


@RestController
@RequestMapping("/playlist")
@RequiredArgsConstructor
public class PlaylistController {

    private final PlaylistService playlistService;
    private final PlaylistMapper playlistMapper;

    @GetMapping("/{playlistId}")
    public ResponseEntity<GetPlaylistDTO> getPlaylistById(@PathVariable UUID playlistId) {
        var playlist = playlistService.getPlaylistById(playlistId);

        GetPlaylistDTO responseDTO = playlistMapper.toFullDTO(playlist);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{playlistId}/attributes")
    public ResponseEntity<GetPlaylistNoMusicDTO> editPlaylistAttributes(@PathVariable UUID playlistId,
                                                                        @RequestBody PutPlaylistDTO changesDTO) {
        var edited_playlist = playlistService.editPlaylistAttributes(playlistId, changesDTO);

        GetPlaylistNoMusicDTO responseDTO = playlistMapper.toDTO(edited_playlist);

        return ResponseEntity.ok(responseDTO);
    }

    @PatchMapping("/{playlistId}/{musicId}")
    public ResponseEntity<GetPlaylistDTO> addMusicToPlaylist(@PathVariable UUID playlistId,
                                                             @PathVariable UUID musicId) {
        var playlist = playlistService.addMusicToPlaylist(playlistId, musicId);

        GetPlaylistDTO responseDTO = playlistMapper.toFullDTO(playlist);

        return ResponseEntity.ok(responseDTO);
    }

    // Adiciona uma música PERMITINDO DUPLICATAS (append incondicional). O frontend já
    // confirmou o "tem certeza?" antes de chamar; diferente do PATCH acima, este NÃO
    // rejeita se a música já estiver na playlist — cada chamada cria uma nova ocorrência.
    // Corpo é ignorado (o cliente manda {} vazio). 404 se a playlist ou a música não existir.
    @PostMapping("/{playlistId}/musics/{musicId}")
    public ResponseEntity<GetPlaylistDTO> appendMusicToPlaylist(@PathVariable UUID playlistId,
                                                                @PathVariable UUID musicId) {
        GetPlaylistDTO responseDTO = playlistService.appendMusicToPlaylist(playlistId, musicId);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    public GetPlaylistNoMusicDTO createPlaylist(@RequestBody CreatePlaylistDTO playlist) {
        return playlistService.createPlaylist(playlist);
    }

    // Set EXPLÍCITO (não toggle) da privacidade. Corpo: { "isPrivate": bool }. Retorna o
    // summary atualizado (mesmo shape do PUT /attributes) p/ o front atualizar o estado
    // local sem um GET extra. 400 se isPrivate ausente; 404 se não existir; 409 na
    // "Músicas Curtidas" (sempre privada). Endpoint SEPARADO do /attributes de propósito:
    // privacidade é um conceito de permissão distinto de nome/descrição.
    @PatchMapping("/{playlistId}/private")
    public ResponseEntity<GetPlaylistNoMusicDTO> setPlaylistPrivacy(@PathVariable UUID playlistId,
                                                                    @RequestBody SetPlaylistPrivacyDTO body) {
        GetPlaylistNoMusicDTO responseDTO = playlistService.setPrivacy(playlistId, body.isPrivate());

        return ResponseEntity.ok(responseDTO);
    }


    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylistById(@PathVariable UUID playlistId) {
        playlistService.deletePlaylistById(playlistId);

        return ResponseEntity.ok().build();
    }

    // Remove a ocorrência na POSIÇÃO informada (índice 0-based na ordem da tracklist).
    // Substitui o antigo DELETE /{playlistId}/{musicId} (que apagava TODAS as ocorrências):
    // suporta playlists com duplicatas apagando exatamente uma linha, e o @OrderColumn
    // recompacta as posições restantes. Retorna a playlist atualizada (mesmo shape do GET,
    // simetria com add/append/reorder). 404 se a playlist não existir ou a posição estiver
    // fora do intervalo. `position` não-inteiro -> 400 (bind do Spring).
    @DeleteMapping("/{playlistId}/positions/{position}")
    public ResponseEntity<GetPlaylistDTO> removeMusicAtPosition(@PathVariable UUID playlistId,
                                                                @PathVariable int position) {
        GetPlaylistDTO responseDTO = playlistService.removeMusicAtPosition(playlistId, position);

        return ResponseEntity.ok(responseDTO);
    }
}
