package com.catijr.backend.playlist;

/**
 * O usuário autenticado não é dono da playlist. Vira 403. Hoje o projeto não tem
 * camada de auth nem campo de dono (modelo de usuário único), então este caminho
 * não é alcançado; existe para quando a autorização real for adicionada.
 */
public class NotPlaylistOwnerException extends RuntimeException {
}
