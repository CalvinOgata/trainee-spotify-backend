package com.catijr.backend.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.catijr.backend.DTOs.Album.GetAlbumDTO;
import com.catijr.backend.DTOs.Album.GetAlbumNoMusicsDTO;
import com.catijr.backend.Entities.Album;

@Mapper(componentModel = "spring", uses = MusicMapper.class)
public interface AlbumMapper {

    @Mapping(target = "artistId", source = "owner.id")
    @Mapping(target = "artistName", source = "owner.name")
    GetAlbumDTO toDTO(Album album);

    @Mapping(target = "artistId", source = "owner.id")
    @Mapping(target = "artistName", source = "owner.name")
    GetAlbumNoMusicsDTO toNoMusicsDTO(Album album);
}
