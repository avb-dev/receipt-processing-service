package application.data;

import application.exceptions.SqlException;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class PaymentRepository {

    private final JdbcTemplate jdbcTemplate;

    public int insertPayment(int paymentId, String uuid) {
        try {
            return jdbcTemplate.update(
                    "INSERT INTO receipts(payment_id, uuid) VALUES (?, ?)", paymentId, uuid
            );
        } catch (Exception exception) {
            throw new SqlException("Ошибка при добавлении данных в postgres", exception);
        }
    }
}
