package application;

import application.config.MyTaxClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.loolzaaa.nalog.mytax.client.MyTaxClient;
import ru.loolzaaa.nalog.mytax.client.dto.AuthenticationDTO;
import ru.loolzaaa.nalog.mytax.client.exception.ApiException;
import ru.loolzaaa.nalog.mytax.client.exception.ApiRequestException;
import ru.loolzaaa.nalog.mytax.client.pojo.Receipt;
import ru.loolzaaa.nalog.mytax.client.pojo.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptJob {

    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final MyTaxClientProperties myTaxClientProperties;
    private final MyTaxClient myTaxClient;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    @Scheduled(fixedDelay = 20, initialDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void prepareReceipts() {
        System.out.println("START !");

        if (redisService.getQueueSize("receipt").equals(0L)) {
            log.info("Очередь пуста");
            return;
        }

        try {
            String login = myTaxClientProperties.getLogin();
            String password = myTaxClientProperties.getPassword();
            AuthenticationDTO.Profile profile = myTaxClient.init(login, password);
            log.info("Выполнен вход. Пользователь: {}", profile.getDisplayName());

            while (redisService.getQueueSize("receipt") > 0) {

                List<Service> serviceForMyTax = null;
                OffsetDateTime dateTimePlusZone = null;

                //Проверка на упавший редис
                log.info("Передача информации из Redis в приложение");
                String dataFromRedis = redisService.getDataFromEnd("receipt");
                log.info("Получена строка из Redis: {}", dataFromRedis);

                try {
                    log.info("Маппинг строки");
                    DataForReceipt data = objectMapper.readValue(dataFromRedis, DataForReceipt.class);

                    Service service = new Service(data.getName(), data.getQuantity(), data.getAmount());
                    LocalDateTime dateTime = LocalDateTime.parse(data.getTimestamp(), formatter);
                    dateTimePlusZone = dateTime.atOffset(ZoneOffset.of("+03:00")).
                            truncatedTo(ChronoUnit.SECONDS);

                    log.info("Данные готовы для обработки: {}Время операции: {}", service, dateTimePlusZone);

                    serviceForMyTax = List.of(service);
                } catch (Exception exception) {
                    log.info("Ошибка при маппинге строки: {}", exception.getMessage());
                    redisService.addTestDataToBeginning("receipt", dataFromRedis);
                    return;
                }

                //Выделение в отдельный метод класса (для контроллера)
                try {
                    log.info("Обработка строки в библиотеке MyTax");
                    Receipt receipt = myTaxClient.addIncome(serviceForMyTax, dateTimePlusZone.toString());
                    log.info("Успешная обработка, чек: {}, Uuid = {}", receipt.printUrl(), receipt.uuid());
                } catch (ApiException exc) {
                    log.info("Ошибка при обработке чека{}", exc.getMessage());
                    redisService.addTestDataToBeginning("receipt", dataFromRedis);
                }
            }
        } catch (ApiRequestException exc) {
            log.info("Не выполнен вход в мой налог !");
        }
    }
}
