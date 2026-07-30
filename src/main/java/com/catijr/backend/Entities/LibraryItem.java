package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Uma entrada da biblioteca do usuário: música salva, álbum salvo ou artista
 * seguido — as três antes eram tabelas idênticas (saved_music, saved_album,
 * followed_artist) e agora vivem numa única tabela polimórfica {@code
 * library_items}, discriminada por {@link #kind} (mesmo estilo de
 * {@link Play}/tb_plays).
 *
 * <p>Projeto single-user: NÃO há coluna de usuário. A PK é o próprio
 * {@code item_id} (o id do item no catálogo). Como os UUID do catálogo são
 * globalmente únicos, um id pertence a exatamente uma música/álbum/artista, então
 * a PK sozinha já garante no máximo uma linha por item (idempotência de
 * save/follow) — não é preciso PK composta com o kind.
 *
 * <p>SEM FK para o catálogo (polimórfica, igual a tb_plays): o {@code item_id}
 * cruza três tabelas, e um item apagado é simplesmente filtrado na leitura (o
 * catálogo não o devolve), sem 500. {@code addedAt} ordena o GET: mais
 * recentemente adicionado primeiro.
 */
@Entity
@Table(name = "library_items")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class LibraryItem {

    @Id
    @Column(name = "item_id")
    private UUID itemId;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private LibraryKind kind;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}
