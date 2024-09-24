package ru.tehnobear.essence.share.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;
import ru.tehnobear.essence.share.dto.Result;
import ru.tehnobear.essence.share.exception.ForbiddenException;
import ru.tehnobear.essence.share.exception.ResultException;
import ru.tehnobear.essence.share.exception.UnauthorizedException;

@Slf4j
@ControllerAdvice(annotations = ReportExceptionHandler.class)
public class ReportExceptionHandlerAdvice {

    @ExceptionHandler({UnauthorizedException.class})
    public ResponseEntity<Void> handleUnauthorized(UnauthorizedException exception) {
        return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
    }

    @ExceptionHandler({ForbiddenException.class})
    public ResponseEntity<Void> handleForbiddenException(ForbiddenException exception) {
        return ResponseEntity.status(HttpStatus.FORBIDDEN).build();
    }

    @ExceptionHandler({ResultException.class})
    public ResponseEntity<Result> handleResultException(ResultException exception) {
        return ResponseEntity.ok(exception.getResult());
    }

    @ExceptionHandler({Exception.class})
    public ResponseEntity<Result> handleException(Exception exception) {
        log.error(exception.getLocalizedMessage(), exception);
        return ResponseEntity.ok(Result.builder().build().addError(exception.getLocalizedMessage()));
    }
}
