package application.service;

import application.config.ClientConfig;
import application.dto.AuthenticationDto;
import application.dto.RefreshTokenDto;
import application.entity.DataForReceipt;
import application.entity.Receipt;
import application.exceptions.ApiException;
import application.exceptions.ApiRequestException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;
import lombok.Getter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.nio.charset.StandardCharsets;
import java.security.SecureRandom;
import java.time.Duration;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;

import java.util.List;
import java.util.concurrent.locks.ReadWriteLock;
import java.util.concurrent.locks.ReentrantReadWriteLock;
import java.util.stream.IntStream;

@Component
public class ClientService {

    private static final Logger log = LoggerFactory.getLogger(ClientService.class);

    private static final ObjectMapper MAPPER = new ObjectMapper();

    private final HttpClient httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_1_1)
            .connectTimeout(Duration.ofSeconds(8))
            .build();

    private final ReadWriteLock refreshTokenLock = new ReentrantReadWriteLock(true);

    private final ClientConfig clientConfig;

    @Getter
    private final String deviceId;

    private String refreshToken;
    private String token;
    private String tokenExpireIn;

    private String inn;

    public ClientService(ClientConfig clientConfig) {
        this.clientConfig = clientConfig;
        this.deviceId = generateDeviceId(this.clientConfig.getPrefix());
    }

    public AuthenticationDto.Profile init(String username, String password) {
        AuthenticationDto authenticate = authenticate(username, password);
        this.refreshToken = authenticate.getRefreshToken();
        this.token = authenticate.getToken();
        this.tokenExpireIn = authenticate.getTokenExpireIn();
        this.inn = authenticate.getProfile().getInn();
        return authenticate.getProfile();
    }

    public Receipt addIncome(List<DataForReceipt> services, String operationTime) {
        checkToken();

        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("paymentType", "CASH");
        payload.put("ignoreMaxTotalIncomeRestriction", false);
        ObjectNode clientNode = payload.putObject("client");
        clientNode.putNull("contactPhone");
        clientNode.putNull("displayName");
        clientNode.putNull("inn");
        clientNode.put("incomeType", "FROM_INDIVIDUAL");
        payload.put("operationTime", operationTime);
        payload.put("requestTime", operationTime);

        ArrayNode servicesNode = payload.putArray("services");
        double totalAmount = 0;
        for (DataForReceipt dataForReceipt : services) {
            double serviceTotalAmount = dataForReceipt.quantity() * dataForReceipt.amount();
            ObjectNode serviceNode = servicesNode.addObject();
            serviceNode.put("name", dataForReceipt.name());
            serviceNode.put("quantity", dataForReceipt.quantity());
            serviceNode.put("amount", BigDecimal.valueOf(serviceTotalAmount).setScale(2, RoundingMode.HALF_UP));
            totalAmount += serviceTotalAmount;
        }
        payload.put("totalAmount", BigDecimal.valueOf(totalAmount).setScale(2, RoundingMode.HALF_UP));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(clientConfig.getApiPath() + "/income"))
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .headers(getCommonHeaders())
                .header(clientConfig.getRefererHeader(), "https://lknpd.nalog.ru/sales/create")
                .header("Authorization", "Bearer " + token)
                .build();

        refreshTokenLock.readLock().lock();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();
            String body = response.body();
            if (statusCode != 200) {
                throw new ApiRequestException("add income error", statusCode, body);
            }
            String approvedReceiptUuid = MAPPER.readTree(body).path("approvedReceiptUuid").asText();
            final String jsonUrl = String.format("%s/receipt/%s/%s/json", clientConfig.getApiPath(), inn, approvedReceiptUuid);
            final String printUrl = String.format("%s/receipt/%s/%s/print", clientConfig.getApiPath(), inn, approvedReceiptUuid);
            return new Receipt(approvedReceiptUuid, jsonUrl, printUrl);
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e.getMessage());
        } finally {
            refreshTokenLock.readLock().unlock();
        }
    }

    public String returnReceipt(String uuid, String operationTime) {
        checkToken();

        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("comment", "Чек сформирован ошибочно");
        payload.put("partnerCode", "null");
        payload.put("operationTime", operationTime);
        payload.put("requestTime", operationTime);
        payload.put("receiptUuid", uuid);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(clientConfig.getApiPath() + "/cancel"))
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .headers(getCommonHeaders())
                .header(clientConfig.getRefererHeader(), "https://lknpd.nalog.ru/sales")
                .header("Authorization", "Bearer " + token)
                .build();

        refreshTokenLock.readLock().lock();

        //возврат
        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();
            String body = response.body();
            if (statusCode != 200) {
                throw new ApiRequestException("return error", statusCode, body);
            }
            String name = MAPPER.readTree(body).path("incomeInfo").path("name").asText();
            String uuidFromResponse = MAPPER.readTree(body).path("incomeInfo").path("approvedReceiptUuid").asText();
            String amount = MAPPER.readTree(body).path("incomeInfo").path("totalAmount").asText();

            return uuidFromResponse + "; наименование услуги: " + name + "; " + "стоимость услуги: " + amount + ";";
        } catch (Exception e) {
            throw new ApiException(e.getMessage());
        } finally {
            refreshTokenLock.readLock().unlock();
        }
    }

    private AuthenticationDto authenticate(String username, String password) {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.put("username", username);
        payload.put("password", password);
        payload.set("deviceInfo", getDeviceInfo(deviceId));

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(clientConfig.getApiPath() + "/auth/lkfl"))
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .headers(getCommonHeaders())
                .header(clientConfig.getRefererHeader(), "https://lknpd.nalog.ru/auth/login")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();
            String body = response.body();
            if (statusCode != 200) {
                throw new ApiRequestException("authentication error", statusCode, body);
            }
            return MAPPER.readValue(body, AuthenticationDto.class);
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e.getMessage());
        }
    }

    private RefreshTokenDto refreshToken() {
        ObjectNode payload = MAPPER.createObjectNode();
        payload.set("deviceInfo", getDeviceInfo(deviceId));
        payload.put("refreshToken", refreshToken);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(clientConfig.getApiPath() + "/auth/token"))
                .timeout(Duration.ofSeconds(15))
                .POST(HttpRequest.BodyPublishers.ofString(payload.toString()))
                .headers(getCommonHeaders())
                .header(clientConfig.getRefererHeader(), "https://lknpd.nalog.ru/sales")
                .build();

        try {
            HttpResponse<String> response = httpClient.send(request, HttpResponse.BodyHandlers.ofString(StandardCharsets.UTF_8));
            int statusCode = response.statusCode();
            String body = response.body();
            if (statusCode != 200) {
                throw new ApiRequestException("refresh token error", statusCode, body);
            }
            return MAPPER.readValue(body, RefreshTokenDto.class);
        } catch (IOException e) {
            throw new ApiException(e.getMessage());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new ApiException(e.getMessage());
        }
    }

    private void checkToken() {
        if (token == null) {
            throw new IllegalStateException("Token is null, user not authenticated");
        }
        OffsetDateTime offsetDateTime = OffsetDateTime.now().withOffsetSameInstant(ZoneOffset.UTC);
        OffsetDateTime tokenExpireDatetime = OffsetDateTime.parse(tokenExpireIn);
        if (offsetDateTime.isAfter(tokenExpireDatetime)) {
            refreshTokenLock.writeLock().lock();
            try {
                RefreshTokenDto refreshTokenDTO = refreshToken();
                this.refreshToken = refreshTokenDTO.getRefreshToken();
                this.token = refreshTokenDTO.getToken();
                this.tokenExpireIn = refreshTokenDTO.getTokenExpireIn();
                log.debug("Token refreshed");
            } finally {
                refreshTokenLock.writeLock().unlock();
            }
        } else {
            log.trace("Token is not expire");
        }
    }

    private String generateDeviceId(String prefix) {
        SecureRandom random = new SecureRandom();
        final String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijklmnopqrstuvwxyz";
        StringBuilder deviceInfoBuilder = new StringBuilder(prefix);
        IntStream.range(0, 21 - prefix.length()).forEach(v -> {
            int index = random.nextInt(chars.length());
            deviceInfoBuilder.append(chars.charAt(index));
        });
        return deviceInfoBuilder.toString();
    }

    private ObjectNode getDeviceInfo(String deviceId) {
        ObjectNode deviceInfoNode = MAPPER.createObjectNode();
        deviceInfoNode.put("appVersion", "1.0.0");
        deviceInfoNode.put("sourceType", "WEB");
        deviceInfoNode.put("sourceDeviceId", deviceId);
        ObjectNode metaDetailsNode = deviceInfoNode.putObject("metaDetails");
        metaDetailsNode.put("userAgent", "Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/130.0.0.0");
        return deviceInfoNode;
    }

    private String[] getCommonHeaders() {
        return new String[]{
                "Accept", "application/json, text/plain, */*",
                "Accept-Language", "ru,en;q=0.9",
                "Content-Type", "application/json"
        };
    }
}