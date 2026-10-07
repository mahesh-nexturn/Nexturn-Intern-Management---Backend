package com.nexturn.internmanagement.document;

import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DocumentRepository extends JpaRepository<Document, Long> {
    List<Document> findByUploadedById(Long userId);

    List<Document> findAllByOrderByUploadDateDesc(Pageable pageable);
}
