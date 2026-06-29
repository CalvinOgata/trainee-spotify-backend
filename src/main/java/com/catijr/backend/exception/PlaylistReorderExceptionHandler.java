package com.catijr.backend.exception;

import com.catijr.backend.Controllers.PlaylistReorderController;
import com.catijr.backend.playlist.InvalidReorderException;
import com.catijr.backend.playlist.NotPlaylistOwnerException;
import com.catijr.backend.playlist.PlaylistContentsChangedException;
import com.catijr.backend.playlist.PlaylistNotFoundException;
import com.catijr.backend.playlist.ReorderFailedException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.http.converter.HttpMessageNotReadableException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.Map;

/**
 * Tratamento de erros restrito ao {@link PlaylistReorderController} (assignableTypes),
 * para não mudar o comportamento de erro dos demais endpoints de playlist. Todos os
 * corpos são strings FIXAS — nunca ecoam os musicIds, SQL, parâmetros ou stack trace.
 */
@RestControllerAdvice(assignableTypes = PlaylistReorderController.class)
public class PlaylistReorderExceptionHandler {

    private static ResponseEntity<Map<String, String>> body(HttpStatus status, String error) {
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .body(Map.of("error", error));
    }

    /** Corpo ausente/musicIds inválido/não-UUID/duplicado/acima do teto — 400 fixo. */
    @ExceptionHandler(InvalidReorderException.class)
    public ResponseEntity<Map<String, String>> handleInvalid(InvalidReorderException ex) {
        return body(HttpStatus.BAD_REQUEST, "invalid musicIds");
    }

    /** JSON malformado (não desserializa para ReorderPlaylistDTO) — mesmo 400 fixo. */
    @ExceptionHandler(HttpMessageNotReadableException.class)
    public ResponseEntity<Map<String, String>> handleUnreadable(HttpMessageNotReadableException ex) {
        return body(HttpStatus.BAD_REQUEST, "invalid musicIds");
    }

    /** O conjunto enviado não bate com o atual (cliente desatualizado) — 409 fixo. */
    @ExceptionHandler(PlaylistContentsChangedException.class)
    public ResponseEntity<Map<String, String>> handleConflict(PlaylistContentsChangedException ex) {
        return body(HttpStatus.CONFLICT, "playlist contents changed, refetch");
    }

    /** Não é dono — 403. Sem conteúdo da playlist no corpo. (Hoje inalcançável.) */
    @ExceptionHandler(NotPlaylistOwnerException.class)
    public ResponseEntity<Map<String, String>> handleForbidden(NotPlaylistOwnerException ex) {
        return body(HttpStatus.FORBIDDEN, "forbidden");
    }

    /** Playlist inexistente (ou id malformado) — 404, sem vazar existência. */
    @ExceptionHandler(PlaylistNotFoundException.class)
    public ResponseEntity<Map<String, String>> handleNotFound(PlaylistNotFoundException ex) {
        return body(HttpStatus.NOT_FOUND, "playlist not found");
    }

    /** Qualquer falha de banco — 500 com corpo fixo (já logado no serviço). */
    @ExceptionHandler(ReorderFailedException.class)
    public ResponseEntity<Map<String, String>> handleFailed(ReorderFailedException ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "reorder failed");
    }
}
