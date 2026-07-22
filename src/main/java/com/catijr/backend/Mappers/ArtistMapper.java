package com.catijr.backend.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.catijr.backend.DTOs.Artist.GetArtistDTO;
import com.catijr.backend.Entities.Artist;
import com.catijr.backend.config.ImageUrlResolver;

@Mapper(componentModel="spring", uses = ImageUrlResolver.class)
public interface ArtistMapper {
    @Mapping(target = "imageUrl", source = "artist", qualifiedByName = "artistImageUrl")
    GetArtistDTO  toDTO(Artist artist);
}
