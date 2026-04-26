package app;


import controller.UserController;
import controller.StationController;
import controller.TicketController;
import controller.CustomerController;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import dto.ActionResponse;
import dto.SellRoundTripRequest;
import model.entity.Payment;
import model.entity.Station;
import com.fasterxml.jackson.databind.ObjectMapper;
import dto.LoginResponse;
import dto.SellTicketRequest;
import model.entity.Ticket;

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
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);
    private final TicketController ticketController = new TicketController();
    private final CustomerController customerController = new CustomerController();

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
        
        if (trimmed.toUpperCase().startsWith("GET_SCHEDULES|")) {
            return handleGetSchedules(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("GET_SEATS|")) {
            return handleGetSeats(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("GET_ALL_SEATS_INFO|")) {
            return handleGetAllSeatsInfo(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("GET_CUSTOMER|")) {
            return handleGetCustomer(trimmed);
        }
        if ("GET_ALL_CUSTOMERS".equalsIgnoreCase(trimmed)) {
            try {
                return objectMapper.writeValueAsString(customerController.getAllCustomers());
            } catch (Exception e) {
                return "ERROR: Server error when fetching customers";
            }
        }
        if ("GET_ALL_STATIONS".equalsIgnoreCase(trimmed)) {
            try {
                List<Station> stations = stationController.getAllStations();
                return objectMapper.writeValueAsString(stations);
            } catch (Exception e) {
                return "ERROR: Server error when fetching stations";
            }
        }
        if (trimmed.toUpperCase().startsWith("SELL_ROUND_TRIP|")) {
            return handleSellRoundTrip(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("SELL_TICKET|")) {
            return handleSellTicket(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("UPDATE_PAYMENT_STATUS|")) {
            return handleUpdatePaymentStatus(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("UPDATE_TICKET_STATUS|")) {
            return handleUpdateTicketStatus(trimmed);
        }        if ("BOOKING_STATUS".equalsIgnoreCase(trimmed)) {
            return "SERVER_READY";
        }
        if ("QUIT".equalsIgnoreCase(trimmed)) {
            return "BYE";
        }
        if (trimmed.toUpperCase().startsWith("GET_TICKET|")) {
            return handleGetTicket(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("CANCEL_TICKET|")) {
            return handleCancelTicket(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("EXCHANGE_TICKET|")) {
            return handleExchangeTicket(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("GET_PAYMENT|"))  return handleGetPayment(trimmed); // ← MỚI
        return "RECEIVED: " + trimmed;
    }

    private String handleGetTicket(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 2) return "ERROR|Invalid format";
        try {
            Ticket ticket = ticketController.getTicketById(parts[1].trim());
            if (ticket == null) return "ERROR|Không tìm thấy vé";
            return objectMapper.writeValueAsString(ticket);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleCancelTicket(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 3) return "ERROR|Invalid format";
        try {
            ActionResponse result = ticketController.cancelTicket(
                    parts[1].trim(), parts[2].trim());
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetPayment(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 2) return "ERROR|Invalid format. Expected: GET_PAYMENT|ticketId";
        try {
            Payment payment = ticketController.getPaymentByTicketId(parts[1].trim());
            if (payment == null) return "ERROR|Không tìm thấy payment cho vé này";
            return objectMapper.writeValueAsString(payment);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }
    private String handleExchangeTicket(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 4) return "ERROR|Invalid format. Expected: EXCHANGE_TICKET|ticketId|newScheduleId|newSeatId";
        try {
            ActionResponse result = ticketController.exchangeTicket(
                    parts[1].trim(),
                    parts[2].trim(),
                    parts[3].trim());
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
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

    private String handleGetSchedules(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 4) return "ERROR|Invalid format";
        try {
            java.util.List<dto.ScheduleInfoResponse> res = ticketController.getSchedulesWithAvailableSeats(parts[1], parts[2], java.time.LocalDate.parse(parts[3]));
            return objectMapper.writeValueAsString(res);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetSeats(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 2) return "ERROR|Invalid format";
        try {
            java.util.List<model.entity.Seat> res = ticketController.getAvailableSeats(parts[1]);
            return objectMapper.writeValueAsString(res);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetAllSeatsInfo(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 2) return "ERROR|Invalid format";
        try {
            dto.SeatsInfoResponse res = ticketController.getSeatsInfoForSchedule(parts[1]);
            return objectMapper.writeValueAsString(res);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleSellTicket(String command) {
        try {
            String payload = command.substring(command.indexOf('|') + 1);
            SellTicketRequest request = objectMapper.readValue(payload, SellTicketRequest.class);
            dto.ActionResponse result = ticketController.sellTicket(request);
            System.out.println("handle sell ticket: "+result.getTotal());
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleSellRoundTrip(String command) {
        try {
            String payload = command.substring(command.indexOf('|') + 1);
            SellRoundTripRequest request = objectMapper.readValue(payload, SellRoundTripRequest.class);
            dto.ActionResponse result = ticketController.sellRoundTrip(request);
            return objectMapper.writeValueAsString(result);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleUpdatePaymentStatus(String command) {
        try {
            String[] parts = command.split("\\|", 3);
            if (parts.length < 3) {
                return "ERROR|Invalid format. Expected: UPDATE_PAYMENT_STATUS|paymentId|status";
            }
            String paymentId = parts[1].trim();
            String statusName = parts[2].trim().toUpperCase();
            model.entity.enums.PaymentStatus status = model.entity.enums.PaymentStatus.valueOf(statusName);
            dto.ActionResponse result = ticketController.updatePaymentStatus(paymentId, status);
            return objectMapper.writeValueAsString(result);
        } catch (IllegalArgumentException e) {
            return "ERROR|Invalid payment status: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleUpdateTicketStatus(String command) {
        try {
            String[] parts = command.split("\\|", 3);
            if (parts.length < 3) {
                return "ERROR|Invalid format. Expected: UPDATE_TICKET_STATUS|ticketId|status";
            }
            String ticketId = parts[1].trim();
            String statusName = parts[2].trim().toUpperCase();
            model.entity.enums.TicketStatus status = model.entity.enums.TicketStatus.valueOf(statusName);
            dto.ActionResponse result = ticketController.updateTicketStatus(ticketId, status);
            return objectMapper.writeValueAsString(result);
        } catch (IllegalArgumentException e) {
            return "ERROR|Invalid ticket status: " + e.getMessage();
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetCustomer(String command) {
        String[] parts = command.split("\\|");
        if (parts.length < 2) return "ERROR|Invalid format";
        try {
            String customerId = parts[1].trim();
            model.entity.Customer customer = customerController.getCustomerById(customerId);
            if (customer == null) {
                return "ERROR|Customer not found";
            }
            return objectMapper.writeValueAsString(customer);
        } catch (Exception e) {
            e.printStackTrace();
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