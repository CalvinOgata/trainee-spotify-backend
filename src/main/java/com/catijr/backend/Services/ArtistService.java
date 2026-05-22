package com.catijr.backend.Services;


import com.catijr.backend.DTOs.GetMusicDTO;
import com.catijr.backend.Entities.Artist;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Repositories.ArtistRepository;
import com.catijr.backend.Repositories.MusicRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;
import java.util.stream.Collector;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    //private MusicRepository musicRepository;

    public List<Music> getPopularMusicsByArtistId(UUID artistId) {
        var a = artistRepository.findById(artistId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Music> pop = a.getSongs();

        pop.sort((m1, m2) -> Integer.compare(m1.getTimesListen(), m2.getTimesListen()));

        return pop;
    }
}
