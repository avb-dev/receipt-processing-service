package application;

import application.config.MyTaxClientProperties;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import ru.loolzaaa.nalog.mytax.client.MyTaxClient;
import ru.loolzaaa.nalog.mytax.client.dto.AuthenticationDTO;
import ru.loolzaaa.nalog.mytax.client.pojo.Receipt;
import ru.loolzaaa.nalog.mytax.client.pojo.Service;

import java.util.List;

@SpringBootApplication
@ConfigurationPropertiesScan
//@EnableScheduling
@RequiredArgsConstructor
public class Application implements CommandLineRunner
{
    private final ObjectMapper objectMapper;
    private final RedisService redisService;
    private final MyTaxClientProperties myTaxClientProperties;
    private final MyTaxClient myTaxClient;

    //Поднимаю контекст
    public static void main( String[] args ) {
        SpringApplication.run(Application.class, args);
    }

    @Override
    public void run(String... args) {
        System.out.println("START !");

        redisService.addTestDataToBeginning("receipt", "{\"name\":\"Услуга 9\", \"amount\":50, \"quantity\":1}");
        //String receiptString1 = redisService.getDataFromEnd("receipt");

        //redisService.addTestDataToBeginning("receipt", "{\"name\":\"Usluga 2\", \"amount\":20, \"quantity\":1}");
        //String receiptString2 = redisService.getDataFromEnd("receipt");

        //redisService.addTestDataToBeginning("receipt", "{\"name\":\"Usluga 3\", \"amount\":30, \"quantity\":1}");
        //String receiptString3 = redisService.getDataFromEnd("receipt");

        System.out.println("log 1");

        try {
            String login = myTaxClientProperties.getLogin();
            String password = myTaxClientProperties.getPassword();
            System.out.println(myTaxClientProperties.getLogin());
            System.out.println(myTaxClientProperties.getPassword());

            System.out.println("log2");

            AuthenticationDTO.Profile profile = myTaxClient.init(login, password);
            System.out.println(profile.getDisplayName());

            Long size = redisService.getQueueSize("receipt");
            int i = 0;

            while (i < size) {
                System.out.println("Получение чека на новую услугу");
                Service service = objectMapper.readValue(redisService.getDataFromEnd("receipt"), Service.class);
                System.out.println(service);
                List<Service> serviceForMyTax = List.of(service);

                Receipt receipt = myTaxClient.addIncome(serviceForMyTax);

                System.out.println(receipt.printUrl());
                System.out.println(receipt.jsonUrl());
                System.out.println(receipt.uuid());
                i++;
            }
        } catch (Exception exc) {
            throw new RuntimeException("Ошибка", exc);

        }
    }
}
