package com.javarush.caesar;

import com.javarush.caesar.core.BruteForce;
import com.javarush.caesar.core.Cipher;
import com.javarush.caesar.core.StatisticalAnalyzer;
import com.javarush.caesar.exception.CipherException;
import com.javarush.caesar.service.FileManager;
import com.javarush.caesar.service.Validator;

import java.util.Scanner;

public class MainApp {

    private static final char[] ALPHABET = {
            'а', 'б', 'в', 'г', 'д', 'е', 'ё', 'ж', 'з', 'и', 'й', 'к', 'л', 'м', 'н', 'о', 'п',
            'р', 'с', 'т', 'у', 'ф', 'х', 'ц', 'ч', 'ш', 'щ', 'ъ', 'ы', 'ь', 'э', 'ю', 'я',
            '.', ',', '«', '»', '"', '\'', ':', '!', '?', ' '
    };

    private final Scanner scanner = new Scanner(System.in);
    private final FileManager fileManager = new FileManager();
    private final Validator validator = new Validator();
    private final Cipher cipher = new Cipher(ALPHABET);
    private final BruteForce bruteForce = new BruteForce();
    private final StatisticalAnalyzer statisticalAnalyzer = new StatisticalAnalyzer();

    public static void main(String[] args) {
        new MainApp().run();
    }

    private void run() {
        System.out.println("Шифр Цезаря: шифрование и расшифровка файлов");
        while (true) {
            printMenu();
            String choice = ask("Выберите действие: ");
            try {
                switch (choice) {
                    case "1":
                        encryptFile();
                        break;
                    case "2":
                        decryptFile();
                        break;
                    case "3":
                        bruteForceFile();
                        break;
                    case "4":
                        statisticalFile();
                        break;
                    case "0":
                    case "exit":
                        System.out.println("До свидания!");
                        return;
                    default:
                        System.out.println("Нет такого пункта: " + choice);
                }
            } catch (CipherException e) {
                System.out.println("Ошибка: " + e.getMessage());
            }
        }
    }

    private void printMenu() {
        System.out.println();
        System.out.println("1. Зашифровать файл");
        System.out.println("2. Расшифровать файл с ключом");
        System.out.println("3. Взломать перебором ключей");
        System.out.println("4. Взломать статистическим анализом");
        System.out.println("0. Выход (или exit)");
    }

    private void encryptFile() throws CipherException {
        String source = askExistingFile("Исходный файл: ");
        String destination = askDestination(source);
        int key = askKey();
        String text = fileManager.readFile(source);
        fileManager.writeFile(cipher.encrypt(text, key), destination);
        System.out.println("Готово: зашифрованный текст записан в " + destination);
    }

    private void decryptFile() throws CipherException {
        String source = askExistingFile("Зашифрованный файл: ");
        String destination = askDestination(source);
        int key = askKey();
        String text = fileManager.readFile(source);
        fileManager.writeFile(cipher.decrypt(text, key), destination);
        System.out.println("Готово: расшифрованный текст записан в " + destination);
    }

    private void bruteForceFile() throws CipherException {
        String source = askExistingFile("Зашифрованный файл: ");
        String destination = askDestination(source);
        String text = fileManager.readFile(source);
        String result = bruteForce.decryptByBruteForce(text, ALPHABET);
        fileManager.writeFile(result, destination);
        System.out.println("Готово: найден ключ " + bruteForce.getLastKey()
                + ", результат записан в " + destination);
    }

    private void statisticalFile() throws CipherException {
        String source = askExistingFile("Зашифрованный файл: ");
        String sample = askExistingFile("Файл-образец (незашифрованный текст того же автора): ");
        String destination = askDestination(source);
        String text = fileManager.readFile(source);
        String sampleText = fileManager.readFile(sample);
        String result = statisticalAnalyzer.decryptByStatistics(text, sampleText, ALPHABET);
        fileManager.writeFile(result, destination);
        System.out.println("Готово: найден ключ " + statisticalAnalyzer.getLastKey()
                + ", результат записан в " + destination);
    }

    private String askExistingFile(String prompt) throws CipherException {
        String path = ask(prompt);
        if (!validator.isFileExists(path)) {
            throw new CipherException("Файл не найден или это папка: " + path);
        }
        return path;
    }

    private String askDestination(String source) throws CipherException {
        String path = ask("Файл для результата: ");
        if (path.isEmpty()) {
            throw new CipherException("Не указан файл для результата");
        }
        if (validator.isSameFile(source, path)) {
            throw new CipherException("Нельзя записывать результат в исходный файл");
        }
        return path;
    }

    private int askKey() throws CipherException {
        String input = ask("Ключ (от 0 до " + (ALPHABET.length - 1) + "): ");
        int key;
        try {
            key = Integer.parseInt(input);
        } catch (NumberFormatException e) {
            throw new CipherException("Ключ должен быть целым числом: " + input);
        }
        if (!validator.isValidKey(key, ALPHABET)) {
            throw new CipherException("Ключ должен быть от 0 до " + (ALPHABET.length - 1));
        }
        return key;
    }

    private String ask(String prompt) {
        System.out.print(prompt);
        return scanner.nextLine().trim();
    }
}
