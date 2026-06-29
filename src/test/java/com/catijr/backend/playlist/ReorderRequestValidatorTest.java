package com.catijr.backend.playlist;

import com.catijr.backend.DTOs.Playlist.ReorderPlaylistDTO;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;

class ReorderRequestValidatorTest {

    private static ReorderPlaylistDTO dto(List<String> ids) {
        return new ReorderPlaylistDTO(ids);
    }

    @Test
    void rejectsNullBody() {
        assertThrows(InvalidReorderException.class, () -> ReorderRequestValidator.validate(null));
    }

    @Test
    void rejectsNullMusicIds() {
        assertThrows(InvalidReorderException.class, () -> ReorderRequestValidator.validate(dto(null)));
    }

    @Test
    void rejectsNonUuidElement() {
        assertThrows(InvalidReorderException.class,
                () -> ReorderRequestValidator.validate(dto(List.of("not-a-uuid"))));
    }

    @Test
    void rejectsNullElement() {
        List<String> ids = new ArrayList<>();
        ids.add(UUID.randomUUID().toString());
        ids.add(null);
        assertThrows(InvalidReorderException.class, () -> ReorderRequestValidator.validate(dto(ids)));
    }

    @Test
    void rejectsDuplicates() {
        String id = UUID.randomUUID().toString();
        assertThrows(InvalidReorderException.class,
                () -> ReorderRequestValidator.validate(dto(Arrays.asList(id, id))));
    }

    @Test
    void rejectsOverMaxIdsWithoutParsing() {
        // MAX_IDS + 1 entradas; mesmo sendo lixo (não-UUID), o teto é checado ANTES
        // de parsear, então deve recusar sem nem tentar converter.
        List<String> ids = new ArrayList<>();
        for (int i = 0; i < ReorderRequestValidator.MAX_IDS + 1; i++) {
            ids.add("x");
        }
        assertThrows(InvalidReorderException.class, () -> ReorderRequestValidator.validate(dto(ids)));
    }

    @Test
    void acceptsEmptyList() {
        assertEquals(List.of(), ReorderRequestValidator.validate(dto(List.of())));
    }

    @Test
    void parsesAndPreservesOrder() {
        UUID a = UUID.randomUUID();
        UUID b = UUID.randomUUID();
        UUID c = UUID.randomUUID();
        List<UUID> result = ReorderRequestValidator.validate(
                dto(List.of(a.toString(), b.toString(), c.toString())));
        assertEquals(List.of(a, b, c), result);
    }
}
