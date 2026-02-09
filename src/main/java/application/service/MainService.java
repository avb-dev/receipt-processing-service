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

    public String addReceipt(DataForReceiptDto dataForReceiptDto) throws MessagingException {
        DataForReceipt dataForReceipt = new DataForReceipt(
                dataForReceiptDto.getName(),
                dataForReceiptDto.getQuantity(),
                dataForReceiptDto.getAmount()
        );

        List<DataForReceipt> dataForReceiptList = List.of(dataForReceipt);

        Receipt receipt = clientService.addIncome(dataForReceiptList, dataForReceiptDto.getTimestamp());
        log.info("Успешная обработка в НАЛОГЕ, чек: {}, Uuid = {}", receipt.printUrl(), receipt.uuid());

        if (dataForReceiptDto.getPaymentId() != 2) {
            paymentRepository.insertPayment(dataForReceiptDto.getPaymentId(),
                    receipt.uuid(),
                    OffsetDateTime.parse(dataForReceiptDto.getTimestamp()));
        }

        emailService.sendHtml(dataForReceiptDto.getEmail(),
                "Спасибо за покупку!",
                receipt.printUrl());

        return receipt.printUrl();
    }

    public void returnReceipt(String uuid) {
        OffsetDateTime operationTime = LocalDateTime.now().atOffset(ZoneOffset.of("+03:00"))
                .truncatedTo(ChronoUnit.SECONDS);
        String result = clientService.returnReceipt(uuid, operationTime.toString());
        paymentRepository.updateRefund(uuid);

        log.info("Успешный возврат чека в НАЛОГЕ: {}", result);
    }

    public DataForReceiptDto mapData(String dataFromRedis) throws JsonProcessingException {
        DataForReceiptDto data = objectMapper.readValue(dataFromRedis, DataForReceiptDto.class);

        LocalDateTime dateTime = LocalDateTime.parse(data.getTimestamp(), formatter);
        OffsetDateTime dateTimePlusZone = dateTime.atOffset(ZoneOffset.of("+03:00"))
                .truncatedTo(ChronoUnit.SECONDS);
        data.setTimestamp(dateTimePlusZone.toString());

        log.info("Маппинг данных завершён: {}", data);
        return data;
    }

    public String correctTimestampForRedis(DataForReceiptDto dataForReceiptDto) {
        OffsetDateTime dateTime = OffsetDateTime.parse(dataForReceiptDto.getTimestamp());
        LocalDateTime dateTimePlusSecond = dateTime.toLocalDateTime().plusSeconds(1);
        String dateTimePlusSecondString = dateTimePlusSecond.format(formatter);
        dataForReceiptDto.setTimestamp(dateTimePlusSecondString);
        return dataForReceiptDto.toJsonAsString();
    }
}
