package application.exceptions;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.loolzaaa.nalog.mytax.client.exception.ApiException;
import ru.loolzaaa.nalog.mytax.client.exception.ApiRequestException;

@Slf4j
@ControllerAdvice
@RequiredArgsConstructor
public class ExceptionController {

    @ExceptionHandler(SqlException.class)
    public ExceptionResponse handleSqlException(SqlException exception) {
        log.info(exception.getMessage());
        return new ExceptionResponse(exception.getMessage());
    }

    @ExceptionHandler(MappingException.class)
    public ExceptionResponse handleMappingException(MappingException exception) {
        log.info(exception.getMessage());
        return new ExceptionResponse(exception.getMessage());
    }

    @ExceptionHandler(RedisConnectionException.class)
    public RedisConnectionException handleRedisConnectionException(RedisConnectionException exception) {
        log.info(exception.getMessage());
        return new RedisConnectionException(exception.getMessage());
    }

    @ExceptionHandler(ApiException.class)
    public ExceptionResponse handleApiException(ApiException exception) {
        log.info(exception.getMessage());
        return new ExceptionResponse(exception.getMessage());
    }

    @ExceptionHandler(ApiRequestException.class)
    public ExceptionResponse handleApiRequestException(ApiRequestException exception) {
        log.info(exception.getMessage());
        return new ExceptionResponse(exception.getMessage());
    }
}
