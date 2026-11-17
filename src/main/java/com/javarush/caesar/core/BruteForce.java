package com.javarush.caesar.core;

import com.javarush.caesar.exception.CipherException;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Взлом шифра Цезаря перебором: расшифровывает текст всеми ключами
 * и выбирает вариант, больше всего похожий на русский текст.
 */
public class BruteForce {

    /** Доля пробелов в обычном русском тексте. */
    private static final double EXPECTED_SPACE_SHARE = 0.16;
    /** Доля знаков препинания в обычном русском тексте. */
    private static final double EXPECTED_PUNCT_SHARE = 0.03;
    private static final String PUNCTUATION = ".,«»\"':!?";
    /** Знаки, после которых идет пробел, а перед которыми пробела нет. */
    private static final String NEEDS_SPACE_AFTER = ".,:!?»";
    /** Слов длиннее этого в русском тексте почти не бывает. */
    private static final int MAX_WORD_LENGTH = 20;
    private static final String VOWELS = "аеёиоуыэюя";
    /** Частые короткие слова: чем их больше, тем вероятнее, что текст русский. */
    private static final Set<String> COMMON_WORDS = Set.of(
            "и", "в", "не", "на", "что", "с", "а", "по", "как", "это",
            "к", "но", "он", "она", "я", "из", "за", "то", "у", "же", "так", "все");

    /**
     * Примерные частоты символов в русском тексте: буквы - по частотному словарю,
     * доли считаются от всего текста вместе с пробелами и знаками.
     */
    private static final Map<Character, Double> RUSSIAN_FREQUENCIES = new HashMap<>();
    /** Частота для символов, которых нет в таблице (кавычки, апостроф). */
    private static final double RARE_CHAR_FREQUENCY = 0.0005;
    /** Штраф за пробел в начале или в конце текста. */
    private static final double EDGE_SPACE_PENALTY = 0.5;
    /** Если в расшифровке меньше слов, оценка ненадежна. */
    private static final int MIN_WORDS_FOR_RELIABLE_RESULT = 4;

    static {
        String letters = "оеаинтсрвлкмдпуяыьгзбчйхжшюцщэфъё";
        double[] shares = {
                .1097, .0845, .0801, .0735, .0670, .0626, .0547, .0473, .0454, .0440, .0349,
                .0321, .0298, .0281, .0262, .0201, .0190, .0174, .0170, .0165, .0159, .0144,
                .0121, .0097, .0094, .0073, .0064, .0048, .0036, .0032, .0026, .0004, .0004};
        double lettersShare = 1 - EXPECTED_SPACE_SHARE - EXPECTED_PUNCT_SHARE;
        for (int i = 0; i < letters.length(); i++) {
            RUSSIAN_FREQUENCIES.put(letters.charAt(i), shares[i] * lettersShare);
        }
        RUSSIAN_FREQUENCIES.put(' ', EXPECTED_SPACE_SHARE);
        RUSSIAN_FREQUENCIES.put('.', 0.010);
        RUSSIAN_FREQUENCIES.put(',', 0.015);
        RUSSIAN_FREQUENCIES.put('!', 0.002);
        RUSSIAN_FREQUENCIES.put('?', 0.002);
        RUSSIAN_FREQUENCIES.put(':', 0.001);
    }

    /** Ключ, найденный последним вызовом {@link #decryptByBruteForce}; -1, если вызова еще не было. */
    private int lastKey = -1;

    /**
     * Вариант расшифровки.
     *
     * @param key  ключ
     * @param text текст, расшифрованный этим ключом
     */
    public record Candidate(int key, String text) {
    }

    /**
     * Подбирает ключ перебором и возвращает самый правдоподобный вариант расшифровки.
     * Найденный ключ можно получить через {@link #getLastKey()}.
     *
     * @param encryptedText зашифрованный текст
     * @param alphabet      алфавит шифра
     * @return расшифрованный текст
     * @throws CipherException если в тексте есть символ вне алфавита
     */
    public String decryptByBruteForce(String encryptedText, char[] alphabet) throws CipherException {
        Candidate best = findBestCandidates(encryptedText, alphabet, 1).get(0);
        lastKey = best.key();
        return best.text();
    }

