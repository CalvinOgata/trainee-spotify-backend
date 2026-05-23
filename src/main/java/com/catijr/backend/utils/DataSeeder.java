package com.catijr.backend.utils;

import com.catijr.backend.Entities.*;
import com.catijr.backend.Repositories.*;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;


@Slf4j
@Component
@Profile("dev")
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

    private final ArtistRepository artistRepository;
    private final AlbumRepository albumRepository;
    private final MusicRepository musicRepository;
    private final PlaylistRepository playlistRepository;

    @Override
    @Transactional // Mantém a sessão aberta para gerenciar as listas do ManyToMany de forma segura
    public void run(String... args) throws Exception {

        System.out.println("====== INICIANDO SEEDER: ARQUITETURA CONSOLIDADA ======");

        List<Music> allCreatedSongs = new ArrayList<>();

        // 1. Gerar 5 Artistas
        for (int i = 1; i <= 5; i++) {
            Artist artist = Artist.builder()
                    .name("Artista" + i)
                    .listeners(200_000 * i)
                    .about("Biografia do Artista " + i + ". Totalmente gerada via automação de teste.")
                    .build();
            artist = artistRepository.save(artist);

            List<Album> artistAlbums = new ArrayList<>();
            List<Music> artistSongs = new ArrayList<>();

            // 2. Cada Artista possui 4 Álbuns
            for (int j = 1; j <= 4; j++) {
                Album album = Album.builder()
                        .title("Álbum " + j + " do Artista " + i)
                        .year("202" + j)
                        .owner(artist)
                        .build();
                album = albumRepository.save(album);
                artistAlbums.add(album);

                List<Music> albumSongs = new ArrayList<>();

                // 3. Cada Álbum possui 8 Músicas (Total: 160 músicas)
                for (int k = 1; k <= 8; k++) {
                    Music music = Music.builder()
                            .title("Música " + k + " [Álbum " + j + " - Artista " + i + "]")
                            .artist(artist)
                            .album(album)
                            .duration(160 + (k * 15)) // Durações variadas para testar somatórios
                            .explicit(k % 3 == 0)
                            .timesListen(5000 * k)
                            .releaseDate(Instant.now().minus(k * 10, ChronoUnit.DAYS))
                            .build();

                    music = musicRepository.save(music);
                    albumSongs.add(music);
                    artistSongs.add(music);
                    allCreatedSongs.add(music);
                }

                // Vincula as músicas criadas de volta ao álbum em memória
                album.setMusics(albumSongs);
                albumRepository.save(album);
            }

            // Sincroniza as coleções no objeto do artista
            artist.setAlbums(artistAlbums);
            artist.setSongs(artistSongs);
            artistRepository.save(artist);
        }

        System.out.println(">> Catálogo criado: " + allCreatedSongs.size() + " músicas prontas para as playlists.");

        // 4. Configuração das 4 Playlists solicitadas
        String[] playlistNames = {
                "Músicas Curtidas", // Obrigatória
                "Playlist de Academia",
                "Estudo Concentrado",
                "Modo Viagem"
        };

        for (int p = 0; p < playlistNames.length; p++) {
            // A quantidade de músicas vai variar de 1 a 4 dependendo do índice da playlist (p + 1)
            int targetMusicCount = p + 1;
            List<Music> playlistSongs = new ArrayList<>();
            int totalDuration = 0;

            // Seleciona músicas bem espaçadas para garantir que venham de álbuns/artistas distintos
            for (int s = 0; s < targetMusicCount; s++) {
                int songIndex = (p * 37 + s * 19) % allCreatedSongs.size();
                Music selectedMusic = allCreatedSongs.get(songIndex);

                playlistSongs.add(selectedMusic);
                totalDuration += selectedMusic.getDuration();
            }

            // 5. Criar e Salvar a Playlist com a relação ManyToMany direta e metadados calculados
            Playlist playlist = Playlist.builder()
                    .name(playlistNames[p])
                    .description("Sua seleção especial para: " + playlistNames[p])
                    .songs(playlistSongs)
                    .musicQtd(targetMusicCount)
                    .duration(totalDuration)
                    .build();

            playlistRepository.save(playlist);

            // 6. Atualizar a relação bidirecional nas entidades de Música para o Hibernate mapear a tabela de junção
            for (Music music : playlistSongs) {
                if (music.getPlaylists() == null) {
                    music.setPlaylists(new ArrayList<>());
                }
                music.getPlaylists().add(playlist);
                musicRepository.save(music); // Persiste o vínculo na tabela 'playlist_music'
            }
        }

        System.out.println("====== SEEDER EXECUTADO COM SUCESSO: BANCO TOTALMENTE POPULADO ======");
    }
}