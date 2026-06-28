package com.catijr.backend.exception;

import com.catijr.backend.Controllers.SearchController;
import com.catijr.backend.search.InvalidSearchQueryException;
import com.catijr.backend.search.RateLimitExceededException;
import com.catijr.backend.search.SearchFailedException;
import org.springframework.http.CacheControl;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;
import org.springframework.web.method.annotation.MethodArgumentTypeMismatchException;

import java.util.Map;

/**
 * Tratamento de erros restrito ao {@link SearchController} (assignableTypes), para
 * não alterar o comportamento de erro dos demais endpoints. Todos os corpos são
 * strings FIXAS — nunca ecoam o q, SQL, parâmetros ou stack trace.
 */
@RestControllerAdvice(assignableTypes = SearchController.class)
public class SearchExceptionHandler {

    private static ResponseEntity<Map<String, String>> body(HttpStatus status, String error) {
        return ResponseEntity.status(status)
                .cacheControl(CacheControl.noStore())
                .body(Map.of("error", error));
    }

    /** q ausente/vazio/muito longo — 400 com mensagem fixa ("q is required" / "q is too long"). */
    @ExceptionHandler(InvalidSearchQueryException.class)
    public ResponseEntity<Map<String, String>> handleInvalid(InvalidSearchQueryException ex) {
        return body(HttpStatus.BAD_REQUEST, ex.getMessage());
    }

    /** limit não-numérico, etc. — 400 sem ecoar o valor recebido. */
    @ExceptionHandler(MethodArgumentTypeMismatchException.class)
    public ResponseEntity<Map<String, String>> handleTypeMismatch(MethodArgumentTypeMismatchException ex) {
        return body(HttpStatus.BAD_REQUEST, "invalid request");
    }

    /** Limite de requisições excedido — 429 com Retry-After. */
    @ExceptionHandler(RateLimitExceededException.class)
    public ResponseEntity<Map<String, String>> handleRateLimited(RateLimitExceededException ex) {
        return ResponseEntity.status(HttpStatus.TOO_MANY_REQUESTS)
                .header(HttpHeaders.RETRY_AFTER, String.valueOf(ex.getRetryAfterSeconds()))
                .cacheControl(CacheControl.noStore())
                .body(Map.of("error", "rate limit exceeded"));
    }

    /** Qualquer falha de banco — 500 com corpo fixo (já logado no serviço). */
    @ExceptionHandler(SearchFailedException.class)
    public ResponseEntity<Map<String, String>> handleFailed(SearchFailedException ex) {
        return body(HttpStatus.INTERNAL_SERVER_ERROR, "search failed");
    }
}
