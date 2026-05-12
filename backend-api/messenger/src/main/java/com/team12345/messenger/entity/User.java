package com.team12345.messenger.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "users", indexes = {
    @Index(name = "idx_username", columnList = "username"),
    @Index(name = "idx_email", columnList = "email")
})
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class User extends BaseEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @NotBlank
    @Size(max = 100)
    @Column(unique = true, nullable = false, length = 100)
    private String username;

    @NotBlank
    @Email
    @Size(max = 150)
    @Column(unique = true, nullable = false, length = 150)
    private String email;

    @Size(max = 512)
    @Column(name = "avatar_url", length = 512)
    private String avatarUrl;

    @Size(max = 50)
    @Column(length = 50)
    private String status;

    @NotBlank
    @Size(min = 60, max = 255)
    @Column(nullable = false, length = 255)
    private String password;

    @Builder.Default
    @Column(name = "is_online", nullable = false)
    private Boolean isOnline = false;

    @Column(name = "last_seen")
    private LocalDateTime lastSeen;

}
