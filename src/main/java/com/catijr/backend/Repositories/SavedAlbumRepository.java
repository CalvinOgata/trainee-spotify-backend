package com.catijr.backend.Repositories;

import com.catijr.backend.Entities.SavedAlbum;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface SavedAlbumRepository extends JpaRepository<SavedAlbum, UUID> {
}
