package application;

import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class RedisService {

    private final StringRedisTemplate redisTemplate;

    //Добавление строки в начало очереди (списка)
    public void addTestDataToBeginning(String key, String data) {
        redisTemplate.opsForList().leftPush(key, data);
    }

    //Получение строки из конца очереди (списка)
    public String getDataFromEnd(String key) {
            return redisTemplate.opsForList().rightPop(key);
    }

    //Получение количества элементов в очереди
    public Long getQueueSize(String key) {
        return redisTemplate.opsForList().size(key);
    }
}
