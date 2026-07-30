package com.catijr.backend.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.catijr.backend.DTOs.Artist.GetArtistDTO;
import com.catijr.backend.Entities.Artist;
import com.catijr.backend.config.ImageUrlResolver;

@Mapper(componentModel="spring", uses = ImageUrlResolver.class)
public interface ArtistMapper {
    // lastPlayedAt não é mais coluna: é derivado de tb_plays e carimbado na SERVICE
    // apenas nos endpoints de biblioteca (UserService). Aqui fica null.
    @Mapping(target = "imageUrl", source = "artist", qualifiedByName = "artistImageUrl")
    @Mapping(target = "lastPlayedAt", ignore = true)
    GetArtistDTO  toDTO(Artist artist);
}
