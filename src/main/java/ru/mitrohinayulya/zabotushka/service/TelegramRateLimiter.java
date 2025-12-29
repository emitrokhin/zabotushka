package ru.mitrohinayulya.zabotushka.service;

import com.google.common.util.concurrent.RateLimiter;
import jakarta.enterprise.context.ApplicationScoped;
import org.eclipse.microprofile.config.inject.ConfigProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.concurrent.TimeUnit;

/**
 * Сервис для ограничения скорости запросов к Telegram API.
 * Telegram API позволяет не более 30 сообщений в секунду.
 */
@ApplicationScoped
public class TelegramRateLimiter {

    private static final Logger log = LoggerFactory.getLogger(TelegramRateLimiter.class);

    private final RateLimiter rateLimiter;

    public TelegramRateLimiter(
            @ConfigProperty(name = "app.telegram.rate-limit.messages-per-second", defaultValue = "25")
            double messagesPerSecond) {
        // Используем 25 msg/sec по умолчанию для безопасности (лимит Telegram - 30 msg/sec)
        this.rateLimiter = RateLimiter.create(messagesPerSecond);
        log.info("TelegramRateLimiter initialized with rate: {} messages/second", messagesPerSecond);
    }

    /**
     * Приобретает разрешение на выполнение запроса к Telegram API.
     * Блокирует выполнение, если лимит исчерпан, до тех пор пока не появится доступное разрешение.
     *
     * @return время ожидания в миллисекундах
     */
    public long acquire() {
        long startTime = System.currentTimeMillis();
        rateLimiter.acquire();
        long waitTime = System.currentTimeMillis() - startTime;

        if (waitTime > 0) {
            log.debug("Rate limit applied, waited {} ms", waitTime);
        }

        return waitTime;
    }

    /**
     * Пытается приобрести разрешение на выполнение запроса к Telegram API с таймаутом.
     *
     * @param timeout максимальное время ожидания
     * @param unit единица измерения времени
     * @return true если разрешение получено, false если таймаут истек
     */
    public boolean tryAcquire(long timeout, TimeUnit unit) {
        boolean acquired = rateLimiter.tryAcquire(timeout, unit);

        if (!acquired) {
            log.warn("Failed to acquire rate limit permission within {} {}", timeout, unit);
        }

        return acquired;
    }

    /**
     * Выполняет действие с применением rate limiting.
     *
     * @param action действие для выполнения
     * @param <T> тип результата
     * @return результат выполнения действия
     */
    public <T> T execute(java.util.function.Supplier<T> action) {
        acquire();
        return action.get();
    }

    /**
     * Выполняет действие с применением rate limiting (void метод).
     *
     * @param action действие для выполнения
     */
    public void executeVoid(Runnable action) {
        acquire();
        action.run();
    }
}
