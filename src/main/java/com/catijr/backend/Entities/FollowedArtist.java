package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Um artista seguido pelo usuário (semanticamente "follow", não "save"). Herda
 * de {@link LibraryItem} a PK compartilhada ({@code item_id} via {@link MapsId})
 * e o {@code addedAt} que ordena o GET (seguido mais recentemente primeiro).
 */
@Entity
@Table(name = "followed_artist")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class FollowedArtist extends LibraryItem {

    @MapsId
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private Artist artist;
}
