package com.javarush.caesar.core;

import com.javarush.caesar.exception.CipherException;

public class StatisticalAnalyzer {

    private int lastKey = -1;

    /**
     * Подбирает ключ по статистике: считает частоты символов в образце (текст того же автора)
     * и выбирает ключ, при котором частоты расшифровки ближе всего к частотам образца.
     */
    public String decryptByStatistics(String encryptedText, String sampleText, char[] alphabet)
            throws CipherException {
        Cipher cipher = new Cipher(alphabet);
        // encrypt с ключом 0 оставляет только символы алфавита в нижнем регистре
        double[] expected = frequencies(cipher.encrypt(sampleText, 0), alphabet);
        if (expected == null) {
            throw new CipherException("В файле-образце нет ни одного символа алфавита");
        }

        double bestDistance = Double.MAX_VALUE;
        String bestText = encryptedText;
        int bestKey = 0;
        for (int key = 0; key < alphabet.length; key++) {
            String candidate = cipher.decrypt(encryptedText, key);
            double[] actual = frequencies(candidate, alphabet);
            if (actual == null) {
                break; // пустой шифротекст: подбирать нечего
            }
            double distance = 0;
            for (int i = 0; i < alphabet.length; i++) {
                distance += Math.pow(actual[i] - expected[i], 2);
            }
            if (distance < bestDistance) {
                bestDistance = distance;
                bestText = candidate;
                bestKey = key;
            }
        }
        lastKey = bestKey;
        return bestText;
    }

    public int getLastKey() {
        return lastKey;
    }

    // доля каждого символа алфавита в тексте; null, если символов алфавита нет
    private double[] frequencies(String text, char[] alphabet) {
        int[] counts = new int[alphabet.length];
        int total = 0;
        for (int i = 0; i < text.length(); i++) {
            int index = indexOf(alphabet, text.charAt(i));
            if (index >= 0) {
                counts[index]++;
                total++;
            }
        }
        if (total == 0) {
            return null;
        }
        double[] result = new double[alphabet.length];
        for (int i = 0; i < alphabet.length; i++) {
            result[i] = (double) counts[i] / total;
        }
        return result;
    }

    private int indexOf(char[] alphabet, char ch) {
        for (int i = 0; i < alphabet.length; i++) {
            if (alphabet[i] == ch) {
                return i;
            }
        }
        return -1;
    }
}
