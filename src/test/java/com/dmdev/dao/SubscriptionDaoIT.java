package com.dmdev.dao;

import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.integration.IntegrationTestBase;
import org.junit.jupiter.api.Test;

import java.time.Instant;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertTrue;


class SubscriptionDaoIT extends IntegrationTestBase {

    private final SubscriptionDao subscriptionDao = SubscriptionDao.getInstance();

    @Test
    void findAll() {
        Subscription subscription1 = subscriptionDao.insert(getSubscription(1));
        Subscription subscription2 = subscriptionDao.insert(getSubscription(2));
        Subscription subscription3 = subscriptionDao.insert(getSubscription(3));

        List<Subscription> actualResult = subscriptionDao.findAll();

        assertThat(actualResult).hasSize(3);
        List<Integer> subscriptionIds = actualResult.stream()
                .map(Subscription::getId)
                .toList();

        assertThat(subscriptionIds).contains(subscription1.getId(), subscription2.getId(), subscription3.getId());

    }

    @Test
    void findById() {
        Subscription subscription = subscriptionDao.insert(getSubscription(1));

        Optional<Subscription> actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getId()).isEqualTo(subscription.getId());
    }

    @Test
    void deleteExistingEntity() {
        Subscription subscription = subscriptionDao.insert(getSubscription(1));

        boolean actualResult = subscriptionDao.delete(subscription.getId());

        assertTrue(actualResult);

    }

    @Test
    void deleteNotExistingEntity() {
        subscriptionDao.insert(getSubscription(1));

        boolean actualResult = subscriptionDao.delete(234567);

        assertFalse(actualResult);
    }


    @Test
    void update() {
        Subscription subscription = getSubscription(1);
        subscriptionDao.insert(subscription);
        subscription.setName("Petr");

        Integer updatedSubscriptionId = subscriptionDao.update(subscription).getId();

        assertThat(updatedSubscriptionId).isEqualTo(subscription.getId());
    }

    @Test
    void insert() {
        Subscription subscription = getSubscription(1);

        Subscription actualResult = subscriptionDao.insert(subscription);

        assertNotNull(actualResult.getId());
    }

    @Test
    void findByUserId() {
        Subscription subscription = subscriptionDao.insert(getSubscription(1));

        Optional<Subscription> actualResult = subscriptionDao.findById(subscription.getId());

        assertThat(actualResult).isPresent();
        assertThat(actualResult.get().getUserId()).isEqualTo(1);
    }

    @Test
    void shouldNotFindByUserIdIfUserDoesNotExist() {
        List<Subscription> actualResult = subscriptionDao.findByUserId(null);

        assertThat(actualResult).isEmpty();
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
}