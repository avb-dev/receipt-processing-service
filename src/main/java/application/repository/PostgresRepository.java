package application.repository;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.OffsetDateTime;
import java.util.List;

@Slf4j
@Repository
@RequiredArgsConstructor
public class PostgresRepository {

    private final JdbcTemplate jdbcTemplate;

    public int insertPayment(int paymentId, String uuid, OffsetDateTime timestamp) {
        int rows = jdbcTemplate.update(
                "INSERT INTO receipts(payment_id, uuid, \"timestamp\") VALUES (?, ?, ?)", paymentId, uuid, timestamp
        );
        log.info("Успешное добавление в БД Postgres чека: {}; paymentId: {}; timestamp: {}", uuid, paymentId, timestamp);
        return rows;
    }

    public int updateRefund(String uuid) {
        int rows = jdbcTemplate.update(
                "UPDATE receipts SET is_refunded = true WHERE uuid = ?", uuid
        );
        log.info("Статус is_refunded обновлен для чека uuid: {}", uuid);
        return rows;
    }

    public boolean isWrittenLocally(OffsetDateTime timestamp) {
        List<String> uuids = jdbcTemplate.query(
                "SELECT receipts.uuid FROM receipts WHERE \"timestamp\" = ?",
                (rs, rowNum) -> rs.getString("uuid"),
                timestamp);
        return !uuids.isEmpty();
    }
}
