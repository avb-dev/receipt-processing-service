package application.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class RedisRepository {

    private final StringRedisTemplate redisTemplate;

    public Long getQueueSize(String key) {
        Long queueSize = redisTemplate.opsForList().size(key);
        log.info("Размер очереди: {} под ключом: {}", queueSize, key);
        return redisTemplate.opsForList().size(key);
    }

    public void addTestDataToBeginning(String key, String data) {
        redisTemplate.opsForList().leftPush(key, data);
        log.info("Добавлена строка в БД Redis : {} под ключом: {}", data, key);
    }

    public String getDataFromEnd(String key) {
        String dataFromRedis = redisTemplate.opsForList().rightPop(key);
        log.info("Получена строка из БД Redis: {} под ключом: {}", dataFromRedis, key);
        return dataFromRedis;
    }
}
