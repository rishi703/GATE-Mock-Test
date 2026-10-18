package com.gate.servlet;

import com.gate.repository.UserRepository;
import com.gate.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

import java.io.IOException;
import java.util.regex.Pattern;

@WebServlet("/signup")
public class SignupServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile(
                    "^[A-Za-z0-9._%+-]+"
                    + "@[A-Za-z0-9.-]+"
                    + "\\.[A-Za-z]{2,}$");

    private static final Pattern USERNAME_PATTERN =
            Pattern.compile("^[A-Za-z0-9_]{3,20}$");

    private static final int MIN_PASSWORD_LENGTH = 6;

    @Override
    protected void doPost(HttpServletRequest request,
                           HttpServletResponse response)
            throws ServletException, IOException {

        request.setCharacterEncoding("UTF-8");

        String name = trim(request.getParameter("name"));
        String username = trim(request.getParameter("username"));
        String email = trim(request.getParameter("email"));
        String password = request.getParameter("password");
        String confirmPassword =
                request.getParameter("confirmPassword");

        // Check empty fields
        if (name.isEmpty() || username.isEmpty()
                || email.isEmpty()
                || password == null
                || password.isEmpty()
                || confirmPassword == null
                || confirmPassword.isEmpty()) {

            response.sendRedirect("signup.html?error=empty");
            return;
        }

        // Validate email format
        if (!EMAIL_PATTERN.matcher(email).matches()) {

            response.sendRedirect("signup.html?error=email");
            return;
        }

        // Validate username format
        if (!USERNAME_PATTERN.matcher(username).matches()) {

            response.sendRedirect("signup.html?error=usernameformat");
            return;
        }

        // Validate password strength (minimum length)
        if (password.length() < MIN_PASSWORD_LENGTH) {

            response.sendRedirect("signup.html?error=passwordshort");
            return;
        }

        // Check password confirmation
        if (!password.equals(confirmPassword)) {

            response.sendRedirect("signup.html?error=password");
            return;
        }

        // Check whether username already exists
        if (UserRepository.findByUsername(username) != null) {

            response.sendRedirect("signup.html?error=username");
            return;
        }

        // Save new user with hashed password
        UserRepository.saveUser(
                name,
                username,
                email,
                PasswordUtil.hash(password));

        // Registration successful
        response.sendRedirect("login.html?success=registered");
    }

    private static String trim(String value) {
        return value == null ? "" : value.trim();
    }
}
