
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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@WebServlet("/mock-test")
public class MockTestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    /*
     * 30 minutes
     */
    private static final long TEST_DURATION =
            30L * 60L * 1000L;

    private QuestionRepository repository;

    @Override
    public void init() throws ServletException {
        repository = new QuestionRepository(getServletContext());
    }

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        String subject = request.getParameter("subject");
        String test = request.getParameter("test");
        String newAttempt = request.getParameter("newAttempt");

        if (subject == null || subject.trim().isEmpty()) {
            response.sendRedirect("index.html");
            return;
        }

        if (test == null ||
                (!test.equals("1") && !test.equals("2"))) {

            showTestSelection(request, response, subject);
            return;
        }

        HttpSession session = request.getSession();

        String sessionKey =
                "questions_" + subject + "_test_" + test;

        @SuppressWarnings("unchecked")
        List<Question> questions =
                (List<Question>) session.getAttribute(sessionKey);

        /*
         * Start a completely new attempt
         */
        if ("true".equals(newAttempt)) {

            /*
             * Save the current attempt before replacing
             * its answers.
             */
            saveCurrentAttempt(session);

            /*
             * Load questions if they are not already loaded.
             */
            if (questions == null) {

                List<Question> allQuestions =
                        repository.getQuestionsBySubject(subject);

                if (allQuestions.size() < 50) {

                    response.setContentType(
                            "text/html;charset=UTF-8"
                    );

                    response.getWriter().println(
                            "<h2>Not enough questions available.</h2>"
                    );

                    return;
                }

                if (test.equals("1")) {

                    questions =
                            new ArrayList<>(
                                    allQuestions.subList(0, 25)
                            );

                } else {

                    questions =
                            new ArrayList<>(
                                    allQuestions.subList(25, 50)
                            );
                }

                session.setAttribute(
                        sessionKey,
                        questions
                );
            }

            /*
             * Fresh answer map
             */
            session.setAttribute(
                    "answers",
                    new HashMap<Integer, Integer>()
            );

            /*
             * Fresh review list
             */
            session.setAttribute(
                    "review",
                    new ArrayList<Integer>()
            );

            /*
             * Start from question 1
             */
            session.setAttribute(
                    "currentIndex",
                    0
            );

            session.setAttribute(
                    "currentSubject",
                    subject
            );

            session.setAttribute(
                    "currentTest",
                    test
            );

            session.setAttribute(
                    "currentAttempt",
                    System.currentTimeMillis()
            );

            /*
             * START 30-MINUTE TIMER
             */
            session.setAttribute(
                    "timerEnd",
                    System.currentTimeMillis()
                            + TEST_DURATION
            );
        }

        /*
         * If this is the first time opening the test,
         * create the test and fresh answer data.
         */
        if (questions == null) {

            List<Question> allQuestions =
                    repository.getQuestionsBySubject(subject);

            if (allQuestions.size() < 50) {

                response.setContentType(
                        "text/html;charset=UTF-8"
                );

                response.getWriter().println(
                        "<h2>Not enough questions available.</h2>"
                );

                return;
            }

            if (test.equals("1")) {

                questions =
                        new ArrayList<>(
                                allQuestions.subList(0, 25)
                        );

            } else {

                questions =
                        new ArrayList<>(
                                allQuestions.subList(25, 50)
                        );
            }

            session.setAttribute(
                    sessionKey,
                    questions
            );

            session.setAttribute(
                    "currentIndex",
                    0
            );

            session.setAttribute(
                    "answers",
                    new HashMap<Integer, Integer>()
            );

            session.setAttribute(
                    "review",
                    new ArrayList<Integer>()
            );

            session.setAttribute(
                    "currentSubject",
                    subject
            );

            session.setAttribute(
                    "currentTest",
                    test
            );

            session.setAttribute(
                    "currentAttempt",
                    System.currentTimeMillis()
            );

            /*
             * START 30-MINUTE TIMER
             */
            session.setAttribute(
                    "timerEnd",
                    System.currentTimeMillis()
                            + TEST_DURATION
            );
        }

        /*
         * Make sure an existing test has a timer.
         */
        if (session.getAttribute("timerEnd") == null) {

            session.setAttribute(
                    "timerEnd",
                    System.currentTimeMillis()
                            + TEST_DURATION
            );
        }

        showQuestion(
                request,
                response,
                subject,
                test,
                questions
        );
    }

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        HttpSession session = request.getSession();

        String subject =
                (String) session.getAttribute(
                        "currentSubject"
                );

        String test =
                (String) session.getAttribute(
                        "currentTest"
                );

        if (subject == null || test == null) {
            response.sendRedirect("index.html");
            return;
        }

        String sessionKey =
                "questions_" + subject + "_test_" + test;

        @SuppressWarnings("unchecked")
        List<Question> questions =
                (List<Question>) session.getAttribute(
                        sessionKey
                );

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> answers =
                (Map<Integer, Integer>)
                        session.getAttribute("answers");

        @SuppressWarnings("unchecked")
        List<Integer> review =
                (List<Integer>)
                        session.getAttribute("review");

        if (answers == null) {
            answers = new HashMap<>();
        }

        if (review == null) {
            review = new ArrayList<>();
        }

        Integer currentIndexObject =
                (Integer) session.getAttribute(
                        "currentIndex"
                );

        int currentIndex =
                currentIndexObject == null
                        ? 0
                        : currentIndexObject;

        Question currentQuestion =
                questions.get(currentIndex);

        /*
         * Save selected answer
         */
        String selected =
                request.getParameter(
                        "q" + currentQuestion.getId()
                );

        if (selected != null) {

            answers.put(
                    currentQuestion.getId(),
                    Integer.parseInt(selected)
            );
        }

        String action =
                request.getParameter("action");

        if ("next".equals(action)) {

            if (currentIndex <
                    questions.size() - 1) {

                currentIndex++;
            }

        } else if ("previous".equals(action)) {

            if (currentIndex > 0) {
                currentIndex--;
            }

        } else if ("review".equals(action)) {

            int id =
                    currentQuestion.getId();

            if (review.contains(id)) {

                review.remove(
                        Integer.valueOf(id)
                );

            } else {

                review.add(id);
            }

        } else if ("goto".equals(action)) {

            String index =
                    request.getParameter("index");

            if (index != null) {

                try {
                    currentIndex =
                            Integer.parseInt(index);
                } catch (NumberFormatException e) {
                    currentIndex = 0;
                }
            }

        } else if ("submit".equals(action)) {

            /*
             * Save the latest answer data before
             * displaying the result.
             */
            session.setAttribute(
                    "answers",
                    answers
            );

            session.setAttribute(
                    "review",
                    review
            );

            session.setAttribute(
                    sessionKey,
                    questions
            );

            /*
             * Stop timer after submission.
             */
            session.removeAttribute("timerEnd");

            response.sendRedirect("result");

            return;
        }

        session.setAttribute(
                "currentIndex",
                currentIndex
        );

        session.setAttribute(
                "answers",
                answers
        );

        session.setAttribute(
                "review",
                review
        );

        showQuestion(
                request,
                response,
                subject,
                test,
                questions
        );
    }

    /*
     * Saves the current attempt before a new attempt
     * is started.
     */
    private void saveCurrentAttempt(HttpSession session) {

        String oldSubject =
                (String) session.getAttribute(
                        "currentSubject"
                );

        String oldTest =
                (String) session.getAttribute(
                        "currentTest"
                );

        if (oldSubject == null || oldTest == null) {
            return;
        }

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> oldAnswers =
                (Map<Integer, Integer>)
                        session.getAttribute("answers");

        if (oldAnswers == null || oldAnswers.isEmpty()) {
            return;
        }

        @SuppressWarnings("unchecked")
        List<Map<String, Object>> history =
                (List<Map<String, Object>>)
                        session.getAttribute(
                                "attemptHistory"
                        );

        if (history == null) {
            history =
                    new ArrayList<>();
        }

        Map<String, Object> attempt =
                new HashMap<>();

        attempt.put(
                "subject",
                oldSubject
        );

        attempt.put(
                "test",
                oldTest
        );

        attempt.put(
                "answers",
                new HashMap<Integer, Integer>(
                        oldAnswers
                )
        );

        attempt.put(
                "date",
                System.currentTimeMillis()
        );

        history.add(attempt);

        session.setAttribute(
                "attemptHistory",
                history
        );
    }

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

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");

        out.println("<meta charset='UTF-8'>");

        out.println(
                "<meta name='viewport' " +
                "content='width=device-width, initial-scale=1.0'>"
        );

        out.println(
                "<title>GATE CSE Practice Tests</title>"
        );

        out.println("<style>");

        out.println("*{box-sizing:border-box;}");

        out.println(
                "body{margin:0;" +
                "font-family:Arial,sans-serif;" +
                "background:#f4f7fb;" +
                "color:#1e293b;}"
        );

        out.println(
                ".header{background:#0f172a;" +
                "color:white;" +
                "padding:18px 40px;}"
        );

        out.println(
                ".header h1{margin:0;font-size:22px;}"
        );

        out.println(
                ".container{max-width:850px;" +
                "margin:55px auto;" +
                "padding:20px;}"
        );

        out.println(
                ".title{text-align:center;" +
                "margin-bottom:35px;}"
        );

        out.println(
                ".title h2{font-size:30px;margin:0 0 10px;}"
        );

        out.println(
                ".title p{color:#64748b;}"
        );

        out.println(
                ".tests{display:grid;" +
                "grid-template-columns:repeat(2,1fr);" +
                "gap:22px;}"
        );

        out.println(
                ".test-card{background:white;" +
                "padding:30px;" +
                "border-radius:14px;" +
                "text-align:center;" +
                "box-shadow:0 5px 18px rgba(0,0,0,.07);" +
                "border:1px solid #e2e8f0;}"
        );

        out.println(
                ".test-card h3{margin:0 0 10px;" +
                "font-size:22px;}"
        );

        out.println(
                ".test-card p{color:#64748b;" +
                "font-size:14px;}"
        );

        out.println(
                ".start-btn{display:inline-block;" +
                "margin-top:15px;" +
                "padding:11px 22px;" +
                "background:#2563eb;" +
                "color:white;" +
                "text-decoration:none;" +
                "border-radius:7px;" +
                "font-weight:bold;}"
        );

        out.println(
                "@media(max-width:650px){" +
                ".tests{grid-template-columns:1fr;}" +
                "}"
        );

        out.println("</style>");
        out.println("</head>");

        out.println("<body>");

        out.println(
                "<div class='header'>" +
                "<h1>GATE CSE Practice Tests</h1>" +
                "</div>"
        );

        out.println(
                "<div class='container'>"
        );

        out.println(
                "<div class='title'>"
        );

        out.println(
                "<h2>" + escape(subject) + "</h2>"
        );

        out.println(
                "<p>Select a practice test</p>"
        );

        out.println("</div>");

        out.println("<div class='tests'>");

        out.println("<div class='test-card'>");

        out.println("<h3>Practice Test 1</h3>");

        out.println("<p>Questions 1 – 25</p>");

        out.println("<p>25 Questions</p>");

        out.println(
                "<a class='start-btn' href='mock-test?subject="
                + encode(subject)
                + "&test=1&newAttempt=true'>" +
                "Start Test</a>"
        );

        out.println("</div>");

        out.println("<div class='test-card'>");

        out.println("<h3>Practice Test 2</h3>");

        out.println("<p>Questions 26 – 50</p>");

        out.println("<p>25 Questions</p>");

        out.println(
                "<a class='start-btn' href='mock-test?subject="
                + encode(subject)
                + "&test=2&newAttempt=true'>" +
                "Start Test</a>"
        );

        out.println("</div>");

        out.println("</div>");

        out.println("</div>");

        out.println("</body>");
        out.println("</html>");
    }

    private void showQuestion(
            HttpServletRequest request,
            HttpServletResponse response,
            String subject,
            String test,
            List<Question> questions)
            throws IOException {

        HttpSession session =
                request.getSession();

        Integer index =
                (Integer) session.getAttribute(
                        "currentIndex"
                );

        int currentIndex =
                index == null ? 0 : index;

        @SuppressWarnings("unchecked")
        Map<Integer, Integer> answers =
                (Map<Integer, Integer>)
                        session.getAttribute("answers");

        @SuppressWarnings("unchecked")
        List<Integer> review =
                (List<Integer>)
                        session.getAttribute("review");

        if (answers == null) {
            answers = new HashMap<>();
        }

        if (review == null) {
            review = new ArrayList<>();
        }

        Question question =
                questions.get(currentIndex);

        /*
         * Get timer end time
         */
        Long timerEnd =
                (Long) session.getAttribute(
                        "timerEnd"
                );

        if (timerEnd == null) {

            timerEnd =
                    System.currentTimeMillis()
                            + TEST_DURATION;

            session.setAttribute(
                    "timerEnd",
                    timerEnd
            );
        }

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        PrintWriter out =
                response.getWriter();

        out.println("<!DOCTYPE html>");
        out.println("<html>");
        out.println("<head>");

        out.println("<meta charset='UTF-8'>");

        out.println(
                "<meta name='viewport' " +
                "content='width=device-width, initial-scale=1.0'>"
        );

        out.println(
                "<title>GATE CSE Practice Test</title>"
        );

        out.println("<style>");

        out.println("*{box-sizing:border-box;}");

        out.println(
                "body{margin:0;font-family:Arial,sans-serif;" +
                "background:#f1f5f9;color:#1e293b;}"
        );

        out.println(
                ".header{height:62px;background:#0f172a;" +
                "color:white;display:flex;align-items:center;" +
                "justify-content:space-between;padding:0 28px;}"
        );

        out.println(
                ".brand{font-size:18px;font-weight:bold;}"
        );

        out.println(
                ".test-info{font-size:14px;color:#cbd5e1;}"
        );

        /*
         * TIMER STYLE
         */
        out.println(
                ".timer{background:#ffffff;" +
                "color:#dc2626;" +
                "padding:8px 14px;" +
                "border-radius:7px;" +
                "font-weight:bold;" +
                "font-size:16px;" +
                "min-width:90px;" +
                "text-align:center;" +
                "border:1px solid #fecaca;}"
        );

        out.println(
                ".timer.warning{background:#fef2f2;" +
                "color:#dc2626;" +
                "animation:pulse 1s infinite;}"
        );

        out.println(
                "@keyframes pulse{" +
                "50%{opacity:.55;}" +
                "}"
        );

        out.println(
                ".layout{max-width:1150px;margin:25px auto;" +
                "padding:0 18px;display:grid;" +
                "grid-template-columns:minmax(0,1fr) 205px;gap:18px;}"
        );

        out.println(
                ".question-card{background:white;border:1px solid #e2e8f0;" +
                "border-radius:12px;padding:28px;" +
                "box-shadow:0 4px 15px rgba(15,23,42,.05);}"
        );

        out.println(
                ".question-top{display:flex;justify-content:space-between;" +
                "align-items:center;border-bottom:1px solid #e2e8f0;" +
                "padding-bottom:16px;margin-bottom:22px;}"
        );

        out.println(
                ".q-number{font-weight:bold;color:#2563eb;}"
        );

        out.println(
                ".q-count{font-size:13px;color:#64748b;}"
        );

        out.println(
                ".question{font-size:18px;line-height:1.65;" +
                "margin-bottom:24px;}"
        );

        out.println(
                ".option{display:block;border:1px solid #dbe3ef;" +
                "border-radius:8px;padding:13px 15px;" +
                "margin-bottom:10px;cursor:pointer;font-size:15px;}"
        );

        out.println(
                ".option:hover{background:#f8fafc;}"
        );

        out.println(
                ".option input{margin-right:10px;}"
        );

        out.println(
                ".actions{display:flex;gap:8px;margin-top:25px;" +
                "padding-top:18px;border-top:1px solid #e2e8f0;" +
                "flex-wrap:wrap;}"
        );

        out.println(
                ".btn{border:0;border-radius:7px;padding:10px 16px;" +
                "font-size:13px;font-weight:bold;cursor:pointer;}"
        );

        out.println(
                ".previous{background:#e2e8f0;color:#334155;}"
        );

        out.println(
                ".next{background:#2563eb;color:white;}"
        );

        out.println(
                ".review-btn{background:#f59e0b;color:white;}"
        );

        out.println(
                ".submit{background:#16a34a;color:white;}"
        );

        out.println(
                ".palette{background:white;border:1px solid #e2e8f0;" +
                "border-radius:12px;padding:16px;height:max-content;" +
                "box-shadow:0 4px 15px rgba(15,23,42,.05);}"
        );

        out.println(
                ".palette h3{font-size:15px;margin:0 0 4px;}"
        );

        out.println(
                ".palette-subtitle{font-size:11px;color:#64748b;" +
                "margin-bottom:13px;}"
        );

        out.println(
                ".palette-grid{display:grid;grid-template-columns:repeat(5,1fr);gap:5px;}"
        );

        out.println(
                ".palette form{margin:0;}"
        );

        out.println(
                ".palette button{width:100%;height:30px;border:0;" +
                "border-radius:5px;font-size:11px;font-weight:bold;" +
                "cursor:pointer;background:#e2e8f0;color:#334155;}"
        );

        out.println(
                ".palette button.current{background:#2563eb;color:white;}"
        );

        out.println(
                ".palette button.answered{background:#16a34a;color:white;}"
        );

        out.println(
                ".palette button.review{background:#f59e0b;color:white;}"
        );

        out.println(
                ".legend{margin-top:15px;border-top:1px solid #e2e8f0;" +
                "padding-top:12px;}"
        );

        out.println(
                ".legend-item{display:flex;align-items:center;gap:7px;" +
                "font-size:11px;margin:7px 0;color:#475569;}"
        );

        out.println(
                ".dot{width:11px;height:11px;border-radius:3px;display:inline-block;}"
        );

        out.println(".dot.current{background:#2563eb;}");
        out.println(".dot.answered{background:#16a34a;}");
        out.println(".dot.unanswered{background:#e2e8f0;border:1px solid #cbd5e1;}");
        out.println(".dot.review{background:#f59e0b;}");

        out.println(
                "@media(max-width:800px){" +
                ".layout{grid-template-columns:1fr;}" +
                ".palette{order:-1;}" +
                ".test-info{display:none;}" +
                ".header{padding:0 15px;}" +
                "}"
        );

        out.println("</style>");

        /*
         * TIMER JAVASCRIPT
         */
        out.println("<script>");

        out.println("let timerEnd = " + timerEnd + ";");

        out.println("function startTimer(){");

        out.println(
                "let timer = document.getElementById('timer');"
        );

        out.println(
                "function updateTimer(){"
        );

        out.println(
                "let remaining = timerEnd - Date.now();"
        );

        out.println(
                "if(remaining <= 0){"
        );

        out.println(
                "timer.innerHTML = '00:00';"
        );

        out.println(
                "clearInterval(timerInterval);"
        );

        out.println(
                "let form = document.getElementById('testForm');"
        );

        out.println(
                "let hiddenAction = document.createElement('input');"
        );

        out.println(
                "hiddenAction.type = 'hidden';"
        );

        out.println(
                "hiddenAction.name = 'action';"
        );

        out.println(
                "hiddenAction.value = 'submit';"
        );

        out.println(
                "form.appendChild(hiddenAction);"
        );

        out.println(
                "form.submit();"
        );

        out.println(
                "return;"
        );

        out.println("}");

        out.println(
                "let totalSeconds = Math.floor(remaining / 1000);"
        );

        out.println(
                "let minutes = Math.floor(totalSeconds / 60);"
        );

        out.println(
                "let seconds = totalSeconds % 60;"
        );

        out.println(
                "timer.innerHTML = " +
                "String(minutes).padStart(2,'0') + ':' + " +
                "String(seconds).padStart(2,'0');"
        );

        out.println(
                "if(totalSeconds <= 300){"
        );

        out.println(
                "timer.classList.add('warning');"
        );

        out.println("}");

        out.println("}");

        out.println("updateTimer();");

        out.println(
                "let timerInterval = setInterval(updateTimer,1000);"
        );

        out.println("}");

        out.println(
                "window.onload = startTimer;"
        );

        out.println("</script>");

        out.println("</head>");
        out.println("<body>");

        /*
         * HEADER
         */
        out.println(
                "<div class='header'>" +

                "<div class='brand'>" +
                "GATE CSE Practice Tests" +
                "</div>" +

                "<div class='test-info'>" +
                escape(subject) +
                " | Practice Test " +
                escape(test) +
                " | 25 Questions" +
                "</div>" +

                "<div id='timer' class='timer'>" +
                "30:00" +
                "</div>" +

                "</div>"
        );

        out.println("<div class='layout'>");

        out.println("<div class='question-card'>");

        out.println(
                "<div class='question-top'>" +
                "<div class='q-number'>Question " +
                (currentIndex + 1) +
                "</div>" +
                "<div class='q-count'>25 Questions</div>" +
                "</div>"
        );

        out.println(
                "<div class='question'>" +
                escape(question.getQuestion()) +
                "</div>"
        );

        /*
         * MAIN TEST FORM
         */
        out.println(
                "<form method='post' action='mock-test' id='testForm'>"
        );

        String[] options = {
                question.getOption1(),
                question.getOption2(),
                question.getOption3(),
                question.getOption4()
        };

        for (int i = 0; i < 4; i++) {

            boolean checked =
                    answers.containsKey(question.getId()) &&
                    answers.get(question.getId()) == i + 1;

            out.println(
                    "<label class='option'>"
            );

            out.println(
                    "<input type='radio' name='q" +
                    question.getId() +
                    "' value='" +
                    (i + 1) +
                    "' " +
                    (checked ? "checked" : "") +
                    ">"
            );

            out.println(
                    "<strong>" +
                    (char) ('A' + i) +
                    ".</strong> " +
                    escape(options[i])
            );

            out.println("</label>");
        }

        out.println("<div class='actions'>");

        if (currentIndex > 0) {

            out.println(
                    "<button class='btn previous' " +
                    "name='action' value='previous'>" +
                    "Previous</button>"
            );
        }

        if (currentIndex < questions.size() - 1) {

            out.println(
                    "<button class='btn next' " +
                    "name='action' value='next'>" +
                    "Save & Next</button>"
            );
        }

        out.println(
                "<button class='btn review-btn' " +
                "name='action' value='review'>" +
                "Mark for Review</button>"
        );

        if (currentIndex == questions.size() - 1) {

            out.println(
                    "<button class='btn submit' " +
                    "name='action' value='submit' " +
                    "onclick=\"return confirm('Are you sure you want to submit the test?');\">" +
                    "Submit Test</button>"
            );
        }

        out.println("</div>");
        out.println("</form>");
        out.println("</div>");

        /*
         * QUESTION PALETTE
         */
        out.println("<div class='palette'>");

        out.println("<h3>Question Palette</h3>");

        out.println(
                "<div class='palette-subtitle'>" +
                "Navigate between questions" +
                "</div>"
        );

        out.println("<div class='palette-grid'>");

        for (int i = 0; i < questions.size(); i++) {

            Question q =
                    questions.get(i);

            String className = "";

            if (i == currentIndex) {

                className = "current";

            } else if (review.contains(q.getId())) {

                className = "review";

            } else if (answers.containsKey(q.getId())) {

                className = "answered";
            }

            out.println(
                    "<form method='post' action='mock-test'>"
            );

            out.println(
                    "<input type='hidden' name='action' value='goto'>"
            );

            out.println(
                    "<input type='hidden' name='index' value='" +
                    i +
                    "'>"
            );

            out.println(
                    "<button type='submit' class='" +
                    className +
                    "'>" +
                    (i + 1) +
                    "</button>"
            );

            out.println("</form>");
        }

        out.println("</div>");

        out.println("<div class='legend'>");

        out.println(
                "<div class='legend-item'>" +
                "<span class='dot current'></span>Current</div>"
        );

        out.println(
                "<div class='legend-item'>" +
                "<span class='dot answered'></span>Answered</div>"
        );

        out.println(
                "<div class='legend-item'>" +
                "<span class='dot unanswered'></span>Not Answered</div>"
        );

        out.println(
                "<div class='legend-item'>" +
                "<span class='dot review'></span>Marked for Review</div>"
        );

        out.println("</div>");

        out.println("</div>");
        out.println("</div>");

        out.println("</body>");
        out.println("</html>");
    }

    private String encode(String value) {

        return value
                .replace("%", "%25")
                .replace(" ", "%20")
                .replace("&", "%26");
    }

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
