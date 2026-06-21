package com.catijr.backend.DTOs.Music;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetMusicDTO(UUID id, String title, UUID artistId,
                          UUID albumId, List<UUID> playlistsId,
                          int duration, Instant releaseDate, int timesListen,
                          Boolean explicit, Instant createdAt,
                          Instant updatedAt) {
}
