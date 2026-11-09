package com.javarush.caesar.core;

import com.javarush.caesar.exception.CipherException;

import java.util.Set;

public class BruteForce {

    // доли в обычном русском тексте: пробелов около 16%, знаков препинания около 3%
    private static final double EXPECTED_SPACE_SHARE = 0.16;
    private static final double EXPECTED_PUNCT_SHARE = 0.03;
    private static final String PUNCTUATION = ".,«»\"':!?";
    private static final String NEEDS_SPACE_AFTER = ",:!?";
    private static final int MAX_WORD_LENGTH = 20;
    private static final Set<String> COMMON_WORDS = Set.of(
            "и", "в", "не", "на", "что", "с", "а", "по", "как", "это",
            "к", "но", "он", "она", "я", "из", "за", "то", "у", "же", "так", "все");

    private int lastKey = -1;

    public String decryptByBruteForce(String encryptedText, char[] alphabet) throws CipherException {
        Cipher cipher = new Cipher(alphabet);
        double bestScore = Double.NEGATIVE_INFINITY;
        String bestText = encryptedText;
        int bestKey = 0;
        for (int key = 1; key < alphabet.length; key++) {
            String candidate = cipher.decrypt(encryptedText, key);
            double score = score(candidate);
            if (score > bestScore) {
                bestScore = score;
                bestText = candidate;
                bestKey = key;
            }
        }
        lastKey = bestKey;
        return bestText;
    }

    // ключ, найденный последним вызовом decryptByBruteForce (для вывода пользователю)
    public int getLastKey() {
        return lastKey;
    }

    // чем больше оценка, тем больше текст похож на русский
    private double score(String text) {
        if (text.isEmpty()) {
            return 0;
        }
        return -10 * frequencyDeviation(text)
                - punctuationWithoutSpaceShare(text)
                - longWordsShare(text)
                + commonWordsShare(text);
    }

    // идея из первой версии: насколько доли пробелов и знаков препинания далеки от нормы
    private double frequencyDeviation(String text) {
        int spaces = 0;
        int punct = 0;
        for (int i = 0; i < text.length(); i++) {
            char ch = text.charAt(i);
            if (ch == ' ') {
                spaces++;
            } else if (PUNCTUATION.indexOf(ch) >= 0) {
                punct++;
            }
        }
        double length = text.length();
        return Math.pow(spaces / length - EXPECTED_SPACE_SHARE, 2)
                + Math.pow(punct / length - EXPECTED_PUNCT_SHARE, 2);
    }

    // после запятой, двоеточия, ! и ? в нормальном тексте стоит пробел или конец текста
    private double punctuationWithoutSpaceShare(String text) {
        int total = 0;
        int bad = 0;
        for (int i = 0; i < text.length(); i++) {
            if (NEEDS_SPACE_AFTER.indexOf(text.charAt(i)) >= 0) {
                total++;
                boolean last = i == text.length() - 1;
                if (!last && text.charAt(i + 1) != ' ') {
                    bad++;
                }
            }
        }
        return total == 0 ? 0 : (double) bad / total;
    }

    // слов длиннее 20 букв в русском тексте почти не бывает
    private double longWordsShare(String text) {
        String[] words = text.trim().split(" +");
        int longWords = 0;
        for (String word : words) {
            if (word.length() > MAX_WORD_LENGTH) {
                longWords++;
            }
        }
        return (double) longWords / words.length;
    }

    // доля частых коротких слов: "и", "в", "не", "на" ...
    private double commonWordsShare(String text) {
        String[] words = text.trim().split(" +");
        int common = 0;
        for (String word : words) {
            if (COMMON_WORDS.contains(stripPunctuation(word))) {
                common++;
            }
        }
        return (double) common / words.length;
    }

    private String stripPunctuation(String word) {
        int start = 0;
        int end = word.length();
        while (start < end && PUNCTUATION.indexOf(word.charAt(start)) >= 0) {
            start++;
        }
        while (end > start && PUNCTUATION.indexOf(word.charAt(end - 1)) >= 0) {
            end--;
        }
        return word.substring(start, end);
    }
}
