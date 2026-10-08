
package com.gate.repository;

import com.gate.model.Question;
import jakarta.servlet.ServletContext;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

public class QuestionRepository {

    private final List<Question> questions = new ArrayList<>();

    public QuestionRepository(ServletContext context) {
        loadQuestions(context);
    }

    private void loadQuestions(ServletContext context) {

        try (InputStream inputStream =
                     context.getResourceAsStream("/data/questions.csv")) {

            if (inputStream == null) {
                throw new RuntimeException(
                        "questions.csv not found."
                );
            }

            BufferedReader reader =
                    new BufferedReader(
                            new InputStreamReader(
                                    inputStream,
                                    StandardCharsets.UTF_8
                            )
                    );

            // Skip CSV header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                if (line.trim().isEmpty()) {
                    continue;
                }

                List<String> values =
                        parseCSVLine(line);

                if (values.size() < 11) {
                    continue;
                }

                try {

                    Question question =
                            new Question(
                                    Integer.parseInt(
                                            values.get(0).trim()
                                    ),

                                    values.get(1).trim(),

                                    values.get(2).trim(),

                                    values.get(3).trim(),

                                    values.get(4).trim(),

                                    values.get(5).trim(),

                                    values.get(6).trim(),

                                    values.get(7).trim(),

                                    Integer.parseInt(
                                            values.get(8).trim()
                                    ),

                                    values.get(9).trim(),

                                    values.get(10).trim()
                            );

                    questions.add(question);

                } catch (NumberFormatException e) {

                    System.err.println(
                            "Skipping invalid CSV row: "
                                    + line
                    );
                }
            }

            System.out.println(
                    "Questions loaded successfully: "
                            + questions.size()
            );

        } catch (Exception e) {

            throw new RuntimeException(
                    "Error while loading questions.csv",
                    e
            );
        }
    }

    public List<Question> getAllQuestions() {

        return new ArrayList<>(questions);
    }

    public List<Question> getQuestionsBySubject(
            String subject) {

        if (subject == null) {
            return new ArrayList<>();
        }

        String searchSubject =
                subject.trim();

        return questions.stream()
                .filter(q ->
                        q.getSubject() != null
                                &&
                        q.getSubject()
                                .trim()
                                .equalsIgnoreCase(
                                        searchSubject
                                )
                )
                .collect(Collectors.toList());
    }

    public List<Question> getRandomQuestions(
            String subject,
            int count) {

        List<Question> subjectQuestions =
                getQuestionsBySubject(subject);

        Collections.shuffle(
                subjectQuestions
        );

        int size =
                Math.min(
                        count,
                        subjectQuestions.size()
                );

        return new ArrayList<>(
                subjectQuestions.subList(
                        0,
                        size
                )
        );
    }

    public List<Question> getQuestionsByTopic(
            String topic) {

        if (topic == null) {
            return new ArrayList<>();
        }

        String searchTopic =
                topic.trim();

        return questions.stream()
                .filter(q ->
                        q.getTopic() != null
                                &&
                        q.getTopic()
                                .trim()
                                .equalsIgnoreCase(
                                        searchTopic
                                )
                )
                .collect(Collectors.toList());
    }

    private List<String> parseCSVLine(
            String line) {

        List<String> values =
                new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0;
             i < line.length();
             i++) {

            char character =
                    line.charAt(i);

            if (character == '"') {

                insideQuotes =
                        !insideQuotes;

            } else if (
                    character == ','
                            &&
                    !insideQuotes) {

                values.add(
                        current
                                .toString()
                                .trim()
                );

                current.setLength(0);

            } else {

                current.append(
                        character
                );
            }
        }

        values.add(
                current
                        .toString()
                        .trim()
        );

        return values;
    }
}
