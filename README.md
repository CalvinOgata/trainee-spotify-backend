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

### Biblioteca (músicas salvas, álbuns salvos e artistas seguidos)
Coleções persistentes do usuário (implícito — o app tem um único usuário, sem coluna de usuário). Cada coleção tem um `GET` (itens ordenados por adição, **mais recente primeiro**) e um par `POST`/`DELETE` por id. As mutações são **idempotentes**: retornam `204 No Content` tanto se a linha já existia quanto se já estava ausente. O `404` só ocorre quando a própria música/álbum/artista referenciada **não existe** no catálogo. *(Playlists já são cobertas por `/user/playlists` — não entram aqui.)*

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| GET | /user/savedMusics | Retorna as músicas salvas (`Music[]`), da mais recente para a mais antiga. |
| POST | /user/savedMusics/{musicId} | Salva a música na biblioteca. `204`; `404` se a música não existir. |
| DELETE | /user/savedMusics/{musicId} | Remove a música da biblioteca. `204`; `404` se a música não existir. |
| GET | /user/savedAlbums | Retorna os álbuns salvos (`AlbumSummary[]`), do mais recente para o mais antigo. |
| POST | /user/savedAlbums/{albumId} | Salva o álbum na biblioteca. `204`; `404` se o álbum não existir. |
| DELETE | /user/savedAlbums/{albumId} | Remove o álbum da biblioteca. `204`; `404` se o álbum não existir. |
| GET | /user/followedArtists | Retorna os artistas seguidos (`Artist[]`), do seguido mais recentemente ao mais antigo. |
| POST | /user/followedArtists/{artistId} | Segue o artista. `204`; `404` se o artista não existir. |
| DELETE | /user/followedArtists/{artistId} | Deixa de seguir o artista. `204`; `404` se o artista não existir. |

---

## Playlist (/playlist)
Rotas destinadas à criação, atualização, exclusão e visualização de playlists e suas músicas.

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| GET | /playlist/{playlistId} | Retorna todos os detalhes de uma playlist específica a partir do seu Id. |
| POST | /playlist | Cria uma nova playlist. Espera um JSON estruturado no corpo da requisição. |
| PUT | /playlist/{playlistId}/attributes | Atualiza os atributos de texto/metadados da playlist (exceto a lista de músicas). |
| PATCH | /playlist/{playlistId}/{musicId} | Insere uma música específica (`musicId`) na playlist (`playlistId`). |
| PUT | /playlist/{playlistId}/order | Reordena as músicas da playlist (usado pelo *drag-and-drop* do frontend). Espera no corpo `{ "musicIds": [...] }` com a ordem completa e final das músicas — o mesmo conjunto já presente na playlist (apenas reordenação, sem inserir/remover). A operação é idempotente. |
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

## Imagens (/images)
Capas de álbuns/playlists e fotos de artistas são servidas como **arquivos estáticos** — os bytes **nunca** vêm embutidos no JSON. Cada entidade (Artista, Álbum, Playlist e Música) inclui no seu JSON o campo `imageUrl`: uma **string com caminho relativo** (ex.: `/images/albums/<id>.jpg`) ou `null` quando não há capa. Nunca uma URL absoluta — o frontend prefixa sua própria base (`VITE_API_BASE`) e o browser faz um segundo GET direto no caminho para baixar a imagem.

*Música não guarda capa própria: herda a capa do seu álbum (ou `null` se o álbum não tiver capa / a faixa não tiver álbum).* Os bytes ficam em disco sob `app.images.dir` (padrão `./storage/images`), espelhando o caminho da URL.

**Como o `imageUrl` é preenchido (álbuns e artistas):** um valor explícito na coluna `image_url` sempre vence (permite override manual). Se estiver vazio, o backend deriva o caminho por convenção a partir do id — procura em disco `<app.images.dir>/albums/<id>.jpg` (e depois `.png`) para álbuns, e `.../artists/<id>.jpg`/`.png` para artistas. Se o arquivo existir, `imageUrl` vira o caminho relativo correspondente (`/images/albums/<id>.jpg`); senão, `null`. Como o caminho só é devolvido quando o arquivo **realmente existe**, o `imageUrl` nunca aponta pra uma capa inexistente. Na prática, para dar capa a um álbum/artista basta soltar `<id>.jpg` (ou `<id>.png`) em `storage/images/albums/` ou `storage/images/artists/` — **sem tocar no banco**. (Música herda a capa derivada do seu álbum.)

| Método | Endpoint | Descrição |
| :--- | :--- | :--- |
| GET | /images/{caminho} | Retorna os bytes da imagem apontada por um `imageUrl` (ex.: `/images/albums/<id>.jpg`). `Content-Type` é inferido pela extensão (image/jpeg, image/png, image/webp); `Cache-Control: max-age=3600, public`. Retorna 404 se o arquivo não existir. |

---

## Música
*Esta entidade não possui rotas próprias de gerenciamento direto na API. O acesso às músicas é feito através das rotas de Playlists, Álbuns ou Usuário.*
