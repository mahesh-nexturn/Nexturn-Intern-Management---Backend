package com.nexturn.internmanagement.document;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import java.io.IOException;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;
import software.amazon.awssdk.core.sync.RequestBody;
import software.amazon.awssdk.services.s3.S3Client;
import software.amazon.awssdk.services.s3.model.DeleteObjectRequest;
import software.amazon.awssdk.services.s3.model.GetObjectRequest;
import software.amazon.awssdk.services.s3.model.NoSuchKeyException;
import software.amazon.awssdk.services.s3.model.PutObjectRequest;
import software.amazon.awssdk.services.s3.model.S3Exception;

/** Stores uploads in the private S3 bucket; all access goes through the authenticated API. */
@Component
@ConditionalOnExpression("!'${app.s3.bucket:}'.isEmpty()")
@RequiredArgsConstructor
public class S3FileStorage implements FileStorage {

    private final S3Client s3;

    @Value("${app.s3.bucket}")
    private String bucket;

    @Override
    public void store(String key, MultipartFile file) throws IOException {
        PutObjectRequest request = PutObjectRequest.builder()
                .bucket(bucket)
                .key(key)
                .contentType(file.getContentType())
                .build();
        s3.putObject(request, RequestBody.fromInputStream(file.getInputStream(), file.getSize()));
    }

    @Override
    public Resource load(String key) {
        try {
            byte[] bytes = s3.getObjectAsBytes(GetObjectRequest.builder().bucket(bucket).key(key).build())
                    .asByteArray();
            return new ByteArrayResource(bytes);
        } catch (NoSuchKeyException e) {
            throw new ResourceNotFoundException("File not found: " + key);
        } catch (S3Exception e) {
            throw new IllegalStateException("Could not read file from storage", e);
        }
    }

    @Override
    public void delete(String key) {
        try {
            s3.deleteObject(DeleteObjectRequest.builder().bucket(bucket).key(key).build());
        } catch (S3Exception ignored) {
            // best-effort cleanup; DB record removal still proceeds
        }
    }
}
