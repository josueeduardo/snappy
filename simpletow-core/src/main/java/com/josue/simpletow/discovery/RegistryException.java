package com.josue.simpletow.discovery;

/**
 * Created by Josue on 16/06/2016.
 */
public class RegistryException extends Exception {

    private final String code;
    private final int status;

    public RegistryException(String message, String code, int status) {
        super(message);
        this.code = code;
        this.status = status;
    }

    public String getCode() {
        return code;
    }

    public int getStatus() {
        return status;
    }
}
