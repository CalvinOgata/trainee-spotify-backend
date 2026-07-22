package com.catijr.backend.Repositories;

import com.catijr.backend.Entities.SavedMusic;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SavedMusicRepository extends JpaRepository<SavedMusic, UUID> {
}
