package com.catijr.backend.Services;


import com.catijr.backend.Entities.Album;
import com.catijr.backend.Entities.Artist;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Repositories.ArtistRepository;
import com.catijr.backend.utils.EntityLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.Comparator;
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

        // Ordena por popularidade (timesListen) em ordem DECRESCENTE. Empate é
        // desfeito pelo título em ordem alfabética CRESCENTE, de forma determinística
        // (ex.: se 'Música 3' e 'Música 4' têm o mesmo timesListen, 'Música 3' vem
        // primeiro). Sem esse desempate, faixas empatadas sairiam na ordem que o
        // banco devolvesse — não garantida.
        pop.sort(Comparator.comparingInt(Music::getTimesListen).reversed()
                .thenComparing(Music::getTitle));

        return pop;
    }

    public List<Album> getAlbumsByArtistId(UUID artistId) {
        var artist = EntityLookup.getOr404(artistRepository, artistId);

        return artist.albums;
    }
}
