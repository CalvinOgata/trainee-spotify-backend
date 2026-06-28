package com.catijr.backend.Controllers;

import com.catijr.backend.DTOs.Search.SearchResponseDTO;
import com.catijr.backend.Services.SearchService;
import com.catijr.backend.search.RateLimitExceededException;
import com.catijr.backend.search.SearchQuerySanitizer;
import com.catijr.backend.search.SearchRateLimiter;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.CacheControl;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.util.HexFormat;

/**
 * GET /search?q=<string>&limit=<int> — busca incremental para a página de
 * resultados do frontend (chamada a cada tecla, com debounce no cliente).
 * Retorna quatro listas: musics, playlists, artists, albums.
 */
@Slf4j
@RestController
@RequiredArgsConstructor
public class SearchController {

    private static final int DEFAULT_LIMIT = 20;
    private static final int MIN_LIMIT = 1;
    private static final int MAX_LIMIT = 50;

    private final SearchService searchService;
    private final SearchRateLimiter rateLimiter;

    /** Higiene de log: por padrão NUNCA logamos a query crua, só tamanho + hash. */
    @Value("${search.log-queries:false}")
    private boolean logQueries;

    @GetMapping("/search")
    public ResponseEntity<SearchResponseDTO> search(
            @RequestParam(name = "q", required = false) String q,
            @RequestParam(name = "limit", defaultValue = "" + DEFAULT_LIMIT) int limit,
            HttpServletRequest request) {

        // 1) Rate limit por IP (antes de qualquer trabalho).
        String clientIp = request.getRemoteAddr();
        if (!rateLimiter.tryAcquire(clientIp)) {
            log.debug("Rate limit excedido na busca (ipHash={})", hash(clientIp));
            throw new RateLimitExceededException(1);
        }

        // 2) Validação/normalização em memória (lança 400 com mensagem fixa). Sem tocar o banco.
        String normalized = SearchQuerySanitizer.normalize(q);

        // 3) Clamp do limite para [1, 50].
        int clamped = Math.max(MIN_LIMIT, Math.min(MAX_LIMIT, limit));

        if (logQueries) {
            log.debug("Busca q='{}' limit={}", normalized, clamped); // só com flag explícita
        } else {
            log.debug("Busca q.len={} q.hash={} limit={}", normalized.length(), hash(normalized), clamped);
        }

        SearchResponseDTO body = searchService.search(normalized, clamped);

        // 4) Respostas de busca podem ser pessoais (playlists) — não cachear.
        return ResponseEntity.ok()
                .cacheControl(CacheControl.noStore())
                .body(body);
    }

    /** Hash curto (12 hex de SHA-256) para correlacionar logs sem revelar o conteúdo. */
    private static String hash(String s) {
        try {
            byte[] digest = MessageDigest.getInstance("SHA-256")
                    .digest(s.getBytes(StandardCharsets.UTF_8));
            return HexFormat.of().formatHex(digest, 0, 6);
        } catch (Exception e) {
            return "na";
        }
    }
}
