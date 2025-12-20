package application.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@ConfigurationProperties(prefix = "conf")
public class MyTaxClientProperties {
    private String prefix;
    private String zone;
    private String referer;
    private String apiPath;
}
