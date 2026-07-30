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
                          // Última reprodução deste álbum pelo usuário (ISO-8601 UTC) ou null. Derivado
                          // de tb_plays; hoje não é preenchido no GET de álbum completo (null aqui).
                          Instant lastPlayedAt,
                          // Caminho relativo da capa (ex.: "/images/albums/<id>.jpg") ou null.
                          String imageUrl) {
}
