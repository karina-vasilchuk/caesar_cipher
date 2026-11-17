package com.javarush.caesar.exception;

/**
 * Ошибка работы программы с понятным пользователю сообщением:
 * неверный ключ, недоступный файл, символ вне алфавита и т.п.
 */
public class CipherException extends Exception {

    private static final long serialVersionUID = 1L;

    /**
     * @param message сообщение, которое увидит пользователь
     */
    public CipherException(String message) {
        super(message);
    }

    /**
     * @param message сообщение, которое увидит пользователь
     * @param cause   исходное исключение (например, {@link java.io.IOException})
     */
    public CipherException(String message, Throwable cause) {
        super(message, cause);
    }
}
