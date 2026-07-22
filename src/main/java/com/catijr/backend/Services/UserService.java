package com.catijr.backend.Services;

import java.time.Instant;
import java.util.List;
import java.util.UUID;

import org.springframework.data.domain.Sort;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import com.catijr.backend.DTOs.Album.GetAlbumNoMusicsDTO;
import com.catijr.backend.DTOs.Artist.GetArtistDTO;
import com.catijr.backend.DTOs.Music.GetMusicDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;
import com.catijr.backend.Entities.Album;
import com.catijr.backend.Entities.Artist;
import com.catijr.backend.Entities.FollowedArtist;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Playlist;
import com.catijr.backend.Entities.SavedAlbum;
import com.catijr.backend.Entities.SavedMusic;
import com.catijr.backend.Repositories.AlbumRepository;
import com.catijr.backend.Repositories.ArtistRepository;
import com.catijr.backend.Repositories.FollowedArtistRepository;
import com.catijr.backend.Repositories.MusicRepository;
import com.catijr.backend.Repositories.PlaylistRepository;
import com.catijr.backend.Repositories.SavedAlbumRepository;
import com.catijr.backend.Repositories.SavedMusicRepository;
import com.catijr.backend.Mappers.AlbumMapper;
import com.catijr.backend.Mappers.ArtistMapper;
import com.catijr.backend.Mappers.MusicMapper;
import com.catijr.backend.Mappers.PlaylistMapper;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService {
    
    private final AlbumRepository       albumRepository;
    private final MusicRepository       musicRepository;
    private final PlaylistRepository    playlistRepository;
    private final ArtistRepository      artistRepository;

    private final SavedMusicRepository      savedMusicRepository;
    private final SavedAlbumRepository      savedAlbumRepository;
    private final FollowedArtistRepository  followedArtistRepository;

    private final AlbumMapper           albumMapper;
    private final PlaylistMapper        playlistMapper;
    private final ArtistMapper          artistMapper;
    private final MusicMapper           musicMapper;

    // GET das coleções de biblioteca: mais recentemente adicionado primeiro.
    private static final Sort ADDED_AT_DESC = Sort.by(Sort.Direction.DESC, "addedAt");


    public List<GetPlaylistNoMusicDTO> getUserPlaylists(){
        // Ordem estável entre mutações: createdAt ASC (playlists antigas ficam no
        // topo, novas entram no fim). NÃO ordenar por updatedAt — reordenar faixas
        // bumpa updatedAt e jogaria a playlist recém-editada pro fim da sidebar.
        // Desempate por id garante ordem total mesmo se dois createdAt coincidirem.
        List<Playlist> playlists = playlistRepository.findAll(
                Sort.by(Sort.Order.asc("createdAt"), Sort.Order.asc("id")));

        return playlists.stream().map(playlistMapper::toDTO).toList();
    }

    public List<GetArtistDTO> getUserRecentArtists(){
        List<Artist> artists = artistRepository.findTop5By();

        return artists.stream().map(artistMapper::toDTO).toList();   
    }

    public List<GetArtistDTO> getUserMostPlayedArtists(){
        List<Artist> artists = artistRepository.findTop5ByOrderByListenersDesc();
        
        return artists.stream().map(artistMapper::toDTO).toList();
    }

    public List<GetMusicDTO> getUserRecentMusics(){
        List<Music> musics = musicRepository.findTop5By();

        return musics.stream().map(musicMapper::toDTO).toList();
    }

    public List<GetMusicDTO> getUserMostPlayedMusics(){
        List<Music> musics = musicRepository.findTop5ByOrderByTimesListenDesc();

        return musics.stream().map(musicMapper::toDTO).toList();
    }

    public List<GetAlbumNoMusicsDTO> getUserRecentAlbums(){
        List<Album> albums= albumRepository.findTop5By();

        return albums.stream().map(albumMapper::toNoMusicsDTO).toList();
    }

    // ------------------------------------------------------------------
    // Biblioteca: músicas salvas, álbuns salvos e artistas seguidos.
    //
    // POST/DELETE são idempotentes (a PK compartilhada com o id do item já
    // impede duplicatas): retornam sempre 204, tanto se a linha já existia
    // quanto se já estava ausente. O 404 é reservado para quando a própria
    // música/álbum/artista referenciada não existe no catálogo — por isso a
    // checagem de existência é feita no repositório do CATÁLOGO, não no de
    // biblioteca. Um re-POST NÃO atualiza o addedAt (preserva a ordem original).
    // ------------------------------------------------------------------

    public List<GetMusicDTO> getSavedMusics(){
        return savedMusicRepository.findAll(ADDED_AT_DESC).stream()
                .map(SavedMusic::getMusic)
                .map(musicMapper::toDTO)
                .toList();
    }

    @Transactional
    public void saveMusic(UUID musicId){
        Music music = musicRepository.findById(musicId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!savedMusicRepository.existsById(musicId)) {
            savedMusicRepository.save(
                    SavedMusic.builder().music(music).addedAt(Instant.now()).build());
        }
    }

    @Transactional
    public void unsaveMusic(UUID musicId){
        if (!musicRepository.existsById(musicId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (savedMusicRepository.existsById(musicId)) {
            savedMusicRepository.deleteById(musicId);
        }
    }

    public List<GetAlbumNoMusicsDTO> getSavedAlbums(){
        return savedAlbumRepository.findAll(ADDED_AT_DESC).stream()
                .map(SavedAlbum::getAlbum)
                .map(albumMapper::toNoMusicsDTO)
                .toList();
    }

    @Transactional
    public void saveAlbum(UUID albumId){
        Album album = albumRepository.findById(albumId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!savedAlbumRepository.existsById(albumId)) {
            savedAlbumRepository.save(
                    SavedAlbum.builder().album(album).addedAt(Instant.now()).build());
        }
    }

    @Transactional
    public void unsaveAlbum(UUID albumId){
        if (!albumRepository.existsById(albumId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (savedAlbumRepository.existsById(albumId)) {
            savedAlbumRepository.deleteById(albumId);
        }
    }

    public List<GetArtistDTO> getFollowedArtists(){
        return followedArtistRepository.findAll(ADDED_AT_DESC).stream()
                .map(FollowedArtist::getArtist)
                .map(artistMapper::toDTO)
                .toList();
    }

    @Transactional
    public void followArtist(UUID artistId){
        Artist artist = artistRepository.findById(artistId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
        if (!followedArtistRepository.existsById(artistId)) {
            followedArtistRepository.save(
                    FollowedArtist.builder().artist(artist).addedAt(Instant.now()).build());
        }
    }

    @Transactional
    public void unfollowArtist(UUID artistId){
        if (!artistRepository.existsById(artistId)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
        if (followedArtistRepository.existsById(artistId)) {
            followedArtistRepository.deleteById(artistId);
        }
    }








}
