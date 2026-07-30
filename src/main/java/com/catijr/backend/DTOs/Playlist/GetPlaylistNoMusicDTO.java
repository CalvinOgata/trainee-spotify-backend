package com.catijr.backend.DTOs.Playlist;

import java.time.Instant;
import java.util.UUID;

public record GetPlaylistNoMusicDTO(UUID id, String name, String description, int musicQtd,
                                    int duration, Instant createdAt, Instant updatedAt,
                                    // Última reprodução desta playlist pelo usuário (ISO-8601 UTC) ou null. Derivado
                                    // de tb_plays em tempo de leitura; preenchido no GET /user/playlists, null nos demais.
                                    Instant lastPlayedAt,
                                    // Caminho relativo da capa (ex.: "/images/playlists/<id>.jpg") ou null.
                                    String imageUrl,
                                    // Privacidade (server-owned). true = oculta de outros usuários; o dono
                                    // vê sempre. Presente em TODA resposta de playlist. Ver PATCH /playlist/{id}/private.
                                    boolean isPrivate ){
}
