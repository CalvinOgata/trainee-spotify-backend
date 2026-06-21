package com.catijr.backend.Services;


import com.catijr.backend.Entities.Album;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Repositories.ArtistRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    //private MusicRepository musicRepository;

    public List<Music> getPopularMusicsByArtistId(UUID artistId) {
        var artist = artistRepository.findById(artistId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        List<Music> pop = artist.getSongs();

        // CORREÇÃO: o comparador estava em ordem crescente, retornando as músicas menos ouvidas em vez das mais populares
        pop.sort((m1, m2) -> Integer.compare(m2.getTimesListen(), m1.getTimesListen()));

        return pop;
    }

    public List<Album> getAlbumsByArtistId(UUID artistId) {
        var artist = artistRepository.findById(artistId).orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));

        return artist.albums;
    }
}
