package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.UUID;

/**
 * Um evento de reprodução "qualificado": o frontend só faz POST /user/plays
 * depois de min(30s, duração/2) de escuta, então NÃO há threshold no backend —
 * toda chamada que chega vira uma linha. A deduplicação dos "recentes" (8
 * entidades distintas) é feita na LEITURA, via GROUP BY entity_id (ver
 * {@link com.catijr.backend.Repositories.PlayRepository}).
 *
 * <p>Projeto single-user: como todo o resto de /user/*, NÃO há coluna user_id
 * (não existe tabela de usuários para referenciar). {@code entityId} aponta para
 * música/álbum/artista/playlist conforme {@link #kind}, SEM FK — o id cruza 4
 * tabelas e uma entidade já apagada é simplesmente filtrada na leitura (o JOIN
 * não a encontra), sem 500.
 */
@Entity
@Table(name = "tb_plays", indexes = {
        @Index(name = "idx_plays_kind_played_at", columnList = "kind, played_at desc")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Play {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "play_id")
    private UUID id;

    @Enumerated(EnumType.STRING)
    @Column(name = "kind", nullable = false)
    private PlayKind kind;

    @Column(name = "entity_id", nullable = false)
    private UUID entityId;

    @Column(name = "played_at", nullable = false)
    private Instant playedAt;

    @PrePersist
    public void onPrePersist() {
        if (this.playedAt == null) {
            this.playedAt = Instant.now();
        }
    }
}
