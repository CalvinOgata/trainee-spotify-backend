package com.catijr.backend.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.catijr.backend.DTOs.Album.GetAlbumDTO;
import com.catijr.backend.DTOs.Album.GetAlbumNoMusicsDTO;
import com.catijr.backend.Entities.Album;
import com.catijr.backend.config.ImageUrlResolver;

@Mapper(componentModel = "spring", uses = {MusicMapper.class, ImageUrlResolver.class})
public interface AlbumMapper {

    // lastPlayedAt não é mais coluna: é derivado de tb_plays e carimbado na SERVICE
    // apenas nos endpoints de biblioteca (UserService). Aqui fica null.
    @Mapping(target = "artistId", source = "owner.id")
    @Mapping(target = "artistName", source = "owner.name")
    @Mapping(target = "imageUrl", source = "album", qualifiedByName = "albumImageUrl")
    @Mapping(target = "lastPlayedAt", ignore = true)
    GetAlbumDTO toDTO(Album album);

    @Mapping(target = "artistId", source = "owner.id")
    @Mapping(target = "artistName", source = "owner.name")
    @Mapping(target = "imageUrl", source = "album", qualifiedByName = "albumImageUrl")
    @Mapping(target = "lastPlayedAt", ignore = true)
    GetAlbumNoMusicsDTO toNoMusicsDTO(Album album);
}
