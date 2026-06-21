package com.catijr.backend.Controllers;


import com.catijr.backend.DTOs.Playlist.GetPlaylistDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;
import com.catijr.backend.DTOs.Playlist.PutPlaylistDTO;
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
    public ResponseEntity<GetPlaylistDTO> getPlaylistById(@PathVariable String playlistId) {
        var playlist = playlistService.getPlaylistById(UUID.fromString(playlistId));

        GetPlaylistDTO responseDTO = playlistMapper.toFullDTO(playlist);

        return ResponseEntity.ok(responseDTO);
    }

    @PutMapping("/{playlistId}/attributes")
    public ResponseEntity<GetPlaylistNoMusicDTO> editPlaylistAttributes(@PathVariable String playlistId,
                                                                        @RequestBody PutPlaylistDTO changesDTO) {
        var edited_playlist = playlistService.editPlaylistAttributes(UUID.fromString(playlistId), changesDTO);

        GetPlaylistNoMusicDTO responseDTO = playlistMapper.toDTO(edited_playlist);

        return ResponseEntity.ok(responseDTO);
    }

    @PatchMapping("/{playlistId}/{musicId}")
    public ResponseEntity<GetPlaylistDTO> addMusicToPlaylist(@PathVariable String playlistId,
                                                             @PathVariable String musicId) {
        var playlist = playlistService.addMusicToPlaylist(UUID.fromString(playlistId), UUID.fromString(musicId));

        GetPlaylistDTO responseDTO = playlistMapper.toFullDTO(playlist);

        return ResponseEntity.ok(responseDTO);
    }

    @PostMapping
    public GetPlaylistNoMusicDTO createPlaylist(@RequestBody CreatePlaylistDTO playlist) {
        return playlistService.createPlaylist(playlist);
    }


    @DeleteMapping("/{playlistId}")
    public ResponseEntity<Void> deletePlaylistById(@PathVariable String playlistId) {
        playlistService.deletePlaylistById(UUID.fromString(playlistId));

        return ResponseEntity.ok().build();
    }

    @DeleteMapping("/{playlistId}/{musicId}")
    public ResponseEntity<Void> deleteMusicById(@PathVariable String playlistId,
                                                @PathVariable String musicId) {
        playlistService.deleteMusicById(UUID.fromString(playlistId), UUID.fromString(musicId));

        return ResponseEntity.ok().build();
    }
}
