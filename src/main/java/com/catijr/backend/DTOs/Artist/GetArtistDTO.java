package com.catijr.backend.DTOs.Artist;

import java.time.Instant;
import java.util.UUID;

public record GetArtistDTO(UUID id, String name, int listeners, String about,
    Instant createdAt, Instant updatedAt,
    // Última reprodução deste artista pelo usuário (ISO-8601 UTC) ou null. Derivado de
    // tb_plays em tempo de leitura; preenchido só nos endpoints de biblioteca
    // (GET /user/followedArtists), null nos demais.
    Instant lastPlayedAt,
    // Caminho relativo da foto de perfil (ex.: "/images/artists/<id>.jpg") ou null.
    String imageUrl
){
}
