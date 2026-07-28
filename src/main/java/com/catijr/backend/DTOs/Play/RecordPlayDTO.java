package com.catijr.backend.DTOs.Play;

/**
 * Corpo de POST /user/plays:
 * {@code {"kind":"music|album|artist|playlist","id":"<uuid>"}}.
 *
 * <p>{@code kind} e {@code id} chegam como String (não PlayKind/UUID) de
 * propósito — mesma escolha do
 * {@link com.catijr.backend.DTOs.Playlist.ReorderPlaylistDTO}: assim uma entrada
 * malformada cai num 400 CONTROLADO em {@code UserService.recordPlay}, e não no
 * erro de desserialização do Jackson.
 */
public record RecordPlayDTO(String kind, String id) {
}
