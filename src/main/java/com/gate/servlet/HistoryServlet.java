package com.gate.servlet;

import com.gate.repository.AttemptedHistoryRepository;

import jakarta.servlet.ServletException;
import jakarta.servlet.annotation.WebServlet;
import jakarta.servlet.http.HttpServlet;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import jakarta.servlet.http.HttpSession;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.List;

@WebServlet("/history")
public class HistoryServlet extends HttpServlet {

    private static final long serialVersionUID = 1L;

    @Override
    protected void doGet(
            HttpServletRequest request,
            HttpServletResponse response)
            throws ServletException, IOException {

        // =====================================================
        // GET LOGGED-IN USER
        // =====================================================

        HttpSession session = request.getSession(false);

        if (session == null) {

            response.sendRedirect("login.html");
            return;
        }

        String username =
                (String) session.getAttribute("username");

        if (username == null ||
                username.trim().isEmpty()) {

            response.sendRedirect("login.html");
            return;
        }

        // =====================================================
        // GET ONLY THIS USER'S HISTORY
        // =====================================================

        List<String[]> attempts =
                AttemptedHistoryRepository
                        .getAttemptsByUsername(username);

        // =====================================================
        // HTML RESPONSE
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

                    <title>Attempt History</title>

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
                            max-width: 1250px;
                            margin: 35px auto;
                            padding: 0 20px;
                        }

                        .title {
                            margin-bottom: 25px;
                        }

                        .title h2 {
                            margin: 0 0 8px;
                            font-size: 30px;
                        }

                        .title p {
                            margin: 0;
                            color: #6b7280;
                        }

                        .user-name {
                            color: #2563eb;
                            font-weight: bold;
                        }

                        .card {
                            background: white;
                            border-radius: 14px;
                            padding: 25px;
                            box-shadow:
                                0 4px 15px
                                rgba(0,0,0,0.07);
                            overflow-x: auto;
                        }

                        table {
                            width: 100%;
                            border-collapse: collapse;
                            min-width: 900px;
                        }

                        th {
                            background: #111827;
                            color: white;
                            padding: 14px 12px;
                            text-align: center;
                            font-size: 13px;
                            white-space: nowrap;
                        }

                        td {
                            padding: 13px 12px;
                            border-bottom: 1px solid #e5e7eb;
                            text-align: center;
                            font-size: 14px;
                            white-space: nowrap;
                        }

                        tr:hover td {
                            background: #f9fafb;
                        }

                        .subject {
                            font-weight: bold;
                            text-align: left;
                        }

                        .correct {
                            color: #16a34a;
                            font-weight: bold;
                        }

                        .incorrect {
                            color: #dc2626;
                            font-weight: bold;
                        }

                        .unanswered {
                            color: #d97706;
                            font-weight: bold;
                        }

                        .percentage {
                            color: #2563eb;
                            font-weight: bold;
                        }

                        .accuracy {
                            color: #7c3aed;
                            font-weight: bold;
                        }

                        .empty {
                            text-align: center;
                            padding: 60px 20px;
                            color: #6b7280;
                        }

                        .empty-icon {
                            font-size: 45px;
                            margin-bottom: 15px;
                        }

