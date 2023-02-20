package com.dmdev.service;


import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;

import static org.assertj.core.api.Assertions.assertThat;

class SubscriptionServiceIT extends IntegrationTestBase {

    private SubscriptionService subscriptionService;
    private SubscriptionDao subscriptionDao;
    private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());

    @BeforeEach
    void init() {
        subscriptionDao = SubscriptionDao.getInstance();
        subscriptionService = new SubscriptionService(
                subscriptionDao,
                CreateSubscriptionMapper.getInstance(),
                CreateSubscriptionValidator.getInstance(),
                clock
        );
    }

    @Test
    void upsert() {
        CreateSubscriptionDto dto = getSubscriptionDto();
        Subscription actualResult = subscriptionService.upsert(dto);

        assertThat(actualResult.getStatus()).isEqualTo(Status.ACTIVE);
    }

    @Test
    void cancel() {
        Subscription subscription = getSubscription(1);
        subscriptionDao.insert(subscription);
        subscriptionService.cancel(subscription.getId());
        subscription.setStatus(Status.CANCELED);

        Subscription updatedSubscription = subscriptionDao.update(subscription);

        assertThat(updatedSubscription.getStatus()).isEqualTo(Status.CANCELED);

    }

    @Test
    void expire() {

        Subscription subscription = getSubscription(1);
        subscriptionDao.insert(subscription);
        subscriptionService.expire(subscription.getId());
        subscription.setStatus(Status.EXPIRED);

        Subscription updatedSubscription = subscriptionDao.update(subscription);

        assertThat(updatedSubscription.getStatus()).isEqualTo(Status.EXPIRED);
    }

    private Subscription getSubscription(Integer userId) {
        return Subscription.builder()
                .userId(userId)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(Instant.MAX)
                .status(Status.ACTIVE)
                .build();
    }

    private CreateSubscriptionDto getSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.MAX)
                .build();
    }

}