package com.dmdev.validator;

import com.dmdev.dto.CreateSubscriptionDto;

import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;

class CreateSubscriptionValidatorTest {

    private final CreateSubscriptionValidator validator = CreateSubscriptionValidator.getInstance();

    @Test
    void shouldPassValidation() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.MAX)
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertFalse(actualResult.hasErrors());
    }

    @Test
    void invalidUserId() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.MAX)
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(100);
    }

    @Test
    void invalidUerName() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("")
                .provider("GOOGLE")
                .expirationDate(Instant.MAX)
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(101);
    }

    @Test
    void invalidProvider() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("")
                .expirationDate(Instant.MAX)
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(102);
    }

    @Test
    void invalidExpirationDate() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.MIN)
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(1);
        assertThat(actualResult.getErrors().get(0).getCode()).isEqualTo(103);
    }

    @Test
    void invalidUserIdNameProviderExpirationDate() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(null)
                .name("")
                .provider("")
                .expirationDate(Instant.MIN)
                .build();

        ValidationResult actualResult = validator.validate(dto);

        assertThat(actualResult.getErrors()).hasSize(4);
        List<Integer> errorCodes = actualResult.getErrors().stream()
                .map(Error::getCode)
                .toList();
        assertThat(errorCodes).contains(100, 101, 102, 103);
    }

}