/*package application;

import application.config.MyTaxClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import ru.loolzaaa.nalog.mytax.client.MyTaxClient;
import ru.loolzaaa.nalog.mytax.client.dto.AuthenticationDTO;
import ru.loolzaaa.nalog.mytax.client.pojo.Receipt;
import ru.loolzaaa.nalog.mytax.client.pojo.Service;

import java.util.List;
import java.util.concurrent.TimeUnit;

@Component
@RequiredArgsConstructor
public class ReceiptJob {

    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final MyTaxClientProperties myTaxClientProperties;
    private final MyTaxClient myTaxClient;

    @Scheduled(fixedDelay = 20, initialDelay = 10, timeUnit = TimeUnit.SECONDS)
    public void prepareReceipts() {
        System.out.println("START !");

        if (redisService.getQueueSize("receipt").equals(0L)) {
            return;
        }

        try {

            String login = myTaxClientProperties.getLogin();
            String password = myTaxClientProperties.getPassword();
            AuthenticationDTO.Profile profile = myTaxClient.init(login, password);
            System.out.println(profile.getDisplayName());

            Long size = redisService.getQueueSize("receipt");
            int i = 0;

            while (redisService.getQueueSize("receipt") > 0) {
                System.out.println("Получение чека на новую услугу");
                Service service = objectMapper.readValue(redisService.getDataFromEnd("receipt"), Service.class);
                System.out.println(service);
                List<Service> serviceForMyTax = List.of(service);

                Receipt receipt = myTaxClient.addIncome(serviceForMyTax);

                System.out.println(receipt.jsonUrl());
                i++;
            }
        } catch (Exception exc) {
            throw new RuntimeException("Ошибка", exc);
        }
    }
}*/
