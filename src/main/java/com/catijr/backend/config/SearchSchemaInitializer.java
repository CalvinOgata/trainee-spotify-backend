package com.catijr.backend.config;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.core.annotation.Order;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Component;

import java.sql.Connection;

/**
 * Garante (de forma idempotente) a infraestrutura de banco que a busca usa:
 * extensões unaccent + pg_trgm, uma função imutável f_unaccent() que serve de
 * base para índices funcionais, e índices GIN de trigrama em cada coluna
 * pesquisável. Roda no startup (após o Hibernate criar/atualizar as tabelas).
 *
 * <p>Só executa em PostgreSQL; em outros bancos apenas loga um aviso (a busca
 * cai para case-insensitive simples, ver TODO no PR).
 */
@Slf4j
@Component
@Order(0)
@RequiredArgsConstructor
public class SearchSchemaInitializer implements CommandLineRunner {

    private final JdbcTemplate jdbc;

    private static final String[] DDL = {
            "CREATE EXTENSION IF NOT EXISTS unaccent",
            "CREATE EXTENSION IF NOT EXISTS pg_trgm",
            // unaccent() é STABLE; este wrapper IMMUTABLE permite indexá-la.
            "CREATE OR REPLACE FUNCTION f_unaccent(text) RETURNS text AS "
                    + "$func$ SELECT public.unaccent('public.unaccent', $1) $func$ "
                    + "LANGUAGE sql IMMUTABLE PARALLEL SAFE STRICT",
            // Índices GIN de trigrama: tornam LIKE '%termo%' rápido sem âncora à esquerda.
            "CREATE INDEX IF NOT EXISTS idx_music_title_trgm    ON tb_musics    USING gin (f_unaccent(lower(title)) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_artist_name_trgm    ON tb_artists   USING gin (f_unaccent(lower(artist_name)) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_album_title_trgm    ON tb_albums    USING gin (f_unaccent(lower(album_title)) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_playlist_name_trgm  ON tb_playlists USING gin (f_unaccent(lower(name)) gin_trgm_ops)",
            "CREATE INDEX IF NOT EXISTS idx_playlist_desc_trgm  ON tb_playlists USING gin (f_unaccent(lower(description)) gin_trgm_ops)",
    };

    @Override
    public void run(String... args) {
        String product = jdbc.execute((Connection con) -> con.getMetaData().getDatabaseProductName());
        if (product == null || !product.toLowerCase().contains("postgres")) {
            log.warn("Inicialização do schema de busca PULADA: banco não-PostgreSQL ({}). "
                    + "Busca acento-insensível requer PostgreSQL (unaccent/pg_trgm).", product);
            return;
        }
        for (String stmt : DDL) {
            jdbc.execute(stmt);
        }
        log.info("Schema de busca pronto: extensões unaccent + pg_trgm, função f_unaccent() e índices de trigrama.");
    }
}
