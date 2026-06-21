package com.catijr.backend.DTOs.Album;

import java.time.Instant;
import java.util.UUID;

public record GetAlbumNoMusicsDTO(UUID id, String title,
                          String year, UUID artistId,
                          String artistName,
                          Instant createdAt, Instant updatedAt) {
}
