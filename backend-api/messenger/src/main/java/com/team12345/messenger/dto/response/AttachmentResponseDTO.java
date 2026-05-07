package com.team12345.messenger.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class AttachmentResponseDTO {
    private String url;
    private String type;
    private Integer fileSize;
}