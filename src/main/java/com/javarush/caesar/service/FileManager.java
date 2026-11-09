package com.javarush.caesar.service;

import com.javarush.caesar.exception.CipherException;

public class FileManager {

    public String readFile(String filePath) throws CipherException {
        // TODO прочитать файл в кодировке UTF-8 через Path и Files
        //  IOException завернуть в CipherException с понятным сообщением
        throw new UnsupportedOperationException("Метод readFile пока не написан");
    }

    public void writeFile(String content, String filePath) throws CipherException {
        // TODO записать текст в файл, исходный файл перезаписывать нельзя
        throw new UnsupportedOperationException("Метод writeFile пока не написан");
    }
}
