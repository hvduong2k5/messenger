package com.team12345.messenger.entity;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.Validator;
import jakarta.validation.ValidatorFactory;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

public class UserValidationTest {

    private static Validator validator;

    @BeforeAll
    public static void setUpValidator() {
        try (ValidatorFactory factory = Validation.buildDefaultValidatorFactory()) {
            validator = factory.getValidator();
        }
    }

    @Test
    public void testValidUser() {
        User user = User.builder()
                .username("testuser")
                .email("test@example.com")
                .password("password123")
                .status("ACTIVE")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isEmpty();
    }

    @Test
    public void testBlankUsername() {
        User user = User.builder()
                .username("")
                .email("test@example.com")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
        boolean hasUsernameViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("username"));
        assertThat(hasUsernameViolation).isTrue();
    }

    @Test
    public void testInvalidEmail() {
        User user = User.builder()
                .username("testuser")
                .email("not-an-email")
                .password("password123")
                .build();

        Set<ConstraintViolation<User>> violations = validator.validate(user);
        assertThat(violations).isNotEmpty();
        boolean hasEmailViolation = violations.stream()
                .anyMatch(v -> v.getPropertyPath().toString().equals("email"));
        assertThat(hasEmailViolation).isTrue();
    }
}