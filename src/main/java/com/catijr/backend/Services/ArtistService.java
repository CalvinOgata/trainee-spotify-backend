package com.catijr.backend.Services;


import com.catijr.backend.Entities.Album;
import com.catijr.backend.Entities.Artist;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Repositories.ArtistRepository;
import com.catijr.backend.utils.EntityLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class ArtistService {

    private final ArtistRepository artistRepository;
    //private MusicRepository musicRepository;

    public Artist getArtistById(UUID artistId) {
        return EntityLookup.getOr404(artistRepository, artistId);
    }

    public List<Music> getPopularMusicsByArtistId(UUID artistId) {
        var artist = EntityLookup.getOr404(artistRepository, artistId);

        List<Music> pop = artist.getSongs();

        // CORREÇÃO: o comparador estava em ordem crescente, retornando as músicas menos ouvidas em vez das mais populares
        pop.sort((m1, m2) -> Integer.compare(m2.getTimesListen(), m1.getTimesListen()));

        return pop;
    }

    public List<Album> getAlbumsByArtistId(UUID artistId) {
        var artist = EntityLookup.getOr404(artistRepository, artistId);

        return artist.albums;
    }
}
