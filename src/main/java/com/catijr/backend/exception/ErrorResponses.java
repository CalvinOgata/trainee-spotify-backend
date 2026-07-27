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
 *
 * <p><b>Inconsistência conhecida (TODO):</b> um id de path malformado (UUID
 * inválido) é rejeitado pelo próprio Spring ao bindar {@code @PathVariable UUID},
 * ANTES de chegar a qualquer handler — devolvendo 400 no formato PADRÃO do Boot
 * ({@code {"timestamp","status","error","path"}}), e não neste
 * {@code {"error": "..."}}. Só ocorre para entradas que o frontend nunca envia
 * (todo id vem de uma resposta anterior da API), por isso foi deixado assim de
 * propósito. Para unificar o corpo: adicionar um {@code @RestControllerAdvice}
 * GLOBAL que trate {@code MethodArgumentTypeMismatchException} e reencaminhe para
 * {@link #body} (a "Opção C" da revisão de DRY).
 */
public final class ErrorResponses {

    private ErrorResponses() {}

    public static ResponseEntity<Map<String, String>> body(HttpStatus status, String error) {
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .body(Map.of("error", error));
    }
}
