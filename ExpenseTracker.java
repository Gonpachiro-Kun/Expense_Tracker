import java.io.*;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.text.DecimalFormat;

class Expense {
    private String description;
    private double amount;
    private LocalDateTime timestamp;
    public Expense(String description, double amount) {
        this.description = description;
        this.amount = amount;
        this.timestamp = LocalDateTime.now();
    }
    public Expense(LocalDateTime timestamp, String description, double amount) {
        this.description = description;
        this.amount = amount;
        this.timestamp = timestamp;
    }

    public String getDescription() {
        return description;
    }

    public double getAmount() {
        return amount;
    }

    public LocalDateTime getTimestamp() {
        return timestamp;
    }

    @Override
    public String toString() {
        return ("Date: " + timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"))+" Description: " + description + ", Amount: Rs" + ExpenseTracker.addComma(amount));
    }

    public String toCSV() {
        return description + "," + amount + "," + timestamp.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
    }

    public static Expense fromCSV(String csvLine) {
        try {
            String[] parts = csvLine.split(",");
            if (parts.length < 3) {
                throw new IllegalArgumentException("Invalid data format: " + csvLine);
            }
            String description = parts[0];
            double amount = Double.parseDouble(parts[1]);
            LocalDateTime timestamp = LocalDateTime.parse(parts[2], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
            return new Expense(timestamp, description, amount);
        } catch (Exception e) {
            System.out.println("Error parsing CSV line: " + csvLine + ". Skipping this entry.");
            return null;
        }
    }
}

public class ExpenseTracker {
    private static ArrayList<Expense> expenses = new ArrayList<>();
    private static Scanner sc = new Scanner(System.in);
    private static final String FILE_PATH = "expenses.csv";
    private static final String TARGET_FILE = "targets.csv";
    private static double weeklyTarget = 0;
    private static double monthlyTarget = 0;
    private static LocalDateTime lastWeeklyTargetSet = null;
    private static LocalDateTime lastMonthlyTargetSet = null;

    public static void main(String[] args) {
        loadFromFile();
        loadTargets();
        System.out.println("Welcome to the Expense Tracker!!\n");
        checkAndSetTargets();
        boolean exit = false;
        while (!exit) {
            System.out.println("\nExpense Tracker Menu:");
            System.out.println("1. Add an Expense");
            System.out.println("2. View All Expenses");
            System.out.println("3. Calculate Total Expenses");
            System.out.println("4. Search Expense by Description");
            System.out.println("5. Generate Expense Report (Weekly/Monthly)");
            System.out.println("6. Edit Weekly/Monthly Targets");
            System.out.println("7. Delete an Expense");
            System.out.println("8. Exit");
            System.out.print("Choose an option: ");
            try {
                int choice = sc.nextInt();
                sc.nextLine();

                switch (choice) {
                    case 1:
                        addExpense();
                        break;
                    case 2:
                        viewExpenses();
                        break;
                    case 3:
                        calculateTotalExpenses();
                        break;
                    case 4:
                        searchExpenseByDescription();
                        break;
                    case 5:
                        generateReport();
                        break;
                    case 6:
                        setTargets();
                        break;
                    case 7:
                        deleteExpense();
                        break;
                    case 8:
                        saveTargets();
                        exit = true;
                        System.out.println("Exiting the Expense Tracker. Goodbye!");
                        break;
                    default:
                        System.out.println("Invalid option. Please try again.");
                }
            } catch (InputMismatchException e) {
                System.out.println("Invalid input. Please enter a number.");
                sc.nextLine();
            }
        }
        sc.close();
    }

    private static void addExpense() {
        System.out.print("How many commodities do you want to add? ");
        int numberOfCommodities = sc.nextInt();
        sc.nextLine();
        for (int i = 1; i <= numberOfCommodities; i++) {
            System.out.print("Enter the description of commodity " + i + ": ");
            String description = sc.nextLine();
            while (true) {
                try {
                    System.out.printf("Enter the price of the commodity " + i + ": ");
                    double amount = sc.nextDouble();
                    sc.nextLine();
                    Expense expense = new Expense(description, amount);
                    expenses.add(expense);
                    saveToFile(expense);
                    System.out.println("Commodity \"" + description + "\" added successfully!");
                    checkTargetLimits();

                    break;
                } catch (InputMismatchException e) {
                    System.out.println("Invalid amount. Please enter a numeric value.");
                    sc.nextLine(); // Clear invalid input
                }
            }
        }
    }

    private static void viewExpenses() {
        if (expenses.isEmpty()) {
            System.out.println("No expenses recorded.");
        } else {
            System.out.println("\nList of Expenses:");
            for (Expense expense : expenses) {
                System.out.println(expense);
            }
        }
    }

    private static void calculateTotalExpenses() {
        double total = 0;
        for (Expense expense : expenses) {
            total += expense.getAmount();
        }
        if (total == 0) {
            System.out.println("Please add the expenses by pressing option 1.");
        } else {
            System.out.println("Total Expenses: Rs" + addComma(total));
        }
    }

    private static void searchExpenseByDescription() {
        System.out.print("Enter the description to search: ");
        String searchQuery = sc.nextLine();
        boolean found = false;
        for (Expense expense : expenses) {
            if (expense.getDescription().equalsIgnoreCase(searchQuery)) {
                System.out.println("Found: " + expense);
                found = true;
            }
        }
        if (!found) {
            System.out.println("No expense found with the description \"" + searchQuery + "\".");
        }
    }

    private static void generateReport() {
        System.out.println("Generate Report:");
        System.out.println("1. Weekly Report");
        System.out.println("2. Monthly Report");
        System.out.print("Choose an option: ");
        try {
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1:
                    generateWeeklyReport();
                    break;
                case 2:
                    generateMonthlyReport();
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1 or 2.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            sc.nextLine();
        }
    }

    private static void generateWeeklyReport() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        double total = 0;
        System.out.println("\nWeekly Expense Report:");
        for (Expense expense : expenses) {
            if (expense.getTimestamp().isAfter(oneWeekAgo)) {
                System.out.println(expense);
                total += expense.getAmount();
            }
        }
        System.out.println("Total Weekly Expenses: Rs" + addComma(total));
    }

    private static void generateMonthlyReport() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        double total = 0;
        System.out.println("\nMonthly Expense Report:");
        for (Expense expense : expenses) {
            if (expense.getTimestamp().isAfter(oneMonthAgo)) {
                System.out.println(expense);
                total += expense.getAmount();
            }
        }
        System.out.println("Total Monthly Expenses: Rs" + addComma(total));
    }

