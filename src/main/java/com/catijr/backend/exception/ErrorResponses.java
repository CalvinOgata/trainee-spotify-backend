package com.catijr.backend.exception;

import org.springframework.http.CacheControl;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;

import java.util.Map;

/**
 * Corpo de erro padrão da API: um JSON fixo {@code {"error": "..."}} com
 * {@code Cache-Control: no-store}. Compartilhado pelos handlers de exceção
 * ({@link SearchExceptionHandler} e {@link PlaylistReorderExceptionHandler}),
 * onde a mensagem é sempre uma string FIXA — nunca ecoa entrada do cliente.
 */
public final class ErrorResponses {

    private ErrorResponses() {}

    public static ResponseEntity<Map<String, String>> body(HttpStatus status, String error) {
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .body(Map.of("error", error));
    }
}
