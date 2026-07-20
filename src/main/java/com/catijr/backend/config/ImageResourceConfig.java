package com.catijr.backend.config;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.CacheControl;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.time.Duration;

/**
 * Serve os bytes das capas/fotos como arquivos estáticos sob {@code /images/**}.
 *
 * <p>O contrato com o frontend é: cada entidade traz em seu JSON um {@code imageUrl}
 * que é um caminho RELATIVO (ex.: {@code /images/albums/<id>.jpg}). O browser então
 * faz um segundo GET direto nesse caminho para baixar os bytes — as imagens nunca
 * viajam embutidas no JSON. Guardamos os arquivos em disco, sob {@code app.images.dir},
 * espelhando o caminho da URL: {@code /images/albums/x.jpg} => {@code <dir>/albums/x.jpg}.
 *
 * <p>O {@code Content-Type} é inferido pela extensão do arquivo (image/jpeg, image/png,
 * image/webp, ...) pelo próprio handler de recursos do Spring, que também bloqueia
 * path traversal ({@code ..}) resolvendo o recurso e checando se está sob o diretório.
 *
 * <p>Cache: 1h público. Como os caminhos ainda não são content-hashed, mantemos um
 * max-age curto para permitir a troca de uma capa sem que o cache antigo persista muito.
 */
@Slf4j
@Configuration
public class ImageResourceConfig implements WebMvcConfigurer {

    private final Path imagesDir;

    public ImageResourceConfig(@Value("${app.images.dir}") String imagesDir) {
        this.imagesDir = Path.of(imagesDir).toAbsolutePath().normalize();
    }

    /** Garante que o diretório existe para o handler não falhar ao resolver locais. */
    @PostConstruct
    void ensureDirectory() {
        try {
            Files.createDirectories(imagesDir);
            log.info("Diretório de imagens pronto: {}", imagesDir);
        } catch (IOException e) {
            log.warn("Não foi possível criar o diretório de imagens {} ({}). "
                    + "Requisições a /images/** retornarão 404 até que exista.", imagesDir, e.getMessage());
        }
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        registry.addResourceHandler("/images/**")
                .addResourceLocations("file:" + imagesDir + "/")
                .setCacheControl(CacheControl.maxAge(Duration.ofHours(1)).cachePublic());
    }
}
