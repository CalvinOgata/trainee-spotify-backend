package com.catijr.backend.Controllers;

import com.catijr.backend.DTOs.Album.GetAlbumDTO;
import com.catijr.backend.DTOs.Artist.GetArtistDTO;
import com.catijr.backend.DTOs.Music.GetMusicDTO;
import com.catijr.backend.Mappers.AlbumMapper;
import com.catijr.backend.Mappers.ArtistMapper;
import com.catijr.backend.Mappers.MusicMapper;
import com.catijr.backend.Services.ArtistService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/artist")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final MusicMapper musicMapper;
    private final AlbumMapper albumMapper;
    private final ArtistMapper artistMapper;

    // Mesmo shape/mapper de /user/followedArtists (artistMapper.toDTO) — JSON idêntico
    // para o mesmo id. 404 (ResponseStatusException) quando o artista não existe,
    // igual aos demais endpoints de /artist e /user.
    @GetMapping("/{artistId}")
    public ResponseEntity<GetArtistDTO> getArtistById(@PathVariable String artistId) {
        var artist = artistService.getArtistById(UUID.fromString(artistId));

        return ResponseEntity.ok(artistMapper.toDTO(artist));
    }

    @GetMapping("/{artistId}/popularMusics")
    public ResponseEntity<List<GetMusicDTO>> getPopularMusicsByArtistId(@PathVariable String artistId) {
        var popMusics = artistService.getPopularMusicsByArtistId(UUID.fromString(artistId));

        List<GetMusicDTO> responseDTO = popMusics.stream().limit(5).map(musicMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{artistId}/albums")
    public ResponseEntity<List<GetAlbumDTO>> getAlbumsByArtistId(@PathVariable String artistId) {
        var albums = artistService.getAlbumsByArtistId(UUID.fromString(artistId));

        List<GetAlbumDTO> responseDTO = albums.stream().map(albumMapper::toDTO).collect(Collectors.toList());

        return ResponseEntity.ok(responseDTO);
    }
}
