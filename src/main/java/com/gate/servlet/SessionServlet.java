
package com.gate.servlet;

import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;

@WebServlet("/session")
public class SessionServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws IOException {

        response.setContentType("application/json");
        response.setCharacterEncoding("UTF-8");

        PrintWriter out = response.getWriter();

        HttpSession session =
                request.getSession(false);

        if (session == null) {
            out.print("{\"loggedIn\":false}");
            return;
        }

        Object usernameObject =
                session.getAttribute("username");

        Object nameObject =
                session.getAttribute("name");

        if (usernameObject == null) {
            out.print("{\"loggedIn\":false}");
            return;
        }

        String username =
                usernameObject.toString();

        String name =
                nameObject != null
                        ? nameObject.toString()
                        : username;

        out.print(
                "{\"loggedIn\":true,"
                + "\"username\":\""
                + escapeJson(username)
                + "\","
                + "\"name\":\""
                + escapeJson(name)
                + "\"}"
        );
    }

    private String escapeJson(String value) {

        if (value == null) {
            return "";
        }

        return value
                .replace("\\", "\\\\")
                .replace("\"", "\\\"")
                .replace("\r", "\\r")
                .replace("\n", "\\n");
    }
}
