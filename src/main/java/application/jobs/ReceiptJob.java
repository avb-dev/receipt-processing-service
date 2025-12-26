package application.jobs;

import application.service.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.Optional;
import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptJob {

    private final MainService mainService;

    //как работает fix delay
    @Scheduled(fixedDelayString = "${FIXED_DELAY}",
            initialDelayString = "${INITIAL_DELAY}",
            timeUnit = TimeUnit.SECONDS)
    public void prepareReceipts() {
        log.info("Начало работы ReceiptJob!");

        //Проверка поднятого Redis
        if (!mainService.checkRedis()) {
            return;
        }

        if (mainService.getQueueSize("receipt").equals(0L)) {
            log.info("Очередь пуста");
            return;
        }

        //Задержка
        while (mainService.getQueueSize("receipt") > 0) {
            String dataFromRedis = mainService.getDataFromEnd("receipt");
            Optional<String> result = mainService.addReceipt(dataFromRedis, false);
            if (result.isEmpty()) {
                return;
            }
        }
    }
}
