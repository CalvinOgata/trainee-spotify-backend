package com.catijr.backend.utils;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.util.UUID;

/**
 * Busca de entidade por id com a política de 404 centralizada. Antes, o par
 * {@code findById(id).orElseThrow(() -> new ResponseStatusException(NOT_FOUND))}
 * (e a variante com {@code existsById}) estava copiado em ~13 pontos dos
 * services; aqui vira um único lugar, mantendo o mesmo comportamento.
 */
public final class EntityLookup {

    private EntityLookup() {}

    /** Retorna a entidade ou lança 404 se o id não existir no repositório. */
    public static <T> T getOr404(JpaRepository<T, UUID> repository, UUID id) {
        return repository.findById(id)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND));
    }

    /** Garante que o id existe no repositório; caso contrário lança 404. */
    public static void existsOr404(JpaRepository<?, UUID> repository, UUID id) {
        if (!repository.existsById(id)) {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND);
        }
    }
}
