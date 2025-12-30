package application.service;

import application.config.AppConfigProperties;
import application.dto.AuthenticationDto;
import application.dto.DataForReceiptDto;
import application.entity.DataForReceipt;
import application.entity.Receipt;
import application.repository.PostgresRepository;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.mail.MessagingException;
import lombok.extern.slf4j.Slf4j;
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
public class MainService {

    private final String mailAdmin;
    private final String login;
    private final String password;

    private final ObjectMapper objectMapper;
    private final EmailService emailService;
    private final ClientService clientService;
    private final PostgresRepository paymentRepository;

    private final DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSSSSS");
    private OffsetDateTime lastOperationTime;

    public MainService(AppConfigProperties appConfigProperties,
                       ObjectMapper objectMapper,
                       EmailService emailService,
                       ClientService clientService,
                       PostgresRepository paymentRepository) {
        this.mailAdmin = appConfigProperties.getMailAdmin();
        this.login = appConfigProperties.getApiLogin();
        this.password = appConfigProperties.getApiPassword();
        this.objectMapper = objectMapper;
        this.emailService = emailService;
        this.clientService = clientService;
        this.paymentRepository = paymentRepository;
    }

    public void authentification() {
        try {
            AuthenticationDto.Profile profile = clientService.init(login, password);
            log.info("Выполнен вход. Пользователь: {}", profile.getDisplayName());
        } catch (Exception exception) {
            log.warn("Не выполнен вход в мой налог !{}", exception.getMessage());
            emailService.sendText(mailAdmin,
                    "Ошибка аутентификации",
                    String.valueOf(exception));
            throw new IllegalStateException("Ошибка аутентификации в Мой налог: " + exception.getMessage());
        }
    }

    public String addReceipt(String dataInString) throws JsonProcessingException, MessagingException {
        DataForReceiptDto dataForReceiptDto = mapData(dataInString);
        OffsetDateTime operationTime = mapTime(dataForReceiptDto);

        if (lastOperationTime != null) {
            if (operationTime.isEqual(lastOperationTime) || operationTime.isBefore(lastOperationTime)) {
                operationTime = lastOperationTime.plusSeconds(1);
            }
        }
        log.info("Время операции с учетом проверки на совпадение: {}", operationTime);

        DataForReceipt dataForReceipt = new DataForReceipt(
                dataForReceiptDto.getName(),
                dataForReceiptDto.getQuantity(),
                dataForReceiptDto.getAmount()
        );

        List<DataForReceipt> serviceForMyTax = List.of(dataForReceipt);

        Receipt receipt = clientService.addIncome(serviceForMyTax, operationTime.toString());
        log.info("Успешная обработка в НАЛОГЕ, чек: {}, Uuid = {}", receipt.printUrl(), receipt.uuid());
        lastOperationTime = operationTime;

        paymentRepository.insertPayment(dataForReceiptDto.getPaymentId(), receipt.uuid());

        emailService.sendHtml(dataForReceiptDto.getEmail(),
                "Спасибо за покупку!",
                receipt.printUrl());

        return receipt.printUrl();
    }

    public DataForReceiptDto mapData(String dataFromRedis) throws JsonProcessingException {
        DataForReceiptDto data = objectMapper.readValue(dataFromRedis, DataForReceiptDto.class);
        log.info("Маппинг данных завершён: {}", data);
        return data;
    }

    private OffsetDateTime mapTime(DataForReceiptDto dataForReceiptDto) {
        LocalDateTime dateTime = LocalDateTime.parse(dataForReceiptDto.getTimestamp(), formatter);
        OffsetDateTime dateTimePlusZone = dateTime.atOffset(ZoneOffset.of("+03:00")).
                truncatedTo(ChronoUnit.SECONDS);
        log.info("Маппинг времени завершён, {}", dateTimePlusZone);
        return dateTimePlusZone;
    }
}
