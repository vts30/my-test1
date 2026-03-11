package de.vts.monitor.forum;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import org.junit.jupiter.api.Test;

import java.io.ByteArrayInputStream;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

/**
 * Utility test to populate local MinIO with test backup data.
 * Requires local MinIO running: docker-compose up -d
 *
 * Run manually: mvn test -Dtest=MinioTestDataCreator -Dspring.profiles.active=local
 */
class MinioTestDataCreator {

    private static final String ENDPOINT   = "http://localhost:9000";
    private static final String ACCESS_KEY = "minioadmin";
    private static final String SECRET_KEY = "minioadmin";
    private static final String BUCKET     = "backups";
    private static final String PREFIX     = "forum";

    private static final DateTimeFormatter TS_FORMAT = DateTimeFormatter.ofPattern("yyyyMMdd_HHmmss");

    @Test
    void createTestData() throws Exception {
        MinioClient client = MinioClient.builder()
                .endpoint(ENDPOINT)
                .credentials(ACCESS_KEY, SECRET_KEY)
                .build();

        ensureBucketExists(client);

        LocalDateTime now = LocalDateTime.now();

        // UP-TO-DATE mandants (backup from today)
        upload(client, "MANDANT_A",  "forumsuite",        now);
        upload(client, "MANDANT_B",  "forumsuite",        now.minusHours(2));

        // SLIGHTLY OUTDATED (1 day old — on the boundary)
        upload(client, "MANDANT_C",  "forumsuite",        now.minusDays(1));

        // OUTDATED mandants (2–5 days old)
        upload(client, "MANDANT_D",  "forumsuite",        now.minusDays(2));
        upload(client, "MANDANT_E",  "forumsuite",        now.minusDays(3));
        upload(client, "MANDANT_F",  "forumsuite",        now.minusDays(5));

        // VERY OUTDATED (10+ days old)
        upload(client, "MANDANT_G",  "forumsuite",        now.minusDays(10));
        upload(client, "MANDANT_H",  "forumsuite",        now.minusDays(14));

        // ZAM mandants with multiple instances
        upload(client, "ZAM_001",    "forumsuite-prod1",  now.minusDays(1));
        upload(client, "ZAM_001",    "forumsuite-prod2",  now.minusDays(3));
        upload(client, "ZAM_002",    "forumsuite-prod1",  now);
        upload(client, "ZAM_002",    "forumsuite-prod2",  now.minusDays(2));

        // Mandant with multiple backups (only latest should be picked)
        upload(client, "MANDANT_MULTI", "forumsuite",     now.minusDays(7));
        upload(client, "MANDANT_MULTI", "forumsuite",     now.minusDays(3));
        upload(client, "MANDANT_MULTI", "forumsuite",     now.minusDays(1));

        // MISSING mandants — no valid zip, only a placeholder file
        // These mandants appear in the bucket listing but getLatestZipInfo returns null
        uploadPlaceholder(client, "MANDANT_MISSING_1", "forumsuite");
        uploadPlaceholder(client, "MANDANT_MISSING_2", "forumsuite");

        System.out.println("Test data created successfully in bucket '" + BUCKET + "'");
    }

    private void ensureBucketExists(MinioClient client) throws Exception {
        boolean exists = client.bucketExists(BucketExistsArgs.builder().bucket(BUCKET).build());
        if (!exists) {
            client.makeBucket(MakeBucketArgs.builder().bucket(BUCKET).build());
            System.out.println("Created bucket: " + BUCKET);
        } else {
            System.out.println("Bucket already exists: " + BUCKET);
        }
    }

    private void uploadPlaceholder(MinioClient client, String mandant, String instance) throws Exception {
        String objectPath = String.format("%s/mandant=%s/instance=%s/README.txt", PREFIX, mandant, instance);
        byte[] content = "no backup available".getBytes();
        client.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(objectPath)
                        .stream(new ByteArrayInputStream(content), content.length, -1)
                        .contentType("text/plain")
                        .build()
        );
        System.out.printf("Uploaded placeholder (no zip): %s%n", objectPath);
    }

    private void upload(MinioClient client, String mandant, String instance, LocalDateTime timestamp) throws Exception {
        String filename = "backup_" + timestamp.format(TS_FORMAT) + ".zip";
        String objectPath = String.format("%s/mandant=%s/instance=%s/%s", PREFIX, mandant, instance, filename);

        byte[] dummyContent = ("dummy-backup-content-for-" + mandant).getBytes();

        client.putObject(
                PutObjectArgs.builder()
                        .bucket(BUCKET)
                        .object(objectPath)
                        .stream(new ByteArrayInputStream(dummyContent), dummyContent.length, -1)
                        .contentType("application/zip")
                        .build()
        );

        System.out.printf("Uploaded: %s%n", objectPath);
    }
}
