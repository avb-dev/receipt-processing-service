package application;

import lombok.RequiredArgsConstructor;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.context.properties.ConfigurationPropertiesScan;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@ConfigurationPropertiesScan
@RequiredArgsConstructor
@EnableScheduling
public class Application
{
    //Поднимаю контекст
    public static void main( String[] args ) {
        SpringApplication.run(Application.class, args);
    }

}
