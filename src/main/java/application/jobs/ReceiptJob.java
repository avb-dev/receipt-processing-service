package application.jobs;

import application.config.AppConfigProperties;
import application.exceptions.ApiException;
import application.repository.RedisRepository;
import application.service.EmailService;
import application.service.MainService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
public class ReceiptJob {

    private final MainService mainService;
    private final RedisRepository redisRepository;
    private final EmailService emailService;

    private final Long delay;

    private final String mailAdmin;

    private String lastExceptionMessage;

    public ReceiptJob(MainService mainService,
                      RedisRepository redisRepository,
                      EmailService emailService,
                      AppConfigProperties appConfigProperties) {
        this.mainService = mainService;
        this.redisRepository = redisRepository;
        this.emailService = emailService;
        this.delay = Long.parseLong(appConfigProperties.getDelay());
        this.mailAdmin = appConfigProperties.getMailAdmin();
    }

    @Scheduled(fixedDelayString = "${app.job.fixed-delay}",
            initialDelayString = "${app.job.initial-delay}",
            timeUnit = TimeUnit.SECONDS)
    public void prepareReceipts() {
        log.info("Начало работы ReceiptJob!");

        String dataFromRedis = null;
        StringBuilder excMessageBuilder = new StringBuilder();

        try {
            Long queueSize = redisRepository.getQueueSize("receipt");

            if (queueSize.equals(0L)) {
                log.info("Очередь пуста");
                return;
            }

            long queueSizeForReceipt;
            if (queueSize <= delay) {
                queueSizeForReceipt = queueSize;
            } else {
                queueSizeForReceipt = delay;
            }

            for (int i = 1; i <= queueSizeForReceipt; i++) {
                dataFromRedis = redisRepository.getDataFromEnd("receipt");
                mainService.addReceipt(dataFromRedis);
                if (i > 1 || i == queueSizeForReceipt ) {
                    lastExceptionMessage = "";
                }
            }
        } catch (ApiException apiException) {
            log.warn("Ошибка при добавлении чека в API налога, связанная с НАЛОГОМ {}", String.valueOf(apiException));

            if (dataFromRedis != null) {
                excMessageBuilder.append(dataFromRedis).append("; ");
            }
            excMessageBuilder.append(apiException);
            String excMessage = excMessageBuilder.toString();
            if (!apiException.getMessage().equals(lastExceptionMessage)) {
                emailService.sendText(mailAdmin,
                        "Ошибка при добавлении чека в API налога, связанная с НАЛОГОМ",
                        excMessage);
            }
            lastExceptionMessage = apiException.getMessage();

            if (dataFromRedis != null) {
                redisRepository.addTestDataToBeginning("receipt", dataFromRedis);
            }
        } catch (Exception exception) {
            log.warn("Ошибка при добавлении чека в API налога, несвязанная с НАЛОГОМ {}", String.valueOf(exception));

            if (dataFromRedis != null) {
                excMessageBuilder.append(dataFromRedis).append("; ");
            }
            excMessageBuilder.append(exception);
            String excMessage = excMessageBuilder.toString();
            emailService.sendText(mailAdmin,
                    "Ошибка при добавлении чека в API налога, несвязанная с НАЛОГОМ",
                    excMessage);
        }
    }
}
