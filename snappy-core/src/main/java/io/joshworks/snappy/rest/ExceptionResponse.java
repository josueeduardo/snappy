package io.joshworks.snappy.rest;

import java.io.Serializable;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class ExceptionResponse implements Serializable {

    private final int status;
    private final String message;

    public ExceptionResponse(int status, String message) {
        this.status = status;
        this.message = message;
    }

    public int getStatus() {
        return status;
    }

    public String getMessage() {
        return message;
    }

    @Override
    public String toString() {
        return "status=" + status + "\nmessage='" + message + '\'';
    }
}
