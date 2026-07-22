package com.catijr.backend.config;

import com.catijr.backend.Entities.Album;
import com.catijr.backend.Entities.Artist;
import org.mapstruct.Named;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.UUID;

/**
 * Deriva o {@code imageUrl} (o "link" relativo servido em {@code /images/**}) que
 * cada álbum/artista carrega no seu JSON. É a outra metade do contrato de imagens:
 * {@link ImageResourceConfig} serve os BYTES sob {@code app.images.dir}; este resolver
 * decide o CAMINHO que aponta pra eles.
 *
 * <p>Regra: um {@code image_url} explícito no banco sempre vence (permite override
 * manual). Se estiver vazio, deriva pelo id por convenção — se existir em disco
 * {@code <dir>/<folder>/<id>.jpg} (ou {@code .png}), devolve {@code /images/<folder>/<id>.<ext>};
 * senão, {@code null}. Só devolvemos um caminho quando o arquivo realmente existe, então
 * o {@code imageUrl} nunca aponta pra uma imagem inexistente (o frontend nunca leva 404).
 *
 * <p>Fluxo do usuário: basta soltar {@code <id>.jpg} (ou {@code .png}) em
 * {@code storage/images/albums/} ou {@code storage/images/artists/} — sem tocar no banco.
 */
@Component
public class ImageUrlResolver {

    // Extensões tentadas, em ordem de preferência, ao derivar a capa pelo id.
    private static final List<String> EXTENSIONS = List.of("jpg", "png");

    private final Path imagesDir;

    public ImageUrlResolver(@Value("${app.images.dir}") String imagesDir) {
        this.imagesDir = Path.of(imagesDir).toAbsolutePath().normalize();
    }

    @Named("albumImageUrl")
    public String albumImageUrl(Album album) {
        if (album == null) {
            return null;
        }
        return resolve("albums", album.getId(), album.getImageUrl());
    }

    @Named("artistImageUrl")
    public String artistImageUrl(Artist artist) {
        if (artist == null) {
            return null;
        }
        return resolve("artists", artist.getId(), artist.getImageUrl());
    }

    private String resolve(String folder, UUID id, String storedUrl) {
        if (storedUrl != null && !storedUrl.isBlank()) {
            return storedUrl; // override explícito no banco vence sempre
        }
        if (id == null) {
            return null;
        }
        for (String ext : EXTENSIONS) {
            String fileName = id + "." + ext;
            if (Files.isRegularFile(imagesDir.resolve(folder).resolve(fileName))) {
                return "/images/" + folder + "/" + fileName;
            }
        }
        return null;
    }
}
