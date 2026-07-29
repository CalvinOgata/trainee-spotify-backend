package com.catijr.backend.DTOs.Music;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetMusicDTO(UUID id, String title, UUID artistId,
                          UUID albumId, List<UUID> playlistsId,
                          int duration, Instant releaseDate, int timesListen,
                          Boolean explicit, Instant createdAt,
                          Instant updatedAt,
                          // Última reprodução desta faixa (kind=music) pelo usuário, ISO-8601 UTC ou null.
                          // Server-owned, derivado de POST /user/plays. Ver Music.lastPlayedAt.
                          Instant lastPlayedAt,
                          // Capa da faixa. Como não guardamos arte por faixa, herda a
                          // capa do álbum (relativa, ex.: "/images/albums/<id>.jpg") ou null.
                          String imageUrl) {
}
