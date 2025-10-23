# urlshortener
Developed a lightweight URL shortening service using plain Java and the built-in HttpServer class. The application allows users to convert long URLs into short, easy-to-share links and supports redirection from the shortened URL to the original link.

**Key Features:**

URL Shortening: Users can submit a long URL via a web interface or HTTP request, and the application generates a unique short code.

Redirection: Visiting the shortened URL automatically redirects the user to the original long URL.

In-Memory Storage: URL mappings are stored in a thread-safe HashMap, with atomic counters to ensure unique short codes.

Simple Frontend: HTML/CSS-based UI for entering URLs and displaying shortened links.

Standalone Java Server: Uses Java’s built-in HttpServer, eliminating the need for external frameworks like Spring Boot.

JSON/Plain Text Support: Supports sending requests as JSON or plain text for flexibility.

Atomic ID Encoding: Converts numeric IDs into alphanumeric short codes using a custom base-52 encoding scheme.

**Technologies Used:**

Java (SE)

HttpServer (built-in Java HTTP server)

HTML/CSS and vanilla JavaScript for frontend

JSON for API communication