    private static void setTargets() {
        System.out.println("Edit Targets:");
        System.out.println("1. Set Weekly Target");
        System.out.println("2. Set Monthly Target");
        System.out.print("Choose an option: ");
        try {
            int choice = sc.nextInt();
            sc.nextLine();
            switch (choice) {
                case 1:
                    setWeeklyTarget();
                    break;
                case 2:
                    setMonthlyTarget();
                    break;
                default:
                    System.out.println("Invalid option. Please choose 1 or 2.");
            }
        } catch (InputMismatchException e) {
            System.out.println("Invalid input. Please enter a number.");
            sc.nextLine();
        }
    }

    private static void checkAndSetTargets() {
        LocalDateTime now = LocalDateTime.now();

        if (lastWeeklyTargetSet == null || now.isAfter(lastWeeklyTargetSet.plusWeeks(1))) {
            System.out.println("A new week has started. Please set your weekly target.");
            setWeeklyTarget();
        }

        if (lastMonthlyTargetSet == null || now.isAfter(lastMonthlyTargetSet.plusMonths(1))) {
            System.out.println("A new month has started. Please set your monthly target.");
            setMonthlyTarget();
        }
    }

    private static void setWeeklyTarget() {
        System.out.print("Enter your weekly expense target: Rs ");
        weeklyTarget = sc.nextDouble();
        sc.nextLine();
        lastWeeklyTargetSet = LocalDateTime.now();
        saveTargets();
    }

    private static void setMonthlyTarget() {
        System.out.print("Enter your monthly expense target: Rs ");
        monthlyTarget = sc.nextDouble();
        sc.nextLine();
        lastMonthlyTargetSet = LocalDateTime.now();
        saveTargets();
    }

    private static void deleteExpense() {
        System.out.print("Enter the description of the expense you want to delete: ");
        String deleteQuery = sc.nextLine();
        boolean found = false;
        Iterator<Expense> iterator = expenses.iterator();
        while (iterator.hasNext()) {
            Expense expense = iterator.next();
            if (expense.getDescription().equalsIgnoreCase(deleteQuery)) {
                iterator.remove();
                found = true;
                System.out.println("Expense \"" + deleteQuery + "\" deleted successfully.");
                break;
            }
        }
        if (!found) {
            System.out.println("No expense found with the description \"" + deleteQuery + "\".");
        } else {
            saveAllToFile();
        }
    }

