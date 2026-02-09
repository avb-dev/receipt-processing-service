package application;

import application.dto.DataForReceiptDto;
import application.repository.PostgresRepository;
import application.repository.RedisRepository;
import application.service.MainService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/nalog")
public class Controller {

    private final MainService mainService;
    private final RedisRepository redisRepository;
    private final PostgresRepository postgresRepository;

    DateTimeFormatter formatterForDublicate = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ssXXX");

    @GetMapping("/checkDublicate")
    public ResponseEntity<String> addReceiptByHandle(@RequestBody String timestamp)
            throws MessagingException, JsonProcessingException {
        OffsetDateTime checkTime = OffsetDateTime.parse(timestamp, formatterForDublicate);
        Boolean isDublicate = postgresRepository.isWrittenLocally(checkTime);
        return ResponseEntity.status(HttpStatus.OK)
                .body("Есть ли дубликат: " + isDublicate);
    }

    @PostMapping("/redisIncome")
    public ResponseEntity<String> addRedisIncomeByHandle(@RequestBody DataForReceiptDto dataForReceiptDto) {
        String dataForRedis = dataForReceiptDto.toJsonAsString();
        redisRepository.addTestDataToBeginning("receipt", dataForRedis);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Успешно добавлена строка в очередь receipt: " + dataForRedis);
    }

    @PostMapping("/redisCancel")
    public ResponseEntity<String> addRedisCancelByHandle(@RequestBody String uuid) {
        redisRepository.addTestDataToBeginning("return", uuid);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Успешно добавлена строка в очередь return: " + uuid);
    }
}