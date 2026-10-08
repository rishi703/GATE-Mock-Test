package com.gate.servlet;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = request.getParameter("name");
        String username = request.getParameter("username");
        String email = request.getParameter("email");
        String password = request.getParameter("password");
        String confirmPassword = request.getParameter("confirmPassword");

        // Check empty fields
        if (name == null || username == null ||
            email == null || password == null ||
            confirmPassword == null ||
            name.trim().isEmpty() ||
            username.trim().isEmpty() ||
            email.trim().isEmpty() ||
            password.trim().isEmpty()) {

            response.sendRedirect("signup.html?error=empty");
            return;
        }

        // Check password confirmation
        if (!password.equals(confirmPassword)) {

            response.sendRedirect("signup.html?error=password");
            return;
        }

        // Get users.csv path
        String filePath = getServletContext()
                .getRealPath("/data/users.csv");

        File file = new File(filePath);

        // Create parent folder if necessary
        File parent = file.getParentFile();

        if (parent != null && !parent.exists()) {
            parent.mkdirs();
        }

        // Check whether username already exists
        if (file.exists()) {

            try (BufferedReader reader =
                         new BufferedReader(new FileReader(file))) {

                String line;

                // Skip header
                reader.readLine();

                while ((line = reader.readLine()) != null) {

                    String[] data = line.split(",", -1);

                    if (data.length >= 2) {

                        String existingUsername =
                                data[1].trim();

                        if (existingUsername.equalsIgnoreCase(username.trim())) {

                            response.sendRedirect(
                                    "signup.html?error=username"
                            );

                            return;
                        }
                    }
                }
            }
        }

        // Save new user
        try (BufferedWriter writer =
                     new BufferedWriter(new FileWriter(file, true))) {

            writer.write(
                    name.trim() + "," +
                    username.trim() + "," +
                    email.trim() + "," +
                    password
            );

            writer.newLine();
        }

        // Registration successful
        response.sendRedirect("login.html?success=registered");
    }
}