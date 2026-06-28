package com.catijr.backend.search;

/**
 * Lançada quando o parâmetro {@code q} é inválido. A mensagem é SEMPRE uma
 * constante fixa — nunca contém o valor digitado pelo usuário, para não
 * vazar a query de volta em respostas de erro.
 */
public class InvalidSearchQueryException extends RuntimeException {

    private InvalidSearchQueryException(String message) {
        super(message);
    }

    public static InvalidSearchQueryException required() {
        return new InvalidSearchQueryException("q is required");
    }

    public static InvalidSearchQueryException tooLong() {
        return new InvalidSearchQueryException("q is too long");
    }
}
