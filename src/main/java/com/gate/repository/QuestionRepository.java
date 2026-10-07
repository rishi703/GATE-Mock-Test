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
                throw new RuntimeException("questions.csv not found.");
            }

            BufferedReader reader = new BufferedReader(
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

                List<String> values = parseCSVLine(line);

                if (values.size() < 11) {
                    continue;
                }

                Question question = new Question(
                        Integer.parseInt(values.get(0)),
                        values.get(1),
                        values.get(2),
                        values.get(3),
                        values.get(4),
                        values.get(5),
                        values.get(6),
                        values.get(7),
                        Integer.parseInt(values.get(8)),
                        values.get(9),
                        values.get(10)
                );

                questions.add(question);
            }

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

        return questions.stream()
                .filter(q ->
                        q.getSubject()
                                .equalsIgnoreCase(subject))
                .collect(Collectors.toList());
    }

    public List<Question> getRandomQuestions(
            String subject,
            int count) {

        List<Question> subjectQuestions =
                getQuestionsBySubject(subject);

        Collections.shuffle(subjectQuestions);

        int size = Math.min(
                count,
                subjectQuestions.size()
        );

        return new ArrayList<>(
                subjectQuestions.subList(0, size)
        );
    }

    public List<Question> getQuestionsByTopic(
            String topic) {

        return questions.stream()
                .filter(q ->
                        q.getTopic()
                                .equalsIgnoreCase(topic))
                .collect(Collectors.toList());
    }

    private List<String> parseCSVLine(String line) {

        List<String> values = new ArrayList<>();

        StringBuilder current =
                new StringBuilder();

        boolean insideQuotes = false;

        for (int i = 0; i < line.length(); i++) {

            char character = line.charAt(i);

            if (character == '"') {

                insideQuotes = !insideQuotes;

            } else if (character == ','
                    && !insideQuotes) {

                values.add(
                        current.toString().trim()
                );

                current.setLength(0);

            } else {

                current.append(character);
            }
        }

        values.add(
                current.toString().trim()
        );

        return values;
    }
}