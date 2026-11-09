package com.javarush.caesar.exception;

public class CipherException extends Exception {

    private static final long serialVersionUID = 1L;

    public CipherException(String message) {
        super(message);
    }

    public CipherException(String message, Throwable cause) {
        super(message, cause);
    }
}
