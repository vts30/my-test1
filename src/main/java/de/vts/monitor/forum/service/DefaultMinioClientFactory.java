package de.vts.monitor.forum.service;

import de.vts.monitor.forum.config.BspConfiguration;
import de.vts.monitor.forum.security.JwtTokenFetcher;
import io.minio.MinioClient;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.io.IOException;

@Service
@Profile("!local")
public class DefaultMinioClientFactory implements MinioClientFactory {

    private final BspConfiguration bspConfiguration;
    private final JwtTokenFetcher tokenFetcher;

    public DefaultMinioClientFactory(BspConfiguration bspConfiguration, JwtTokenFetcher tokenFetcher) {
        this.bspConfiguration = bspConfiguration;
        this.tokenFetcher = tokenFetcher;
    }

    @Override
    public MinioClient createClient() {
        String jwt;
        try {
            jwt = tokenFetcher.fetch(
                    bspConfiguration.getDih().getS3().getLoginTokenUrl(),
                    bspConfiguration.getDih().getLogin().getUser(),
                    bspConfiguration.getDih().getLogin().getPasswd()
            );
        } catch (IOException e) {
            throw new RuntimeException("Failed to fetch JWT token", e);
        }

        return MinioClient.builder()
                .endpoint(bspConfiguration.getDih().getS3().getEndpoint())
                .credentials(jwt, "backup file read")
                .build();
    }
}
