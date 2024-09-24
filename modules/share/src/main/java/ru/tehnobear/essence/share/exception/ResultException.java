package ru.tehnobear.essence.share.exception;

import lombok.Getter;
import ru.tehnobear.essence.share.dto.Result;

@Getter
public class ResultException extends RuntimeException {
    private final Result result;
    public ResultException(Result result) {
        super("ResultException");
        this.result = result;
    }
}
