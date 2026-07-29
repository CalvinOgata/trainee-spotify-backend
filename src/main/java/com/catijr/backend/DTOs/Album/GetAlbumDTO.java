package com.catijr.backend.DTOs.Album;

import com.catijr.backend.DTOs.Music.GetMusicDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetAlbumDTO(UUID id, String title,
                          String year, UUID artistId,
                          String artistName,
                          List<GetMusicDTO> musics,
                          Instant createdAt, Instant updatedAt,
                          // Última reprodução deste álbum (kind=album) pelo usuário, ISO-8601 UTC ou null.
                          // Server-owned, derivado de POST /user/plays. Ver Album.lastPlayedAt.
                          Instant lastPlayedAt,
                          // Caminho relativo da capa (ex.: "/images/albums/<id>.jpg") ou null.
                          String imageUrl) {
}
