package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="tb_musics")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Music {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "music_id")
    private UUID id;

    @Column(name = "title")
    private String title;

    @ManyToOne
    @JoinColumn(name = "artist")
    private Artist artist;

    @ManyToOne
    @JoinColumn(name = "album")
    private Album album;

    @ManyToMany(mappedBy = "songs")
    private List<Playlist> playlists;

    @Column(name = "duration")
    private int duration;

    @Column(name = "release_date")
    private Instant releaseDate;

    @Column(name = "times_listen")
    private int timesListen;

    @Column(name = "explicit")
    private Boolean explicit;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Última reprodução "promovida" (kind=music) desta faixa: atualizado pelo
    // POST /user/plays (UserService.recordPlay), NULL até a primeira reprodução
    // (nunca é backfilled). ISO-8601 UTC no JSON, igual a createdAt/updatedAt.
    @Column(name = "last_played_at")
    private Instant lastPlayedAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }


}
