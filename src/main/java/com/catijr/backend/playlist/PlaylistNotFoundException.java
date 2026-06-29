package com.catijr.backend.playlist;

/**
 * Playlist inexistente OU que o chamador não tem direito de saber que existe.
 * Vira 404 (em vez de 403) para não vazar a existência do recurso.
 */
public class PlaylistNotFoundException extends RuntimeException {
}
