package application;

import application.dto.DataForReceiptDto;
import application.service.MainService;
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

    //Получение чека
    @PostMapping("/receipt")
    public ResponseEntity<String> addReceiptByHandle(@RequestBody DataForReceiptDto dataForReceiptDto) {
        String dataForFunction = dataForReceiptDto.toJsonAsString();
        String printUrl = mainService.addReceipt(dataForFunction, true).orElse("");
        if (printUrl.isBlank()) {
            return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                    .body("Чек не добавлен !");
        }
        return ResponseEntity.status(HttpStatus.BAD_REQUEST)
                .body("Чек добавлен: " + printUrl);
    }

    //Добавление записи в БД Redis
    @PostMapping("/redis")
    public ResponseEntity<String> addRedisByHandle(@RequestBody DataForReceiptDto dataForReceiptDto) {
        boolean redisConnection = mainService.checkRedis();
        if (!redisConnection) {
            return ResponseEntity.status(HttpStatus.SERVICE_UNAVAILABLE)
                    .body("Нет подлкючения к БД Redis");
        }
        String dataForRedis = dataForReceiptDto.toJsonAsString();
        mainService.addTestDataToBeginning("receipt", dataForRedis);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Успешно добавлена строка в очередь receipt: " + dataForRedis);
    }
}