package app;

import controller.UserController;
import dto.LoginResponse;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.ServerSocket;
import java.net.Socket;
import java.nio.charset.StandardCharsets;

public class SocketServer {
    public static final int DEFAULT_PORT = 9999;
    private final UserController userController = new UserController();

    public void start(int port) {
        try (ServerSocket serverSocket = new ServerSocket(port)) {
            System.out.println("Socket server is listening on port " + port);

            while (true) {
                Socket clientSocket = serverSocket.accept();
                Thread.ofVirtual().start(() -> handleClient(clientSocket));
            }
        } catch (IOException e) {
            throw new RuntimeException("Cannot start socket server on port " + port, e);
        }
    }

    private void handleClient(Socket clientSocket) {
        try (Socket socket = clientSocket;
             BufferedReader reader = new BufferedReader(
                     new InputStreamReader(socket.getInputStream(), StandardCharsets.UTF_8));
             BufferedWriter writer = new BufferedWriter(
                     new OutputStreamWriter(socket.getOutputStream(), StandardCharsets.UTF_8))) {

            System.out.println("Client connected: " + socket.getRemoteSocketAddress());
            writer.write("CONNECTED");
            writer.newLine();
            writer.flush();

            String message;
            while ((message = reader.readLine()) != null) {
                System.out.println("[" + socket.getRemoteSocketAddress() + "] " + message);
                String response = processMessage(message);
                writer.write(response);
                writer.newLine();
                writer.flush();

                if ("QUIT".equalsIgnoreCase(message.trim())) {
                    break;
                }
            }
            System.out.println("Client disconnected: " + socket.getRemoteSocketAddress());
        } catch (IOException e) {
            System.err.println("Client disconnected with error: " + e.getMessage());
        }
    }

    private String processMessage(String message) {
        String trimmed = message == null ? "" : message.trim();

        if (trimmed.isEmpty()) {
            return "ERROR: empty message";
        }
        if (trimmed.toUpperCase().startsWith("LOGIN|")) {
            return handleLogin(trimmed);
        }
        if ("PING".equalsIgnoreCase(trimmed)) {
            return "PONG";
        }
        if ("BOOKING_STATUS".equalsIgnoreCase(trimmed)) {
            return "SERVER_READY";
        }
        if ("QUIT".equalsIgnoreCase(trimmed)) {
            return "BYE";
        }
        return "RECEIVED: " + trimmed;
    }

    private String handleLogin(String command) {
        String[] parts = command.split("\\|", 3);
        if (parts.length < 3) {
            return "LOGIN_FAIL|Sai dinh dang. Dung: LOGIN|username|password";
        }

        String username = parts[1].trim();
        String password = parts[2].trim();
        if (username.isEmpty() || password.isEmpty()) {
            return "LOGIN_FAIL|Username/password khong duoc rong";
        }

        try {
            LoginResponse result = userController.login(username, password);
            if (result != null && result.isSuccess()) {
                return "LOGIN_SUCCESS|"
                        + safe(result.getUserID()) + "|"
                        + safe(result.getFullName()) + "|"
                        + safe(result.getRole()) + "|"
                        + safe(result.getMessage());
            }

            String failMessage = result == null ? "Dang nhap that bai" : safe(result.getMessage());
            return "LOGIN_FAIL|" + failMessage;
        } catch (Exception e) {
            return "LOGIN_FAIL|" + safe(e.getMessage());
        }
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('|', '/');
    }
}