package ru.clouddonate.cloudpayments.util;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class FileUtil {

    public static String readInputStream(InputStream inputStream) {
        StringBuilder stringBuilder = new StringBuilder();
        try {
            String line;
            BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
            while ((line = bufferedReader.readLine()) != null) {
                stringBuilder.append(line).append('\n');
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
        return stringBuilder.toString();
    }

    public static List<String> readFile(String filePath) {
        ArrayList<String> lines = new ArrayList<>();
        try (BufferedReader reader = new BufferedReader(new FileReader(filePath));) {
            String line;
            while ((line = reader.readLine()) != null) {
                lines.add(line);
            }
        } catch (IOException e) {
            System.err.println("Ошибка при чтении файла: " + e.getMessage());
        }
        return lines;
    }

    public static void writeFile(String filePath, List<String> lines) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath));) {
            for (String line : lines) {
                writer.write(line);
                writer.newLine();
            }
        } catch (IOException e) {
            System.err.println("Ошибка при записи файла: " + e.getMessage());
        }
    }

    public static void appendToFile(String filePath, String text) {
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(filePath, true));) {
            writer.write(text);
            writer.newLine();
        } catch (IOException e) {
            System.err.println("Ошибка при добавлении текста в файл: " + e.getMessage());
        }
    }

    public static List<String> searchInFile(String filePath, String searchText) {
        ArrayList<String> result = new ArrayList<>();
        List<String> lines = FileUtil.readFile(filePath);
        for (String line : lines) {
            if (!line.contains(searchText)) continue;
            result.add(line);
        }
        return result;
    }

    public static void removeLines(String filePath, String textToRemove) {
        List<String> lines = FileUtil.readFile(filePath);
        lines.removeIf(line -> line.contains(textToRemove));
        FileUtil.writeFile(filePath, lines);
    }
}
