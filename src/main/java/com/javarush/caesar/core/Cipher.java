package com.javarush.caesar.core;

import com.javarush.caesar.exception.CipherException;

import java.util.HashMap;
import java.util.Map;

/**
 * Шифр Цезаря: каждый символ сдвигается на заданное число позиций по кругу алфавита.
 */
public class Cipher {

    private final char[] alphabet;

    /** Символ -> его позиция в алфавите: поиск за O(1) вместо перебора массива. */
    private final Map<Character, Integer> charToIndex = new HashMap<>();

    /**
     * @param alphabet алфавит шифра; порядок символов задает направление сдвига
     */
    public Cipher(char[] alphabet) {
        this.alphabet = alphabet;
        for (int i = 0; i < alphabet.length; i++) {
            charToIndex.put(alphabet[i], i);
        }
    }

    /**
     * Шифрует текст сдвигом вперед.
     * В алфавите только строчные буквы, поэтому заглавные сначала приводятся к нижнему регистру.
     * Символы, которых нет в алфавите (например, переводы строк), пропускаются.
     *
     * @param text  исходный текст
     * @param shift ключ - на сколько позиций сдвигать
     * @return зашифрованный текст
     */
    public String encrypt(String text, int shift) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            char ch = Character.toLowerCase(text.charAt(i));
            Integer index = charToIndex.get(ch);
            if (index == null) {
                continue;
            }
            result.append(shiftChar(index, shift));
        }
        return result.toString();
    }

    /**
     * Расшифровывает текст сдвигом назад.
     *
     * @param encryptedText зашифрованный текст
     * @param shift         ключ, которым текст был зашифрован
     * @return расшифрованный текст
     * @throws CipherException если в тексте есть символ вне алфавита: такой текст не мог получиться при шифровании
     */
    public String decrypt(String encryptedText, int shift) throws CipherException {
        StringBuilder result = new StringBuilder(encryptedText.length());
        for (int i = 0; i < encryptedText.length(); i++) {
            char ch = encryptedText.charAt(i);
            Integer index = charToIndex.get(ch);
            if (index == null) {
                throw new CipherException("Символ вне алфавита в позиции " + i + ": '" + ch + "'");
            }
            result.append(shiftChar(index, -shift));
        }
        return result.toString();
    }

    /**
     * Сдвигает символ по кругу алфавита.
     * {@link Math#floorMod} всегда возвращает неотрицательный остаток,
     * поэтому сдвиг работает в обе стороны.
     *
     * @param index позиция символа в алфавите
     * @param shift сдвиг: положительный - вперед, отрицательный - назад
     * @return символ алфавита после сдвига
     */
    private char shiftChar(int index, int shift) {
        return alphabet[Math.floorMod(index + shift, alphabet.length)];
    }
}
