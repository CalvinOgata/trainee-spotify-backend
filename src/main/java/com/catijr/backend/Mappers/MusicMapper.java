package com.catijr.backend.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.catijr.backend.DTOs.Music.GetMusicDTO;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Playlist;

import java.util.UUID;

@Mapper(componentModel="spring")
public interface MusicMapper {

    // CORREÇÃO: o MapStruct não estava mapeando os IDs aninhados, então artistId/albumId/playlistsId vinham nulos
    @Mapping(target = "artistId", source = "artist.id")
    @Mapping(target = "albumId", source = "album.id")
    @Mapping(target = "playlistsId", source = "playlists")
    GetMusicDTO toDTO(Music music);

    default UUID playlistToId(Playlist playlist) {
        return playlist != null ? playlist.getId() : null;
    }
}
