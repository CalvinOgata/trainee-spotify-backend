package com.catijr.backend.DTOs.Album;

import com.catijr.backend.DTOs.Music.GetMusicDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetAlbumDTO(UUID id, String title,
                          String year, UUID artistId,
                          String artistName,
                          List<GetMusicDTO> musics,
                          Instant createdAt, Instant updatedAt) {
}
