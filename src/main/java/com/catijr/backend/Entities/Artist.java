package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="tb_artists")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Artist {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "artist_id")
    private UUID id;

    @Column(name = "artist_name")
    private String name;

    @Column(name = "num_listeners")
    private int listeners;

    //Foto

    @Column(name = "about")
    private String about;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    @OneToMany(mappedBy = "artist")
    public List<Music> songs;

    @OneToMany(mappedBy = "owner")
    public List<Album> albums;
}
