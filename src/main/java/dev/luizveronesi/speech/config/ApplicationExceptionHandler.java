package dev.luizveronesi.speech.config;

import org.apache.commons.lang3.exception.ExceptionUtils;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.ResponseBody;

import lombok.extern.slf4j.Slf4j;

@Slf4j
@Order(Ordered.LOWEST_PRECEDENCE)
@ControllerAdvice
public class ApplicationExceptionHandler {

    private final static String HANDLING_TYPE_MESSAGE = "Handling {} - Type: {}, Message: {}";

    @ExceptionHandler(Exception.class)
    @ResponseBody
    public ResponseEntity<ErrorResponse> handleException(Exception e) {
        log.info(HANDLING_TYPE_MESSAGE, "Exception", e.getClass().getName(), ExceptionUtils.getStackTrace(e));

        var err = ErrorResponse.builder()
                .description(e.getMessage())
                .details(ExceptionUtils.getStackTrace(e))
                .build();

        return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body(err);
    }
}