    private static void saveAllToFile() {
        try (FileWriter writer = new FileWriter(FILE_PATH)) {
            for (Expense expense : expenses) {
                writer.write(expense.toCSV() + "\n");
            }
            System.out.println("File updated successfully after deletion.");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    private static void saveTargets() {
        try (FileWriter writer = new FileWriter(TARGET_FILE)) {
            writer.write(weeklyTarget + "," + monthlyTarget + "," +
                    (lastWeeklyTargetSet != null ? lastWeeklyTargetSet.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "") + "," +
                    (lastMonthlyTargetSet != null ? lastMonthlyTargetSet.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")) : "") + "\n");
            System.out.println("Targets saved successfully.");
        } catch (IOException e) {
            System.out.println("Error saving targets: " + e.getMessage());
        }
    }

    private static void loadTargets() {
        File file = new File(TARGET_FILE);
        if (file.exists()) {
            try (Scanner fileScanner = new Scanner(file)) {
                if (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    String[] parts = line.split(",");
                    if (parts.length >= 4) {
                        weeklyTarget = Double.parseDouble(parts[0]);
                        monthlyTarget = Double.parseDouble(parts[1]);
                        lastWeeklyTargetSet = parts[2].isEmpty() ? null : LocalDateTime.parse(parts[2], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                        lastMonthlyTargetSet = parts[3].isEmpty() ? null : LocalDateTime.parse(parts[3], DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss"));
                    }
                }
            } catch (FileNotFoundException e) {
                System.out.println("Error reading targets from file: " + e.getMessage());
            } catch (Exception e) {
                System.out.println("Error parsing targets from file: " + e.getMessage());
            }
        }
    }

    private static void checkTargetLimits() {
        double weeklyExpenses = calculateWeeklyExpenses();
        double monthlyExpenses = calculateMonthlyExpenses();

        if (weeklyExpenses >= 0.9 * weeklyTarget) {
            System.out.println("Warning: Your weekly expenses have reached 90% of the target!");
        }

        if (weeklyExpenses == weeklyTarget) {
            System.out.println("Warning: Your weekly expenses have been exhausted!");
        }

        if (monthlyExpenses >= 0.9 * monthlyTarget) {
            System.out.println("Warning: Your monthly expenses have reached 90% of the target!");
        }

        if (monthlyExpenses == monthlyTarget) {
            System.out.println("Warning: Your monthly expenses have been exhausted!");
        }

    }

    private static double calculateWeeklyExpenses() {
        LocalDateTime oneWeekAgo = LocalDateTime.now().minusWeeks(1);
        double total = 0;
        for (Expense expense : expenses) {
            if (expense.getTimestamp().isAfter(oneWeekAgo)) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    private static double calculateMonthlyExpenses() {
        LocalDateTime oneMonthAgo = LocalDateTime.now().minusMonths(1);
        double total = 0;
        for (Expense expense : expenses) {
            if (expense.getTimestamp().isAfter(oneMonthAgo)) {
                total += expense.getAmount();
            }
        }
        return total;
    }

    public static String addComma(double amount) {
        DecimalFormat format = new DecimalFormat("##,##,##,##0.00");
        return format.format(amount);
    }

    private static void saveToFile(Expense expense) {
        try (FileWriter writer = new FileWriter(FILE_PATH, true)) {
            writer.write(expense.toCSV() + "\n");
            System.out.println("Expense saved to file successfully!");
        } catch (IOException e) {
            System.out.println("Error writing to file: " + e.getMessage());
        }
    }

    private static void loadFromFile() {
        File file = new File(FILE_PATH);
        if (file.exists()) {
            try (Scanner fileScanner = new Scanner(file)) {
                while (fileScanner.hasNextLine()) {
                    String line = fileScanner.nextLine();
                    Expense expense = Expense.fromCSV(line);
                    if (expense != null) {
                        expenses.add(expense);
                    }
                }
                System.out.println("Expenses loaded from file.");
            } catch (FileNotFoundException e) {
                System.out.println("Error reading from file: " + e.getMessage());
            }
        } else {
            System.out.println("No previous expenses found. Starting fresh.");
        }
    }
}