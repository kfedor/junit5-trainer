package com.dmdev.mapper;

import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import org.junit.jupiter.api.Test;

import java.time.Instant;

import static org.assertj.core.api.Assertions.assertThat;


class CreateSubscriptionMapperTest {

    private final CreateSubscriptionMapper mapper = CreateSubscriptionMapper.getInstance();

    @Test
    void map() {
        CreateSubscriptionDto dto = CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.MAX)
                .build();

        Subscription actualResult = mapper.map(dto);

        Subscription expectedResult = Subscription.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(Instant.MAX)
                .status(Status.ACTIVE)
                .build();

        assertThat(actualResult).isEqualTo(expectedResult);
    }

}