function startTest(subject) {
    window.location.href =
        "mock-test?subject=" + encodeURIComponent(subject);
}


// ======================================================
// LOGIN / LOGOUT STATUS
// ======================================================

document.addEventListener("DOMContentLoaded", function () {

    const authArea =
        document.getElementById("auth-area");

    if (!authArea) {
        return;
    }

    fetch("./session", {
        method: "GET",
        credentials: "same-origin",
        cache: "no-store"
    })
    .then(function (response) {

        if (!response.ok) {
            throw new Error(
                "Session request failed: " + response.status
            );
        }

        return response.json();
    })
    .then(function (data) {

        console.log("Login status:", data);

        if (data.loggedIn === true) {

            const displayName =
                data.name &&
                data.name.trim() !== ""
                    ? data.name
                    : data.username;

            authArea.innerHTML =
                '<div class="user-menu">' +
                    '<span class="welcome-user">' +
                        'Hi, ' +
                        escapeHtml(displayName) +
                    '</span>' +

                    '<a href="./logout" ' +
                       'class="login-btn logout-btn">' +
                        'Logout' +
                    '</a>' +
                '</div>';

        } else {

            authArea.innerHTML =
                '<a href="login.html" ' +
                   'class="login-btn">' +
                    'Login' +
                '</a>';
        }
    })
    .catch(function (error) {

        console.error(
            "Unable to check login status:",
            error
        );

        authArea.innerHTML =
            '<a href="login.html" ' +
               'class="login-btn">' +
                'Login' +
            '</a>';
    });
});


// ======================================================
// HTML ESCAPE
// ======================================================

function escapeHtml(value) {

    if (value == null) {
        return "";
    }

    return String(value)
        .replace(/&/g, "&amp;")
        .replace(/</g, "&lt;")
        .replace(/>/g, "&gt;")
        .replace(/"/g, "&quot;")
        .replace(/'/g, "&#039;");
}