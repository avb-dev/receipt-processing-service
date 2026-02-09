package application.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@Getter
@Setter
@NoArgsConstructor
@ConfigurationProperties(prefix = "app.config")
public class AppConfigProperties {
    private String prefix;
    private String zone;
    private String referer;
    private String apiPath;

    private String mailAdmin;

    private String apiLogin;
    private String apiPassword;

    private String delay;
}
