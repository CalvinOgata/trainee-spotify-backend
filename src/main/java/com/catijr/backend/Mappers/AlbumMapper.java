package com.catijr.backend.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.catijr.backend.DTOs.Album.GetAlbumDTO;
import com.catijr.backend.DTOs.Album.GetAlbumNoMusicsDTO;
import com.catijr.backend.Entities.Album;
import com.catijr.backend.config.ImageUrlResolver;

@Mapper(componentModel = "spring", uses = {MusicMapper.class, ImageUrlResolver.class})
public interface AlbumMapper {

    @Mapping(target = "artistId", source = "owner.id")
    @Mapping(target = "artistName", source = "owner.name")
    @Mapping(target = "imageUrl", source = "album", qualifiedByName = "albumImageUrl")
    GetAlbumDTO toDTO(Album album);

    @Mapping(target = "artistId", source = "owner.id")
    @Mapping(target = "artistName", source = "owner.name")
    @Mapping(target = "imageUrl", source = "album", qualifiedByName = "albumImageUrl")
    GetAlbumNoMusicsDTO toNoMusicsDTO(Album album);
}
