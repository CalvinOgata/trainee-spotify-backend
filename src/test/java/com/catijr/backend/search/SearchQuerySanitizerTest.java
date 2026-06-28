package com.catijr.backend.search;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

/**
 * Testes unitários puros (sem Spring/banco) da validação/normalização da query
 * e do escape de curingas de LIKE.
 */
class SearchQuerySanitizerTest {

    @Test
    void nullQueryIsRejectedAsRequired() {
        InvalidSearchQueryException ex =
                assertThrows(InvalidSearchQueryException.class, () -> SearchQuerySanitizer.normalize(null));
        assertEquals("q is required", ex.getMessage());
    }

    @Test
    void blankAndWhitespaceOnlyQueriesAreRejectedAsRequired() {
        assertEquals("q is required",
                assertThrows(InvalidSearchQueryException.class,
                        () -> SearchQuerySanitizer.normalize("   \t \n ")).getMessage());
    }

    @Test
    void queryLongerThan200CharsIsRejectedAsTooLong() {
        String longQ = "a".repeat(201);
        assertEquals("q is too long",
                assertThrows(InvalidSearchQueryException.class,
                        () -> SearchQuerySanitizer.normalize(longQ)).getMessage());
    }

    @Test
    void exactly200CharsIsAccepted() {
        String q = "a".repeat(200);
        assertEquals(200, SearchQuerySanitizer.normalize(q).length());
    }

    @Test
    void trimsAndCollapsesInternalWhitespace() {
        assertEquals("foo bar baz", SearchQuerySanitizer.normalize("  foo   bar\t\tbaz  "));
    }

    @Test
    void controlCharactersAreTreatedAsSeparators() {
        // Chars de controle construídos em runtime (sem bytes não-imprimíveis no fonte).
        String bel = String.valueOf((char) 0x07);
        String soh = String.valueOf((char) 0x01);
        String del = String.valueOf((char) 0x7F);
        assertEquals("foo bar", SearchQuerySanitizer.normalize("foo" + bel + soh + "bar"));
        assertEquals("foo bar", SearchQuerySanitizer.normalize("foo" + del + "bar"));
    }

    @Test
    void escapeLikeWildcardsEscapesBackslashThenPercentAndUnderscore() {
        assertEquals("\\%", SearchQuerySanitizer.escapeLikeWildcards("%"));
        assertEquals("\\_", SearchQuerySanitizer.escapeLikeWildcards("_"));
        // backslash escapado primeiro: "\%" -> "\\" + "\%" = "\\\%"
        assertEquals("\\\\\\%", SearchQuerySanitizer.escapeLikeWildcards("\\%"));
    }

    @Test
    void sqlInjectionPayloadIsKeptAsLiteralText() {
        // Não há sanitização "anti-SQL" aqui (o bind cuida disso); só normalização.
        String normalized = SearchQuerySanitizer.normalize("'; DROP TABLE music; --");
        assertTrue(normalized.contains("DROP TABLE music"));
    }
}
