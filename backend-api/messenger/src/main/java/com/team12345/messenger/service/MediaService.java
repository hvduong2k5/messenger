package com.team12345.messenger.service;

import org.springframework.web.multipart.MultipartFile;
import java.util.Map;

public interface MediaService {
    /**
     * Uploads a file to a cloud provider (e.g. Cloudinary)
     * @return A map containing url, type (image, video, etc), and potentially size.
     */
    Map<String, Object> uploadFile(MultipartFile file);
}