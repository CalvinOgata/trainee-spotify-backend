package com.catijr.backend.Repositories;

import com.catijr.backend.Entities.Play;
import com.catijr.backend.Entities.PlayKind;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Collection;
import java.util.List;
import java.util.UUID;

@Repository
public interface PlayRepository extends JpaRepository<Play, UUID> {

    /**
     * Ids das entidades tocadas mais recentemente (DISTINTAS) para um {@code kind},
     * da mais recente para a mais antiga. Deduplica por entity_id com GROUP BY e
     * ordena por MAX(played_at) DESC; o LIMIT vem do {@link Pageable} (ex.: 8).
     * Devolve só os ids — o catálogo é buscado depois, o que filtra naturalmente
     * entidades apagadas e mantém a resposta idêntica à de antes.
     */
    @Query("""
            SELECT p.entityId
            FROM Play p
            WHERE p.kind = :kind
            GROUP BY p.entityId
            ORDER BY MAX(p.playedAt) DESC
            """)
    List<UUID> findRecentEntityIds(@Param("kind") PlayKind kind, Pageable pageable);

    /**
     * Última reprodução (MAX(played_at)) de cada entidade de um {@code kind}, entre
     * os {@code ids} pedidos. Substitui a coluna denormalizada {@code last_played_at}
     * do catálogo: a biblioteca deriva a recência daqui em tempo de leitura, então
     * uma reprodução só grava uma linha em tb_plays (nada de UPDATE no catálogo).
     * Ids sem nenhum play simplesmente não aparecem no resultado (lastPlayedAt null).
     */
    @Query("""
            SELECT p.entityId AS entityId, MAX(p.playedAt) AS lastPlayedAt
            FROM Play p
            WHERE p.kind = :kind AND p.entityId IN :ids
            GROUP BY p.entityId
            """)
    List<LastPlayedRow> findLastPlayedByKind(@Param("kind") PlayKind kind,
                                             @Param("ids") Collection<UUID> ids);

    /** Projeção (entityId -> última reprodução) usada para carimbar lastPlayedAt nos DTOs. */
    interface LastPlayedRow {
        UUID getEntityId();
        Instant getLastPlayedAt();
    }
}
