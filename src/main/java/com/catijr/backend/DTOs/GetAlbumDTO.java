package com.catijr.backend.DTOs;

import com.catijr.backend.Entities.Album;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record GetAlbumDTO(UUID id, String title,
                          String year, UUID artistId,
                          String artistName,
                          List<GetMusicDTO> musics,
                          Instant createdAt, Instant updatedAt) {

    public GetAlbumDTO(Album album) {
        this(
                album.getId(),
                album.getTitle(),
                album.getYear(),
                album.getOwner().getId(),
                album.getOwner().getName(),
                album.getMusics().stream().map(GetMusicDTO::new).collect(Collectors.toList()),
                album.getCreatedAt(),
                album.getUpdatedAt()
        );
    }
}
