package com.team12345.messenger.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.PrePersist;
import jakarta.persistence.Table;
import lombok.Data;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Entity
@Table(name = "attachments")
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Attachment {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(name="file_url", nullable=false, columnDefinition="TEXT")
    private String fileUrl;

    @Column(name="file_type", length=50)
    private String fileType; //image, video, file...

    @Column(name="file_size")
    private Integer fileSize;

    @Column(name="public_id", length=255)
    private String publicId;

    @Column(name="uploaded_at")
    private LocalDateTime uploadedAt;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="message_id")
    private Message message;

    @ManyToOne(fetch=FetchType.LAZY)
    @JoinColumn(name="uploader_id")
    private User uploader;

    @PrePersist
    protected void onCreate(){
        this.uploadedAt = LocalDateTime.now();
    }
}
