package com.catijr.backend.Services;

import com.catijr.backend.DTOs.Search.SearchResponseDTO;
import com.catijr.backend.Entities.Album;
import com.catijr.backend.Entities.Artist;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Playlist;
import com.catijr.backend.Mappers.AlbumMapper;
import com.catijr.backend.Mappers.ArtistMapper;
import com.catijr.backend.Mappers.MusicMapper;
import com.catijr.backend.Mappers.PlaylistMapper;
import com.catijr.backend.search.SearchFailedException;
import com.catijr.backend.search.SearchQueryExecutor;
import com.catijr.backend.search.SearchQuerySanitizer;
import com.catijr.backend.search.SearchTarget;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.LinkedHashMap;
import java.util.List;
import java.util.UUID;

@Slf4j
@Service
@RequiredArgsConstructor
public class SearchService {

    private final SearchQueryExecutor searchQueryExecutor;

    private final MusicMapper musicMapper;
    private final PlaylistMapper playlistMapper;
    private final ArtistMapper artistMapper;
    private final AlbumMapper albumMapper;

    /**
     * Executa a busca em cada categoria (todas pelo mesmo executor) e monta a
     * resposta. Além das quatro categorias, deriva {@code musicAlbums} e
     * {@code musicArtists}: o álbum-pai (AlbumSummary) e o artista-pai de cada
     * faixa encontrada, deduplicados por id — contexto de navegação para a faixa
     * tocando, não listas renderizadas.
     *
     * @param normalizedQuery termo já validado, normalizado (NFC) e com whitespace colapsado
     * @param limit           máximo por categoria, já clampeado para [1, 50]
     */
    @Transactional(readOnly = true)
    public SearchResponseDTO search(String normalizedQuery, int limit) {
        // Curingas escapados ANTES do bind; '=' (faixa "exato") não usa curinga.
        String escaped = SearchQuerySanitizer.escapeLikeWildcards(normalizedQuery);
        String contains = "%" + escaped + "%";
        String prefix = escaped + "%";
        String exact = normalizedQuery;

        try {
            List<Music> musicRows = searchQueryExecutor.search(SearchTarget.MUSIC, contains, prefix, exact, limit);
            List<Playlist> playlistRows = searchQueryExecutor.search(SearchTarget.PLAYLIST, contains, prefix, exact, limit);
            List<Artist> artistRows = searchQueryExecutor.search(SearchTarget.ARTIST, contains, prefix, exact, limit);
            List<Album> albumRows = searchQueryExecutor.search(SearchTarget.ALBUM, contains, prefix, exact, limit);

            // Álbum-pai e artista-pai de cada faixa encontrada, deduplicados por id e
            // preservando a ordem em que as músicas aparecem. Faixa sem álbum/artista é
            // ignorada nesse campo. Independem de `albumRows`/`artistRows` (casaram por
            // nome): a mesma entidade pode sair nos dois campos — o frontend faz o merge.
            LinkedHashMap<UUID, Album> parentAlbumsById = new LinkedHashMap<>();
            LinkedHashMap<UUID, Artist> parentArtistsById = new LinkedHashMap<>();
            for (Music music : musicRows) {
                Album album = music.getAlbum();
                if (album != null) {
                    parentAlbumsById.putIfAbsent(album.getId(), album);
                }
                Artist artist = music.getArtist();
                if (artist != null) {
                    parentArtistsById.putIfAbsent(artist.getId(), artist);
                }
            }

            return new SearchResponseDTO(
                    musicRows.stream().map(musicMapper::toDTO).toList(),
                    playlistRows.stream().map(playlistMapper::toDTO).toList(),
                    artistRows.stream().map(artistMapper::toDTO).toList(),
                    albumRows.stream().map(albumMapper::toNoMusicsDTO).toList(),
                    parentAlbumsById.values().stream().map(albumMapper::toNoMusicsDTO).toList(),
                    parentArtistsById.values().stream().map(artistMapper::toDTO).toList());
        } catch (DataAccessException ex) {
            // Loga só o tamanho da query + um id de correlação; nunca o conteúdo, SQL ou stack na resposta.
            String requestId = UUID.randomUUID().toString();
            log.error("Falha na busca (requestId={}, q.len={})", requestId, normalizedQuery.length(), ex);
            throw new SearchFailedException(ex);
        }
    }
}
