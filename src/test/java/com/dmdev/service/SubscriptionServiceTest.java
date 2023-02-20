package com.dmdev.service;


import com.dmdev.dao.SubscriptionDao;
import com.dmdev.dto.CreateSubscriptionDto;
import com.dmdev.entity.Provider;
import com.dmdev.entity.Status;
import com.dmdev.entity.Subscription;
import com.dmdev.exception.SubscriptionException;
import com.dmdev.exception.ValidationException;
import com.dmdev.mapper.CreateSubscriptionMapper;
import com.dmdev.validator.CreateSubscriptionValidator;
import com.dmdev.validator.Error;
import com.dmdev.validator.ValidationResult;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.Clock;
import java.time.Instant;
import java.time.ZoneId;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.doReturn;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.verifyNoInteractions;
import static org.mockito.Mockito.verifyNoMoreInteractions;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class SubscriptionServiceTest {

    @Mock
    private SubscriptionDao subscriptionDao;
    @Mock
    private CreateSubscriptionMapper createSubscriptionMapper;
    @Mock
    private CreateSubscriptionValidator createSubscriptionValidator;
    private static final Clock clock = Clock.fixed(Instant.now(), ZoneId.systemDefault());
    @InjectMocks
    private SubscriptionService subscriptionService;

    @BeforeEach
    void setUp() {
        subscriptionService = new SubscriptionService
                (subscriptionDao,
                        createSubscriptionMapper,
                        createSubscriptionValidator,
                        clock);
    }

    @Test
    void upsert() {
        CreateSubscriptionDto createSubscriptionDto = getSubscriptionDto();
        Subscription subscription = getSubscription();

        doReturn(new ValidationResult()).when(createSubscriptionValidator).validate(createSubscriptionDto);
        doReturn(subscription).when(createSubscriptionMapper).map(createSubscriptionDto);
        doReturn(subscription).when(subscriptionDao).upsert(subscription);

        Subscription actualResult = subscriptionService.upsert(createSubscriptionDto);

        assertThat(actualResult).isEqualTo(subscription);
    }

    @Test
    void shouldThrowExceptionIfDtoInvalid() {
        CreateSubscriptionDto createSubscriptionDto = getSubscriptionDto();
        ValidationResult validationResult = new ValidationResult();
        validationResult.add(Error.of(100, "invalid ID"));
        doReturn(validationResult).when(createSubscriptionValidator).validate(createSubscriptionDto);

        assertThrows(ValidationException.class, () -> subscriptionService.upsert(createSubscriptionDto));
        verifyNoInteractions(subscriptionDao, createSubscriptionMapper);
    }

    @Nested
    class TestCancel {
        private static final int SUB_ID = 1;

        @Test
        void shouldThrowExceptionIfNotFoundById() {
            doReturn(Optional.empty()).when(subscriptionDao).findById(SUB_ID);
            assertThrows(IllegalArgumentException.class, () -> subscriptionService.cancel(SUB_ID));
            verify(subscriptionDao).findById(SUB_ID);
            verifyNoMoreInteractions(subscriptionDao);
        }

        @Test
        void shouldThrowExceptionIfStatusIsNotActive() {
            Subscription subscription = Subscription.builder().status(Status.CANCELED).build();
            doReturn(Optional.of(subscription)).when(subscriptionDao).findById(SUB_ID);
            SubscriptionException exception =
                    assertThrows(SubscriptionException.class, () -> subscriptionService.cancel(SUB_ID));
            assertThat(exception.getMessage()).isEqualTo("Only active subscription 1 can be canceled");
        }

        @Nested
        class WhenActive {
            private Subscription subscription;

            @BeforeEach
            void setUp() {
                subscription = mock(Subscription.class);
                when(subscriptionDao.findById(SUB_ID)).thenReturn(Optional.of(subscription));
                when(subscription.getStatus()).thenReturn(Status.ACTIVE);
            }

            @Test
            void shouldSetStatusCanceled() {
                subscriptionService.cancel(SUB_ID);
                verify(subscription).setStatus(Status.CANCELED);
            }

            @Test
            void shouldUpdateSubscription() {
                subscriptionService.cancel(SUB_ID);
                verify(subscriptionDao).update(subscription);
            }
        }
    }

    @Nested
    class TestExpire {
        private static final int SUB_ID = 1;

        @Test
        void shouldThrowExceptionIfNotFoundById() {
            doReturn(Optional.empty()).when(subscriptionDao).findById(SUB_ID);
            assertThrows(IllegalArgumentException.class, () -> subscriptionService.expire(SUB_ID));
            verify(subscriptionDao).findById(SUB_ID);
            verifyNoMoreInteractions(subscriptionDao);
        }

        @Test
        void shouldThrowExceptionIfStatusIsExpired() {
            var subscription = Subscription.builder().status(Status.EXPIRED).build();
            doReturn(Optional.of(subscription)).when(subscriptionDao).findById(SUB_ID);
            var exception =
                    assertThrows(SubscriptionException.class, () -> subscriptionService.expire(SUB_ID));
            assertThat(exception.getMessage()).isEqualTo("Subscription 1 has already expired");
        }

        @Nested
        class WhenActive {
            private Subscription subscription;

            @BeforeEach
            void setUp() {
                subscription = mock(Subscription.class);
                when(subscriptionDao.findById(SUB_ID)).thenReturn(Optional.of(subscription));
                when(subscription.getStatus()).thenReturn(Status.ACTIVE);
            }

            @Test
            void shouldSetExpirationDate() {
                subscriptionService.expire(SUB_ID);
                verify(subscription).setExpirationDate(clock.instant());
            }

            @Test
            void shouldSetStatusExpired() {
                subscriptionService.expire(SUB_ID);
                verify(subscription).setStatus(Status.EXPIRED);
            }

            @Test
            void shouldUpdateSubscription() {
                subscriptionService.expire(SUB_ID);
                verify(subscriptionDao).update(subscription);
            }
        }
    }

    private CreateSubscriptionDto getSubscriptionDto() {
        return CreateSubscriptionDto.builder()
                .userId(1)
                .name("Ivan")
                .provider("GOOGLE")
                .expirationDate(Instant.MAX)
                .build();
    }

    private Subscription getSubscription() {
        return Subscription.builder()
                .userId(1)
                .name("Ivan")
                .provider(Provider.GOOGLE)
                .expirationDate(Instant.MAX)
                .status(Status.ACTIVE)
                .build();
    }

}