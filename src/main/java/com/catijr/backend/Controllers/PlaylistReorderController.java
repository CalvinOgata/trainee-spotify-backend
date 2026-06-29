package com.catijr.backend.Controllers;

import com.catijr.backend.DTOs.Playlist.GetPlaylistDTO;
import com.catijr.backend.DTOs.Playlist.ReorderPlaylistDTO;
import com.catijr.backend.Services.PlaylistReorderService;
import com.catijr.backend.playlist.PlaylistNotFoundException;
import com.catijr.backend.playlist.ReorderRequestValidator;
import lombok.RequiredArgsConstructor;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PutMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.UUID;

/**
 * Endpoint dedicado de reordenação. Fica num controller SEPARADO de propósito:
 * o {@code PlaylistReorderExceptionHandler} mapeia {@code HttpMessageNotReadableException}
 * (JSON malformado) para 400 "invalid musicIds"; isolar a rota evita que esse
 * tratamento vaze para os outros endpoints de playlist.
 */
@RestController
@RequestMapping("/playlist")
@RequiredArgsConstructor
public class PlaylistReorderController {

    private final PlaylistReorderService playlistReorderService;

    @PutMapping("/{playlistId}/order")
    public ResponseEntity<GetPlaylistDTO> reorder(@PathVariable String playlistId,
                                                  @RequestBody(required = false) ReorderPlaylistDTO body) {
        // Id malformado não corresponde a recurso nenhum -> 404 (esconde existência),
        // nunca 500 por IllegalArgumentException vazada.
        UUID id;
        try {
            id = UUID.fromString(playlistId);
        } catch (IllegalArgumentException ex) {
            throw new PlaylistNotFoundException();
        }

        // Validação barata do corpo ANTES de tocar o banco (400 em qualquer violação).
        List<UUID> requestedOrder = ReorderRequestValidator.validate(body);

        GetPlaylistDTO dto = playlistReorderService.reorder(id, requestedOrder);

        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(dto);
    }
}
