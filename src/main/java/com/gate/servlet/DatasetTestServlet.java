
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
import java.util.List;

@WebServlet("/dataset-test")
public class DatasetTestServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private QuestionRepository repository;

    @Override
    public void init() throws ServletException {
        repository = new QuestionRepository(
                getServletContext()
        );
    }

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // Check whether the user is logged in
        HttpSession session =
                request.getSession(false);

        if (session == null ||
                session.getAttribute("username") == null) {

            response.sendRedirect("login.html");
            return;
        }

        response.setContentType(
                "text/html;charset=UTF-8"
        );

        PrintWriter out =
                response.getWriter();

        List<Question> questions =
                repository.getAllQuestions();

        out.println("<html>");
        out.println("<head>");

        out.println("<title>Dataset Test</title>");

        out.println("<style>");

        out.println("body {");
        out.println("font-family: Arial, sans-serif;");
        out.println("padding: 30px;");
        out.println("background: #f5f7fb;");
        out.println("}");

        out.println("h1 {");
        out.println("color: #1f2937;");
        out.println("}");

        out.println(".question {");
        out.println("background: white;");
        out.println("padding: 20px;");
        out.println("margin-bottom: 20px;");
        out.println("border-radius: 10px;");
        out.println("box-shadow: 0 2px 8px rgba(0,0,0,0.08);");
        out.println("}");

        out.println(".subject {");
        out.println("color: #3157d5;");
        out.println("font-weight: bold;");
        out.println("}");

        out.println(".option {");
        out.println("margin: 8px 0;");
        out.println("}");

        out.println("</style>");

        out.println("</head>");

        out.println("<body>");

        out.println("<h1>GATE Mock Test Dataset</h1>");

        out.println("<h2>Total Questions: "
                + questions.size()
                + "</h2>");

        for (Question q : questions) {

            out.println("<div class='question'>");

            out.println("<h3>"
                    + q.getId()
                    + ". "
                    + escapeHtml(q.getQuestion())
                    + "</h3>");

            out.println("<p class='subject'>Subject: "
                    + escapeHtml(q.getSubject())
                    + "</p>");

            out.println("<p>Topic: "
                    + escapeHtml(q.getTopic())
                    + "</p>");

            out.println("<p class='option'>1. "
                    + escapeHtml(q.getOption1())
                    + "</p>");

            out.println("<p class='option'>2. "
                    + escapeHtml(q.getOption2())
                    + "</p>");

            out.println("<p class='option'>3. "
                    + escapeHtml(q.getOption3())
                    + "</p>");

            out.println("<p class='option'>4. "
                    + escapeHtml(q.getOption4())
                    + "</p>");

            out.println("</div>");
        }

        out.println("</body>");
        out.println("</html>");
    }

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
