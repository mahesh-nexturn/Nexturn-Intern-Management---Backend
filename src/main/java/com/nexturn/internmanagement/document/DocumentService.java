package com.nexturn.internmanagement.document;
import com.nexturn.internmanagement.document.dto.DocumentResponseDto;

import com.nexturn.internmanagement.common.ResourceNotFoundException;
import com.nexturn.internmanagement.user.User;
import com.nexturn.internmanagement.user.UserRepository;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import org.springframework.core.io.Resource;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

@Service
@RequiredArgsConstructor
public class DocumentService {

    private final DocumentRepository documentRepository;
    private final UserRepository userRepository;
    private final FileStorage fileStorage;

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

    private static final java.util.Set<String> ALLOWED_EXTENSIONS = java.util.Set.of(
            "pdf", "doc", "docx", "xls", "xlsx", "ppt", "pptx", "txt", "csv", "png", "jpg", "jpeg");

    public DocumentResponseDto upload(MultipartFile file, String documentType, Long uploadedByUserId) {
        DocumentType type = parseType(documentType);
        User uploader = userRepository.findById(uploadedByUserId)
                .orElseThrow(() -> new ResourceNotFoundException("User not found: " + uploadedByUserId));
        String originalName = sanitizeFileName(file.getOriginalFilename());
        String ext = originalName.contains(".")
                ? originalName.substring(originalName.lastIndexOf('.') + 1).toLowerCase() : "";
        if (!ALLOWED_EXTENSIONS.contains(ext)) {
            throw new IllegalArgumentException("File type not allowed: ." + ext);
        }
        String storedName = UUID.randomUUID() + "_" + originalName;
        try {
            fileStorage.store(storedName, file);
        } catch (IOException e) {
            throw new IllegalStateException("Failed to store file: " + e.getMessage(), e);
        }

        Document document = Document.builder()
                .fileName(originalName)
                .fileUrl(storedName)
                .documentType(type)
                .uploadedBy(uploader)
                .uploadDate(LocalDateTime.now())
                .build();
        return DocumentResponseDto.from(documentRepository.save(document));
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
        return fileStorage.load(getEntity(id).getFileUrl());
    }

    public void delete(Long id) {
        Document document = getEntity(id);
        fileStorage.delete(document.getFileUrl());
        documentRepository.delete(document);
    }
    public List<DocumentResponseDto> latest(int limit) {
        return documentRepository
                .findAllByOrderByUploadDateDesc(org.springframework.data.domain.PageRequest.of(0, limit))
                .stream().map(DocumentResponseDto::from).toList();
    }
}
