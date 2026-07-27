package com.catijr.backend.Entities;

import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

import java.time.Instant;
import java.util.UUID;

/**
 * Base das coleções de biblioteca (músicas salvas, álbuns salvos e artistas
 * seguidos). O app tem um único usuário implícito, então NÃO há coluna de
 * usuário: a PK ({@code item_id}) é o próprio id do item, compartilhada via
 * {@link jakarta.persistence.MapsId @MapsId} com a FK para a tabela do catálogo
 * — mapeada na subclasse. Isso garante no máximo uma linha por item
 * (idempotência da PK). {@code addedAt} ordena o GET: mais recente primeiro.
 *
 * <p>Cada subclasse só precisa declarar a associação tipada
 * ({@code @ManyToOne @MapsId} para Music/Album/Artist) e a sua própria
 * {@code @Table} — o resto do mapeamento (PK compartilhada + addedAt) mora aqui.
 */
@MappedSuperclass
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public abstract class LibraryItem {

    @Id
    @Column(name = "item_id")
    private UUID itemId;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}
