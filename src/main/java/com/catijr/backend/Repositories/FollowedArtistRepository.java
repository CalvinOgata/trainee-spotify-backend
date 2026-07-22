package com.catijr.backend.Repositories;

import com.catijr.backend.Entities.FollowedArtist;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.UUID;

@Repository
public interface FollowedArtistRepository extends JpaRepository<FollowedArtist, UUID> {
}
