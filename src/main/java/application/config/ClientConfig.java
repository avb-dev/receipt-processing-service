package application.config;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@Getter
@NoArgsConstructor
public class ClientConfig {
    private String prefix;
    private String apiPath;
    private String zoneOffset;
    private String refererHeader;
}
