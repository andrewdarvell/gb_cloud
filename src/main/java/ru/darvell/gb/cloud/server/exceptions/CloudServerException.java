package ru.darvell.gb.cloud.server.exceptions;

public class CloudServerException extends RuntimeException {

    public CloudServerException(String message, Throwable cause) {
        super(message, cause);
    }
}
