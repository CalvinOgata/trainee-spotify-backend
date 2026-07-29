package com.catijr.backend.Repositories;

import com.catijr.backend.Entities.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

@Repository
public interface MusicRepository extends JpaRepository<Music, UUID> {

    List<Music> findTop5ByOrderByTimesListenDesc();

    // Carimba lastPlayedAt via UPDATE em massa: NÃO dispara o @PreUpdate (updatedAt
    // não é bumpado — tocar não é editar) e é tolerante a id inexistente (0 linhas).
    @Modifying
    @Query("UPDATE Music m SET m.lastPlayedAt = :when WHERE m.id = :id")
    int touchLastPlayedAt(@Param("id") UUID id, @Param("when") Instant when);
}
