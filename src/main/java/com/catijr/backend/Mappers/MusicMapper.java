package com.catijr.backend.Mappers;

import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

import com.catijr.backend.DTOs.Music.GetMusicDTO;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Playlist;
import com.catijr.backend.config.ImageUrlResolver;

import java.util.UUID;

@Mapper(componentModel="spring", uses = ImageUrlResolver.class)
public interface MusicMapper {

    // CORREÇÃO: o MapStruct não estava mapeando os IDs aninhados, então artistId/albumId/playlistsId vinham nulos
    @Mapping(target = "artistId", source = "artist.id")
    @Mapping(target = "albumId", source = "album.id")
    @Mapping(target = "playlistsId", source = "playlists")
    // Não guardamos capa por faixa: a música herda a capa do álbum. Resolve pelo
    // álbum (mesmo resolver do AlbumMapper): override no banco vence, senão deriva
    // pelo id do álbum. Álbum nulo -> null (o resolver é null-safe).
    @Mapping(target = "imageUrl", source = "album", qualifiedByName = "albumImageUrl")
    GetMusicDTO toDTO(Music music);

    default UUID playlistToId(Playlist playlist) {
        return playlist != null ? playlist.getId() : null;
    }
}
