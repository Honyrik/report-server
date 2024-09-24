package ru.tehnobear.essence.share.dto;

import com.fasterxml.jackson.annotation.JsonValue;

public enum EMessage {
    ALL,
    WARNING,
    ERROR,
    INFO,
    NOTIFICATION,
    DEBUG,
    BLOCK,
    UNBLOCK;

    @Override
    @JsonValue
    public String toString() {
        return name().toLowerCase();
    }
}
