package application;

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Setter
@NoArgsConstructor
@AllArgsConstructor
@Getter
public class DataForReceipt {

    @JsonProperty("payment_id")
    private int paymentId;

    private int quantity;
    private int amount;
    private String name;
    private String timestamp;

    @Override
    public String toString() {
        return "DataForReceipt{" +
                "paymentId=" + paymentId +
                ", quantity=" + quantity +
                ", amount=" + amount +
                ", name='" + name + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}
