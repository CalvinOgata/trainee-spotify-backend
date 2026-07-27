package com.catijr.backend.Entities;

import jakarta.persistence.*;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.SuperBuilder;

/**
 * Uma música salva na biblioteca. Herda de {@link LibraryItem} a PK
 * compartilhada ({@code item_id} via {@link MapsId}) e o {@code addedAt} que
 * ordena o GET (mais recente primeiro).
 */
@Entity
@Table(name = "saved_music")
@Getter
@Setter
@NoArgsConstructor
@SuperBuilder
public class SavedMusic extends LibraryItem {

    @MapsId
    @ManyToOne(optional = false, fetch = FetchType.EAGER)
    @JoinColumn(name = "item_id")
    private Music music;
}
