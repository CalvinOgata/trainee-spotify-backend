package com.catijr.backend.Services;

import com.catijr.backend.DTOs.Playlist.GetPlaylistDTO;
import com.catijr.backend.Entities.Music;
import com.catijr.backend.Entities.Playlist;
import com.catijr.backend.Mappers.PlaylistMapper;
import com.catijr.backend.Repositories.PlaylistRepository;
import com.catijr.backend.playlist.PlaylistContentsChangedException;
import com.catijr.backend.playlist.PlaylistNotFoundException;
import com.catijr.backend.playlist.ReorderFailedException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Instant;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

/**
 * Reordenação de uma playlist (PUT /playlist/{id}/order).
 *
 * <p>Regras de segurança/consistência aplicadas aqui:
 * <ul>
 *   <li><b>Atômico</b>: tudo numa única transação. O lock pessimista
 *       ({@link PlaylistRepository#findByIdForUpdate}) serializa reorders
 *       concorrentes na mesma playlist — sem estado intermediário quebrado.</li>
 *   <li><b>Reorder-only</b>: o conjunto enviado tem que bater (como CONJUNTO)
 *       com o conteúdo atual; senão é 409 (cliente desatualizado), não inserção
 *       nem remoção silenciosa.</li>
 *   <li><b>Idempotente</b>: se a ordem pedida já é a atual, não escreve nada
 *       (não bumpa updatedAt).</li>
 *   <li><b>Ownership antes de tudo</b>: a verificação de dono (hoje no-op, ver
 *       abaixo) acontece logo após carregar a playlist e ANTES de validar
 *       conteúdo, para não vazar informação a quem não é dono.</li>
 *   <li><b>Sem vazamento em falha de banco</b>: qualquer {@link DataAccessException}
 *       é logada com playlistId+requestId (NUNCA os musicIds) e convertida em
 *       {@link ReorderFailedException} (vira 500 com corpo fixo).</li>
 * </ul>
 *
 * <p>A escrita da nova ordem é delegada ao Hibernate via {@code @OrderColumn}:
 * substituir a lista por {@code setSongs(novaOrdem)} faz o ORM reescrever a
 * coluna {@code song_position} da tabela de junção de forma parametrizada
 * (nenhum SQL concatenado à mão).
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class PlaylistReorderService {

    private final PlaylistRepository playlistRepository;
    private final PlaylistMapper playlistMapper;

    @Transactional
    public GetPlaylistDTO reorder(UUID playlistId, List<UUID> requestedOrder) {
        try {
            // 1) Carrega COM lock pessimista (SELECT ... FOR UPDATE) ou 404.
            Playlist playlist = playlistRepository.findByIdForUpdate(playlistId)
                    .orElseThrow(PlaylistNotFoundException::new);

            // 2) Ownership ANTES de olhar conteúdo. Hoje é no-op: não há auth nem
            //    campo de dono (modelo de usuário único). Quando houver, lançar
            //    NotPlaylistOwnerException aqui (ou PlaylistNotFoundException para
            //    esconder a existência), antes de qualquer validação de conteúdo.

            // 3) Ordem atual (já ordenada pelo @OrderColumn).
            List<Music> current = playlist.getSongs() == null
                    ? new ArrayList<>()
                    : new ArrayList<>(playlist.getSongs());

            List<UUID> currentOrder = new ArrayList<>(current.size());
            Map<UUID, Music> byId = new LinkedHashMap<>(current.size());
            for (Music m : current) {
                currentOrder.add(m.getId());
                byId.put(m.getId(), m);
            }

            // 4) Reorder-only: o CONJUNTO tem que ser idêntico. O validador já
            //    garante que requestedOrder não tem duplicatas, então comparar
            //    conjuntos basta (cobre tamanho, ids a mais e ids a menos).
            if (!new HashSet<>(currentOrder).equals(new HashSet<>(requestedOrder))) {
                throw new PlaylistContentsChangedException();
            }

            // 5) Idempotência: se já está nessa ordem, não escreve nada.
            if (currentOrder.equals(requestedOrder)) {
                return playlistMapper.toFullDTO(playlist);
            }

            // 6) Aplica a nova ordem. byId tem exatamente as mesmas chaves
            //    (passo 4), então nenhum get() retorna null.
            List<Music> reordered = new ArrayList<>(requestedOrder.size());
            for (UUID id : requestedOrder) {
                reordered.add(byId.get(id));
            }
            playlist.setSongs(reordered);
            playlist.setUpdatedAt(Instant.now()); // marca dirty + garante o bump
            Playlist saved = playlistRepository.save(playlist);

            return playlistMapper.toFullDTO(saved);
        } catch (DataAccessException ex) {
            // Sem SQL, sem musicIds, sem stack no corpo da resposta. Só correlação.
            String requestId = UUID.randomUUID().toString().substring(0, 8);
            log.error("Falha de banco no reorder. playlistId={} requestId={}", playlistId, requestId, ex);
            throw new ReorderFailedException(ex);
        }
    }
}
