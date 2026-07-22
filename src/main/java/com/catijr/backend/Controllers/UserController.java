package com.catijr.backend.Controllers;

import com.catijr.backend.DTOs.Playlist.GetPlaylistDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catijr.backend.DTOs.Album.GetAlbumNoMusicsDTO;
import com.catijr.backend.DTOs.Artist.GetArtistDTO;
import com.catijr.backend.DTOs.Music.GetMusicDTO;
import com.catijr.backend.DTOs.Playlist.GetPlaylistNoMusicDTO;
import com.catijr.backend.Services.UserService;

import lombok.RequiredArgsConstructor;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;


@RestController
@RequestMapping("/user")
@RequiredArgsConstructor
public class UserController {
    
    private final UserService userService;

    /*
    GET METHOD:

    This method is used to list all the playlists of the user
    
    **since there is only a single user in this project, this is
    equivalent to listing all the playlists of the database

    */
    @GetMapping("/playlists")
    public ResponseEntity<List<GetPlaylistNoMusicDTO>> getUserPlaylists() {
        return ResponseEntity.ok( userService.getUserPlaylists());
    }

    /*
    GET METHOD:

    This method is used to list (the last 5) artists the user recently listened to,
    since this project has no support for metrics that allow this to be
    a functional route, the result of this method will be a fixed set of artists
    initialized in the database
    */
    @GetMapping("/recentArtists")
    public ResponseEntity<List<GetArtistDTO>> getUserRecentArtists() {
        return ResponseEntity.ok(userService.getUserRecentArtists());
    }
    

    /*
    GET METHOD:

    This method is used to list the 5 most played artists of the user,
    since this project has no support for metrics that allow this to be
    a functional route, the result of this method will be a fixed set of artists
    initialized in the database

    */
    @GetMapping("/mostPlayedArtists")
    public ResponseEntity<List<GetArtistDTO>> getUserMostPlayedArtists() {
        return ResponseEntity.ok( userService.getUserMostPlayedArtists());
    }

    /*
    GET METHOD:
    
    This method is used to list the user's (last 5) recently played musics,
    since this project has no support for metrics that allow this to be
    a functional route, the result of this method will be a fixed set of musics
    initialized in the database
    */
    @GetMapping("/recentMusics")
    public ResponseEntity<List<GetMusicDTO>> getUserRecentMusics() {
        return ResponseEntity.ok(userService.getUserRecentMusics());
    }

    /*
    GET METHOD:
    
    This method is used to list the user's 5 most played musics,
    since this project has no support for metrics that allow this to be
    a functional route, the result of this method will be a fixed set of musics
    initialized in the database
    */
    @GetMapping("/mostPlayedMusics")
    public ResponseEntity<List<GetMusicDTO>> getUserMostPlayedMusics() {
        return ResponseEntity.ok(userService.getUserMostPlayedMusics());
    }

     /*
    GET METHOD:
    
    This method is used to list the user's (last 5) recently played albums,
    since this project has no support for metrics that allow this to be
    a functional route, the result of this method will be a fixed set of albums
    initialized in the database
    */
    @GetMapping("/recentAlbums")
    public ResponseEntity<List<GetAlbumNoMusicsDTO>> getUserRecentAlbums() {
        return ResponseEntity.ok( userService.getUserRecentAlbums());
    }

    /*
    GET METHOD:

    THis method is used to list the user's followers, since this project
    has no support for this logic, the result of this method will be a
    fixed set of data not initialized in the database
    */ 
    @GetMapping("/followers")
    public ResponseEntity< List<String>> getUserFollowers() {
        List<String> followers =new ArrayList<>(List.of("deadbeat7","xmc0-Infinity","John Doe", "Jose Manuel Alberto Lopez","XCS_2026"));

        return ResponseEntity.ok(followers);
    }

    /*
    BIBLIOTECA — músicas salvas, álbuns salvos e artistas seguidos.

    Cada coleção tem um GET (itens ordenados por adição, mais recente primeiro)
    e um par POST/DELETE idempotente por id: ambos retornam 204 independentemente
    de a linha já existir/estar ausente. O 404 só ocorre quando a própria
    música/álbum/artista referenciada não existe no catálogo.
    */

    @GetMapping("/savedMusics")
    public ResponseEntity<List<GetMusicDTO>> getSavedMusics() {
        return ResponseEntity.ok(userService.getSavedMusics());
    }

    @PostMapping("/savedMusics/{musicId}")
    public ResponseEntity<Void> saveMusic(@PathVariable String musicId) {
        userService.saveMusic(UUID.fromString(musicId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/savedMusics/{musicId}")
    public ResponseEntity<Void> unsaveMusic(@PathVariable String musicId) {
        userService.unsaveMusic(UUID.fromString(musicId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/savedAlbums")
    public ResponseEntity<List<GetAlbumNoMusicsDTO>> getSavedAlbums() {
        return ResponseEntity.ok(userService.getSavedAlbums());
    }

    @PostMapping("/savedAlbums/{albumId}")
    public ResponseEntity<Void> saveAlbum(@PathVariable String albumId) {
        userService.saveAlbum(UUID.fromString(albumId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/savedAlbums/{albumId}")
    public ResponseEntity<Void> unsaveAlbum(@PathVariable String albumId) {
        userService.unsaveAlbum(UUID.fromString(albumId));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followedArtists")
    public ResponseEntity<List<GetArtistDTO>> getFollowedArtists() {
        return ResponseEntity.ok(userService.getFollowedArtists());
    }

    @PostMapping("/followedArtists/{artistId}")
    public ResponseEntity<Void> followArtist(@PathVariable String artistId) {
        userService.followArtist(UUID.fromString(artistId));
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/followedArtists/{artistId}")
    public ResponseEntity<Void> unfollowArtist(@PathVariable String artistId) {
        userService.unfollowArtist(UUID.fromString(artistId));
        return ResponseEntity.noContent().build();
    }

}