                        .buttons {
                            text-align: center;
                            margin-top: 25px;
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

                        .test {
                            background: #2563eb;
                            color: white;
                        }

                        .summary {
                            display: flex;
                            gap: 15px;
                            margin-bottom: 20px;
                            flex-wrap: wrap;
                        }

                        .summary-card {
                            background: white;
                            border-radius: 10px;
                            padding: 18px 25px;
                            box-shadow:
                                0 3px 10px
                                rgba(0,0,0,0.05);
                            min-width: 160px;
                        }

                        .summary-card .number {
                            font-size: 25px;
                            font-weight: bold;
                        }

                        .summary-card .label {
                            color: #6b7280;
                            font-size: 13px;
                            margin-top: 4px;
                        }

                        @media (max-width: 700px) {

                            .header {
                                padding: 15px 20px;
                            }

                            .header h1 {
                                font-size: 18px;
                            }

                            .container {
                                margin-top: 20px;
                            }

                            .title h2 {
                                font-size: 25px;
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
                "Logged in as: " +
                escape(username)
        );

        out.println("""
                        </span>

                    </div>

                    <div class="container">

                        <div class="title">

                            <h2>Attempt History</h2>

                            <p>
                                Your previous mock test
                                performance
                            </p>

                        </div>
                """);

        // =====================================================
        // SUMMARY
        // =====================================================

        out.println("""
                        <div class="summary">

                            <div class="summary-card">

                                <div class="number">
                """);

        out.print(attempts.size());

        out.println("""
                                </div>

                                <div class="label">
                                    Total Attempts
                                </div>

                            </div>

                        </div>
                """);

        // =====================================================
        // NO HISTORY
        // =====================================================

        if (attempts.isEmpty()) {

            out.println("""
                        <div class="card">

                            <div class="empty">

                                <div class="empty-icon">
                                    📋
                                </div>

                                <h3>
                                    No Attempts Yet
                                </h3>

                                <p>
                                    Complete a mock test to
                                    see your attempt history
                                    here.
                                </p>

                            </div>

                        </div>
                    """);

        } else {

            // =================================================
            // HISTORY TABLE
            // =================================================

            out.println("""
                        <div class="card">

                            <table>

                                <thead>

                                    <tr>

                                        <th>Date</th>
                                        <th>Subject</th>
                                        <th>Test</th>
                                        <th>Total</th>
                                        <th>Attempted</th>
                                        <th>Correct</th>
                                        <th>Incorrect</th>
                                        <th>Unanswered</th>
                                        <th>Accuracy</th>
                                        <th>Score</th>

                                    </tr>

                                </thead>

                                <tbody>
                    """);

            /*
             * New CSV columns:
             *
             * 0 = Username
             * 1 = Date
             * 2 = Subject
             * 3 = Test
             * 4 = Total
             * 5 = Attempted
             * 6 = Correct
             * 7 = Incorrect
             * 8 = Unanswered
             * 9 = Accuracy
             * 10 = Percentage
             */

            // Display newest attempts first
            for (int i = attempts.size() - 1;
                 i >= 0;
                 i--) {

                String[] row =
                        attempts.get(i);

                if (row.length < 11) {
                    continue;
                }

                out.println("<tr>");

                // Date
                out.println(
                        "<td>" +
                        escape(row[1]) +
                        "</td>"
                );

                // Subject
                out.println(
                        "<td class='subject'>" +
                        escape(row[2]) +
                        "</td>"
                );

                // Test
                out.println(
                        "<td>" +
                        escape(row[3]) +
                        "</td>"
                );

                // Total
                out.println(
                        "<td>" +
                        escape(row[4]) +
                        "</td>"
                );

                // Attempted
                out.println(
                        "<td>" +
                        escape(row[5]) +
                        "</td>"
                );

                // Correct
                out.println(
                        "<td class='correct'>" +
                        escape(row[6]) +
                        "</td>"
                );

                // Incorrect
                out.println(
                        "<td class='incorrect'>" +
                        escape(row[7]) +
                        "</td>"
                );

                // Unanswered
                out.println(
                        "<td class='unanswered'>" +
                        escape(row[8]) +
                        "</td>"
                );

                // Accuracy
                out.println(
                        "<td class='accuracy'>" +
                        escape(row[9]) +
                        "%" +
                        "</td>"
                );

                // Percentage / Score
                out.println(
                        "<td class='percentage'>" +
                        escape(row[10]) +
                        "%" +
                        "</td>"
                );

                out.println("</tr>");
            }

            out.println("""
                                </tbody>

                            </table>

                        </div>
                    """);
        }

        // =====================================================
        // BUTTONS
        // =====================================================

        out.println("""
                        <div class="buttons">

                            <a class="btn test"
                               href="index.html">
                                Take Another Test
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