import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.util.List;
import java.util.Scanner;

public class Main {

    private static final String SCENARIOS_FOLDER = "C:\\Users\\grisg\\Videos\\TextAdventure\\scenarios";
    private static final Scanner scanner = new Scanner(System.in);

    public static void main(String[] args) {
        while (true) {
            List<String> scenarioList = getScenariosList(SCENARIOS_FOLDER);

            System.out.println("Доступные сценарии:");
            for (int i = 0; i < scenarioList.size(); i++) {
                System.out.println((i + 1) + ". " + scenarioList.get(i));
            }

            System.out.println("0. Выход");
            System.out.println("-1. Удалить сценарий");
            System.out.println("N. Создать новый сценарий");
            System.out.print("Выберите сценарий (или N для создания нового, 0 для выхода): ");
            String choice = scanner.nextLine();

            if (choice.equals("0")) {
                System.out.println("Выход из программы.");
                scanner.close();
                System.exit(0);
            } else if (choice.equals("-1")) {
                deleteScenario();
            } else if (choice.equalsIgnoreCase("N")) {
                addCustomScenario();
            } else if (isNumeric(choice)) {
                int scenarioNumber = Integer.parseInt(choice);
                if (scenarioNumber > 0 && scenarioNumber <= scenarioList.size()) {
                    String selectedScenario = scenarioList.get(scenarioNumber - 1);
                    runScenario(SCENARIOS_FOLDER + File.separator + selectedScenario);
                } else {
                    System.out.println("Некорректный выбор. Попробуйте еще раз.");
                }
            } else {
                System.out.println("Некорректный выбор. Попробуйте еще раз.");
            }
        }
    }

    private static List<String> getScenariosList(String folderPath) {
        File folder = new File(folderPath);
        String[] scenarioArray = folder.list((dir, name) -> new File(dir, name).isDirectory());
        return List.of(scenarioArray != null ? scenarioArray : new String[0]);
    }

