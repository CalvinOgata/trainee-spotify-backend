package com.catijr.backend.Mappers;

import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;
import com.catijr.backend.DTOs.Playlist.CreatePlaylistDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistDTO;
import com.catijr.backend.Entities.Playlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MusicMapper.class)
public interface PlaylistMapper {

    GetPlaylistNoMusicDTO toDTO(Playlist playlist);

    @Mapping(target = "musics", source = "songs")
    GetPlaylistDTO toFullDTO(Playlist playlist);

    // Playlist recém-criada nasce sem capa (image_url NULL); o front usa sua arte padrão.
    @Mapping(target = "imageUrl", ignore = true)
    Playlist toEntity(CreatePlaylistDTO playlist);
}
