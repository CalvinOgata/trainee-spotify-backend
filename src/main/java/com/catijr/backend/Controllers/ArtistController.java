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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

@RestController
@RequestMapping("/artist")
@RequiredArgsConstructor
public class ArtistController {

    private final ArtistService artistService;
    private final MusicMapper musicMapper;
    private final AlbumMapper albumMapper;
    private final ArtistMapper artistMapper;

    // Tamanho da prévia da seção "Populares" (sem ?all=true).
    private static final int PREVIEW_LIMIT = 5;

    // Mesmo shape/mapper de /user/followedArtists (artistMapper.toDTO) — JSON idêntico
    // para o mesmo id. 404 (ResponseStatusException) quando o artista não existe,
    // igual aos demais endpoints de /artist e /user.
    @GetMapping("/{artistId}")
    public ResponseEntity<GetArtistDTO> getArtistById(@PathVariable UUID artistId) {
        var artist = artistService.getArtistById(artistId);

        return ResponseEntity.ok(artistMapper.toDTO(artist));
    }

    // Preview vs. lista completa: por padrão devolve só o TOP 5 (a prévia da seção
    // "Populares"). Com ?all=true devolve TODAS as músicas do artista, já ordenadas
    // por popularidade (timesListen DESC, título ASC) — é o que o botão "Mostrar Tudo"
    // consome. Antes, a prévia batia neste endpoint (ordenado) mas o "Mostrar Tudo"
    // caía na lista de álbuns (SEM ordenação), quebrando a ordem. Mesma fonte ordenada
    // para os dois casos agora.
    @GetMapping("/{artistId}/popularMusics")
    public ResponseEntity<List<GetMusicDTO>> getPopularMusicsByArtistId(
            @PathVariable UUID artistId,
            @RequestParam(name = "all", defaultValue = "false") boolean all) {
        var popMusics = artistService.getPopularMusicsByArtistId(artistId);

        var ordered = all ? popMusics.stream() : popMusics.stream().limit(PREVIEW_LIMIT);
        List<GetMusicDTO> responseDTO = ordered.map(musicMapper::toDTO).toList();

        return ResponseEntity.ok(responseDTO);
    }

    @GetMapping("/{artistId}/albums")
    public ResponseEntity<List<GetAlbumDTO>> getAlbumsByArtistId(@PathVariable UUID artistId) {
        var albums = artistService.getAlbumsByArtistId(artistId);

        List<GetAlbumDTO> responseDTO = albums.stream().map(albumMapper::toDTO).toList();

        return ResponseEntity.ok(responseDTO);
    }
}
