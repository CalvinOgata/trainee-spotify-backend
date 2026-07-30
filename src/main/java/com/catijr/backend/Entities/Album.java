package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.*;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Entity
@Table(name="tb_albums")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Album {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    @Column(name = "album_id")
    private UUID id;

    @Column(name = "album_title")
    private String title;

    @Column(name = "release_year")
    private String year;

    @Column(name = "created_at")
    private Instant createdAt;

    @Column(name = "updated_at")
    private Instant updatedAt;

    // Caminho RELATIVO da capa (ex.: "/images/albums/<id>.jpg"), servido
    // estaticamente em /images/**. NULL quando não há capa — o frontend usa sua
    // arte padrão nesse caso. NUNCA uma URL absoluta (o front prefixa a base).
    @Column(name = "image_url")
    private String imageUrl;

    @PrePersist
    public void onPrePersist() {
        this.createdAt = Instant.now();
    }

    @PreUpdate
    public void onPreUpdate() {
        this.updatedAt = Instant.now();
    }

    //Relação com musica

    @ManyToOne
    @JoinColumn(name = "artist")
    private Artist owner;

    @OneToMany(mappedBy = "album")
    private List<Music> musics;
}
