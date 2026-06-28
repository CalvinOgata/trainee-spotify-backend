package com.catijr.backend.search;

/**
 * Envolve qualquer falha de banco durante a busca. O handler converte isso em
 * 500 com corpo fixo {@code {"error":"search failed"}} — nunca expõe SQL,
 * parâmetros ou stack trace na resposta.
 */
public class SearchFailedException extends RuntimeException {

    public SearchFailedException(Throwable cause) {
        super(cause);
    }
}
