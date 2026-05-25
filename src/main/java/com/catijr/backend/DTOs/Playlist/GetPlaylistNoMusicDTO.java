package com.catijr.backend.DTOs.Playlist;

import com.catijr.backend.Entities.Playlist;

public record GetPlaylistNoMusicDTO(String name, String description) {

    public GetPlaylistNoMusicDTO(Playlist playlist) {
        this(
                playlist.getName(),
                playlist.getDescription()
        );
    }

}
