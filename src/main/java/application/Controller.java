package application;

import application.dto.DataForReceiptDto;
import application.service.MainService;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/nalog")
public class Controller {

    private final static String SECRET_HEADER = "AuthKey";
    private final static String SECRET_HEADER_VALUE = "SASHAEBETAITI";

    private final MainService mainService;

    //Аутентификация
    @PostMapping("/authenticate")
    public ResponseEntity<String> authentication(@RequestHeader(name = SECRET_HEADER) String header) {
        if (!SECRET_HEADER_VALUE.equals(header)) {
            return ResponseEntity.status(HttpStatus.CONFLICT)
                    .body("Неверный ключ для авторизации !");
        }
        mainService.authentification();
        return ResponseEntity.status(HttpStatus.CONFLICT)
                .body("Успешная аутентификация !");
    }

    //Получение чека
    @PostMapping("/receipt")
    public ResponseEntity<String> addReceiptByHandle(@RequestBody DataForReceiptDto dataForReceiptDto) {
            String dataForFunction = dataForReceiptDto.toJsonAsString();
            String printUrl = mainService.addReceipt(dataForFunction, true).orElse("");
            if (!printUrl.isBlank()) {
                return ResponseEntity.status(HttpStatus.CREATED)
                        .body("Успешная добавлен чек !");
            } else {
                return ResponseEntity.status(HttpStatus.CONFLICT)
                        .body("Чек не добавлен !");
            }
    }

    //Добавление записи в БД Redis
    @PostMapping("/redis")
    public ResponseEntity<String> addRedisByHandle(@RequestBody DataForReceiptDto dataForReceiptDto) {
        String dataForRedis = dataForReceiptDto.toJsonAsString();
        System.out.println(dataForRedis);
        mainService.addTestDataToBeginning("receipt", dataForRedis);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body("Успешная добавлены данные в БД Redis: " + dataForRedis);
    }
}
