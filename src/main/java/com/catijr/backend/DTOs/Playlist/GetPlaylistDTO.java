package com.catijr.backend.DTOs.Playlist;

import com.catijr.backend.DTOs.Music.GetMusicDTO;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

public record GetPlaylistDTO(UUID id, String name, String description, int musicQtd,
                             int duration, List<GetMusicDTO> musics,
                             Instant createdAt, Instant updatedAt,
                             // Última reprodução desta playlist (kind=playlist) pelo usuário, ISO-8601 UTC ou null.
                             // Server-owned, derivado de POST /user/plays. Ver Playlist.lastPlayedAt.
                             Instant lastPlayedAt,
                             // Caminho relativo da capa (ex.: "/images/playlists/<id>.jpg") ou null.
                             String imageUrl,
                             // Privacidade (server-owned). true = oculta de outros usuários; o dono
                             // vê sempre. Presente em TODA resposta de playlist. Ver PATCH /playlist/{id}/private.
                             boolean isPrivate ) {
}
