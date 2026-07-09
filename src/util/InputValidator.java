package util;

import java.time.LocalDate;
import java.time.LocalTime;
import java.time.format.DateTimeFormatter;
import java.time.format.DateTimeParseException;
import java.util.Scanner;

public class InputValidator {
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter TIME_FORMATTER = DateTimeFormatter.ofPattern("HH:mm");

    public static int readInt(Scanner scanner, String prompt, int min, int max) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            try {
                int val = Integer.parseInt(input);
                if (val >= min && val <= max) {
                    return val;
                }
                System.out.println("Error: Input must be between " + min + " and " + max + ".");
            } catch (NumberFormatException e) {
                System.out.println("Error: Please enter a valid integer.");
            }
        }
    }

    public static String readString(Scanner scanner, String prompt, boolean allowEmpty) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (!allowEmpty && input.isEmpty()) {
                System.out.println("Error: Input cannot be empty.");
                continue;
            }
            return input;
        }
    }

    public static String readPhoneNumber(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt);
            String input = scanner.nextLine().trim();
            if (isValidPhoneNumber(input)) {
                return input;
            }
            System.out.println("Error: Phone number must contain only digits and have a length of 7 to 15 digits.");
        }
    }

    public static boolean isValidPhoneNumber(String phone) {
        if (phone == null || phone.isEmpty()) {
            return false;
        }
        if (phone.length() < 7 || phone.length() > 15) {
            return false;
        }
        for (int i = 0; i < phone.length(); i++) {
            if (!Character.isDigit(phone.charAt(i))) {
                return false;
            }
        }
        return true;
    }

    public static LocalDate readDate(Scanner scanner, String prompt, boolean futureOrPresentOnly) {
        while (true) {
            System.out.print(prompt + " (YYYY-MM-DD): ");
            String input = scanner.nextLine().trim();
            try {
                LocalDate date = LocalDate.parse(input, DATE_FORMATTER);
                if (futureOrPresentOnly && date.isBefore(LocalDate.now())) {
                    System.out.println("Error: Date cannot be in the past.");
                    continue;
                }
                return date;
            } catch (DateTimeParseException e) {
                System.out.println("Error: Invalid date format. Use YYYY-MM-DD.");
            }
        }
    }

    public static LocalTime readTime(Scanner scanner, String prompt) {
        while (true) {
            System.out.print(prompt + " (HH:MM in 24hr format): ");
            String input = scanner.nextLine().trim();
            try {
                return LocalTime.parse(input, TIME_FORMATTER);
            } catch (DateTimeParseException e) {
                System.out.println("Error: Invalid time format. Use HH:MM.");
            }
        }
    }
}
