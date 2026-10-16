
package org.example.dao;

import org.example.model.User;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.MessageDigest;
import java.security.SecureRandom;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.Base64;

public class UserDao {

    private static final int ITERATIONS = 210000;
    private static final int KEY_LENGTH = 256;
    private static final SecureRandom RANDOM = new SecureRandom();

    // Register a new account
    public boolean register(String username, String password, String role) {
        String sql = """
                INSERT INTO users (username, password, roles, status)
                VALUES (?, ?, ?, 'PENDING')
                """;

        String hashedPassword = hashPassword(password);

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);
            statement.setString(2, hashedPassword);
            statement.setString(3, role);

            statement.executeUpdate();
            return true;

        } catch (SQLException e) {
            if (e.getErrorCode() == 19) {
                System.out.println("Username already exists.");
            } else {
                System.out.println("Registration failed: " + e.getMessage());
            }
            return false;
        }
    }

    // Verify username and password
    public User login(String username, String password) {
        String sql = """
                SELECT id, username, password, roles, status
                FROM users
                WHERE username = ?
                """;

        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {

            statement.setString(1, username);

            try (ResultSet result = statement.executeQuery()) {
                if (result.next()) {
                    String storedPassword = result.getString("password");

                    if (verifyPassword(password, storedPassword)) {
                        return new User(
                                result.getInt("id"),
                                result.getString("username"),
                                storedPassword,
                                result.getString("roles"),
                                result.getString("status")
                        );
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println("Login error: " + e.getMessage());
        }

        return null;
    }

    // Hash the password using PBKDF2
    private String hashPassword(String password) {
        byte[] salt = new byte[16];
        RANDOM.nextBytes(salt);

        byte[] hash = deriveHash(password, salt);

        return ITERATIONS + ":"
                + Base64.getEncoder().encodeToString(salt) + ":"
                + Base64.getEncoder().encodeToString(hash);
    }

    // Verify a password against the saved hash
    private boolean verifyPassword(String password, String storedPassword) {
        try {
            String[] parts = storedPassword.split(":");

            int iterations = Integer.parseInt(parts[0]);
            byte[] salt = Base64.getDecoder().decode(parts[1]);
            byte[] expectedHash = Base64.getDecoder().decode(parts[2]);

            byte[] actualHash = deriveHash(password, salt, iterations);

            return MessageDigest.isEqual(expectedHash, actualHash);

        } catch (Exception e) {
            return false;
        }
    }

    private byte[] deriveHash(String password, byte[] salt) {
        return deriveHash(password, salt, ITERATIONS);
    }

    private byte[] deriveHash(
            String password, byte[] salt, int iterations) {

        PBEKeySpec spec = new PBEKeySpec(
                password.toCharArray(),
                salt,
                iterations,
                KEY_LENGTH
        );

        try {
            SecretKeyFactory factory =
                    SecretKeyFactory.getInstance("PBKDF2WithHmacSHA256");

            return factory.generateSecret(spec).getEncoded();

        } catch (Exception e) {
            throw new IllegalStateException(
                    "Password hashing failed.", e);

        } finally {
            spec.clearPassword();
        }
    }
}
