package com.catijr.backend.Repositories;

import com.catijr.backend.Entities.Music;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface MusicRepository extends JpaRepository<Music, UUID> {

    List<Music> findTop5ByOrderByTimesListenDesc();
}
