package com.javarush.caesar.service;

import com.javarush.caesar.exception.CipherException;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.CharacterCodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.InvalidPathException;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;

/**
 * Чтение и запись текстовых файлов в UTF-8.
 * Файлы обрабатываются блоками, поэтому размер файла не ограничен объемом памяти.
 */
public class FileManager {

    /** Размер блока в символах: столько текста одновременно находится в памяти. */
    private static final int BLOCK_SIZE = 8192;

    /**
     * Преобразование одного блока текста, например шифрование или расшифровка с ключом.
     */
    @FunctionalInterface
    public interface BlockTransformer {
        String transform(String block) throws CipherException;
    }

    /**
     * Читает исходный файл блоками, преобразует каждый блок и сразу записывает результат.
     * Результат сначала пишется во временный файл и только в конце переносится на место,
     * поэтому при ошибке посередине не остается наполовину записанного файла.
     *
     * @param sourcePath      исходный файл
     * @param destinationPath файл для результата (папки создаются при необходимости)
     * @param transformer     что сделать с каждым блоком
     * @throws CipherException если файл не читается, не записывается или блок не удалось преобразовать
     */
    public void transformFile(String sourcePath, String destinationPath, BlockTransformer transformer)
            throws CipherException {
        Path source = toPath(sourcePath);
        Path destination = toPath(destinationPath);
        if (Files.isDirectory(destination)) {
            throw new CipherException("Указана папка, а нужен файл: " + destinationPath);
        }
        Path temp = null;
        try {
            Path parent = destination.toAbsolutePath().getParent();
            Files.createDirectories(parent);
            temp = Files.createTempFile(parent, "caesar-", ".tmp");
            long processed = 0;
            try (BufferedReader reader = Files.newBufferedReader(source, StandardCharsets.UTF_8);
                 BufferedWriter writer = Files.newBufferedWriter(temp, StandardCharsets.UTF_8)) {
                char[] buffer = new char[BLOCK_SIZE];
                int read;
                while ((read = reader.read(buffer)) != -1) {
                    try {
                        writer.write(transformer.transform(new String(buffer, 0, read)));
                    } catch (CipherException e) {
                        throw new CipherException("Ошибка в блоке, начиная с символа " + processed
                                + ": " + e.getMessage(), e);
                    }
                    processed += read;
                }
            }
            Files.move(temp, destination, StandardCopyOption.REPLACE_EXISTING);
            temp = null;
        } catch (CharacterCodingException e) {
            throw notUtf8(sourcePath, e);
        } catch (IOException e) {
            throw new CipherException("Ошибка при обработке " + sourcePath + " -> " + destinationPath
                    + ": " + e.getMessage(), e);
        } finally {
            deleteQuietly(temp);
        }
    }

    /**
     * Читает только начало файла. Для подбора ключа весь текст не нужен:
     * статистики первых тысяч символов достаточно, а большой файл не загружается в память.
     *
     * @param filePath путь к файлу
     * @param maxChars сколько символов прочитать максимум
     * @return начало файла (или весь файл, если он короче)
     * @throws CipherException если файл не удалось прочитать
     */
    public String readBeginning(String filePath, int maxChars) throws CipherException {
        Path path = toPath(filePath);
        try (BufferedReader reader = Files.newBufferedReader(path, StandardCharsets.UTF_8)) {
            char[] buffer = new char[maxChars];
            int total = 0;
            int read;
            while (total < maxChars && (read = reader.read(buffer, total, maxChars - total)) != -1) {
                total += read;
            }
            return new String(buffer, 0, total);
        } catch (CharacterCodingException e) {
            throw notUtf8(filePath, e);
        } catch (IOException e) {
            throw new CipherException("Не удалось прочитать файл " + filePath + ": " + e.getMessage(), e);
        }
    }

    private CipherException notUtf8(String filePath, CharacterCodingException e) {
        return new CipherException("Файл " + filePath + " не в кодировке UTF-8. "
                + "Пересохраните его в UTF-8 (например, в IntelliJ: File > File Properties > File Encoding)", e);
    }

    private Path toPath(String filePath) throws CipherException {
        try {
            return Path.of(filePath);
        } catch (InvalidPathException e) {
            throw new CipherException("Некорректный путь: " + filePath, e);
        }
    }

    private void deleteQuietly(Path path) {
        if (path == null) {
            return;
        }
        try {
            Files.deleteIfExists(path);
        } catch (IOException ignored) {
            // временный файл не удалился - не критично, основная ошибка уже передана выше
        }
    }
}
