package com.team12345.messenger.service.impl;

import com.cloudinary.Cloudinary;
import com.cloudinary.utils.ObjectUtils;
import com.team12345.messenger.service.MediaService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Service
@RequiredArgsConstructor
@Slf4j
public class CloudinaryMediaServiceImpl implements MediaService {

    private final Cloudinary cloudinary;

    @Override
    public Map<String, Object> uploadFile(MultipartFile file) {
        try {
            // Setup options for upload
            Map<String, Object> options = ObjectUtils.asMap(
                    "resource_type", "auto" // Automatically detect if it's an image, video, or raw file
            );

            // Upload the file
            Map<?, ?> uploadResult = cloudinary.uploader().upload(file.getBytes(), options);

            // Extract the result we care about
            Map<String, Object> result = new HashMap<>();
            
            // Cloudinary returns "secure_url" for https
            String url = (String) uploadResult.get("secure_url");
            result.put("url", url);
            
            // The size in bytes
            Integer bytes = (Integer) uploadResult.get("bytes");
            result.put("size", bytes);

            // Resource type from Cloudinary (image, video, raw)
            String resourceType = (String) uploadResult.get("resource_type");
            
            // Refine the resource type logic based on our Attachment entity expectations
            String mappedType = mapResourceType(resourceType, file.getContentType());
            result.put("type", mappedType);

            return result;

        } catch (IOException e) {
            log.error("Failed to upload file to Cloudinary: {}", e.getMessage());
            throw new RuntimeException("Failed to upload file", e);
        }
    }

    private String mapResourceType(String cloudinaryType, String originalContentType) {
        if ("image".equals(cloudinaryType)) {
            return "IMAGE";
        } else if ("video".equals(cloudinaryType)) {
            // Audio files might be uploaded as 'video' in Cloudinary depending on config
            if (originalContentType != null && originalContentType.startsWith("audio/")) {
                return "AUDIO";
            }
            return "VIDEO";
        }
        return "FILE";
    }
}