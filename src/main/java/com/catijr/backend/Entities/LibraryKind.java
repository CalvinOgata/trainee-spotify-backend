package com.catijr.backend.Entities;

/**
 * Tipo de uma entrada da biblioteca ({@link LibraryItem}). Diferente de
 * {@link PlayKind}, NÃO inclui PLAYLIST: playlists são do próprio usuário e não
 * são "salvas" na biblioteca. Persistido como STRING; o Hibernate gera um CHECK
 * constraint na coluna {@code kind} com exatamente estes valores.
 */
public enum LibraryKind {
    MUSIC, ALBUM, ARTIST
}
