package com.javarush.caesar.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Проверки пользовательского ввода: ключа и путей к файлам.
 */
public class Validator {

    /**
     * @param key      ключ, введенный пользователем
     * @param alphabet алфавит шифра
     * @return true, если ключ от 0 до длины алфавита минус один
     */
    public boolean isValidKey(int key, char[] alphabet) {
        return key >= 0 && key < alphabet.length;
    }

    /**
     * Проверяет, что путь указывает на существующий файл.
     * {@link Files#isRegularFile} возвращает false и для несуществующего пути, и для папки.
     *
     * @param filePath путь к файлу
     * @return true, если это существующий обычный файл
     */
    public boolean isFileExists(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        return Files.isRegularFile(Path.of(filePath));
    }

    /**
     * Проверяет, указывают ли два пути на один и тот же файл (в том числе через относительный путь
     * или ссылку). Нужен, чтобы результат случайно не перезаписал исходный файл.
     *
     * @param firstPath  первый путь
     * @param secondPath второй путь
     * @return true, если это один и тот же файл
     */
    public boolean isSameFile(String firstPath, String secondPath) {
        Path first = Path.of(firstPath).toAbsolutePath().normalize();
        Path second = Path.of(secondPath).toAbsolutePath().normalize();
        if (first.equals(second)) {
            return true;
        }
        try {
            return Files.exists(second) && Files.isSameFile(first, second);
        } catch (IOException e) {
            return false;
        }
    }
}
