import Models.FileManager;
import Models.User;
import Utils.AuthSystem;

import java.util.Scanner;

public class Main {
    public static void main(String[] args) {
        Scanner scanner = new Scanner(System.in);
        AuthSystem authSystem = new AuthSystem();

        FileManager.createDir();
        FileManager.loadStorage();

        while (true) {
            System.out.print("Введите имя пользователя (или 'quit' для выхода): ");
            String username = scanner.nextLine();
            if (username.equalsIgnoreCase("quit")) {
                FileManager.saveStorage();
                System.out.println("Завершение программы.");
                break;
            }

            System.out.print("Введите пароль: ");
            String password = scanner.nextLine();

            User currentUser = authSystem.auth(username, password);
            if (currentUser == null) {
                System.out.println("Неверное имя пользователя или пароль. Попробуйте еще раз.");
                continue;
            }

            System.out.println("Добро пожаловать, " + currentUser.getUsername() + ".");
            System.out.println("Введите команду (help для справки).");

            while (true) {
                System.out.print(currentUser.getUsername() + "@console:~$ ");
                String input = scanner.nextLine().trim();
                if (input.isEmpty()) {
                    continue;
                }

                String[] cmdargs = input.split("\\s+");
                String command = cmdargs[0].toLowerCase();

                try {
                    switch (command) {
                        case "ls":
                            FileManager.listFiles();
                            break;

                        case "touch":
                            if (cmdargs.length < 3) {
                                System.out.println("Использование: touch <имя_файла> <права_для_остальных>");
                            } else {
                                String filename = cmdargs[1];
                                int othersPerm = Integer.parseInt(cmdargs[2]);
                                FileManager.createFile(filename, currentUser.getUsername(), othersPerm);
                            }
                            break;

                        case "rm":
                            if (cmdargs.length < 2) {
                                System.out.println("Использование: rm <имя_файла>");
                            } else {
                                FileManager.deleteFile(cmdargs[1], currentUser);
                            }
                            break;

                        case "cat":
                            if (cmdargs.length < 2) {
                                System.out.println("Использование: cat <имя_файла>");
                            } else {
                                FileManager.readFile(cmdargs[1], currentUser);
                            }
                            break;

                        case "echo":
                            handleEchoCommand(input, currentUser);
                            break;

                        case "help":
                            printHelp();
                            break;

                        case "exit":
                            System.out.println("Выход из аккаунта " + currentUser.getUsername() + ".");
                            break;

                        case "chown":
                            if (cmdargs.length < 3) {
                                System.out.println("Использование: chown <новый_владелец> <имя_файла>");
                            } else {
                                String newOwner = cmdargs[1];
                                String filename = cmdargs[2];
                                FileManager.changeOwner(filename, newOwner, currentUser);
                            }
                            break;

                        case "chmod":
                            if (cmdargs.length < 3) {
                                System.out.println("Использование: chmod <новые_права> <имя_файла> " +
                                        "(Владелец не может понизить себе права)");
                            } else {
                                try {
                                    int newPerm = Integer.parseInt(cmdargs[1]);
                                    String filename = cmdargs[2];
                                    FileManager.changePerm(filename, newPerm, currentUser);
                                } catch (NumberFormatException e) {
                                    System.out.println("Неверный формат прав доступа. Используйте число от 0 до 77.");
                                }
                            }
                            break;

                        default:
                            System.out.println("Неизвестная команда. Введите 'help' для справки.");
                    }

                    if (command.equals("exit")) {
                        break;
                    }
                } catch (Exception e) {
                    System.out.println("Ошибка выполнения команды: " + e.getMessage());
                }
            }
        }
    }

    private static void handleEchoCommand(String input, User user) {
        int redirectIndex = input.indexOf('>');
        if (redirectIndex == -1 || !input.contains(">")) {
            System.out.println("Использование: echo \"текст\" > <имя_файла> или echo \"текст\" >> <имя_файла>");
            return;
        }

        String text = input.substring(5, redirectIndex).trim().replaceAll("\"", "");
        String redirectPart = input.substring(redirectIndex).trim();

        if (redirectPart.startsWith(">>")) {
            String filename = redirectPart.substring(2).trim();
            FileManager.writeFile(filename, text, true, user);
        } else if (redirectPart.startsWith(">")) {
            String filename = redirectPart.substring(1).trim();
            FileManager.writeFile(filename, text, false, user);
        } else {
            System.out.println("Неправильный формат редиректа.");
        }
    }

    private static void printHelp() {
        System.out.println("Список доступных команд:");
        System.out.println("ls                  - Вывести список файлов");
        System.out.println("touch <имя_файла> <права_для_остальных> - Создать пустой файл");
        System.out.println("cat <имя_файла>    - Прочитать содержимое файла");
        System.out.println("echo \"текст\" > <имя_файла> - Записать текст в файл");
        System.out.println("echo \"текст\" >> <имя_файла> - Добавить текст в конец файла");
        System.out.println("rm <имя_файла>     - Удалить файл");
        System.out.println("chown <новый_владелец> <имя_файла>     - Сменить владельца файла");
        System.out.println("chmod <новые_права> <имя_файла>     - Сменить права на файл");
        System.out.println("help                - Показать справку");
        System.out.println("exit                - Выйти из текущего аккаунта");
    }
}