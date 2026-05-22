package com.catijr.backend.DTOs;

import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Playlist;

import java.time.Instant;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

public record GetMusicDTO(UUID id, String title, UUID artistId,
                          UUID albumId, List<UUID> playlistsId,
                          int duration, Instant releaseDate, int timesListen,
                          Boolean explicit, Instant createdAt,
                          Instant updatedAt) {


    public GetMusicDTO(Music music) {
        this (
                music.getId(),
                music.getTitle(),
                music.getArtist().getId(),
                music.getAlbum().getId(),
                music.getPlaylists().stream().map(
                        Playlist::getId
                ).collect(Collectors.toList()),
                music.getDuration(),
                music.getReleaseDate(),
                music.getTimesListen(),
                music.getExplicit(),
                music.getCreatedAt(),
                music.getUpdatedAt()
        );
    }
}
