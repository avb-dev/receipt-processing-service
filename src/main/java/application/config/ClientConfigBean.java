package application.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class ClientConfigBean {

    @Bean
    public ClientConfig clientConfig(AppConfigProperties appConfigProperties) {
        ClientConfig cfg = new ClientConfig();
        cfg.setPrefix(appConfigProperties.getPrefix());
        cfg.setApiPath(appConfigProperties.getApiPath());
        cfg.setZoneOffset(appConfigProperties.getZone());
        cfg.setRefererHeader(appConfigProperties.getReferer());
        return cfg;
    }
}
