package com.catijr.backend.playlist;

/**
 * O conjunto de musicIds enviado não bate (como CONJUNTO) com o conteúdo atual
 * da playlist — o cliente estava com uma visão desatualizada. Vira 409 com corpo
 * fixo {@code {"error":"playlist contents changed, refetch"}}.
 */
public class PlaylistContentsChangedException extends RuntimeException {
}
