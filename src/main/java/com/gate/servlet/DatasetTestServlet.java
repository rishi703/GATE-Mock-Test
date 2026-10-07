package com.gate.servlet;

import com.gate.model.Question;
import com.gate.repository.QuestionRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/dataset-test")
public class DatasetTestServlet extends HttpServlet {

    /**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private QuestionRepository repository;

    @Override
    public void init() throws ServletException {
        repository = new QuestionRepository(
                getServletContext()
        );
    }

    @Override
    protected void doGet(HttpServletRequest request,
                          HttpServletResponse response)
            throws ServletException, IOException {

        response.setContentType("text/html;charset=UTF-8");

        PrintWriter out = response.getWriter();

        List<Question> questions =
                repository.getAllQuestions();

        out.println("<html>");
        out.println("<head>");
        out.println("<title>Dataset Test</title>");
        out.println("</head>");

        out.println("<body>");

        out.println("<h1>GATE Mock Test Dataset</h1>");

        out.println("<h2>Total Questions: "
                + questions.size() + "</h2>");

        for (Question q : questions) {

            out.println("<hr>");

            out.println("<h3>"
                    + q.getId()
                    + ". "
                    + q.getQuestion()
                    + "</h3>");

            out.println("<p>Subject: "
                    + q.getSubject()
                    + "</p>");

            out.println("<p>Topic: "
                    + q.getTopic()
                    + "</p>");

            out.println("<p>1. "
                    + q.getOption1()
                    + "</p>");

            out.println("<p>2. "
                    + q.getOption2()
                    + "</p>");

            out.println("<p>3. "
                    + q.getOption3()
                    + "</p>");

            out.println("<p>4. "
                    + q.getOption4()
                    + "</p>");
        }

        out.println("</body>");
        out.println("</html>");
    }
}