    /**
     * Перебирает все ключи и возвращает несколько лучших вариантов - от самого похожего
     * на русский текст к менее похожим. На коротком тексте оценка может ошибиться,
     * поэтому полезно показать пользователю несколько вариантов.
     *
     * @param encryptedText зашифрованный текст
     * @param alphabet      алфавит шифра
     * @param count         сколько вариантов вернуть
     * @return лучшие варианты, отсортированные по убыванию оценки
     * @throws CipherException если в тексте есть символ вне алфавита
     */
    public List<Candidate> findBestCandidates(String encryptedText, char[] alphabet, int count)
            throws CipherException {
        Cipher cipher = new Cipher(alphabet);
        List<Candidate> candidates = new ArrayList<>();
        Map<Candidate, Double> scores = new HashMap<>();
        for (int key = 0; key < alphabet.length; key++) {
            Candidate candidate = new Candidate(key, cipher.decrypt(encryptedText, key));
            candidates.add(candidate);
            scores.put(candidate, score(candidate.text()));
        }
        candidates.sort(Comparator.comparingDouble((Candidate c) -> scores.get(c)).reversed());
        return candidates.subList(0, Math.min(count, candidates.size()));
    }

    /**
     * Проверяет, достаточно ли в тексте слов для надежной оценки.
     * Передавать нужно расшифрованный текст: в шифротексте пробелы тоже зашифрованы.
     *
     * @param decryptedText лучший вариант расшифровки
     * @return true, если слов мало и результат стоит показать пользователю для проверки
     */
    public boolean isTooShortForReliableResult(String decryptedText) {
        return decryptedText.trim().split(" +").length < MIN_WORDS_FOR_RELIABLE_RESULT;
    }

    /**
     * @return ключ, найденный последним вызовом {@link #decryptByBruteForce}
     */
    public int getLastKey() {
        return lastKey;
    }

    /**
     * Оценивает, насколько текст похож на русский: чем больше оценка, тем больше похож.
     * Складывает несколько признаков, каждый описан в своем методе.
     */
    private double score(String text) {
        if (text.isEmpty()) {
            return 0;
        }
        return -10 * frequencyDeviation(text)
                - badPunctuationShare(text)
                - longWordsShare(text)
                + commonWordsShare(text)
                + wordsWithVowelShare(text)
                + charsLikelihood(text)
                - edgeSpacePenalty(text);
    }

    /**
     * @return насколько доли пробелов и знаков препинания далеки от обычных для русского текста
     */
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

    /**
     * После . , : ! ? » в нормальном тексте идет пробел, другой знак (?! или .») или конец текста,
     * а перед ними не бывает пробела.
     *
     * @return доля знаков, для которых это правило нарушено
     */
    private double badPunctuationShare(String text) {
        int total = 0;
        int bad = 0;
        for (int i = 0; i < text.length(); i++) {
            if (NEEDS_SPACE_AFTER.indexOf(text.charAt(i)) >= 0) {
                total++;
                boolean letterAfter = i < text.length() - 1
                        && text.charAt(i + 1) != ' '
                        && PUNCTUATION.indexOf(text.charAt(i + 1)) < 0;
                boolean spaceBefore = i > 0 && text.charAt(i - 1) == ' ';
                if (letterAfter || spaceBefore) {
                    bad++;
                }
            }
        }
        return total == 0 ? 0 : (double) bad / total;
    }

    /**
     * @return доля слов длиннее {@link #MAX_WORD_LENGTH} символов
     */
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

    /**
     * @return доля частых коротких слов ("и", "в", "не", "на" ...) среди всех слов
     */
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

    /**
     * В русском почти каждое слово содержит гласную; "чшрймъ" - явно не слово.
     *
     * @return доля слов, в которых есть гласная
     */
    private double wordsWithVowelShare(String text) {
        String[] words = text.trim().split(" +");
        int withVowel = 0;
        for (String word : words) {
            for (int i = 0; i < word.length(); i++) {
                if (VOWELS.indexOf(word.charAt(i)) >= 0) {
                    withVowel++;
                    break;
                }
            }
        }
        return (double) withVowel / words.length;
    }

    /**
     * Текст обычно не начинается и не заканчивается пробелом
     * (перевод строки в конце файла при шифровании выбрасывается, пробел на его месте не появляется).
     *
     * @return штраф за пробелы по краям текста
     */
    private double edgeSpacePenalty(String text) {
        double penalty = 0;
        if (text.charAt(0) == ' ') {
            penalty += EDGE_SPACE_PENALTY;
        }
        if (text.charAt(text.length() - 1) == ' ') {
            penalty += EDGE_SPACE_PENALTY;
        }
        return penalty;
    }

    /**
     * Средний логарифм частоты символа. В русском тексте чаще встречаются частые буквы (о, е, а),
     * в неверной расшифровке - редкие (ъ, ё, ф, кавычки). Работает даже на коротких фразах.
     *
     * @return чем больше, тем больше текст похож на русский
     */
    private double charsLikelihood(String text) {
        double sum = 0;
        for (int i = 0; i < text.length(); i++) {
            sum += Math.log(RUSSIAN_FREQUENCIES.getOrDefault(text.charAt(i), RARE_CHAR_FREQUENCY));
        }
        return sum / text.length();
    }

    /**
     * @return слово без знаков препинания в начале и в конце
     */
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
