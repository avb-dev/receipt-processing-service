package application.dto;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DataForReceiptDto {

    @JsonProperty("payment_id")
    private int paymentId;

    private int quantity;
    private int amount;
    private String name;
    private String timestamp;
    private String email;

    @Override
    public String toString() {
        return "DataForReceipt{" +
                "paymentId=" + paymentId +
                ", quantity=" + quantity +
                ", amount=" + amount +
                ", name='" + name + '\'' +
                ", timestamp='" + timestamp + '\'' +
                ", email='" + email + '\'' +
                '}';
    }

    public String toJsonAsString() {
        return String.format(
                "{\"payment_id\":%d,\"quantity\":%d,\"amount\":%d,\"name\":\"%s\",\"timestamp\":\"%s\",\"email\":\"%s\"}",
                paymentId, quantity, amount, name, timestamp, email
        );
    }
}
