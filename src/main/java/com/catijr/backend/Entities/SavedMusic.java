package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Uma música salva na biblioteca. O app tem um único usuário implícito, então
 * NÃO há coluna de usuário — a PK é o próprio id da música, compartilhada via
 * {@link MapsId} com a FK para tb_musics. Isso garante no máximo uma linha por
 * música (idempotência da PK). {@code addedAt} ordena o GET: mais recente primeiro.
 */
@Entity
@Table(name = "saved_music")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SavedMusic {

    @Id
    @Column(name = "item_id")
    private UUID itemId;

    @MapsId
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private Music music;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}
