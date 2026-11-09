package com.javarush.caesar.service;

import com.javarush.caesar.exception.CipherException;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;

public class FileManager {

    public String readFile(String filePath) throws CipherException {
        try {
            return Files.readString(Path.of(filePath), StandardCharsets.UTF_8);
        } catch (InvalidPathException e) {
            throw new CipherException("Некорректный путь: " + filePath, e);
        } catch (IOException e) {
            throw new CipherException("Не удалось прочитать файл " + filePath + ": " + e.getMessage(), e);
        }
    }

    public void writeFile(String content, String filePath) throws CipherException {
        try {
            Path path = Path.of(filePath);
            if (Files.isDirectory(path)) {
                throw new CipherException("Указана папка, а нужен файл: " + filePath);
            }
            Path parent = path.toAbsolutePath().getParent();
            if (parent != null) {
                Files.createDirectories(parent);
            }
            Files.writeString(path, content, StandardCharsets.UTF_8);
        } catch (InvalidPathException e) {
            throw new CipherException("Некорректный путь: " + filePath, e);
        } catch (IOException e) {
            throw new CipherException("Не удалось записать файл " + filePath + ": " + e.getMessage(), e);
        }
    }
}
