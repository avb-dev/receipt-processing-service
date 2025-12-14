package application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "nalog")
public class MyTaxClientProperties {

    private String login;
    private String password;
    private String prefix;
    private String zone;
    private String referer;
    private String apiPath;
}
