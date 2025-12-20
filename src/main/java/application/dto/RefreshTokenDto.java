package application.dto;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
@JsonIgnoreProperties(ignoreUnknown = true)
public class RefreshTokenDto {
    private String refreshToken;
    private String refreshTokenExpiresIn;
    private String token;
    private String tokenExpireIn;
}
