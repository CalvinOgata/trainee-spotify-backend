package com.catijr.backend.DTOs.Album;

import java.time.Instant;
import java.util.UUID;

public record GetAlbumNoMusicsDTO(UUID id, String title,
                          String year, UUID artistId,
                          String artistName,
                          Instant createdAt, Instant updatedAt,
                          // Última reprodução deste álbum (kind=album) pelo usuário, ISO-8601 UTC ou null.
                          // Server-owned, derivado de POST /user/plays. Ver Album.lastPlayedAt.
                          Instant lastPlayedAt,
                          // Caminho relativo da capa (ex.: "/images/albums/<id>.jpg") ou null.
                          String imageUrl) {
}
