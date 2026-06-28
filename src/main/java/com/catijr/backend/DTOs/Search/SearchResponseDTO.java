package com.catijr.backend.DTOs.Search;

import com.catijr.backend.DTOs.Album.GetAlbumNoMusicsDTO;
import com.catijr.backend.DTOs.Artist.GetArtistDTO;
import com.catijr.backend.DTOs.Music.GetMusicDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;

import java.util.List;

/**
 * Resposta do endpoint GET /search. As quatro listas reutilizam exatamente os
 * mesmos DTOs já retornados por /user/* e /playlist/{id} (Music, PlaylistSummary,
 * Artist, AlbumSummary no frontend). Sempre listas vazias — nunca null.
 */
public record SearchResponseDTO(
        List<GetMusicDTO> musics,
        List<GetPlaylistNoMusicDTO> playlists,
        List<GetArtistDTO> artists,
        List<GetAlbumNoMusicsDTO> albums
) {
}
