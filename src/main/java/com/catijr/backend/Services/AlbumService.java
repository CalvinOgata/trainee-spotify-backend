package com.catijr.backend.Services;

import com.catijr.backend.Entities.Music;
import com.catijr.backend.Repositories.AlbumRepository;
import com.catijr.backend.utils.EntityLookup;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class AlbumService {

    private final AlbumRepository albumRepository;

    public List<Music> getMusicsByAlbumId(UUID albumId) {
        var album = EntityLookup.getOr404(albumRepository, albumId);

        return album.getMusics();
    }

}
