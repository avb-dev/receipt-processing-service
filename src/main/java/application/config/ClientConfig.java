package application.config;

import lombok.*;

@Getter
@Setter
@NoArgsConstructor
public class ClientConfig {
    @NonNull
    private String prefix = "";
    @NonNull
    private String apiPath = "https://lknpd.nalog.ru/api/v1";
    @NonNull
    private String zoneOffset = "Z";
    @NonNull
    private String refererHeader = "Referer";
}
