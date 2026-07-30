package com.catijr.backend.Repositories;

import com.catijr.backend.Entities.LibraryItem;
import com.catijr.backend.Entities.LibraryKind;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface LibraryItemRepository extends JpaRepository<LibraryItem, UUID> {

    // Itens de um tipo, do mais recentemente adicionado ao mais antigo (ordem do GET).
    List<LibraryItem> findByKindOrderByAddedAtDesc(LibraryKind kind);
}
