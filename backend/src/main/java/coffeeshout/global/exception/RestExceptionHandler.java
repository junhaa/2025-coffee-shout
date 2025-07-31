package coffeeshout.global.exception;

import coffeeshout.global.exception.custom.InvalidArgumentException;
import coffeeshout.global.exception.custom.InvalidStateException;
import coffeeshout.global.exception.custom.NotExistElementException;
import java.time.LocalDateTime;
import lombok.extern.slf4j.Slf4j;
import lombok.extern.slf4j.XSlf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ProblemDetail;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

@Slf4j
@RestControllerAdvice
public class RestExceptionHandler {

    @ExceptionHandler(Exception.class)
    public ProblemDetail handleException(Exception exception) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception, new ErrorCode() {
            @Override
            public String getCode() {
                return "INTERNAL_SERVER_ERROR";
            }

            @Override
            public String getMessage() {
                return "서버 오류가 발생했습니다.";
            }
        });
    }

    @ExceptionHandler(InvalidArgumentException.class)
    public ProblemDetail handleInvalidArgumentException(InvalidArgumentException exception) {
        log.warn("InvalidArgumentException: {}", exception.getMessage());
        return getProblemDetail(HttpStatus.BAD_REQUEST, exception, exception.getErrorCode());
    }

    @ExceptionHandler(InvalidStateException.class)
    public ProblemDetail handleInvalidStateException(InvalidStateException exception) {
        return getProblemDetail(HttpStatus.INTERNAL_SERVER_ERROR, exception, exception.getErrorCode());
    }

    @ExceptionHandler(NotExistElementException.class)
    public ProblemDetail handleNotExistElementException(NotExistElementException exception) {
        return getProblemDetail(HttpStatus.NOT_FOUND, exception, exception.getErrorCode());
    }

    private static ProblemDetail getProblemDetail(HttpStatus status, Exception exception, ErrorCode errorCode) {
        final ProblemDetail problemDetail = ProblemDetail.forStatusAndDetail(status, errorCode.getMessage());

        problemDetail.setProperty("errorCode", errorCode.getCode());
        problemDetail.setProperty("timestamp", LocalDateTime.now());
        problemDetail.setProperty("exception", exception.getClass().getSimpleName());

        return problemDetail;
    }

}