    private static void runScenario(String scenarioPath) {
        try {
            File scenarioFolder = new File(scenarioPath);
            File mainScenarioFile = new File(scenarioFolder, "main.txt");

            if (mainScenarioFile.exists()) {
                List<String> mainScenarioLines = Files.readAllLines(mainScenarioFile.toPath());
                for (String line : mainScenarioLines) {
                    System.out.println(line);
                }

                handleUserChoice(scanner.nextLine(), scenarioFolder);
            } else {
                System.out.println("Основной файл сценария не найден.");
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    private static void handleUserChoice(String userChoice, File scenarioFolder) {
        try {
            int choiceIndex = Integer.parseInt(userChoice);
            File[] choiceFolders = scenarioFolder.listFiles(File::isDirectory);

            if (choiceFolders != null && choiceIndex > 0 && choiceIndex <= choiceFolders.length) {
                File choiceFolder = choiceFolders[choiceIndex - 1];

                // Показываем содержимое выбранной директории (папки с продолжением сценария)
                showScenarioContent(choiceFolder);

                // Рекурсивный вызов для обработки продолжения истории
                handleUserChoice(scanner.nextLine(), choiceFolder);
            } else if (choiceIndex == 0) {
                System.out.println("Вы завершили сценарий.");
            } else {
                System.out.println("Некорректный выбор. Попробуйте еще раз.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Некорректный выбор. Попробуйте еще раз.");
        }
    }

    private static void addCustomScenario() {
        System.out.print("Введите название нового сценария: ");
        String newScenarioName = scanner.nextLine();
        File newScenarioFolder = new File(SCENARIOS_FOLDER + File.separator + newScenarioName);

        if (newScenarioFolder.exists()) {
            System.out.println("Сценарий с таким названием уже существует.");
            return;
        }

        if (newScenarioFolder.mkdirs()) {
            System.out.println("Сценарий успешно создан.");

            // Запрашиваем у пользователя, хочет ли он добавить варианты продолжения для основной истории
            System.out.print("Хотите добавить варианты продолжения для основной истории? (да/нет): ");
            String addMainChoices = scanner.nextLine();

            if (addMainChoices.equalsIgnoreCase("да")) {
                // Создаем основной файл сценария
                File mainScenarioFile = new File(newScenarioFolder, "main.txt");
                System.out.println("Введите описание основной истории (введите 'выход' для завершения):");

                try {
                    FileWriter writer = new FileWriter(mainScenarioFile);
                    String line;
                    while (!(line = scanner.nextLine()).equalsIgnoreCase("выход")) {
                        writer.write(line + System.lineSeparator());
                    }
                    writer.close();

                    System.out.println("Основной файл сценария успешно создан.");
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Ошибка при создании основного файла сценария.");
                    return;
                }

                // Добавляем варианты выбора для основной истории
                addChoices(scanner, newScenarioFolder, "main", "choice");
            } else {
                System.out.println("Основная история не будет содержать вариантов продолжения.");
            }

            System.out.println("Сценарий успешно создан.");
        } else {
            System.out.println("Ошибка при создании папки для нового сценария.");
        }
    }

    private static void addChoices(Scanner scanner, File scenarioFolder, String storyType, String prefix) {
        boolean addChoices = true;
        int choiceNumber = 1;

        while (addChoices) {
            System.out.print("Введите вариант выбора " + choiceNumber + " для " + storyType + " (введите 'выход' для завершения): ");
            String choice = scanner.nextLine();

            if (choice.equalsIgnoreCase("выход")) {
                addChoices = false;
                break;
            }

            File choiceFolder = new File(scenarioFolder, prefix + choiceNumber);
            if (choiceFolder.mkdirs()) {
                System.out.println("Папка для варианта " + prefix + choiceNumber + " успешно создана.");

                // Создаем файл для варианта
                File choiceFile = new File(choiceFolder, "choice.txt");
                try {
                    FileWriter writer = new FileWriter(choiceFile);
                    writer.write(choice + System.lineSeparator());

                    System.out.print("Введите продолжение истории для варианта " + prefix + choiceNumber + " (введите 'выход' для завершения): ");
                    String line;
                    while (!(line = scanner.nextLine()).equalsIgnoreCase("выход")) {
                        writer.write(line + System.lineSeparator());
                    }
                    writer.close();
                } catch (IOException e) {
                    e.printStackTrace();
                    System.out.println("Ошибка при создании файла для варианта " + prefix + choiceNumber + ".");
                }

                // Рекурсивный вызов для добавления вариантов для нового выбора
                addChoices(scanner, choiceFolder, prefix + choiceNumber, prefix + choiceNumber + "_");
            } else {
                System.out.println("Ошибка при создании папки для варианта " + prefix + choiceNumber + ".");
            }

            choiceNumber++;
        }
    }

    private static void showScenarioContent(File scenarioFolder) {
        File[] scenarioFiles = scenarioFolder.listFiles((dir, name) -> name.endsWith(".txt"));

        if (scenarioFiles != null && scenarioFiles.length > 0) {
            List<String> lines;
            try {
                lines = Files.readAllLines(scenarioFiles[0].toPath());
                for (String line : lines) {
                    System.out.println(line);
                }
            } catch (IOException e) {
                System.out.println("Ошибка чтения сценария.");
            }
        } else {
            System.out.println("Продолжение истории не найдено.");
        }
    }

    private static void deleteScenario() {
        System.out.print("Введите номер сценария для удаления: ");
        String scenarioNumber = scanner.nextLine();

        if (isNumeric(scenarioNumber)) {
            int number = Integer.parseInt(scenarioNumber);
            List<String> scenarioList = getScenariosList(SCENARIOS_FOLDER);

            if (number > 0 && number <= scenarioList.size()) {
                String scenarioToDelete = scenarioList.get(number - 1);
                File scenarioFolderToDelete = new File(SCENARIOS_FOLDER + File.separator + scenarioToDelete);

                if (scenarioFolderToDelete.exists()) {
                    deleteDirectory(scenarioFolderToDelete);
                    System.out.println("Сценарий успешно удален.");
                } else {
                    System.out.println("Сценарий не найден.");
                }
            } else {
                System.out.println("Некорректный номер сценария.");
            }
        } else {
            System.out.println("Некорректный ввод. Введите число.");
        }
    }

    private static void deleteDirectory(File directory) {
        File[] contents = directory.listFiles();
        if (contents != null) {
            for (File file : contents) {
                deleteDirectory(file);
            }
        }
        directory.delete();
    }

    private static boolean isNumeric(String str) {
        try {
            Integer.parseInt(str);
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}