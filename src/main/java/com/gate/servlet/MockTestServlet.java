package com.gate.servlet;

import com.gate.model.Question;
import com.gate.repository.QuestionRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/mock-test")
public class MockTestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final long TEST_DURATION =
            30L * 60L * 1000L;

    private QuestionRepository repository;

    @Override
    public void init() throws ServletException {
        repository = new QuestionRepository(getServletContext());
    }

    // ============================================================
    // GET
    // ============================================================

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // ========================================================
        // LOGIN CHECK
        // ========================================================

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("username") == null) {

            response.sendRedirect("login.html");
            return;
        }

        // ========================================================
        // GET PARAMETERS
        // ========================================================

        String subject =
                request.getParameter("subject");

        String test =
                request.getParameter("test");

        String newAttempt =
                request.getParameter("newAttempt");

        // ========================================================
        // SUBJECT CHECK
        // ========================================================

        if (subject == null ||
                subject.trim().isEmpty()) {

            response.sendRedirect("index.html");
            return;
        }

        // ========================================================
        // IF TEST IS NOT SELECTED
        // SHOW TEST SELECTION
        // ========================================================

        if (test == null ||
                test.trim().isEmpty()) {

            showTestSelection(
                    request,
                    response,
                    subject
            );

            return;
        }

        // ========================================================
        // TEST NUMBER
        // ========================================================

        int testNumber;

        try {

            testNumber =
                    Integer.parseInt(test);

        } catch (NumberFormatException e) {

            response.sendRedirect(
                    "mock-test?subject=" +
                    subject
            );

            return;
        }

        // ========================================================
        // GET QUESTIONS
        // ========================================================

        List<Question> allQuestions =
                repository.getQuestionsBySubject(
                        subject
                );

        if (allQuestions == null ||
                allQuestions.isEmpty()) {

            response.setContentType(
                    "text/html;charset=UTF-8"
            );

            PrintWriter out =
                    response.getWriter();

            out.println("""
                <!DOCTYPE html>
                <html>
                <head>
                    <title>No Questions</title>
                    <style>
                        body {
                            font-family: Arial, sans-serif;
                            background: #f5f7fb;
                            text-align: center;
                            padding-top: 100px;
                        }

                        h2 {
                            color: #333;
                        }

                        a {
                            display: inline-block;
                            margin-top: 20px;
                            padding: 12px 20px;
                            background: #3157d5;
                            color: white;
                            text-decoration: none;
                            border-radius: 8px;
                        }
                    </style>
                </head>

                <body>

                    <h2>No questions available for this subject.</h2>

                    <a href="index.html">
                        Back to Home
                    </a>

                </body>
                </html>
                """);

            return;
        }

        // ========================================================
        // CREATE / GET ATTEMPT
        // ========================================================

        String attemptKey =
                "attempt_" +
                subject +
                "_" +
                testNumber;

        @SuppressWarnings("unchecked")
        List<Question> questions =
                (List<Question>) session.getAttribute(
                        attemptKey
                );

        // ========================================================
        // NEW ATTEMPT
        // ========================================================

        if ("true".equalsIgnoreCase(newAttempt) ||
                questions == null) {

            questions =
                    new ArrayList<>(
                            allQuestions
                    );

            Collections.shuffle(
                    questions
            );

            // Maximum 25 questions
            if (questions.size() > 25) {

                questions =
                        new ArrayList<>(
                                questions.subList(
                                        0,
                                        25
                                )
                        );
            }

            session.setAttribute(
                    attemptKey,
                    questions
            );

            // Save current test information
            session.setAttribute(
                    "currentSubject",
                    subject
            );

            session.setAttribute(
                    "currentTest",
                    testNumber
            );

            // Start time
            session.setAttribute(
                    "testStartTime",
                    System.currentTimeMillis()
            );

            // ====================================================
            // CLEAR OLD ANSWERS
            // ====================================================

            String answersKey =
                    "answers_" +
                    subject +
                    "_" +
                    testNumber;

            session.removeAttribute(
                    answersKey
            );

            // ====================================================
            // CLEAR REVIEW
            // ====================================================

            String reviewKey =
                    "review_" +
                    subject +
                    "_" +
                    testNumber;

            session.removeAttribute(
                    reviewKey
            );
        }

        // ========================================================
        // CURRENT QUESTION
        // ========================================================

        String questionParam =
                request.getParameter("question");

        int questionIndex = 0;

        if (questionParam != null) {

            try {

                questionIndex =
                        Integer.parseInt(
                                questionParam
                        );

            } catch (NumberFormatException e) {

                questionIndex = 0;
            }
        }

        // Keep question index valid
        if (questionIndex < 0) {
            questionIndex = 0;
        }

        if (questionIndex >= questions.size()) {

            questionIndex =
                    questions.size() - 1;
        }

        // ========================================================
        // SHOW QUESTION
        // ========================================================

        showQuestion(
                request,
                response,
                subject,
                testNumber,
                questions,
                questionIndex
        );
    }

    // ============================================================
    // POST
    // ============================================================

    @Override
    protected void doPost(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        // ========================================================
        // LOGIN CHECK
        // ========================================================

        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("username") == null) {

            response.sendRedirect("login.html");
            return;
        }

        // ========================================================
        // CURRENT TEST INFORMATION
        // ========================================================

        String subject =
                (String) session.getAttribute(
                        "currentSubject"
                );

        Object testObject =
                session.getAttribute(
                        "currentTest"
                );

        if (subject == null ||
                testObject == null) {

            response.sendRedirect(
                    "index.html"
            );

            return;
        }

        int testNumber =
                (Integer) testObject;

        // ========================================================
        // GET ACTION
        // ========================================================

        String action =
                request.getParameter(
                        "action"
                );

        // ========================================================
        // GET QUESTION INDEX
        // ========================================================

        int questionIndex = 0;

        String questionParam =
                request.getParameter(
                        "question"
                );

        if (questionParam != null) {

            try {

                questionIndex =
                        Integer.parseInt(
                                questionParam
                        );

            } catch (NumberFormatException e) {

                questionIndex = 0;
            }
        }

        // ========================================================
        // GET QUESTIONS
        // ========================================================

        String attemptKey =
                "attempt_" +
                subject +
                "_" +
                testNumber;

        @SuppressWarnings("unchecked")
        List<Question> questions =
                (List<Question>) session.getAttribute(
                        attemptKey
                );

        if (questions == null ||
                questions.isEmpty()) {

            response.sendRedirect(
                    "mock-test?subject=" +
                    subject +
                    "&test=" +
                    testNumber
            );

            return;
        }

        // ========================================================
        // ANSWERS MAP
        // ========================================================

        String answersKey =
                "answers_" +
                subject +
                "_" +
                testNumber;

        @SuppressWarnings("unchecked")
        Map<Integer, String> answers =
                (Map<Integer, String>)
                        session.getAttribute(
                                answersKey
                        );

        if (answers == null) {

            answers =
                    new HashMap<>();

            session.setAttribute(
                    answersKey,
                    answers
            );
        }

        // ========================================================
        // SAVE CURRENT ANSWER
        // ========================================================

        String selectedAnswer =
                request.getParameter(
                        "answer"
                );

        if (selectedAnswer != null &&
                !selectedAnswer.trim().isEmpty()) {

            answers.put(
                    questionIndex,
                    selectedAnswer
            );
        }

        // ========================================================
        // REVIEW MAP
        // ========================================================

        String reviewKey =
                "review_" +
                subject +
                "_" +
                testNumber;

        @SuppressWarnings("unchecked")
        Map<Integer, Boolean> reviewMap =
                (Map<Integer, Boolean>)
                        session.getAttribute(
                                reviewKey
                        );

        if (reviewMap == null) {

            reviewMap =
                    new HashMap<>();

            session.setAttribute(
                    reviewKey,
                    reviewMap
            );
        }

        // ========================================================
        // MARK FOR REVIEW
        // ========================================================

        if ("markReview".equals(action)) {

            reviewMap.put(
                    questionIndex,
                    true
            );

            if (questionIndex <
                    questions.size() - 1) {

                questionIndex++;
            }
        }

        // ========================================================
        // REMOVE REVIEW
        // ========================================================

        else if ("removeReview".equals(action)) {

            reviewMap.remove(
                    questionIndex
            );
        }

        // ========================================================
        // NEXT
        // ========================================================

        else if ("next".equals(action)) {

            if (questionIndex <
                    questions.size() - 1) {

                questionIndex++;
            }
        }

        // ========================================================
        // PREVIOUS
        // ========================================================

        else if ("previous".equals(action)) {

            if (questionIndex > 0) {

                questionIndex--;
            }
        }

        // ========================================================
        // GO TO QUESTION
        // ========================================================

        else if ("goto".equals(action)) {

            String gotoQuestion =
                    request.getParameter(
                            "gotoQuestion"
                    );

            if (gotoQuestion != null) {

                try {

                    questionIndex =
                            Integer.parseInt(
                                    gotoQuestion
                            );

                } catch (NumberFormatException e) {

                    questionIndex = 0;
                }
            }
        }

        // ========================================================
        // SUBMIT TEST
        // ========================================================

        else if ("submit".equals(action) ||
                 "timeup".equals(action)) {

            response.sendRedirect(
                    "result?subject=" +
                    subject +
                    "&test=" +
                    testNumber
            );

            return;
        }

        // ========================================================
        // ENSURE INDEX IS VALID
        // ========================================================

        if (questionIndex < 0) {

            questionIndex = 0;
        }

        if (questionIndex >= questions.size()) {

            questionIndex =
                    questions.size() - 1;
        }

        // ========================================================
        // SHOW QUESTION
        // ========================================================

        showQuestion(
                request,
                response,
                subject,
                testNumber,
                questions,
                questionIndex
        );
    }

    // ============================================================
    // TEST SELECTION PAGE
    // ============================================================

    private void showTestSelection(
            HttpServletRequest request,
            HttpServletResponse response,
            String subject)
            throws IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        PrintWriter out =
                response.getWriter();

        String safeSubject =
                escapeHtml(subject);

        out.println("""
            <!DOCTYPE html>
            <html lang="en">

            <head>

                <meta charset="UTF-8">

                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0">

                <title>Select Test</title>

                <style>

                    * {
                        box-sizing: border-box;
                        margin: 0;
                        padding: 0;
                    }

                    body {
                        font-family: Arial, sans-serif;
                        background: #f5f7fb;
                        min-height: 100vh;
                        display: flex;
                        justify-content: center;
                        align-items: center;
                    }

                    .container {
                        width: 90%;
                        max-width: 700px;
                        background: white;
                        padding: 40px;
                        border-radius: 16px;
                        box-shadow: 0 10px 30px rgba(0,0,0,0.08);
                        text-align: center;
                    }

                    h1 {
                        color: #222;
                        margin-bottom: 10px;
                    }

                    .subject {
                        color: #3157d5;
                        font-size: 20px;
                        font-weight: bold;
                        margin-bottom: 30px;
                    }

                    .tests {
                        display: grid;
                        grid-template-columns:
                            repeat(auto-fit, minmax(220px, 1fr));
                        gap: 20px;
                    }

                    .test-card {
                        border: 1px solid #e2e5ec;
                        border-radius: 12px;
                        padding: 25px;
                        transition: 0.2s;
                    }

                    .test-card:hover {
                        transform: translateY(-4px);
                        box-shadow:
                            0 8px 20px rgba(0,0,0,0.08);
                    }

                    .test-card h2 {
                        margin-bottom: 10px;
                        color: #222;
                    }

                    .test-card p {
                        color: #666;
                        margin-bottom: 20px;
                    }

                    .btn {
                        display: inline-block;
                        background: #3157d5;
                        color: white;
                        text-decoration: none;
                        padding: 11px 20px;
                        border-radius: 8px;
                        font-weight: bold;
                    }

                    .btn:hover {
                        background: #2445b5;
                    }

                    .back {
                        display: inline-block;
                        margin-top: 25px;
                        color: #555;
                        text-decoration: none;
                    }

                </style>

            </head>

            <body>

                <div class="container">

                    <h1>Select Your Test</h1>

                    <div class="subject">
            """);

        out.println(safeSubject);

        out.println("""
                    </div>

                    <div class="tests">

                        <div class="test-card">

                            <h2>Test 1</h2>

                            <p>
                                25 Questions<br>
                                30 Minutes
                            </p>

                            <a class="btn"
                               href="mock-test?subject=
            """);

        out.println(
                java.net.URLEncoder.encode(
                        subject,
                        "UTF-8"
                )
        );

        out.println("""
                                &test=1&newAttempt=true">
                                Start Test 1
                            </a>

                        </div>


                        <div class="test-card">

                            <h2>Test 2</h2>

                            <p>
                                25 Questions<br>
                                30 Minutes
                            </p>

                            <a class="btn"
                               href="mock-test?subject=
            """);

        out.println(
                java.net.URLEncoder.encode(
                        subject,
                        "UTF-8"
                )
        );

        out.println("""
                                &test=2&newAttempt=true">
                                Start Test 2
                            </a>

                        </div>

                    </div>

                    <a class="back"
                       href="index.html">
                        ← Back to Home
                    </a>

                </div>

            </body>

            </html>
            """);
    }

    // ============================================================
    // QUESTION PAGE
    // ============================================================

    private void showQuestion(
            HttpServletRequest request,
            HttpServletResponse response,
            String subject,
            int testNumber,
            List<Question> questions,
            int questionIndex)
            throws IOException {

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        PrintWriter out =
                response.getWriter();

        // ========================================================
        // TIMER
        // ========================================================

        HttpSession session =
                request.getSession(false);

        Long startTime =
                (Long) session.getAttribute(
                        "testStartTime"
                );

        long remainingTime =
                TEST_DURATION;

        if (startTime != null) {

            long elapsed =
                    System.currentTimeMillis()
                    - startTime;

            remainingTime =
                    TEST_DURATION - elapsed;
        }

        if (remainingTime < 0) {

            remainingTime = 0;
        }

        long remainingSeconds =
                remainingTime / 1000;

        // ========================================================
        // CURRENT QUESTION
        // ========================================================

        Question question =
                questions.get(
                        questionIndex
                );

        // ========================================================
        // ANSWERS
        // ========================================================

        String answersKey =
                "answers_" +
                subject +
                "_" +
                testNumber;

        @SuppressWarnings("unchecked")
        Map<Integer, String> answers =
                (Map<Integer, String>)
                        session.getAttribute(
                                answersKey
                        );

        if (answers == null) {

            answers =
                    new HashMap<>();

            session.setAttribute(
                    answersKey,
                    answers
            );
        }

        String selectedAnswer =
                answers.get(
                        questionIndex
                );

        // ========================================================
        // REVIEW
        // ========================================================

        String reviewKey =
                "review_" +
                subject +
                "_" +
                testNumber;

        @SuppressWarnings("unchecked")
        Map<Integer, Boolean> reviewMap =
                (Map<Integer, Boolean>)
                        session.getAttribute(
                                reviewKey
                        );

        if (reviewMap == null) {

            reviewMap =
                    new HashMap<>();
        }

        boolean markedForReview =
                Boolean.TRUE.equals(
                        reviewMap.get(
                                questionIndex
                        )
                );

        // ========================================================
        // HTML
        // ========================================================

        out.println("""
            <!DOCTYPE html>
            <html lang="en">

            <head>

                <meta charset="UTF-8">

                <meta name="viewport"
                      content="width=device-width, initial-scale=1.0">

                <title>Mock Test</title>

                <style>

                    * {
                        box-sizing: border-box;
                        margin: 0;
                        padding: 0;
                    }

                    body {
                        font-family: Arial, sans-serif;
                        background: #f4f6fb;
                        color: #222;
                    }

                    .topbar {
                        background: white;
                        padding: 15px 25px;
                        display: flex;
                        justify-content: space-between;
                        align-items: center;
                        box-shadow:
                            0 2px 10px rgba(0,0,0,0.06);
                        position: sticky;
                        top: 0;
                        z-index: 100;
                    }

                    .title {
                        font-size: 20px;
                        font-weight: bold;
                    }

                    .timer {
                        background: #3157d5;
                        color: white;
                        padding: 10px 18px;
                        border-radius: 8px;
                        font-size: 18px;
                        font-weight: bold;
                    }

                    .timer.warning {
                        background: #d9534f;
                    }

                    .main {
                        display: flex;
                        gap: 20px;
                        max-width: 1300px;
                        margin: 25px auto;
                        padding: 0 20px;
                    }

                    .question-area {
                        flex: 1;
                        background: white;
                        border-radius: 12px;
                        padding: 30px;
                        min-height: 550px;
                        box-shadow:
                            0 5px 20px rgba(0,0,0,0.05);
                    }

                    .sidebar {
                        width: 280px;
                        background: white;
                        border-radius: 12px;
                        padding: 20px;
                        box-shadow:
                            0 5px 20px rgba(0,0,0,0.05);
                    }

                    .question-number {
                        color: #3157d5;
                        font-weight: bold;
                        margin-bottom: 15px;
                    }

                    .question-text {
                        font-size: 20px;
                        line-height: 1.6;
                        margin-bottom: 25px;
                    }

                    .option {
                        border: 1px solid #ddd;
                        padding: 14px;
                        border-radius: 8px;
                        margin-bottom: 12px;
                        cursor: pointer;
                        transition: 0.2s;
                    }

                    .option:hover {
                        border-color: #3157d5;
                        background: #f5f7ff;
                    }

                    .option input {
                        margin-right: 10px;
                    }

                    .buttons {
                        display: flex;
                        justify-content: space-between;
                        margin-top: 30px;
                        gap: 10px;
                        flex-wrap: wrap;
                    }

                    button {
                        border: none;
                        padding: 11px 18px;
                        border-radius: 8px;
                        cursor: pointer;
                        font-weight: bold;
                    }

                    .previous {
                        background: #e9ebf0;
                    }

                    .next {
                        background: #3157d5;
                        color: white;
                    }

                    .review {
                        background: #f0ad4e;
                        color: white;
                    }

                    .submit {
                        background: #d9534f;
                        color: white;
                    }

                    .palette-title {
                        font-weight: bold;
                        margin-bottom: 15px;
                    }

                    .palette {
                        display: grid;
                        grid-template-columns:
                            repeat(5, 1fr);
                        gap: 8px;
                    }

                    .palette button {
                        padding: 9px 5px;
                        background: #eee;
                    }

                    .palette button.answered {
                        background: #5cb85c;
                        color: white;
                    }

                    .palette button.current {
                        outline: 3px solid #3157d5;
                    }

                    .palette button.reviewed {
                        background: #f0ad4e;
                        color: white;
                    }

                    .info {
                        margin-top: 20px;
                        line-height: 1.8;
                        color: #555;
                    }

                    @media(max-width: 850px) {

                        .main {
                            flex-direction: column;
                        }

                        .sidebar {
                            width: 100%;
                        }

                    }

                </style>

            </head>

            <body>

                <div class="topbar">

                    <div class="title">
                        GATE Mock Test
                    </div>

                    <div id="timer"
                         class="timer">
                        30:00
                    </div>

                </div>

                <div class="main">

                    <div class="question-area">

            """);

        // ========================================================
        // QUESTION NUMBER
        // ========================================================

        out.println(
                "<div class='question-number'>" +
                "Question " +
                (questionIndex + 1) +
                " of " +
                questions.size() +
                "</div>"
        );

        // ========================================================
        // QUESTION
        // ========================================================

        out.println(
                "<div class='question-text'>" +
                escapeHtml(
                        question.getQuestion()
                ) +
                "</div>"
        );

        // ========================================================
        // OPTIONS
        // ========================================================

        String[] options = {
                question.getOption1(),
                question.getOption2(),
                question.getOption3(),
                question.getOption4()
        };

        String[] optionLetters = {
                "A",
                "B",
                "C",
                "D"
        };

        out.println(
                "<form method='post' action='mock-test'>"
        );

        out.println(
                "<input type='hidden' name='question' value='" +
                questionIndex +
                "'>"
        );

        for (int i = 0;
             i < options.length;
             i++) {

            String letter =
                    optionLetters[i];

            boolean checked =
                    letter.equals(
                            selectedAnswer
                    );

            out.println(
                    "<label class='option'>"
            );

            out.println(
                    "<input type='radio' " +
                    "name='answer' " +
                    "value='" +
                    letter +
                    "'" +
                    (checked
                            ? " checked"
                            : "") +
                    ">"
            );

            out.println(
                    "<strong>" +
                    letter +
                    ".</strong> " +
                    escapeHtml(
                            options[i]
                    )
            );

            out.println(
                    "</label>"
            );
        }

        // ========================================================
        // BUTTONS
        // ========================================================

        out.println("""
                    <div class="buttons">
            """);

        // Previous
        if (questionIndex > 0) {

            out.println("""
                        <button
                            type="submit"
                            name="action"
                            value="previous"
                            class="previous">
                            ← Previous
                        </button>
                """);
        }

        // Review
        if (markedForReview) {

            out.println("""
                        <button
                            type="submit"
                            name="action"
                            value="removeReview"
                            class="review">
                            Remove Review
                        </button>
                """);

        } else {

            out.println("""
                        <button
                            type="submit"
                            name="action"
                            value="markReview"
                            class="review">
                            Mark for Review
                        </button>
                """);
        }

        // Next / Submit
        if (questionIndex <
                questions.size() - 1) {

            out.println("""
                        <button
                            type="submit"
                            name="action"
                            value="next"
                            class="next">
                            Save & Next →
                        </button>
                """);

        } else {

            out.println("""
                        <button
                            type="submit"
                            name="action"
                            value="submit"
                            class="submit">
                            Submit Test
                        </button>
                """);
        }

        out.println("""
                    </div>

                </form>

            </div>

            <div class="sidebar">

                <div class="palette-title">
                    Question Palette
                </div>

                <div class="palette">
            """);

        // ========================================================
        // QUESTION PALETTE
        // ========================================================

        for (int i = 0;
             i < questions.size();
             i++) {

            String classes = "";

            if (i == questionIndex) {

                classes += " current";
            }

            if (answers.containsKey(i)) {

                classes += " answered";
            }

            if (Boolean.TRUE.equals(
                    reviewMap.get(i))) {

                classes += " reviewed";
            }

            out.println(
                    "<form method='post' " +
                    "action='mock-test' " +
                    "style='display:inline;'>"
            );

            out.println(
                    "<input type='hidden' " +
                    "name='gotoQuestion' " +
                    "value='" +
                    i +
                    "'>"
            );

            out.println(
                    "<button " +
                    "type='submit' " +
                    "name='action' " +
                    "value='goto' " +
                    "class='" +
                    classes +
                    "'>" +
                    (i + 1) +
                    "</button>"
            );

            out.println(
                    "</form>"
            );
        }

        out.println("""
                </div>

                <div class="info">

                    <p>
                        <strong>Subject:</strong>
            """);

        out.println(
                escapeHtml(subject)
        );

        out.println("""
                    </p>

                    <p>
                        <strong>Test:</strong>
            """);

        out.println(
                testNumber
        );

        out.println("""
                    </p>

                    <p>
                        <strong>Total Questions:</strong>
            """);

        out.println(
                questions.size()
        );

        out.println("""
                    </p>

                    <p>
                        <strong>Answered:</strong>
            """);

        out.println(
                answers.size()
        );

        out.println("""
                    </p>

                </div>

            </div>

        </div>


        <script>

            let remainingSeconds =
            """);

        out.println(
                remainingSeconds
        );

        out.println("""
            ;

            const timer =
                document.getElementById("timer");

            function updateTimer() {

                let minutes =
                    Math.floor(
                        remainingSeconds / 60
                    );

                let seconds =
                    remainingSeconds % 60;

                let formattedSeconds =
                    seconds < 10
                    ? "0" + seconds
                    : seconds;

                timer.innerText =
                    minutes +
                    ":" +
                    formattedSeconds;

                if (remainingSeconds <= 300) {
                    timer.classList.add("warning");
                }

                if (remainingSeconds <= 0) {

                    clearInterval(
                        timerInterval
                    );

                    const form =
                        document.createElement(
                            "form"
                        );

                    form.method = "post";
                    form.action = "mock-test";

                    const action =
                        document.createElement(
                            "input"
                        );

                    action.type = "hidden";
                    action.name = "action";
                    action.value = "timeup";

                    form.appendChild(action);

                    document.body.appendChild(form);

                    form.submit();

                    return;
                }

                remainingSeconds--;

            }

            updateTimer();

            const timerInterval =
                setInterval(
                    updateTimer,
                    1000
                );

        </script>

        </body>

        </html>
        """);
    }

    // ============================================================
    // HTML ESCAPE
    // ============================================================

    private String escapeHtml(String value) {

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