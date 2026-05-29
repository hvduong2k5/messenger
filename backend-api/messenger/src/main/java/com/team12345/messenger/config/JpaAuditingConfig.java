package com.team12345.messenger.config;

import jakarta.persistence.EntityManagerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@Configuration
@ConditionalOnBean(EntityManagerFactory.class)
@EnableJpaAuditing
public class JpaAuditingConfig {
	// Enables JPA auditing only when an EntityManagerFactory is present in the context
}