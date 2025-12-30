package application;

import application.dto.DataForReceiptDto;
import application.repository.RedisRepository;
import application.service.MainService;
import com.fasterxml.jackson.core.JsonProcessingException;
import jakarta.mail.MessagingException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@Slf4j
@RestController
@RequiredArgsConstructor
@RequestMapping("/nalog")
public class Controller {

    private final MainService mainService;
    private final RedisRepository redisRepository;

    @PostMapping("/receipt")
    public ResponseEntity<String> addReceiptByHandle(@RequestBody DataForReceiptDto dataForReceiptDto)
            throws JsonProcessingException, MessagingException {
        String dataForFunction = dataForReceiptDto.toJsonAsString();
        String printUrl = mainService.addReceipt(dataForFunction);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Чек добавлен: " + printUrl);
    }

    @PostMapping("/redis")
    public ResponseEntity<String> addRedisByHandle(@RequestBody DataForReceiptDto dataForReceiptDto) {
        String dataForRedis = dataForReceiptDto.toJsonAsString();
        redisRepository.addTestDataToBeginning("receipt", dataForRedis);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Успешно добавлена строка в очередь receipt: " + dataForRedis);
    }
}