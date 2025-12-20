package application.config;

import application.service.ClientService;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientBean {

    @Bean
    public ClientService clientService(ConfigProperties configProperties) {
        ClientConfig clientConfig = new ClientConfig();
        clientConfig.setPrefix(configProperties.getPrefix());
        clientConfig.setApiPath(configProperties.getApiPath());
        clientConfig.setZoneOffset(configProperties.getZone());
        clientConfig.setRefererHeader(configProperties.getReferer());

        return new ClientService(clientConfig);
    }
}
