package com.gym.server;

import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

public class StaticFileHandler implements HttpHandler {
    private final Path frontendFolder = locateFrontendFolder();

    @Override
    public void handle(HttpExchange exchange) throws IOException {
        String requestPath = exchange.getRequestURI().getPath();
        if (requestPath.equals("/")) {
            requestPath = "/index.html";
        }

        Path file = frontendFolder.resolve(requestPath.substring(1)).normalize();
        if (!file.startsWith(frontendFolder) || Files.notExists(file) || Files.isDirectory(file)) {
            sendText(exchange, 404, "File not found");
            return;
        }

        byte[] bytes = Files.readAllBytes(file);
        exchange.getResponseHeaders().set("Content-Type", contentType(file));
        exchange.sendResponseHeaders(200, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private void sendText(HttpExchange exchange, int statusCode, String text) throws IOException {
        byte[] bytes = text.getBytes();
        exchange.getResponseHeaders().set("Content-Type", "text/plain; charset=utf-8");
        exchange.sendResponseHeaders(statusCode, bytes.length);
        exchange.getResponseBody().write(bytes);
        exchange.close();
    }

    private String contentType(Path file) {
        String name = file.getFileName().toString().toLowerCase();
        if (name.endsWith(".html")) return "text/html; charset=utf-8";
        if (name.endsWith(".css")) return "text/css; charset=utf-8";
        if (name.endsWith(".js")) return "application/javascript; charset=utf-8";
        return "application/octet-stream";
    }

    private Path locateFrontendFolder() {
        Path current = Path.of("").toAbsolutePath().normalize();

        while (current != null) {
            Path frontend = current.resolve("frontend").normalize();
            if (Files.exists(frontend.resolve("index.html"))) {
                return frontend;
            }
            current = current.getParent();
        }

        return Path.of("frontend").toAbsolutePath().normalize();
    }
}
