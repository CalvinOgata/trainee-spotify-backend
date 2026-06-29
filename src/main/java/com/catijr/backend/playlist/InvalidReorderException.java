package com.catijr.backend.playlist;

/**
 * Corpo de reorder malformado (musicIds ausente, não-array, id não-UUID,
 * duplicado, ou acima do limite de tamanho). Vira 400 com corpo fixo
 * {@code {"error":"invalid musicIds"}} — nunca ecoa o payload do usuário.
 */
public class InvalidReorderException extends RuntimeException {
}
