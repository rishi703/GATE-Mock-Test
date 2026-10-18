package com.gate.servlet;

import com.gate.repository.UserRepository;
import com.gate.util.PasswordUtil;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

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
        if (username == null || password == null
                || username.trim().isEmpty()
                || password.isEmpty()) {

            response.sendRedirect("login.html?error=empty");
            return;
        }

        // Look up user
        UserRepository.User user =
                UserRepository.findByUsername(username);

        if (user == null) {

            response.sendRedirect("login.html?error=invalid");
            return;
        }

        // Verify password (supports legacy plaintext rows)
        if (!PasswordUtil.verify(
                password, user.getStoredPassword())) {

            response.sendRedirect("login.html?error=invalid");
            return;
        }

        // Transparent migration: upgrade a legacy plaintext
        // row to a hashed password on first successful login
        if (!PasswordUtil.isHashed(user.getStoredPassword())) {

            UserRepository.updatePassword(
                    user.getUsername(),
                    PasswordUtil.hash(password));
        }

        // Login successful
        HttpSession session = request.getSession(true);

        session.setAttribute("username", user.getUsername());
        session.setAttribute("name", user.getName());
        session.setAttribute("email", user.getEmail());

        response.sendRedirect("index.html");
    }
}
