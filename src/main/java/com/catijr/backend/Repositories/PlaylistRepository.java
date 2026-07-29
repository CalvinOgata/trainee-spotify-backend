package com.catijr.backend.Repositories;

import com.catijr.backend.Entities.Playlist;
import jakarta.persistence.LockModeType;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Lock;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.Instant;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface PlaylistRepository extends JpaRepository<Playlist, UUID> {

    @Query("SELECT COUNT(p) > 0 FROM Playlist p JOIN p.songs s WHERE p.id = :playlistId AND s.id = :musicId")
    boolean musicExistsById(@Param("playlistId") UUID playlistId, @Param("musicId") UUID musicId);

    /**
     * Carrega a playlist com SELECT ... FOR UPDATE (lock pessimista na linha de
     * tb_playlists). Usado pelo reorder para serializar chamadas concorrentes:
     * dois reorders na mesma playlist disputam este lock e executam em sequência,
     * sem estado intermediário quebrado.
     */
    @Lock(LockModeType.PESSIMISTIC_WRITE)
    @Query("SELECT p FROM Playlist p WHERE p.id = :id")
    Optional<Playlist> findByIdForUpdate(@Param("id") UUID id);

    // Carimba lastPlayedAt via UPDATE em massa: NÃO dispara o @PreUpdate (updatedAt
    // não é bumpado — tocar não é editar) e é tolerante a id inexistente (0 linhas).
    @Modifying
    @Query("UPDATE Playlist p SET p.lastPlayedAt = :when WHERE p.id = :id")
    int touchLastPlayedAt(@Param("id") UUID id, @Param("when") Instant when);

}
