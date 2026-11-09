package com.javarush.caesar.core;

import com.javarush.caesar.exception.CipherException;

import java.util.HashMap;
import java.util.Map;

public class Cipher {

    private final char[] alphabet;
    // символ -> его позиция в алфавите: поиск за O(1) вместо перебора массива
    private final Map<Character, Integer> charToIndex = new HashMap<>();

    public Cipher(char[] alphabet) {
        this.alphabet = alphabet;
        for (int i = 0; i < alphabet.length; i++) {
            charToIndex.put(alphabet[i], i);
        }
    }

    public String encrypt(String text, int shift) {
        StringBuilder result = new StringBuilder(text.length());
        for (int i = 0; i < text.length(); i++) {
            // в алфавите только строчные буквы, поэтому заглавные приводим к нижнему регистру
            char ch = Character.toLowerCase(text.charAt(i));
            Integer index = charToIndex.get(ch);
            if (index == null) {
                continue; // символа нет в алфавите - пропускаем
            }
            result.append(shiftChar(index, shift));
        }
        return result.toString();
    }

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

    // floorMod всегда возвращает неотрицательный остаток, поэтому сдвиг работает по кругу в обе стороны
    private char shiftChar(int index, int shift) {
        return alphabet[Math.floorMod(index + shift, alphabet.length)];
    }
}
