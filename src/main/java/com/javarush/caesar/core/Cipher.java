package com.javarush.caesar.core;

import com.javarush.caesar.exception.CipherException;

public class Cipher {

    private final char[] alphabet;

    public Cipher(char[] alphabet) {
        this.alphabet = alphabet;
    }

    public String encrypt(String text, int shift) {
        // TODO сдвинуть каждый символ алфавита на shift позиций вперед по кругу
        //  символ, которого нет в алфавите, пропускаем
        //  позицию символа ищем быстрее, чем перебором всего массива
        throw new UnsupportedOperationException("Метод encrypt пока не написан");
    }

    public String decrypt(String encryptedText, int shift) throws CipherException {
        // TODO сдвинуть каждый символ на shift позиций назад по кругу
        //  символ, которого нет в алфавите, - ошибка: бросаем CipherException
        throw new UnsupportedOperationException("Метод decrypt пока не написан");
    }
}
