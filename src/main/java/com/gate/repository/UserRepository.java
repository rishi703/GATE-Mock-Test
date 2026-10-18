package com.gate.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

/**
 * File-based user store.
 *
 * Users are kept OUTSIDE the webapp directory
 * (~/gate-mock-test-data/users.csv) so accounts survive
 * redeploys and work the same in Docker / Eclipse / WAR.
 *
 * CSV format: name,username,email,password
 * where "password" is a PBKDF2 hash (see PasswordUtil);
 * legacy plaintext rows are still readable for migration.
 */
public class UserRepository {

    private static final File DATA_DIR = new File(
            System.getProperty("user.home"),
            "gate-mock-test-data");

    private static final File USERS_FILE =
            new File(DATA_DIR, "users.csv");

    private static final String HEADER =
            "name,username,email,password";

    // =========================================================
    // USER MODEL
    // =========================================================

    public static class User {

        private final String name;
        private final String username;
        private final String email;
        private final String storedPassword;

        public User(String name,
                    String username,
                    String email,
                    String storedPassword) {
            this.name = name;
            this.username = username;
            this.email = email;
            this.storedPassword = storedPassword;
        }

        public String getName() {
            return name;
        }

        public String getUsername() {
            return username;
        }

        public String getEmail() {
            return email;
        }

        public String getStoredPassword() {
            return storedPassword;
        }
    }

    // =========================================================
    // LOOKUP
    // =========================================================

    /**
     * Finds a user by username (case-insensitive),
     * or null when not found.
     */
    public static synchronized User findByUsername(
            String username) {

        if (username == null) {
            return null;
        }

        File file = ensureFile();

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            reader.readLine(); // skip header

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = parseCsvLine(line);

                if (data.length >= 4) {

                    String savedUsername = data[1].trim();

                    if (savedUsername.equalsIgnoreCase(
                            username.trim())) {

                        return new User(
                                data[0].trim(),
                                savedUsername,
                                data[2].trim(),
                                data[3]);
                    }
                }
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error reading users.csv", e);
        }

        return null;
    }

    // =========================================================
    // CREATE
    // =========================================================

    /**
     * Saves a new user. Caller must have already checked
     * that the username is not taken and hashed the password.
     */
    public static synchronized void saveUser(String name,
                                             String username,
                                             String email,
                                             String passwordHash) {

        File file = ensureFile();

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file, true))) {

            writer.write(
                    escape(name.trim()) + ","
                    + escape(username.trim()) + ","
                    + escape(email.trim()) + ","
                    + escape(passwordHash));

            writer.newLine();

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error writing users.csv", e);
        }
    }

    // =========================================================
    // PASSWORD MIGRATION
    // =========================================================

    /**
     * Replaces the stored password for a username
     * (used to upgrade a legacy plaintext row to a hash).
     */
    public static synchronized void updatePassword(
            String username,
            String newPasswordHash) {

        File file = ensureFile();

        StringBuilder content = new StringBuilder();
        content.append(HEADER).append('\n');

        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            reader.readLine(); // skip old header

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                String[] data = parseCsvLine(line);

                if (data.length >= 4
                        && data[1].trim().equalsIgnoreCase(
                                username.trim())) {

                    data[3] = newPasswordHash;
                }

                content.append(escape(data[0].trim()))
                        .append(',')
                        .append(escape(data[1].trim()))
                        .append(',')
                        .append(escape(data[2].trim()))
                        .append(',')
                        .append(escape(data[3]))
                        .append('\n');
            }

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error migrating password in users.csv", e);
        }

        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file))) {

            writer.write(content.toString());

        } catch (IOException e) {
            throw new RuntimeException(
                    "Error writing users.csv", e);
        }
    }

    // =========================================================
    // FILE HELPERS
    // =========================================================

    private static File ensureFile() {

        if (!DATA_DIR.exists()) {
            DATA_DIR.mkdirs();
        }

        if (!USERS_FILE.exists()) {

            try {

                USERS_FILE.createNewFile();

                try (BufferedWriter writer =
                             new BufferedWriter(new FileWriter(USERS_FILE))) {

                    writer.write(HEADER);
                    writer.newLine();
                }

            } catch (IOException e) {
                throw new RuntimeException(
                        "Error creating users.csv", e);
            }
        }

        return USERS_FILE;
    }

    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        if (value.contains(",") || value.contains("\"")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }

        return value;
    }

    /**
     * Parses a single CSV line, honouring quoted fields
     * and "" escapes.
     */
    private static String[] parseCsvLine(String line) {

        java.util.List<String> fields = new java.util.ArrayList<>();

        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char c = line.charAt(i);

            if (inQuotes) {

                if (c == '"') {

                    if (i + 1 < line.length()
                            && line.charAt(i + 1) == '"') {

                        current.append('"');
                        i++;

                    } else {
                        inQuotes = false;
                    }

                } else {
                    current.append(c);
                }

            } else {

                if (c == '"') {
                    inQuotes = true;

                } else if (c == ',') {
                    fields.add(current.toString());
                    current.setLength(0);

                } else {
                    current.append(c);
                }
            }
        }

        fields.add(current.toString());

        return fields.toArray(new String[0]);
    }
}
