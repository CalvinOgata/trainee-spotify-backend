package com.catijr.backend.Entities;

/**
 * Tipo da entidade tocada, registrado em {@link Play} e gravado no banco como
 * STRING ({@code @Enumerated(EnumType.STRING)}).
 *
 * <p>O frontend envia o valor em minúsculas ("music"/"album"/"artist"/"playlist")
 * no corpo do POST /user/plays; a conversão (case-insensitive) e o 400 para um
 * valor desconhecido ficam em {@code UserService.recordPlay}. "playlist" é aceito
 * e armazenado mesmo ainda não havendo endpoint de leitura para ele (uso futuro).
 */
public enum PlayKind {
    MUSIC, ALBUM, ARTIST, PLAYLIST
}
