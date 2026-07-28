package com.catijr.backend.Controllers;

import com.catijr.backend.DTOs.Playlist.GetPlaylistDTO;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.catijr.backend.DTOs.Album.GetAlbumNoMusicsDTO;
import com.catijr.backend.DTOs.Artist.GetArtistDTO;
import com.catijr.backend.DTOs.Music.GetMusicDTO;
import com.catijr.backend.DTOs.Play.RecordPlayDTO;
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
import org.springframework.web.bind.annotation.RequestBody;


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

    Lists the 8 artists the user most recently played, derived from tb_plays
    (see POST /user/plays). Distinct artists, most-recently-played first; deleted
    artists are filtered out and no plays yields an empty list.
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

    Lists the 8 musics the user most recently played, derived from tb_plays
    (see POST /user/plays). Distinct musics, most-recently-played first; deleted
    musics are filtered out and no plays yields an empty list.
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

    Lists the 8 albums the user most recently played, derived from tb_plays
    (see POST /user/plays). Distinct albums, most-recently-played first; deleted
    albums are filtered out and no plays yields an empty list.
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
    POST METHOD:

    Records a "qualified" play. The frontend only POSTs after min(30s, duration/2)
    of listening, so there is NO threshold here — every call becomes a row in
    tb_plays. Body: {"kind":"music|album|artist|playlist","id":"<entity-id>"}.
    Feeds the /recent* endpoints, which dedup at read time. "playlist" is accepted
    and stored even though it has no read endpoint yet.

    204 No Content on success; 400 if kind/id are missing or malformed.
    */
    @PostMapping("/plays")
    public ResponseEntity<Void> recordPlay(@RequestBody RecordPlayDTO play) {
        userService.recordPlay(play.kind(), play.id());
        return ResponseEntity.noContent().build();
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
    public ResponseEntity<Void> saveMusic(@PathVariable UUID musicId) {
        userService.saveMusic(musicId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/savedMusics/{musicId}")
    public ResponseEntity<Void> unsaveMusic(@PathVariable UUID musicId) {
        userService.unsaveMusic(musicId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/savedAlbums")
    public ResponseEntity<List<GetAlbumNoMusicsDTO>> getSavedAlbums() {
        return ResponseEntity.ok(userService.getSavedAlbums());
    }

    @PostMapping("/savedAlbums/{albumId}")
    public ResponseEntity<Void> saveAlbum(@PathVariable UUID albumId) {
        userService.saveAlbum(albumId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/savedAlbums/{albumId}")
    public ResponseEntity<Void> unsaveAlbum(@PathVariable UUID albumId) {
        userService.unsaveAlbum(albumId);
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/followedArtists")
    public ResponseEntity<List<GetArtistDTO>> getFollowedArtists() {
        return ResponseEntity.ok(userService.getFollowedArtists());
    }

    @PostMapping("/followedArtists/{artistId}")
    public ResponseEntity<Void> followArtist(@PathVariable UUID artistId) {
        userService.followArtist(artistId);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/followedArtists/{artistId}")
    public ResponseEntity<Void> unfollowArtist(@PathVariable UUID artistId) {
        userService.unfollowArtist(artistId);
        return ResponseEntity.noContent().build();
    }

}
