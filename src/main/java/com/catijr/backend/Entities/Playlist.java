package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="tb_playlists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Playlist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "playlist_id")
    private UUID id;

    @Column(name = "name")
    private String name;

    @Column(name = "description")
    private String description;

    @Column(name = "num_music")
    private int musicQtd;

    @Column(name = "duration")
    private int duration;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Última reprodução "promovida" (kind=playlist) desta playlist: atualizado pelo
    // POST /user/plays (UserService.recordPlay), NULL até a primeira reprodução
    // (nunca é backfilled). ISO-8601 UTC no JSON, igual a createdAt/updatedAt.
    @Column(name = "last_played_at")
    private Instant lastPlayedAt;

    // Caminho RELATIVO da capa (ex.: "/images/playlists/<id>.jpg"), servido
    // estaticamente em /images/**. NULL quando não há capa — o frontend usa sua
    // arte padrão nesse caso. NUNCA uma URL absoluta (o front prefixa a base).
    @Column(name = "image_url")
    private String imageUrl;

    // Privacidade da playlist (server-owned). true = oculta de todos menos o dono.
    // NOT NULL com default false (pública). O @ColumnDefault garante que o ddl-auto
    // adicione a coluna já com "default false", preenchendo as linhas existentes.
    @Column(name = "is_private", nullable = false)
    @ColumnDefault("false")
    private boolean isPrivate;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    @ManyToMany
    @JoinTable(name = "tb_playlist_music", joinColumns = @JoinColumn(name = "playlist"), inverseJoinColumns = @JoinColumn(name = "songs"))
    // @OrderColumn faz o Hibernate persistir a ORDEM da lista na coluna song_position
    // da tabela de junção. Assim GET /playlist/{id} devolve as músicas ordenadas e
    // add/remove/reorder mantêm a ordem de forma consistente (gerenciada pelo ORM).
    @OrderColumn(name = "song_position")
    private List<Music> songs;
}
