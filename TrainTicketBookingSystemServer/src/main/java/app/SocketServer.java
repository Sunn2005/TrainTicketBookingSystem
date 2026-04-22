package app;


import controller.UserController;
import controller.StationController;
import model.entity.Station;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.LoginResponse;
import java.util.List;

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
    private final StationController stationController = new StationController();
    private final ObjectMapper objectMapper = new ObjectMapper();

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
        if (trimmed.toUpperCase().startsWith("REQUEST_PASSWORD_RESET|")) {
            return handleRequestPasswordReset(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("GET_PASSWORD_RESET_REQUESTS")) {
            return handleGetPasswordResetRequests();
        }
        if (trimmed.toUpperCase().startsWith("RESET_PASSWORD|")) {
            return handleResetPassword(trimmed);
        }
        if ("PING".equalsIgnoreCase(trimmed)) {
            return "PONG";
        }
        if ("GET_ALL_STATIONS".equalsIgnoreCase(trimmed)) {
            try {
                List<Station> stations = stationController.getAllStations();
                return objectMapper.writeValueAsString(stations);
            } catch (Exception e) {
                return "ERROR: Server error when fetching stations";
            }
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

    private String handleRequestPasswordReset(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 5) return "ERROR|Sai dinh dang. Dung: REQUEST_PASSWORD_RESET|userID|fullName|role|email";
        try {
            dto.PasswordResetRequestDTO req = new dto.PasswordResetRequestDTO(parts[1], parts[2], parts[3], parts[4]);
            dto.ActionResponse res = userController.requestPasswordReset(req);
            return res.isSuccess() ? "SUCCESS|" + res.getMessage() : "ERROR|" + res.getMessage();
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetPasswordResetRequests() {
        try {
            java.util.List<dto.PasswordResetRequestDTO> reqs = userController.getPendingPasswordResets();
            return "SUCCESS|" + objectMapper.writeValueAsString(reqs);
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleResetPassword(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 3) return "ERROR|Sai dinh dang. Dung: RESET_PASSWORD|userID|newPassword";
        try {
            dto.ActionResponse res = userController.resetPassword(parts[1], parts[2]);
            return res.isSuccess() ? "SUCCESS|" + res.getMessage() : "ERROR|" + res.getMessage();
        } catch (Exception e) {
            return "ERROR|" + e.getMessage();
        }
    }

    private String safe(String value) {
        if (value == null) {
            return "";
        }
        return value.replace('|', '/');
    }
}