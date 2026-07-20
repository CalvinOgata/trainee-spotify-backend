package com.catijr.backend.DTOs.Artist;

import java.time.Instant;
import java.util.UUID;

public record GetArtistDTO(UUID id, String name, int listeners, String about,
    Instant createdAt, Instant updatedAt,
    // Caminho relativo da foto de perfil (ex.: "/images/artists/<id>.jpg") ou null.
    String imageUrl
){
}
