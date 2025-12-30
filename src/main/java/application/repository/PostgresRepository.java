package application.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostgresRepository {

    private final JdbcTemplate jdbcTemplate;

    public int insertPayment(int paymentId, String uuid) {
        int rows = jdbcTemplate.update(
                "INSERT INTO receipts(payment_id, uuid) VALUES (?, ?)", paymentId, uuid
        );
        log.info("Успешное добавление в БД Postgres чека: " + uuid + "; paymentId: " + paymentId);
        return rows;
    }
}
