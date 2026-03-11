package de.vts.monitor.forum.service;

import io.minio.MinioClient;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

/**
 * Factory for creating MinioClient connected to local MinIO instance.
 */
@Service
@Profile("local") // Activate only under 'local' Spring profile
public class LocalMinioClientFactory implements MinioClientFactory {

    private final String endpoint;
    private final String accessKey;
    private final String secretKey;

    public LocalMinioClientFactory(
            @Value("${local.minio.endpoint:http://localhost:9000}") String endpoint,
            @Value("${local.minio.accessKey:minioadmin}") String accessKey,
            @Value("${local.minio.secretKey:minioadmin}") String secretKey
    ) {
        this.endpoint = endpoint;
        this.accessKey = accessKey;
        this.secretKey = secretKey;
    }

    @Override
    public MinioClient createClient() {
        return MinioClient.builder()
                .endpoint(endpoint)
                .credentials(accessKey, secretKey)
                .build();
    }
}
