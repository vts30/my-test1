package de.vts.monitor.forum.service;

import io.minio.MinioClient;

public interface MinioClientFactory {
    MinioClient createClient();
}
