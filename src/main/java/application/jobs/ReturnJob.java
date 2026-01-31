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
public class ReturnJob {

    private final MainService mainService;
    private final RedisRepository redisRepository;
    private final EmailService emailService;

    private final String mailAdmin;

    private String lastExceptionMessage;

    public ReturnJob(MainService mainService,
                     RedisRepository redisRepository,
                     EmailService emailService,
                     AppConfigProperties appConfigProperties) {
        this.mainService = mainService;
        this.redisRepository = redisRepository;
        this.emailService = emailService;
        this.mailAdmin = appConfigProperties.getMailAdmin();
    }

    @Scheduled(fixedDelayString = "${app.job.return-delay}",
            initialDelayString = "${app.job.return-delay}",
            timeUnit = TimeUnit.SECONDS)
    public void prepareReturn() {
        log.info("Начало работы ReturnJob!");

        String uuidFromRedis = null;
        StringBuilder excMessageBuilder = new StringBuilder();

        try {

            Long queueSize = redisRepository.getQueueSize("return");

            if (queueSize.equals(0L)) {
                log.info("Очередь возвратов пуста");
                return;
            }

            int i = 1;
            while (i <= queueSize) {
                uuidFromRedis = redisRepository.getDataFromEnd("return");
                mainService.returnReceipt(uuidFromRedis);
                lastExceptionMessage = null;
                i++;
            }
        } catch (ApiException apiException) {
            log.warn("Ошибка при возврате чека в API налога, связанная с НАЛОГОМ {}", String.valueOf(apiException));

            excMessageBuilder.append(uuidFromRedis).append("\n ").append(apiException);
            String excMessage = excMessageBuilder.toString();

            if (!apiException.getMessage().equals(lastExceptionMessage)) {
                emailService.sendText(mailAdmin,
                        "Ошибка при возврате чека в API налога, связанная с НАЛОГОМ",
                        excMessage);
            }

            redisRepository.addTestDataToBeginning("return", uuidFromRedis);
        } catch (Exception exception) {
            log.warn("Ошибка при возврате чека в API налога, несвязанная с НАЛОГОМ {}", String.valueOf(exception));

            if (uuidFromRedis != null) {
                excMessageBuilder.append(uuidFromRedis).append("\n ");
            }
            excMessageBuilder.append(exception);
            String excMessage = excMessageBuilder.toString();
            emailService.sendText(mailAdmin,
                    "Ошибка при возврате чека в API налога, несвязанная с НАЛОГОМ",
                    excMessage);
        }
    }
}
