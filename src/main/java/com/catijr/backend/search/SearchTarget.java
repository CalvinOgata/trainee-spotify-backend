package com.catijr.backend.search;

import com.catijr.backend.Entities.Album;
import com.catijr.backend.Entities.Artist;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Playlist;

import java.util.List;

/**
 * Configuração (única fonte de verdade) de cada categoria pesquisável. Os quatro
 * SELECTs de busca eram praticamente idênticos — só mudavam tabela, coluna(s) e
 * ordenação secundária. Aqui ficam exatamente essas diferenças; o SQL em si é
 * montado uma vez só em {@link SearchQueryExecutor}.
 *
 * <p>IMPORTANTE: todos os campos abaixo são CONSTANTES de compilação. Nada vem
 * da requisição. Apenas os valores digitados pelo usuário viram parâmetros
 * bindados ({@code :pattern/:prefix/:exact}) no executor.
 */
public enum SearchTarget {

    MUSIC(Music.class, "tb_musics", List.of("title"), "title", "times_listen DESC"),
    ARTIST(Artist.class, "tb_artists", List.of("artist_name"), "artist_name", "num_listeners DESC"),
    ALBUM(Album.class, "tb_albums", List.of("album_title"), "album_title", "album_title ASC"),
    PLAYLIST(Playlist.class, "tb_playlists", List.of("name", "description"), "name", "name ASC");

    private final Class<?> entityClass;
    private final String table;
    /** Colunas casadas no WHERE (unidas por OR). Ex.: playlist casa name OU description. */
    private final List<String> matchColumns;
    /** Coluna usada para as faixas de relevância (exato > prefixo > contém). */
    private final String relevanceColumn;
    /** Desempate dentro de cada faixa, ex.: "times_listen DESC". */
    private final String secondaryOrder;

    SearchTarget(Class<?> entityClass, String table, List<String> matchColumns,
                 String relevanceColumn, String secondaryOrder) {
        this.entityClass = entityClass;
        this.table = table;
        this.matchColumns = matchColumns;
        this.relevanceColumn = relevanceColumn;
        this.secondaryOrder = secondaryOrder;
    }

    public Class<?> entityClass() {
        return entityClass;
    }

    public String table() {
        return table;
    }

    public List<String> matchColumns() {
        return matchColumns;
    }

    public String relevanceColumn() {
        return relevanceColumn;
    }

    public String secondaryOrder() {
        return secondaryOrder;
    }
}
