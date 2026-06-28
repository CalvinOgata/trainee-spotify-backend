package com.catijr.backend.search;

import jakarta.persistence.EntityManager;
import jakarta.persistence.PersistenceContext;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

/**
 * Única implementação do SQL de busca, compartilhada por todas as categorias.
 * Monta o SELECT a partir do {@link SearchTarget} (tabela/coluna/ordenação) e
 * executa via {@link EntityManager}, mapeando para a entidade do alvo.
 *
 * <p>Segurança: os valores digitados pelo usuário NUNCA entram no texto do SQL —
 * vão sempre como parâmetros bindados ({@code :pattern/:prefix/:exact}) e o
 * {@code LIMIT} via {@code setMaxResults}. Os únicos trechos interpolados são
 * identificadores (tabela/coluna/ordem) vindos do enum {@link SearchTarget},
 * todos constantes de compilação e ainda validados por whitelist abaixo — não
 * há caminho da requisição HTTP até esses identificadores.
 *
 * <p>É anotado como {@code @Repository} para que o Spring traduza exceções de
 * persistência em {@code DataAccessException} (tratadas no serviço).
 */
@Repository
public class SearchQueryExecutor {

    @PersistenceContext
    private EntityManager em;

    /** Nomes de tabela/coluna aceitos: só letras minúsculas e underscore. */
    private static final Pattern SAFE_IDENTIFIER = Pattern.compile("^[a-z_]+$");
    /** Ordenação secundária aceita: "<coluna> ASC|DESC". */
    private static final Pattern SAFE_ORDER = Pattern.compile("^[a-z_]+ (ASC|DESC)$");

    /**
     * @param target alvo (define tabela, colunas e ordenação)
     * @param pattern padrão "%termo%" já com curingas escapados
     * @param prefix  padrão "termo%" já com curingas escapados
     * @param exact   termo cru (faixa "exato", comparada com '=' — sem curinga)
     * @param limit   máximo de linhas (já clampeado pelo controller)
     * @param <T>     tipo da entidade do alvo (inferido pelo contexto de atribuição)
     */
    @SuppressWarnings("unchecked")
    public <T> List<T> search(SearchTarget target, String pattern, String prefix, String exact, int limit) {
        return em.createNativeQuery(buildSql(target), target.entityClass())
                .setParameter("pattern", pattern)
                .setParameter("prefix", prefix)
                .setParameter("exact", exact)
                .setMaxResults(limit)
                .getResultList();
    }

    private static String buildSql(SearchTarget target) {
        String whereClause = target.matchColumns().stream()
                .map(column -> likeFragment(column, "pattern"))
                .collect(Collectors.joining(" OR "));

        return """
                SELECT * FROM %s
                WHERE %s
                ORDER BY
                    CASE
                        WHEN %s = f_unaccent(lower(:exact)) THEN 0
                        WHEN %s THEN 1
                        ELSE 2
                    END,
                    %s
                """.formatted(
                identifier(target.table()),
                whereClause,
                normalized(target.relevanceColumn()),
                likeFragment(target.relevanceColumn(), "prefix"),
                order(target.secondaryOrder()));
    }

    /** {@code f_unaccent(lower(col)) LIKE f_unaccent(lower(:param)) ESCAPE '\'} */
    private static String likeFragment(String column, String param) {
        return normalized(column) + " LIKE f_unaccent(lower(:" + param + ")) ESCAPE '\\'";
    }

    private static String normalized(String column) {
        return "f_unaccent(lower(" + identifier(column) + "))";
    }

    private static String identifier(String id) {
        if (!SAFE_IDENTIFIER.matcher(id).matches()) {
            throw new IllegalStateException("Identificador de busca inválido: " + id);
        }
        return id;
    }

    private static String order(String clause) {
        if (!SAFE_ORDER.matcher(clause).matches()) {
            throw new IllegalStateException("Ordenação de busca inválida: " + clause);
        }
        return clause;
    }
}
