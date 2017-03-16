package io.joshworks.snappy.rest;

/**
 * Created by Josh Gontijo on 3/15/17.
 */
public class ExceptionResponse  {

    private final int status;
    private final Object body;
    private MediaType mediaType;

    public ExceptionResponse(int status, Object body) {
        this.status = status;
        this.body = body;
    }

    public ExceptionResponse(int status, Object body, MediaType mediaType) {
        this.status = status;
        this.body = body;
        this.mediaType = mediaType;
    }

    public int getStatus() {
        return status;
    }

    public Object getBody() {
        return body;
    }

    public MediaType getMediaType() {
        return mediaType;
    }

    @Override
    public String toString() {
        return "status=" + status + "\nmessage='" + String.valueOf(body) + '\'';
    }
}
