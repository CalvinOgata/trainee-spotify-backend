package com.catijr.backend.DTOs.Artist;

import java.time.Instant;
import java.util.UUID;

public record GetArtistDTO(UUID id, String name, int listeners, String about,
    Instant createdAt, Instant updatedAt,
    // Última reprodução deste artista (kind=artist) pelo usuário, ISO-8601 UTC ou null.
    // Server-owned, derivado de POST /user/plays. Ver Artist.lastPlayedAt.
    Instant lastPlayedAt,
    // Caminho relativo da foto de perfil (ex.: "/images/artists/<id>.jpg") ou null.
    String imageUrl
){
}
