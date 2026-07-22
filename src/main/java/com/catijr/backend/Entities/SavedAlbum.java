package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Um álbum salvo na biblioteca. O app tem um único usuário implícito, então NÃO
 * há coluna de usuário — a PK é o próprio id do álbum, compartilhada via
 * {@link MapsId} com a FK para tb_albums. Isso garante no máximo uma linha por
 * álbum (idempotência da PK). {@code addedAt} ordena o GET: mais recente primeiro.
 */
@Entity
@Table(name = "saved_album")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedAlbum {

    @Id
    @Column(name = "item_id")
    private UUID itemId;

    @MapsId
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private Album album;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}
