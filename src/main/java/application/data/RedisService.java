package application.data;

import application.exceptions.RedisConnectionException;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    //Проверка работоспособности поднятого Redis
    public void checkRedis() {
        try {
            String pong = redisTemplate.getConnectionFactory().getConnection().ping();
            if (!"PONG".equalsIgnoreCase(pong)) {
                throw new RedisConnectionException("Redis не вернул PONG");
            }
        } catch (RedisConnectionException exception) {
            throw exception;
        } catch (Exception exception) {
            throw new RedisConnectionException("Ошибка соединения с БД Redis", exception);
        }
    }

    //Получение количества элементов в очереди
    public Long getQueueSize(String key) {
        return redisTemplate.opsForList().size(key);
    }

    //Добавление строки в начало очереди (списка)
    public void addTestDataToBeginning(String key, String data) {
        redisTemplate.opsForList().leftPush(key, data);
    }

    //Получение строки из конца очереди (списка)
    public String getDataFromEnd(String key) {
            return redisTemplate.opsForList().rightPop(key);
    }
}
