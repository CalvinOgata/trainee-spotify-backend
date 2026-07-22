package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Um artista seguido pelo usuário (semanticamente "follow", não "save"). O app
 * tem um único usuário implícito, então NÃO há coluna de usuário — a PK é o
 * próprio id do artista, compartilhada via {@link MapsId} com a FK para
 * tb_artists. Isso garante no máximo uma linha por artista (idempotência da PK).
 * {@code addedAt} ordena o GET: seguido mais recentemente primeiro.
 */
@Entity
@Table(name = "followed_artist")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class FollowedArtist {

    @Id
    @Column(name = "item_id")
    private UUID itemId;

    @MapsId
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private Artist artist;

    @Column(name = "added_at", nullable = false)
    private Instant addedAt;
}
