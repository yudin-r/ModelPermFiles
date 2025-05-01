package Models;

import java.io.*;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

public class FileManager {
    private static final String FILES_DIR = "files";
    private static final String STORAGE_FILE = "Storage.txt";
    private static final Map<String, FileInfo> fileData = new HashMap<>();

    public static void createDir() {
        File directory = new File(FILES_DIR);
        if (!directory.exists()) {
            directory.mkdir();
        }
    }

    public static void loadStorage() {
        File storageFile = new File(STORAGE_FILE);
        if (!storageFile.exists()) {
            return;
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(storageFile))) {
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\\|");
                if (parts.length == 3) {
                    String filename = parts[0];
                    String owner = parts[1];
                    int permissions = Integer.parseInt(parts[2]);
                    fileData.put(filename, new FileInfo(owner, permissions));
                }
            }
        } catch (IOException e) {
            System.out.println("Ошибка при загрузке данных: " + e.getMessage());
        }
    }

    public static void saveStorage() {
        File storageFile = new File(STORAGE_FILE);

        try (BufferedWriter writer = new BufferedWriter(new FileWriter(storageFile))) {
            for (Map.Entry<String, FileInfo> entry : fileData.entrySet()) {
                String filename = entry.getKey();
                FileInfo fileInfo = entry.getValue();
                writer.write(filename + "|" + fileInfo.getOwner() + "|" + fileInfo.getPerm());
                writer.newLine();
            }
        } catch (IOException e) {
            System.out.println("Ошибка при сохранении данных: " + e.getMessage());
        }
    }

    public static void createFile(String filename, String owner, int othersPerm) {
        if (fileData.containsKey(filename)) {
            System.out.println("Файл уже существует.");
            return;
        }

        File file = new File(FILES_DIR, filename);
        try {
            boolean created = file.createNewFile();
            if (!created) {
                System.out.println("Не удалось создать файл.");
                return;
            }
        } catch (Exception e) {
            System.out.println("Ошибка при создании файла: " + e.getMessage());
            return;
        }

        int perm = 70 + (othersPerm % 10);
        fileData.put(filename, new FileInfo(owner, perm));
        System.out.println("Файл '" + filename + "' успешно создан.");
    }

    public static void deleteFile(String filename, User user) {
        FileInfo fileInfo = fileData.get(filename);
        if (fileInfo == null) {
            System.out.println("Файл не найден.");
            return;
        }

        if (!checkAccess(fileInfo, user, 2)) {
            System.out.println("У вас нет прав на удаление файла.");
            return;
        }

        File file = new File(FILES_DIR, filename);
        if (file.delete()) {
            fileData.remove(filename);
            System.out.println("Файл '" + filename + "' успешно удален.");
        } else {
            System.out.println("Не удалось удалить файл.");
        }
    }

    public static void listFiles() {
        if (fileData.isEmpty()) {
            System.out.println("Нет файлов.");
            return;
        }

        SimpleDateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm");

        System.out.println("Права\tВладелец\tРазмер\tПоследнее изменение\tИмя файла");
        for (Map.Entry<String, FileInfo> entry : fileData.entrySet()) {
            String filename = entry.getKey();
            FileInfo fileInfo = entry.getValue();

            File file = new File(FILES_DIR, filename);
            int perm = fileInfo.getPerm();
            long size = file.length();

            Date lastModified = new Date(file.lastModified());
            String modificationDate = dateFormat.format(lastModified);

            System.out.printf("%d\t%s\t%d\t%s\t%s%n",
                    perm,
                    fileInfo.getOwner(),
                    size,
                    modificationDate,
                    filename);
        }
    }

    public static void readFile(String filename, User user) {
        FileInfo fileInfo = fileData.get(filename);
        if (fileInfo == null) {
            System.out.println("Файл не найден.");
            return;
        }

        if (!checkAccess(fileInfo, user, 4)) {
            System.out.println("У вас нет прав на чтение файла.");
            return;
        }

        File file = new File(FILES_DIR, filename);
        try (BufferedReader reader = new BufferedReader(new FileReader(file))) {
            System.out.println("Содержимое файла '" + filename + "':");
            String line;
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
        } catch (IOException e) {
            System.out.println("Ошибка при чтении файла: " + e.getMessage());
        }
    }

    public static void writeFile(String filename, String content, boolean append, User user) {
        FileInfo fileInfo = fileData.get(filename);
        if (fileInfo == null) {
            System.out.println("Файл не найден.");
            return;
        }

        if (!checkAccess(fileInfo, user, 2)) {
            System.out.println("У вас нет прав на запись в файл.");
            return;
        }

        File file = new File(FILES_DIR, filename);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(file, append))) {
            writer.write(content);
            writer.newLine();
            System.out.println("Текст успешно записан в файл.");
        } catch (IOException e) {
            System.out.println("Ошибка при записи в файл: " + e.getMessage());
        }
    }

    private static boolean checkAccess(FileInfo fileInfo, User user, int requiredPerm) {
        if (user.isRoot()) {
            return true;
        }

        int perm = fileInfo.getPerm();
        int userPerm = perm / 10;
        int othersPerm = perm % 10;

        int effectivePerm = user.getUsername().equals(fileInfo.getOwner()) ? userPerm : othersPerm;

        return (effectivePerm & requiredPerm) == requiredPerm;
    }

    public static void changeOwner(String filename, String newOwner, User user) {
        FileInfo fileInfo = fileData.get(filename);
        if (fileInfo == null) {
            System.out.println("Файл не найден.");
            return;
        }

        if (!user.isRoot() && !user.getUsername().equals(fileInfo.getOwner())) {
            System.out.println("У вас нет прав на смену владельца файла.");
            return;
        }

        fileInfo.setOwner(newOwner);
        System.out.println("Владелец файла '" + filename + "' успешно изменен на '" + newOwner + "'.");
    }

    public static void changePerm(String filename, int newPerm, User user) {
        FileInfo fileInfo = fileData.get(filename);
        if (fileInfo == null) {
            System.out.println("Файл не найден.");
            return;
        }

        if (!user.isRoot() && !user.getUsername().equals(fileInfo.getOwner())) {
            System.out.println("У вас нет прав на изменение прав доступа к файлу.");
            return;
        }

        if (!user.isRoot()) {
            int currentPerm = fileInfo.getPerm();
            int ownerPerm = currentPerm / 10;
            newPerm = ownerPerm * 10 + (newPerm % 10);
        }

        fileInfo.setPerm(newPerm);
        System.out.println("Права доступа к файлу '" + filename + "' успешно изменены на " + newPerm + ".");
    }
}