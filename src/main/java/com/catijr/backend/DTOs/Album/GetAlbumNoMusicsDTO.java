package com.catijr.backend.DTOs.Album;

import java.time.Instant;
import java.util.UUID;

public record GetAlbumNoMusicsDTO(UUID id, String title,
                          String year, UUID artistId,
                          String artistName,
                          Instant createdAt, Instant updatedAt,
                          // Última reprodução deste álbum pelo usuário (ISO-8601 UTC) ou null. Derivado
                          // de tb_plays em tempo de leitura; preenchido só nos endpoints de biblioteca
                          // (GET /user/savedAlbums), null nos demais.
                          Instant lastPlayedAt,
                          // Caminho relativo da capa (ex.: "/images/albums/<id>.jpg") ou null.
                          String imageUrl) {
}
