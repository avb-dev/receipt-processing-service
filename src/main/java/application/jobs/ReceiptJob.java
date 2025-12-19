package application.jobs;

import application.MainService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.util.concurrent.TimeUnit;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReceiptJob {

    private final MainService mainService;

    @Scheduled(fixedDelay = 60, initialDelay = 60, timeUnit = TimeUnit.SECONDS)
    public void prepareReceipts() {

        System.out.println("Начало работы ReceiptJob !");

        //Проверка поднятого Redis
        if (!mainService.checkRedis()){
            return;
        }

        if (mainService.getQueueSize("receipt").equals(0L)) {
            log.info("Очередь пуста");
            return;
        }

        while (mainService.getQueueSize("receipt") > 0) {
            log.info("Передача информации из Redis в приложение");
            String dataFromRedis = mainService.getDataFromEnd("receipt");
            log.info("Получена строка из Redis: {}", dataFromRedis);
            mainService.addReceipt(dataFromRedis, false);
        }
    }
}
