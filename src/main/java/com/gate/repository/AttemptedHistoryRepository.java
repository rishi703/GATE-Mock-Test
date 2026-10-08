package com.gate.repository;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class AttemptedHistoryRepository {

    private static final String FILE_NAME =
            "gate-mock-test-history.csv";

    private static File getHistoryFile() {

        String userHome =
                System.getProperty("user.home");

        return new File(userHome, FILE_NAME);
    }

    // =========================================================
    // SAVE ATTEMPT
    // =========================================================

    public static synchronized void saveAttempt(
            String username,
            String subject,
            int test,
            int total,
            int attempted,
            int correct,
            int incorrect,
            int unanswered,
            double accuracy,
            double percentage) {

        File file = getHistoryFile();

        try {

            if (!file.exists()) {

                File parent = file.getParentFile();

                if (parent != null &&
                        !parent.exists()) {

                    parent.mkdirs();
                }

                file.createNewFile();

                try (BufferedWriter writer =
                             new BufferedWriter(
                                     new FileWriter(
                                             file,
                                             true))) {

                    writer.write(
                            "Username,Date,Subject,Test,Total,"
                            + "Attempted,Correct,Incorrect,"
                            + "Unanswered,Accuracy,Percentage"
                    );

                    writer.newLine();
                }
            }

            String date =
                    LocalDateTime.now()
                            .format(
                                    DateTimeFormatter.ofPattern(
                                            "yyyy-MM-dd HH:mm:ss"
                                    )
                            );

            try (BufferedWriter writer =
                         new BufferedWriter(
                                 new FileWriter(
                                         file,
                                         true))) {

                writer.write(
                        escape(username) + "," +
                        escape(date) + "," +
                        escape(subject) + "," +
                        test + "," +
                        total + "," +
                        attempted + "," +
                        correct + "," +
                        incorrect + "," +
                        unanswered + "," +
                        String.format(
                                Locale.US,
                                "%.2f",
                                accuracy
                        ) + "," +
                        String.format(
                                Locale.US,
                                "%.2f",
                                percentage
                        )
                );

                writer.newLine();
            }

        } catch (IOException e) {

            e.printStackTrace();
        }
    }

    // =========================================================
    // GET ALL ATTEMPTS
    // =========================================================

    public static List<String[]> getAllAttempts() {

        List<String[]> attempts =
                new ArrayList<>();

        File file = getHistoryFile();

        if (!file.exists()) {
            return attempts;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file))) {

            // Skip CSV header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.trim().isEmpty()) {

                    attempts.add(
                            parseCsvLine(line)
                    );
                }
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        return attempts;
    }

    // =========================================================
    // GET ATTEMPTS FOR ONE USER
    // =========================================================

    public static List<String[]> getAttemptsByUsername(
            String username) {

        List<String[]> userAttempts =
                new ArrayList<>();

        if (username == null ||
                username.trim().isEmpty()) {

            return userAttempts;
        }

        File file = getHistoryFile();

        if (!file.exists()) {
            return userAttempts;
        }

        try (BufferedReader reader =
                     new BufferedReader(
                             new FileReader(file))) {

            // Skip CSV header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (!line.trim().isEmpty()) {

                    String[] data =
                            parseCsvLine(line);

                    /*
                     * New format:
                     *
                     * 0 = Username
                     * 1 = Date
                     * 2 = Subject
                     * 3 = Test
                     * 4 = Total
                     * 5 = Attempted
                     * 6 = Correct
                     * 7 = Incorrect
                     * 8 = Unanswered
                     * 9 = Accuracy
                     * 10 = Percentage
                     */

                    if (data.length >= 11) {

                        String savedUsername =
                                data[0].trim();

                        if (savedUsername.equalsIgnoreCase(
                                username.trim())) {

                            userAttempts.add(data);
                        }
                    }
                }
            }

        } catch (IOException e) {

            e.printStackTrace();
        }

        return userAttempts;
    }

    // =========================================================
    // CSV ESCAPE
    // =========================================================

    private static String escape(String value) {

        if (value == null) {
            return "";
        }

        return "\"" +
                value.replace("\"", "\"\"") +
                "\"";
    }

    // =========================================================
    // CSV PARSER
    // =========================================================

    private static String[] parseCsvLine(
            String line) {

        List<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0;
             i < line.length();
             i++) {

            char c = line.charAt(i);

            if (c == '"') {

                if (insideQuotes &&
                        i + 1 < line.length() &&
                        line.charAt(i + 1) == '"') {

                    current.append('"');

                    i++;

                } else {

                    insideQuotes =
                            !insideQuotes;
                }

            } else if (c == ',' &&
                    !insideQuotes) {

                values.add(
                        current.toString()
                );

                current.setLength(0);

            } else {

                current.append(c);
            }
        }

        values.add(
                current.toString()
        );

        return values.toArray(
                new String[0]
        );
    }
}