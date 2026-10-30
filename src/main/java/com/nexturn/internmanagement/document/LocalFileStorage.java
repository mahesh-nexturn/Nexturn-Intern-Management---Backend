package com.nexturn.internmanagement.document;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.condition.ConditionalOnExpression;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

/** Used for local development when no S3 bucket is configured. */
@Component
@ConditionalOnExpression("'${app.s3.bucket:}'.isEmpty()")
public class LocalFileStorage implements FileStorage {

    @Value("${app.file-storage.location:uploads}")
    private String storageLocation;

    private Path resolve(String key) {
        Path dir = Paths.get(storageLocation).toAbsolutePath().normalize();
        Path target = dir.resolve(key).normalize();
        if (!target.startsWith(dir)) {
            throw new IllegalArgumentException("Invalid file name");
        }
        return target;
    }

    @Override
    public void store(String key, MultipartFile file) throws IOException {
        Path target = resolve(key);
        Files.createDirectories(target.getParent());
        Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
    }

    @Override
    public Resource load(String key) {
        try {
            Resource resource = new UrlResource(resolve(key).toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found: " + key);
            }
            return resource;
        } catch (java.net.MalformedURLException e) {
            throw new ResourceNotFoundException("Could not read file: " + key);
        }
    }

    @Override
    public void delete(String key) {
        try {
            Files.deleteIfExists(resolve(key));
        } catch (IOException ignored) {
            // best-effort cleanup; DB record removal still proceeds
        }
    }
}
