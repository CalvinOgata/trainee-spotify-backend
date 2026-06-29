package com.catijr.backend.playlist;

import com.catijr.backend.DTOs.Playlist.ReorderPlaylistDTO;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;

/**
 * Validação do corpo do reorder — barata e ANTES de qualquer acesso ao banco.
 * Em qualquer violação lança {@link InvalidReorderException} (vira 400 com
 * mensagem fixa, sem ecoar o payload).
 */
public final class ReorderRequestValidator {

    /** Teto de sanidade: recusa corpos grandes sem tocar o banco. */
    public static final int MAX_IDS = 500;

    private ReorderRequestValidator() {
    }

    /**
     * @return os ids já convertidos para UUID, na ordem recebida
     * @throws InvalidReorderException se o corpo/musicIds for inválido
     */
    public static List<UUID> validate(ReorderPlaylistDTO body) {
        if (body == null || body.musicIds() == null) {
            throw new InvalidReorderException();
        }
        List<String> raw = body.musicIds();

        // Teto checado ANTES de parsear (não tocamos o banco).
        if (raw.size() > MAX_IDS) {
            throw new InvalidReorderException();
        }

        List<UUID> parsed = new ArrayList<>(raw.size());
        for (String id : raw) {
            if (id == null) {
                throw new InvalidReorderException();
            }
            try {
                parsed.add(UUID.fromString(id));
            } catch (IllegalArgumentException ex) {
                throw new InvalidReorderException(); // id não-UUID
            }
        }

        // Duplicatas: mesmo id aparecendo duas vezes.
        if (new HashSet<>(parsed).size() != parsed.size()) {
            throw new InvalidReorderException();
        }

        return parsed;
    }
}
