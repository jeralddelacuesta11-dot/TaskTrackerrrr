
package org.example.ui;

import org.example.dao.UserDao;
import org.example.model.User;

import java.util.Scanner;

public class Menu {

    private final Scanner scanner = new Scanner(System.in);
    private final UserDao userDao = new UserDao();

    public void start() {
        boolean running = true;

        while (running) {
            System.out.println("\n================================");
            System.out.println("       TASK TRACKER SYSTEM");
            System.out.println("================================");
            System.out.println("1. Register Account");
            System.out.println("2. Login");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> registerAccount();
                case "2" -> loginAccount();
                case "3" -> {
                    System.out.println("Thank you for using Task Tracker!");
                    running = false;
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }

        scanner.close();
    }

    // Registration menu
    private void registerAccount() {
        System.out.println("\n--- REGISTER ACCOUNT ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        if (username.isBlank() || password.isBlank()) {
            System.out.println("Username and password are required.");
            return;
        }

        System.out.println("\nSelect your role:");
        System.out.println("1. TEACHER");
        System.out.println("2. STUDENT");
        System.out.print("Choose a role: ");

        String choice = scanner.nextLine().trim();
        String role;

        switch (choice) {
            case "1" -> role = "TEACHER";
            case "2" -> role = "STUDENT";
            default -> {
                System.out.println("Invalid role selection.");
                return;
            }
        }

        boolean registered =
                userDao.register(username, password, role);

        if (registered) {
            System.out.println("Registration successful!");
            System.out.println("Your account status is PENDING.");
            System.out.println("Wait for an administrator to approve it.");
        }
    }

    // Login menu
    private void loginAccount() {
        System.out.println("\n--- LOGIN ---");

        System.out.print("Enter username: ");
        String username = scanner.nextLine().trim();

        System.out.print("Enter password: ");
        String password = scanner.nextLine();

        User user = userDao.login(username, password);

        if (user == null) {
            System.out.println("Invalid username or password.");
            return;
        }

        if ("PENDING".equals(user.status())) {
            System.out.println("Your account is still pending approval.");
            return;
        }

        if ("REJECTED".equals(user.status())) {
            System.out.println("Your account has been rejected.");
            return;
        }

        if (!"APPROVED".equals(user.status())) {
            System.out.println("Your account status is invalid.");
            return;
        }

        System.out.println("\nLogin successful!");
        System.out.println("Welcome, " + user.username() + "!");
        System.out.println("Role: " + user.roles());

        showDashboard(user);
    }

    // Dashboard after successful login
    private void showDashboard(User user) {
        boolean loggedIn = true;

        while (loggedIn) {
            System.out.println("\n================================");
            System.out.println("       TASK TRACKER DASHBOARD");
            System.out.println("================================");
            System.out.println("User: " + user.username());
            System.out.println("Role: " + user.roles());
            System.out.println("1. View Dashboard");
            System.out.println("2. Logout");
            System.out.println("3. Exit");
            System.out.print("Choose an option: ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1" -> {
                    System.out.println("\nTask Tracker Dashboard");
                    System.out.println("You are logged in successfully.");
                    System.out.println(
                            "Task assignment features can be added here.");
                }
                case "2" -> {
                    System.out.println("Logout successful.");
                    loggedIn = false;
                }
                case "3" -> {
                    System.out.println("Exiting Task Tracker...");
                    System.exit(0);
                }
                default -> System.out.println("Invalid option. Try again.");
            }
        }
    }
}
