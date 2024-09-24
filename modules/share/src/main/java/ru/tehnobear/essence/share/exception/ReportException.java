package ru.tehnobear.essence.share.exception;

import org.slf4j.helpers.MessageFormatter;

import java.util.Arrays;
import java.util.List;

public class ReportException extends RuntimeException {
    public ReportException(String message) {
      super(message);
    }
    public ReportException(String message, Exception e) {
      super(message, e);
    }

    public static ReportException fromFormat(String message, Object... obj) {
        List<Object> objects = Arrays.asList(obj);
        if (objects.isEmpty()) {
            return new ReportException(message);
        }
        Object last = objects.get(objects.size()-1);
        Exception exception = null;
        if (last instanceof Throwable) {
            exception = (Exception) last;
            objects = objects.subList(0, objects.size()-1);
        }
        if (objects.isEmpty()) {
            return new ReportException(message, exception);
        }
        var msg = MessageFormatter.arrayFormat(message, objects.toArray());
        return exception == null ? new ReportException(msg.getMessage()) : new ReportException(msg.getMessage(), exception);
    }
}
