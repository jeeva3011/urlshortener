import java.io.File;
import java.io.IOException;
import java.io.OutputStream;
import java.net.InetSocketAddress;
import java.net.URI;
import java.util.*;
import java.util.concurrent.atomic.AtomicLong;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpExchange;
import com.sun.net.httpserver.HttpHandler;


public class UrlShortener {
    private static final Map<String, String> store = new HashMap<>();
    private static final AtomicLong counter = new AtomicLong(100000);
    private static final String alphabets = "abcdefghijklmnopqrstuvwxyzABCDEFGHIJKLMNOPQRSTUVWXYZ";

    public static void main(String[] args) throws IOException {
        HttpServer server = HttpServer.create(new InetSocketAddress(9090),0);
        server.createContext("/", exchange -> {
            File file = new File("index.html");
            if(!file.exists()){
                String msg = "index.html not found";
                exchange.sendResponseHeaders(404, msg.length());
                exchange.getResponseBody().write(msg.getBytes());
                exchange.close();
                return;
            }
            byte[] response = java.nio.file.Files.readAllBytes(file.toPath());
            exchange.getResponseHeaders().set("Content-Type", "text/html");
            exchange.sendResponseHeaders(200, response.length);
            exchange.getResponseBody().write(response);
            exchange.close();
        });
        server.createContext("/shorten", new ShortenHandler());
        server.createContext("/r", new RedirectHandler());
        server.start();
        System.out.println("Server running at http://localhost:9090/");
    }

    private static String encode(long num){
        StringBuilder sb = new StringBuilder();
        while (num>0) {
            int idx = (int) (num%alphabets.length());
            sb.append(alphabets.charAt(idx));
            num/=alphabets.length();
        }
        return sb.reverse().toString();
    }
    static class ShortenHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            if("POST".equals(exchange.getRequestMethod())){
                String body = new String(exchange.getRequestBody().readAllBytes());
                String longurl = extractUrlFromJson(body);

                if(longurl.isEmpty()){
                    sendResponse(exchange, 400, "Missing URL");
                    return;
                }

                long id = counter.incrementAndGet();
                String code = encode(id);
                store.put(code, longurl);

                String response = "{\"shortUrl\": \"http://localhost:9090/r/" + code + "\"}";
                exchange.getResponseHeaders().add("Content-Type", "application/json");
                sendResponse(exchange, 200, response);
            } else {
                sendResponse(exchange, 405, "Method not allowed");
            }
        }        
    }

    static class RedirectHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException{
            URI uri = exchange.getRequestURI();
            String path = uri.getPath();
            String[] parts = path.split("/");

            if(parts.length < 3){
                sendResponse(exchange, 400, "Missing short code");
                return;
            }
            String code = parts[2];
            String longurl = store.get(code);

            if (longurl == null) {
                sendResponse(exchange, 404, "URL not found");
                return;
            }
            exchange.getResponseHeaders().add("Location", longurl);
            exchange.sendResponseHeaders(302, -1);

        }
        
    }
    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException{
        byte[] bytes = response.getBytes();
        exchange.sendResponseHeaders(statusCode, bytes.length);
        try(OutputStream os = exchange.getResponseBody()){
            os.write(bytes);
        }
    }
    private static String extractUrlFromJson(String json) {
        json = json.trim();
        if (json.startsWith("{") && json.endsWith("}")) {
            json = json.substring(1, json.length() - 1); // remove { }
            String[] parts = json.split(":", 2);
            if (parts.length == 2) {
                String val = parts[1].trim();
                val = val.replaceAll("[\"}]", ""); // remove quotes and braces
                return val;
            }
        }
        return null;
    }
}
