package com.catijr.backend.search;

import java.text.Normalizer;

/**
 * Validação e normalização do termo de busca. Tudo feito em memória, ANTES de
 * tocar o banco. Nenhuma engine de regex roda sobre input do usuário (varredura
 * manual de caracteres) para evitar ReDoS.
 */
public final class SearchQuerySanitizer {

    /** Tamanho máximo aceito após normalização. */
    public static final int MAX_QUERY_LENGTH = 200;

    private SearchQuerySanitizer() {
    }

    /**
     * Normaliza e valida a query crua:
     * <ul>
     *   <li>normalização Unicode NFC (formas visualmente iguais batem nas mesmas linhas);</li>
     *   <li>caracteres de controle (U+0000..U+001F, U+007F) e qualquer whitespace
     *       Unicode são tratados como espaço;</li>
     *   <li>sequências de whitespace são colapsadas em um único espaço e as pontas
     *       são aparadas.</li>
     * </ul>
     *
     * @return termo normalizado e não-vazio, com no máximo {@link #MAX_QUERY_LENGTH} chars
     * @throws InvalidSearchQueryException com mensagem FIXA (nunca ecoa o q)
     */
    public static String normalize(String raw) {
        if (raw == null) {
            throw InvalidSearchQueryException.required();
        }

        String nfc = Normalizer.normalize(raw, Normalizer.Form.NFC);

        StringBuilder sb = new StringBuilder(nfc.length());
        boolean pendingSpace = false;
        for (int i = 0; i < nfc.length(); i++) {
            char c = nfc.charAt(i);
            boolean isSeparator = c <= 0x20 || c == 0x7F
                    || Character.isWhitespace(c) || Character.isSpaceChar(c);
            if (isSeparator) {
                if (sb.length() > 0) {
                    pendingSpace = true; // colapsa; ignora whitespace inicial
                }
            } else {
                if (pendingSpace) {
                    sb.append(' ');
                    pendingSpace = false;
                }
                sb.append(c);
            }
        }
        String normalized = sb.toString(); // já sem whitespace nas pontas

        if (normalized.isEmpty()) {
            throw InvalidSearchQueryException.required();
        }
        if (normalized.length() > MAX_QUERY_LENGTH) {
            throw InvalidSearchQueryException.tooLong();
        }
        return normalized;
    }

    /**
     * Escapa os curingas de LIKE para que {@code %} e {@code _} digitados pelo
     * usuário sejam tratados como literais. Backslash primeiro, depois % e _.
     * Deve ser usado em conjunto com {@code LIKE :pattern ESCAPE '\'}.
     */
    public static String escapeLikeWildcards(String s) {
        return s.replace("\\", "\\\\")
                .replace("%", "\\%")
                .replace("_", "\\_");
    }
}
