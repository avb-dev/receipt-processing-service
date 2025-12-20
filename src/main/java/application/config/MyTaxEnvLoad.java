package application.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

@Configuration
@PropertySource("classpath:myTaxClient.properties")
@EnableConfigurationProperties(ConfigProperties.class)
public class MyTaxEnvLoad {
}
