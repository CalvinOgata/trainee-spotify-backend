package com.catijr.backend.DTOs.Playlist;

import com.catijr.backend.DTOs.Music.GetMusicDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetPlaylistDTO(UUID id, String name, String description, int musicQtd,
                             int duration, List<GetMusicDTO> musics,
                             Instant createdAt, Instant updatedAt ) {
}
