package ru.mitrohinayulya.zabotushka.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Тесты для TelegramRateLimiter
 */
class TelegramRateLimiterTest {

    private TelegramRateLimiter rateLimiter;

    @BeforeEach
    void setUp() {
        // Создаем rate limiter с высокой скоростью для быстрых тестов
        rateLimiter = new TelegramRateLimiter(10.0); // 10 запросов в секунду
    }

    @Test
    void shouldAllowRequestsWithinLimit() {
        // Given: Rate limiter с лимитом 10 req/sec

        // When: Выполняем 5 запросов (меньше лимита)
        var startTime = System.currentTimeMillis();
        for (int i = 0; i < 5; i++) {
            rateLimiter.acquire();
        }
        var duration = System.currentTimeMillis() - startTime;

        // Then: Все запросы должны быть выполнены быстро (за ~500ms)
        assertThat(duration).isLessThan(1000);
    }

    @Test
    void shouldThrottleRequestsExceedingLimit() {
        // Given: Rate limiter с лимитом 10 req/sec

        // When: Выполняем 20 запросов (больше лимита)
        var startTime = System.currentTimeMillis();
        for (int i = 0; i < 20; i++) {
            rateLimiter.acquire();
        }
        var duration = System.currentTimeMillis() - startTime;

        // Then: Должна быть задержка (минимум ~1 секунда для второй группы из 10 запросов)
        assertThat(duration).isGreaterThan(1000).isLessThan(3000); // Но не слишком долго
    }

    @Test
    void shouldReturnWaitTimeWhenThrottling() {
        // Given: Rate limiter с лимитом 10 req/sec

        // When: Выполняем запросы до достижения лимита
        for (int i = 0; i < 10; i++) {
            rateLimiter.acquire();
        }

        // Then: Следующий запрос должен вернуть время ожидания > 0
        var waitTime = rateLimiter.acquire();
        assertThat(waitTime).isGreaterThan(0);
    }

    @Test
    void shouldExecuteActionWithRateLimit() {
        // Given: Счетчик вызовов
        var counter = new AtomicInteger(0);

        // When: Выполняем действие через execute
        var result = rateLimiter.execute(() -> {
            counter.incrementAndGet();
            return 42;
        });

        // Then: Действие должно быть выполнено и вернуть результат
        assertThat(result).isEqualTo(42);
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    void shouldExecuteVoidActionWithRateLimit() {
        // Given: Счетчик вызовов
        var counter = new AtomicInteger(0);

        // When: Выполняем void действие через executeVoid
        rateLimiter.executeVoid(counter::incrementAndGet);

        // Then: Действие должно быть выполнено
        assertThat(counter.get()).isEqualTo(1);
    }

    @Test
    void shouldTryAcquireWithTimeout() {
        // Given: Rate limiter с лимитом 10 req/sec

        // When: Пытаемся получить разрешение с таймаутом
        var acquired = rateLimiter.tryAcquire(100, TimeUnit.MILLISECONDS);

        // Then: Должны получить разрешение
        assertThat(acquired).isTrue();
    }

    @Test
    void shouldFailToAcquireWhenTimeoutExpires() {
        // Given: Rate limiter с очень низким лимитом
        var slowLimiter = new TelegramRateLimiter(0.5); // 0.5 req/sec

        // When: Исчерпываем лимит
        slowLimiter.acquire();

        // Then: Попытка получить разрешение с коротким таймаутом должна провалиться
        var acquired = slowLimiter.tryAcquire(50, TimeUnit.MILLISECONDS);
        assertThat(acquired).isFalse();
    }

    @Test
    void shouldHandleConcurrentRequests() throws InterruptedException {
        // Given: Rate limiter и пул потоков
        var executor = Executors.newFixedThreadPool(5);
        var successCount = new AtomicInteger(0);
        int totalRequests = 25;

        // When: Выполняем 25 конкурентных запросов
        var latch = new CountDownLatch(totalRequests);
        for (int i = 0; i < totalRequests; i++) {
            executor.submit(() -> {
                try {
                    rateLimiter.acquire();
                    successCount.incrementAndGet();
                } finally {
                    latch.countDown();
                }
            });
        }

        // Then: Все запросы должны быть успешно обработаны
        var completed = latch.await(5, TimeUnit.SECONDS);
        executor.shutdown();

        assertThat(completed).isTrue();
        assertThat(successCount.get()).isEqualTo(totalRequests);
    }

    @Test
    void shouldMaintainRateLimitAcrossMultipleExecutions() {
        // Given: Rate limiter с лимитом 10 req/sec
        List<Long> timestamps = new ArrayList<>();

        // When: Выполняем 30 запросов и записываем временные метки
        var startTime = System.currentTimeMillis();
        for (int i = 0; i < 30; i++) {
            rateLimiter.acquire();
            timestamps.add(System.currentTimeMillis() - startTime);
        }

        // Then: Проверяем, что скорость соблюдается
        // Первые 10 запросов должны быть быстрыми (в первой секунде)
        assertThat(timestamps.get(9)).isLessThan(1000);

        // Следующие 10 запросов должны быть во второй секунде
        assertThat(timestamps.get(19)).isGreaterThan(1000);
        assertThat(timestamps.get(19)).isLessThan(2500);

        // Последние 10 запросов должны быть в третьей секунде
        assertThat(timestamps.get(29)).isGreaterThan(2000);
        assertThat(timestamps.get(29)).isLessThan(3500);
    }
}
