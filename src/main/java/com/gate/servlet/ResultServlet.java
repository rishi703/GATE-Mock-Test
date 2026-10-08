package com.gate.servlet;

import com.gate.model.Question;
import com.gate.repository.AttemptedHistoryRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/result")
public class ResultServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // =====================================================
        // GET EXISTING SESSION
        // =====================================================

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            response.sendRedirect("login.html");
            return;
        }

        // =====================================================
        // GET LOGGED-IN USER
        // =====================================================

        String username =
                (String) session.getAttribute("username");

        if (username == null ||
                username.trim().isEmpty()) {

            response.sendRedirect("login.html");
            return;
        }

        // =====================================================
        // GET CURRENT SUBJECT
        // =====================================================

        String subject =
                (String) session.getAttribute(
                        "currentSubject"
                );

        // =====================================================
        // GET CURRENT TEST
        // =====================================================

        Object testObject =
                session.getAttribute(
                        "currentTest"
                );

        int test;

        if (testObject instanceof Integer) {

            test = (Integer) testObject;

        } else if (testObject instanceof String) {

            try {

                test = Integer.parseInt(
                        (String) testObject
                );

            } catch (NumberFormatException e) {

                response.sendRedirect("index.html");
                return;
            }

        } else {

            response.sendRedirect("index.html");
            return;
        }

        if (subject == null ||
                subject.trim().isEmpty()) {

            response.sendRedirect("index.html");
            return;
        }

        // =====================================================
        // GET QUESTIONS
        // =====================================================

        /*
         * MockTestServlet stores questions using:
         *
         * attempt_<subject>_<test>
         */

        String attemptKey =
                "attempt_" +
                subject +
                "_" +
                test;

        @SuppressWarnings("unchecked")
        List<Question> questions =
                (List<Question>) session.getAttribute(
                        attemptKey
                );

        if (questions == null ||
                questions.isEmpty()) {

            response.sendRedirect(
                    "mock-test?subject=" +
                    URLEncoder.encode(
                            subject,
                            StandardCharsets.UTF_8
                    ) +
                    "&test=" +
                    test
            );

            return;
        }

        // =====================================================
        // GET ANSWERS
        // =====================================================

        /*
         * MockTestServlet stores answers using:
         *
         * answers_<subject>_<test>
         *
         * The values are:
         *
         * A / B / C / D
         */

        String answersKey =
                "answers_" +
                subject +
                "_" +
                test;

        @SuppressWarnings("unchecked")
        Map<Integer, String> answers =
                (Map<Integer, String>)
                        session.getAttribute(
                                answersKey
                        );

        if (answers == null) {

            answers =
                    new HashMap<>();
        }

        // =====================================================
        // CALCULATE RESULT
        // =====================================================

        int total =
                questions.size();

        int attempted = 0;
        int correct = 0;
        int incorrect = 0;
        int unanswered = 0;

        Map<String, int[]> topicStats =
                new HashMap<>();

        Map<String, int[]> difficultyStats =
                new HashMap<>();

        // =====================================================
        // PROCESS EACH QUESTION
        // =====================================================

        for (int i = 0;
             i < questions.size();
             i++) {

            Question q =
                    questions.get(i);

            /*
             * Answers are stored using the question INDEX,
             * not the question ID.
             */
            String selectedLetter =
                    answers.get(i);

            boolean isCorrect = false;

            // =================================================
            // CHECK ANSWER
            // =================================================

            if (selectedLetter == null ||
                    selectedLetter.trim().isEmpty()) {

                unanswered++;

            } else {

                attempted++;

                int selectedNumber =
                        convertAnswerToNumber(
                                selectedLetter
                        );

                if (selectedNumber ==
                        q.getAnswer()) {

                    correct++;
                    isCorrect = true;

                } else {

                    incorrect++;
                }
            }

            // =================================================
            // TOPIC STATISTICS
            // =================================================

            String topic =
                    q.getTopic();

            if (topic == null ||
                    topic.trim().isEmpty()) {

                topic = "General";
            }

            int[] topicData =
                    topicStats.computeIfAbsent(
                            topic,
                            k -> new int[2]
                    );

            topicData[0]++;

            if (isCorrect) {
                topicData[1]++;
            }

            // =================================================
            // DIFFICULTY STATISTICS
            // =================================================

            String difficulty =
                    q.getDifficulty();

            if (difficulty == null ||
                    difficulty.trim().isEmpty()) {

                difficulty = "Unknown";
            }

            int[] difficultyData =
                    difficultyStats.computeIfAbsent(
                            difficulty,
                            k -> new int[2]
                    );

            difficultyData[0]++;

            if (isCorrect) {
                difficultyData[1]++;
            }
        }

        // =====================================================
        // ACCURACY
        // =====================================================

        double accuracy =
                attempted == 0
                        ? 0
                        : ((double) correct /
                           attempted) * 100;

        double percentage =
                total == 0
                        ? 0
                        : ((double) correct /
                           total) * 100;

        // =====================================================
        // SAVE ATTEMPT HISTORY
        // =====================================================

        String historyKey =
                "history_saved_" +
                username +
                "_" +
                subject +
                "_test_" +
                test;

        Boolean historySaved =
                (Boolean) session.getAttribute(
                        historyKey
                );

        if (!Boolean.TRUE.equals(historySaved)) {

            AttemptedHistoryRepository.saveAttempt(
                    username,
                    subject,
                    test,
                    total,
                    attempted,
                    correct,
                    incorrect,
                    unanswered,
                    accuracy,
                    percentage
            );

            session.setAttribute(
                    historyKey,
                    Boolean.TRUE
            );
        }

        // =====================================================
        // RESULT PAGE
        // =====================================================

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        PrintWriter out =
                response.getWriter();

        out.println("""
                <!DOCTYPE html>
                <html lang="en">

                <head>

                    <meta charset="UTF-8">

                    <meta name="viewport"
                          content="width=device-width,
                                   initial-scale=1.0">

                    <title>Test Result</title>

                    <style>

                        * {
                            box-sizing: border-box;
                        }

                        body {
                            margin: 0;
                            font-family: Arial, sans-serif;
                            background: #f4f6f9;
                            color: #1f2937;
                        }

                        .header {
                            background: #111827;
                            color: white;
                            padding: 18px 40px;
                            display: flex;
                            justify-content: space-between;
                            align-items: center;
                        }

                        .header h1 {
                            margin: 0;
                            font-size: 22px;
                        }

                        .header span {
                            color: #d1d5db;
                            font-size: 14px;
                        }

                        .container {
                            max-width: 1100px;
                            margin: 35px auto;
                            padding: 0 20px;
                        }

                        .title {
                            text-align: center;
                            margin-bottom: 25px;
                        }

                        .title h2 {
                            margin-bottom: 8px;
                            font-size: 30px;
                        }

                        .title p {
                            color: #6b7280;
                            margin: 0;
                        }

                        .score-card {
                            background: white;
                            border-radius: 14px;
                            padding: 30px;
                            text-align: center;
                            box-shadow:
                                0 4px 15px
                                rgba(0,0,0,0.08);
                            margin-bottom: 25px;
                        }

                        .score {
                            font-size: 52px;
                            font-weight: bold;
                            margin: 10px 0;
                        }

                        .score-label {
                            color: #6b7280;
                            font-size: 15px;
                        }

                        .stats {
                            display: grid;
                            grid-template-columns:
                                repeat(4, 1fr);
                            gap: 15px;
                            margin-bottom: 25px;
                        }

                        .stat {
                            background: white;
                            border-radius: 12px;
                            padding: 22px;
                            text-align: center;
                            box-shadow:
                                0 3px 12px
                                rgba(0,0,0,0.06);
                        }

                        .stat h3 {
                            margin: 0;
                            font-size: 28px;
                        }

                        .stat p {
                            margin: 7px 0 0;
                            color: #6b7280;
                            font-size: 14px;
                        }

                        .correct h3 {
                            color: #16a34a;
                        }

                        .wrong h3 {
                            color: #dc2626;
                        }

                        .unanswered h3 {
                            color: #d97706;
                        }

                        .accuracy h3 {
                            color: #2563eb;
                        }

                        .section {
                            background: white;
                            padding: 25px;
                            border-radius: 12px;
                            margin-bottom: 25px;
                            box-shadow:
                                0 3px 12px
                                rgba(0,0,0,0.06);
                        }

                        .section h3 {
                            margin-top: 0;
                            margin-bottom: 18px;
                        }

                        table {
                            width: 100%;
                            border-collapse: collapse;
                        }

                        th,
                        td {
                            padding: 12px;
                            border-bottom: 1px solid
                                       #e5e7eb;
                            text-align: left;
                        }

                        th {
                            background: #f9fafb;
                            font-size: 14px;
                        }

                        td {
                            font-size: 14px;
                        }

                        .buttons {
                            text-align: center;
                            margin-top: 30px;
                            margin-bottom: 30px;
                        }

                        .btn {
                            display: inline-block;
                            padding: 13px 24px;
                            margin: 5px;
                            border-radius: 8px;
                            text-decoration: none;
                            font-weight: bold;
                            font-size: 14px;
                        }

                        .dashboard {
                            background: #111827;
                            color: white;
                        }

                        .again {
                            background: #2563eb;
                            color: white;
                        }

                        .history {
                            background: #16a34a;
                            color: white;
                        }

                        @media (max-width: 700px) {

                            .stats {
                                grid-template-columns:
                                    repeat(2, 1fr);
                            }

                            .header {
                                padding: 15px 20px;
                            }

                            .container {
                                margin-top: 20px;
                            }

                        }

                    </style>

                </head>

                <body>

                    <div class="header">

                        <h1>GATE Mock Test</h1>

                        <span>
                """);

        out.println(
                escape(subject)
                + " &nbsp; | &nbsp; Test "
                + test
        );

        out.println("""
                        </span>

                    </div>

                    <div class="container">

                        <div class="title">

                            <h2>Test Result</h2>

                            <p>
                                Your performance summary
                            </p>

                        </div>

                        <div class="score-card">

                            <div class="score">
                """);

        out.printf(
                "%.2f%%",
                percentage
        );

        out.println("""
                            </div>

                            <div class="score-label">
                                Overall Score
                            </div>

                        </div>

                        <div class="stats">

                            <div class="stat">

                                <h3>
                """);

        out.println(total);

        out.println("""
                                </h3>

                                <p>Total Questions</p>

                            </div>

                            <div class="stat correct">

                                <h3>
                """);

        out.println(correct);

        out.println("""
                                </h3>

                                <p>Correct</p>

                            </div>

                            <div class="stat wrong">

                                <h3>
                """);

        out.println(incorrect);

        out.println("""
                                </h3>

                                <p>Incorrect</p>

                            </div>

                            <div class="stat unanswered">

                                <h3>
                """);

        out.println(unanswered);

        out.println("""
                                </h3>

                                <p>Unanswered</p>

                            </div>

                        </div>

                        <div class="stats">

                            <div class="stat accuracy">

                                <h3>
                """);

        out.printf(
                "%.2f%%",
                accuracy
        );

        out.println("""
                                </h3>

                                <p>Accuracy</p>

                            </div>

                            <div class="stat">

                                <h3>
                """);

        out.println(attempted);

        out.println("""
                                </h3>

                                <p>Attempted</p>

                            </div>

                        </div>

                        <div class="section">

                            <h3>
                                Topic-wise Performance
                            </h3>

                            <table>

                                <tr>
                                    <th>Topic</th>
                                    <th>Total</th>
                                    <th>Correct</th>
                                    <th>Accuracy</th>
                                </tr>
                """);

        for (Map.Entry<String, int[]> entry :
                topicStats.entrySet()) {

            String topic =
                    entry.getKey();

            int topicTotal =
                    entry.getValue()[0];

            int topicCorrect =
                    entry.getValue()[1];

            double topicAccuracy =
                    topicTotal == 0
                            ? 0
                            : ((double) topicCorrect /
                               topicTotal) * 100;

            out.println("<tr>");

            out.println(
                    "<td>" +
                    escape(topic) +
                    "</td>"
            );

            out.println(
                    "<td>" +
                    topicTotal +
                    "</td>"
            );

            out.println(
                    "<td>" +
                    topicCorrect +
                    "</td>"
            );

            out.printf(
                    "<td>%.2f%%</td>",
                    topicAccuracy
            );

            out.println("</tr>");
        }

        out.println("""
                            </table>

                        </div>

                        <div class="section">

                            <h3>
                                Difficulty-wise Performance
                            </h3>

                            <table>

                                <tr>
                                    <th>Difficulty</th>
                                    <th>Total</th>
                                    <th>Correct</th>
                                    <th>Accuracy</th>
                                </tr>
                """);

        for (Map.Entry<String, int[]> entry :
                difficultyStats.entrySet()) {

            String difficulty =
                    entry.getKey();

            int difficultyTotal =
                    entry.getValue()[0];

            int difficultyCorrect =
                    entry.getValue()[1];

            double difficultyAccuracy =
                    difficultyTotal == 0
                            ? 0
                            : ((double) difficultyCorrect /
                               difficultyTotal) * 100;

            out.println("<tr>");

            out.println(
                    "<td>" +
                    escape(difficulty) +
                    "</td>"
            );

            out.println(
                    "<td>" +
                    difficultyTotal +
                    "</td>"
            );

            out.println(
                    "<td>" +
                    difficultyCorrect +
                    "</td>"
            );

            out.printf(
                    "<td>%.2f%%</td>",
                    difficultyAccuracy
            );

            out.println("</tr>");
        }

        out.println("""
                            </table>

                        </div>

                        <div class="buttons">

                            <a class="btn again"
                               href="mock-test?subject=
                """);

        out.print(
                URLEncoder.encode(
                        subject,
                        StandardCharsets.UTF_8
                )
        );

        out.println(
                "&test=" +
                test +
                "&newAttempt=true\">"
        );

        out.println("""
                                Retake Test
                            </a>

                            <a class="btn history"
                               href="history">
                                View Attempt History
                            </a>

                            <a class="btn dashboard"
                               href="index.html">
                                Back to Dashboard
                            </a>

                        </div>

                    </div>

                </body>

                </html>
                """);
    }

    // =========================================================
    // CONVERT A/B/C/D TO 1/2/3/4
    // =========================================================

    private int convertAnswerToNumber(
            String answer) {

        if (answer == null) {
            return -1;
        }

        switch (answer.trim().toUpperCase()) {

            case "A":
                return 1;

            case "B":
                return 2;

            case "C":
                return 3;

            case "D":
                return 4;

            default:
                return -1;
        }
    }

    // =========================================================
    // HTML ESCAPE
    // =========================================================

    private String escape(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("&", "&amp;")
                .replace("<", "&lt;")
                .replace(">", "&gt;")
                .replace("\"", "&quot;")
                .replace("'", "&#39;");
    }
}