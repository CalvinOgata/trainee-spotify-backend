package com.catijr.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * Backfill (idempotente) da coluna {@code song_position} da tabela de junção
 * {@code tb_playlist_music}, introduzida pelo {@code @OrderColumn} em Playlist.songs.
 *
 * <p>Com {@code ddl-auto=update} o Hibernate ADICIONA a coluna no startup, mas as
 * linhas que já existiam ficam com {@code song_position} NULL — e o {@code @OrderColumn}
 * não consegue ler uma lista com posições nulas. Aqui preenchemos essas linhas
 * legadas com posições contíguas (0..n-1) por playlist, de forma determinística.
 *
 * <p>Roda DEPOIS do schema de busca (@Order(1)). Só executa em PostgreSQL.
 * É no-op a partir da segunda execução (só toca linhas com posição NULL).
 */
@Slf4j
@Component
@Order(1)
@RequiredArgsConstructor
public class PlaylistSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    private static final String ADD_COLUMN =
            "ALTER TABLE tb_playlist_music ADD COLUMN IF NOT EXISTS song_position integer";

    // Numera as músicas de cada playlist 0..n-1 (ordem determinística por songs) e
    // grava apenas onde ainda está NULL. ctid identifica cada linha física da junção,
    // que não tem chave primária própria.
    private static final String BACKFILL = """
            UPDATE tb_playlist_music t
            SET song_position = sub.rn
            FROM (
                SELECT ctid,
                       (row_number() OVER (PARTITION BY playlist ORDER BY songs) - 1) AS rn
                FROM tb_playlist_music
            ) sub
            WHERE t.ctid = sub.ctid AND t.song_position IS NULL
            """;

    @Override
    public void run(String... args) {
        String product = jdbc.execute((Connection con) -> con.getMetaData().getDatabaseProductName());
        if (product == null || !product.toLowerCase().contains("postgres")) {
            log.warn("Backfill de song_position PULADO: banco não-PostgreSQL ({}).", product);
            return;
        }
        jdbc.execute(ADD_COLUMN);
        int filled = jdbc.update(BACKFILL);
        if (filled > 0) {
            log.info("Backfill de song_position: {} linha(s) legada(s) preenchida(s) em tb_playlist_music.", filled);
        } else {
            log.info("Backfill de song_position: nada a fazer (todas as linhas já posicionadas).");
        }
    }
}
