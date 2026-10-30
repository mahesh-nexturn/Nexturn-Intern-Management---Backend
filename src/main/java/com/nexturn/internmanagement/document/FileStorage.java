package com.nexturn.internmanagement.document;

import java.io.IOException;
import org.springframework.core.io.Resource;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorage {

    void store(String key, MultipartFile file) throws IOException;

    /** @throws com.nexturn.internmanagement.common.ResourceNotFoundException if missing */
    Resource load(String key);

    void delete(String key);
}
