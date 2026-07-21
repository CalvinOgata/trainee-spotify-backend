package com.catijr.backend.DTOs.Search;

import com.catijr.backend.DTOs.Album.GetAlbumNoMusicsDTO;
import com.catijr.backend.DTOs.Artist.GetArtistDTO;
import com.catijr.backend.DTOs.Music.GetMusicDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;

import java.util.List;

/**
 * Resposta do endpoint GET /search. As listas reutilizam exatamente os mesmos
 * DTOs já retornados por /user/* e /playlist/{id} (Music, PlaylistSummary,
 * Artist, AlbumSummary no frontend). Sempre listas vazias — nunca null.
 *
 * <p>{@code albums}/{@code artists} = entidades cujo NOME/título casou com o
 * termo (dirigem as linhas "Álbum"/"Artista" na UI). {@code musicAlbums} e
 * {@code musicArtists} = álbum-pai (AlbumSummary) e artista-pai de cada faixa em
 * {@code musics}, deduplicados por id — NÃO são listas renderizadas, são apenas
 * contexto de navegação para a faixa que está tocando. Os campos-pai são
 * independentes dos que casaram por nome: uma mesma entidade pode aparecer nos
 * dois (o frontend faz o merge).
 */
public record SearchResponseDTO(
        List<GetMusicDTO> musics,
        List<GetPlaylistNoMusicDTO> playlists,
        List<GetArtistDTO> artists,
        List<GetAlbumNoMusicsDTO> albums,
        List<GetAlbumNoMusicsDTO> musicAlbums,
        List<GetArtistDTO> musicArtists
) {
}
