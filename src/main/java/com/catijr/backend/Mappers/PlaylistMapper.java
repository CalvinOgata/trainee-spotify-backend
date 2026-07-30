package com.catijr.backend.Mappers;

import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;
import com.catijr.backend.DTOs.Playlist.CreatePlaylistDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistDTO;
import com.catijr.backend.Entities.Playlist;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;

@Mapper(componentModel = "spring", uses = MusicMapper.class)
public interface PlaylistMapper {

    // O getter Lombok do campo boolean 'isPrivate' é isPrivate(), que o MapStruct lê
    // como a propriedade "private" (remove o prefixo "is"). O componente do record é
    // "isPrivate", então SEM este @Mapping explícito o MapStruct não casa os dois e
    // devolve false fixo. Mapeamos a origem "private" -> alvo "isPrivate".
    // lastPlayedAt não é mais coluna: é derivado de tb_plays e carimbado na SERVICE
    // (UserService.getUserPlaylists). Aqui fica null.
    @Mapping(target = "isPrivate", source = "private")
    @Mapping(target = "lastPlayedAt", ignore = true)
    GetPlaylistNoMusicDTO toDTO(Playlist playlist);

    @Mapping(target = "musics", source = "songs")
    @Mapping(target = "isPrivate", source = "private")
    @Mapping(target = "lastPlayedAt", ignore = true)
    GetPlaylistDTO toFullDTO(Playlist playlist);

    // Playlist recém-criada nasce sem capa (image_url NULL); o front usa sua arte padrão.
    @Mapping(target = "imageUrl", ignore = true)
    Playlist toEntity(CreatePlaylistDTO playlist);
}
