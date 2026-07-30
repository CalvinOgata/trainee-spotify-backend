package com.catijr.backend.DTOs.Music;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetMusicDTO(UUID id, String title, UUID artistId,
                          UUID albumId, List<UUID> playlistsId,
                          int duration, Instant releaseDate, int timesListen,
                          Boolean explicit, Instant createdAt,
                          Instant updatedAt,
                          // Última reprodução desta faixa pelo usuário (ISO-8601 UTC) ou null. Derivado
                          // de tb_plays em tempo de leitura; preenchido só nos endpoints de biblioteca
                          // (GET /user/savedMusics), null nos demais.
                          Instant lastPlayedAt,
                          // Capa da faixa. Como não guardamos arte por faixa, herda a
                          // capa do álbum (relativa, ex.: "/images/albums/<id>.jpg") ou null.
                          String imageUrl) {
}
