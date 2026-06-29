package com.catijr.backend.DTOs.Playlist;

import java.util.List;

/**
 * Corpo de PUT /playlist/{id}/order. O frontend envia SEMPRE a ordem completa
 * (não um delta), o que mantém a operação idempotente.
 *
 * <p>Os ids vêm como String (não List&lt;UUID&gt;) de propósito: assim a validação
 * de "id não-UUID" cai num único caminho controlado (400 "invalid musicIds")
 * em vez de depender do erro de desserialização do Jackson.
 */
public record ReorderPlaylistDTO(List<String> musicIds) {
}
