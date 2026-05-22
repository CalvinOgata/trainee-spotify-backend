package com.catijr.backend.Controllers;

import com.catijr.backend.DTOs.GetMusicDTO;
import com.catijr.backend.DTOs.GetPopSongsResponseDTO;
import com.catijr.backend.Entities.Music;
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
@RequestMapping("/artist/")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;

    @GetMapping("{artistId}/popularMusics")
    public ResponseEntity<List<GetMusicDTO>> getPopularMusicsByArtistId(@PathVariable String artistId) {
        var popMusics = artistService.getPopularMusicsByArtistId(UUID.fromString(artistId));

        List<GetMusicDTO> responseDTO =  popMusics.stream().limit(5).map(music -> new GetMusicDTO(music)).collect(Collectors.toList());

        return ResponseEntity.ok(responseDTO);
    }
}
