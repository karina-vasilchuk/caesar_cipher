package com.javarush.caesar.service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class Validator {

    public boolean isValidKey(int key, char[] alphabet) {
        return key >= 0 && key < alphabet.length;
    }

    public boolean isFileExists(String filePath) {
        if (filePath == null || filePath.isBlank()) {
            return false;
        }
        // isRegularFile вернет false и для несуществующего пути, и для папки
        return Files.isRegularFile(Path.of(filePath));
    }

    // нужен, чтобы результат случайно не перезаписал исходный файл
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
