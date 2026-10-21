package com.nexturn.internmanagement.document;
import com.nexturn.internmanagement.document.dto.DocumentResponseDto;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;

    @Value("${app.file-storage.location:uploads}")
    private String storageLocation;

    public List<DocumentResponseDto> findAll() {
        return documentRepository.findAll().stream().map(DocumentResponseDto::from).toList();
    }

    public List<DocumentResponseDto> findByUser(Long userId) {
        return documentRepository.findByUploadedById(userId).stream().map(DocumentResponseDto::from).toList();
    }

    public DocumentResponseDto findById(Long id) {
        return DocumentResponseDto.from(getEntity(id));
    }

    public Document getEntity(Long id) {
        return documentRepository.findById(id)
                .orElseThrow(() -> new ResourceNotFoundException("Document not found: " + id));
    }

    public DocumentResponseDto upload(MultipartFile file, String documentType, Long uploadedByUserId) {
        try {
            Path dir = Paths.get(storageLocation).toAbsolutePath().normalize();
            Files.createDirectories(dir);
            String originalName = file.getOriginalFilename() == null ? "file" : file.getOriginalFilename();
            String storedName = UUID.randomUUID() + "_" + originalName;
            Path target = dir.resolve(storedName).normalize();
            if (!target.startsWith(dir)) {
                throw new IllegalArgumentException("Invalid file name");
            }
            Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);

            User uploader = userRepository.findById(uploadedByUserId)
                    .orElseThrow(() -> new ResourceNotFoundException("User not found: " + uploadedByUserId));

            Document document = Document.builder()
                    .fileName(originalName)
                    .fileUrl(storedName)
                    .documentType(type)
                    .uploadedBy(uploader)
                    .uploadDate(LocalDateTime.now())
                    .build();
            return DocumentResponseDto.from(documentRepository.save(document));
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + e.getMessage(), e);
        }
    }

    private static String sanitizeFileName(String name) {
        String base = name == null ? "file" : name.replace('\\', '/');
        base = base.substring(base.lastIndexOf('/') + 1).replaceAll("[^A-Za-z0-9._ -]", "_").trim();
        if (base.isEmpty() || base.equals(".") || base.equals("..")) {
            return "file";
        }
        return base.length() > 150 ? base.substring(base.length() - 150) : base;
    }

    private static DocumentType parseType(String value) {
        try {
            return DocumentType.valueOf(value.trim().replace(' ', '_'));
        } catch (IllegalArgumentException | NullPointerException e) {
            throw new IllegalArgumentException("Invalid document type: " + value);
        }
    }

    public List<DocumentResponseDto> latestForUser(Long userId, int limit) {
        return documentRepository
                .findByUploadedByIdOrderByUploadDateDesc(userId,
                        org.springframework.data.domain.PageRequest.of(0, Math.min(Math.max(limit, 1), 50)))
                .stream().map(DocumentResponseDto::from).toList();
    }

    public Resource loadAsResource(Long id) {
        Document document = getEntity(id);
        try {
            Path filePath = Paths.get(storageLocation).toAbsolutePath().normalize().resolve(document.getFileUrl());
            Resource resource = new UrlResource(filePath.toUri());
            if (!resource.exists() || !resource.isReadable()) {
                throw new ResourceNotFoundException("File not found on disk: " + document.getFileName());
            }
            return resource;
        } catch (Exception e) {
            throw new ResourceNotFoundException("Could not read file: " + document.getFileName());
        }
    }

    public void delete(Long id) {
        Document document = getEntity(id);
        try {
            Path filePath = Paths.get(storageLocation).toAbsolutePath().normalize().resolve(document.getFileUrl());
            Files.deleteIfExists(filePath);
        } catch (IOException ignored) {
            // best-effort disk cleanup; DB record removal still proceeds
        }
        documentRepository.delete(document);
    }

    public List<DocumentResponseDto> latest(int limit) {
        return documentRepository
                .findAllByOrderByUploadDateDesc(org.springframework.data.domain.PageRequest.of(0, limit))
                .stream().map(DocumentResponseDto::from).toList();
    }
}
