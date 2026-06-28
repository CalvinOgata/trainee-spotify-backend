# Documentação da API (Rotas)

## User (/user)
Rotas responsáveis por buscar o histórico e os dados associados ao perfil do usuário. 
*(Nota: Como o projeto foca em um único usuário estático, as rotas que dependem de métricas retornam dados fixos inicializados no banco).*

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| GET | /user/playlists | Retorna todas as playlists do banco de dados (equivalente às playlists do usuário). |
| GET | /user/recentArtists | Retorna os artistas ouvidos recentemente. |
| GET | /user/mostPlayedArtists | Retorna os artistas mais ouvidos. |
| GET | /user/recentMusics | Retorna as músicas ouvidas recentemente. |
| GET | /user/mostPlayedMusics | Retorna as músicas mais ouvidas. |
| GET | /user/recentAlbums | Retorna os álbuns ouvidos recentemente. |
| GET | /user/followers | Retorna a lista de todos os seguidores do usuário. |

---

## Playlist (/playlist)
Rotas destinadas à criação, atualização, exclusão e visualização de playlists e suas músicas.

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| GET | /playlist/{playlistId} | Retorna todos os detalhes de uma playlist específica a partir do seu Id. |
| POST | /playlist | Cria uma nova playlist. Espera um JSON estruturado no corpo da requisição. |
| PUT | /playlist/{playlistId}/attributes | Atualiza os atributos de texto/metadados da playlist (exceto a lista de músicas). |
| PATCH | /playlist/{playlistId}/{musicId} | Insere uma música específica (`musicId`) na playlist (`playlistId`). |
| DELETE | /playlist/{playlistId}/{musicId} | Remove uma música específica (`musicId`) da playlist (`playlistId`). |
| DELETE | /playlist/{playlistId} | Exclui a playlist inteira do banco de dados. |

---

## Artista (/artist)
Rotas para obter detalhes sobre os artistas e seu engajamento.

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| GET | /artist/{artistId}/popularMusics | Retorna a lista (fixa) de músicas mais populares do artista. |
| GET | /artist/{artistId}/albums | Retorna todos os álbuns associados ao artista específico. |

---

## Álbum (/album)
Rotas focadas na consulta de informações sobre os álbuns musicais.

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| GET | /album/{albumId}/musics | Retorna a lista de músicas de um álbum específico a partir do seu Id. |

---

## Busca (/search)
Rota de busca incremental usada pela página de resultados do frontend (chamada a cada tecla, com *debounce* no cliente). A busca é *case* e acento-insensível e retorna quatro listas: `musics`, `playlists`, `artists` e `albums` (sempre arrays — nunca `null`).

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| GET | /search?q={query}&limit={limit} | Busca músicas (por título), playlists (por nome ou descrição), artistas (por nome) e álbuns (por título) que contenham o termo. `q` é obrigatório; `limit` é opcional (padrão 20, máximo 50 por categoria). |

---

## Música
*Esta entidade não possui rotas próprias de gerenciamento direto na API. O acesso às músicas é feito através das rotas de Playlists, Álbuns ou Usuário.*
