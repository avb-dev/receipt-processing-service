package application.service;

import application.dto.AuthenticationDto;
import application.dto.DataForReceiptDto;
import application.entity.DataForReceipt;
import application.entity.Receipt;
import application.repository.PostgresRepository;
import application.exceptions.MappingException;
import application.exceptions.RedisConnectionException;
import application.exceptions.SqlException;
import application.repository.RedisRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.List;
import java.util.Optional;

@Slf4j
@Service
@RequiredArgsConstructor
public class MainService {

    private final ObjectMapper objectMapper;
    private final RedisRepository redisService;
    private final EmailService emailService;
    private final ClientService clientService;
    private final PostgresRepository paymentRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");

    @Value("${MAIL_ADMIN}")
    private String adminMail;

    @Value("${API_LOGIN}")
    private String login;

    @Value("${API_PASSWORD}")
    private String password;

    /*
    Блок для взаимодействия с paymentRepository
     */
    public void insertPayment(int paymentId, String Uuid) {
        try {
            int rows = paymentRepository.insertPayment(paymentId, Uuid);
            log.info("Успешное добавление чека в БД Postgres, обновлено строк: {}", rows);
        } catch (Exception exception) {
            String message = exception.getMessage() + exception.getCause().toString();
            throw new SqlException(message);
        }
    }

    /*
    Блок для взаимодействия с RedisService
     */
    public boolean checkRedis() {
        try {
            redisService.checkRedis();
            return true;
        } catch (RedisConnectionException redisConnectionException) {
            String message;
            if (redisConnectionException.getCause() != null) {
                message = redisConnectionException.getMessage() + " " + redisConnectionException.getCause().toString();
            } else {
                message = redisConnectionException.getMessage();
            }
            log.warn("Ошибка соединения к Redis: {}", message);
            sendText(adminMail, "Ошибка соединения с Redis", message);
            return false;
        }
    }

    public Long getQueueSize(String key) {
        return redisService.getQueueSize(key);
    }

    public void addTestDataToBeginning(String key, String data) {
        redisService.addTestDataToBeginning(key, data);
        log.info("Успешно добавлена строка в очередь {}: {}", key, data);
    }

    public String getDataFromEnd(String key) {
        String dataFromRedis = redisService.getDataFromEnd(key);
        log.info("Получение строки из Redis: {}", dataFromRedis);
        return dataFromRedis;
    }

    /*
    Блок для взаимодействия с EmailService
     */
    public void sendText(String to, String subject, String text) {
        emailService.sendText(to, subject, text);
        log.info("Успешная отправка сообщения об ошибке админу {}", text);
    }

    public void sendHtml(String to, String subject, String printUrl) {
        emailService.sendHtml(to, subject, printUrl);
        log.info("Успешная отправка чека покупателю {} чека {}", to, printUrl);
    }

    /*
    Блок для взаимодействия с MyTalClient
     */
    public void authentification() {
        try {
            AuthenticationDto.Profile profile = clientService.init(login, password);
            log.info("Выполнен вход. Пользователь: {}", profile.getDisplayName());
        } catch (Exception exception) {
            log.warn("Не выполнен вход в мой налог !{}", exception.getMessage());
            sendText(adminMail,
                    "Ошибка аутентификации",
                    exception.getMessage());
            throw new IllegalStateException("Ошибка аутентификации в Мой налог: " + exception.getMessage());
        }
    }

    public Optional<String> addReceipt(String dataInString, boolean handleFlag) {

        DataForReceiptDto dataForReceiptDto;
        Receipt receipt;

        try {
            dataForReceiptDto = mapData(dataInString);
            OffsetDateTime operationTime = mapTime(dataForReceiptDto);

            DataForReceipt service = new DataForReceipt(dataForReceiptDto.getName(),
                    dataForReceiptDto.getQuantity(),
                    dataForReceiptDto.getAmount());

            List<DataForReceipt> serviceForMyTax = List.of(service);

            log.info("Начало обработки строки в сервисе");
            receipt = clientService.addIncome(serviceForMyTax, operationTime.toString());
            log.info("Успешная обработка, чек: {}, Uuid = {}", receipt.printUrl(), receipt.uuid());
        } catch (MappingException mappingException) {
            log.warn(String.valueOf(mappingException));
            sendText(adminMail,
                    "Ошибка при добавлении чека в API налога, некорректный маппинг данных",
                    mappingException.getMessage());
            return Optional.empty();
        } catch (Exception exception) {
            log.warn("Ошибка при добавлении чека в API налога {}", String.valueOf(exception));
            if (!handleFlag) {
                addTestDataToBeginning("receipt", dataInString);
            }
            sendText(adminMail,
                    "Ошибка при добавлении чека в API налога",
                    exception.getMessage());
            return Optional.empty();
        }

        //Если в мой налог Чек добавлен, то даже при ошибках далее (postgres и почта) в Redis ничего не возвращается
        //Добавление в postgres
        try {
            insertPayment(dataForReceiptDto.getPaymentId(), receipt.uuid());
        } catch (Exception exception) {
            log.warn("Чек добавлен в мой налог, но не добавлен в postgres, {}Uuid ЧЕКА: {}",
                    exception.getMessage(),
                    receipt.uuid());
            sendText(adminMail,
                    "Ошибка при добавлении чека в postgres",
                    exception.getMessage() + "Uuid ЧЕКА: " + receipt.uuid());
            return Optional.empty();
        }

        //Отправка по почте
        try {
            sendHtml(dataForReceiptDto.getEmail(),
                    "Спасибо за покупку!",
                    receipt.printUrl());
        } catch (Exception exception) {
            log.warn("Чек добавлен в мой налог, но не отправлен по почте, {}Uuid ЧЕКА: {}",
                    exception.getMessage(),
                    receipt.uuid());
            sendText(adminMail,
                    "Ошибка при отправке чека по почте",
                    exception.getMessage() + "Uuid ЧЕКА: " + receipt.uuid());
            return Optional.empty();
        }
        return Optional.of(receipt.printUrl());
    }

    public DataForReceiptDto mapData(String dataFromRedis) {
        try {
            log.info("Маппинг данных");

            DataForReceiptDto data = objectMapper.readValue(dataFromRedis, DataForReceiptDto.class);

            log.info("Данные готовы для обработки: {}", data);
            return data;
        } catch (Exception exception) {
            throw new MappingException("Ошибка при маппинге данных из Redis: " + dataFromRedis);
        }
    }

    private OffsetDateTime mapTime(DataForReceiptDto dataForReceiptDto) {
        try {
            log.info("Маппинг времени");

            LocalDateTime dateTime = LocalDateTime.parse(dataForReceiptDto.getTimestamp(), formatter);
            OffsetDateTime dateTimePlusZone = dateTime.atOffset(ZoneOffset.of("+03:00")).
                    truncatedTo(ChronoUnit.SECONDS);

            log.info("Маппинг времени завершён, {}", dateTimePlusZone);
            return dateTimePlusZone;
        } catch (Exception exception) {
            throw new MappingException("Ошибка при маппинге времени из Redis: " + dataForReceiptDto.toString());
        }
    }
}
