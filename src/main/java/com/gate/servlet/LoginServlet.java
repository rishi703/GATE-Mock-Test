package com.gate.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;

@WebServlet("/login")
public class LoginServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String username = request.getParameter("username");
        String password = request.getParameter("password");

        // Check empty fields
        if (username == null || password == null ||
            username.trim().isEmpty() ||
            password.trim().isEmpty()) {

            response.sendRedirect("login.html?error=empty");
            return;
        }

        // Get users.csv path
        String filePath = getServletContext()
                .getRealPath("/data/users.csv");

        File file = new File(filePath);

        // Check if users file exists
        if (!file.exists()) {

            response.sendRedirect("login.html?error=noaccount");
            return;
        }

        boolean loginSuccessful = false;

        String loggedInName = "";
        String loggedInEmail = "";

        // Read users.csv
        try (BufferedReader reader =
                     new BufferedReader(new FileReader(file))) {

            // Skip header
            reader.readLine();

            String line;

            while ((line = reader.readLine()) != null) {

                String[] data = line.split(",", -1);

                if (data.length >= 4) {

                    String name = data[0].trim();
                    String savedUsername = data[1].trim();
                    String email = data[2].trim();
                    String savedPassword = data[3];

                    // Check username and password
                    if (savedUsername.equalsIgnoreCase(username.trim())
                            && savedPassword.equals(password)) {

                        loginSuccessful = true;

                        loggedInName = name;
                        loggedInEmail = email;

                        break;
                    }
                }
            }
        }

        // Login successful
        if (loginSuccessful) {

            HttpSession session = request.getSession();

            session.setAttribute("username", username.trim());
            session.setAttribute("name", loggedInName);
            session.setAttribute("email", loggedInEmail);

            // Redirect to home page
            response.sendRedirect("index.html");

        } else {

            // Login failed
            response.sendRedirect("login.html?error=invalid");
        }
    }
}