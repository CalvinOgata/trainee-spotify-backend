package com.catijr.backend.DTOs.Playlist;

/**
 * Corpo de PATCH /playlist/{id}/private: define de forma EXPLÍCITA (set, não toggle)
 * se a playlist é privada. Set explícito evita que abas concorrentes briguem — o
 * frontend já conhece o estado atual e manda o valor desejado.
 *
 * <p>{@code isPrivate} é {@code Boolean} (não {@code boolean}) de propósito: assim
 * dá pra distinguir o campo AUSENTE ({@code null} -> 400) de um valor real.
 */
public record SetPlaylistPrivacyDTO(Boolean isPrivate) {
}
