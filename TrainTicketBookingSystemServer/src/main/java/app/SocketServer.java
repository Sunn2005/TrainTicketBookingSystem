package app;


import controller.*;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import dto.*;
import model.entity.Payment;
import model.entity.Station;
import com.fasterxml.jackson.databind.ObjectMapper;
import model.entity.Ticket;

import java.time.LocalDate;
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
    private final ScheduleController scheduleController = new ScheduleController();
    private final TrainController trainController = new controller.TrainController();

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
        if ("GET_ALL_USERS".equalsIgnoreCase(trimmed)) {
            return handleGetAllUsers();
        }
        if (trimmed.toUpperCase().startsWith("CREATE_USER|")) {
            return handleCreateUser(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("CHANGE_STATUS|")) {
            return handleChangeStatus(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("CHANGE_ROLE|")) {
            return handleChangeRole(trimmed);
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
        if (trimmed.toUpperCase().startsWith("GET_CUSTOMERS_BOOKED_BETWEEN|")) {
            return handleGetCustomersBookedBetween(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("GET_TICKETS_BY_CUSTOMER|")) {
            return handleGetTicketsByCustomer(trimmed);
        }
        if (trimmed.startsWith("REVENUE_STATISTICS|")) {
            return handleRevenueStatistics(trimmed);
        }

        if (trimmed.startsWith("SEAT_TYPE_REVENUE|")) {
            return handleSeatTypeRevenue(trimmed);
        }

        if (trimmed.startsWith("SCHEDULE_STATISTICS|")) {
            return handleScheduleStatistics(trimmed);
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

        if ("GET_ALL_TRAINS".equalsIgnoreCase(trimmed)) {
            return handleGetAllTrains();
        }
        if (trimmed.toUpperCase().startsWith("GET_TRAIN_BY_ID|")) {
            return handleGetTrainById(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("CREATE_TRAIN|")) {
            return handleCreateTrain(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("UPDATE_TRAIN|")) {
            return handleUpdateTrain(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("DELETE_TRAIN|")) {
            return handleDeleteTrain(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("CREATE_SCHEDULE|")) {
            return handleCreateSchedule(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("UPDATE_SCHEDULE|")) {
            return handleUpdateSchedule(trimmed);
        }
        if (trimmed.toUpperCase().startsWith("DELETE_SCHEDULE|")) {
            return handleDeleteSchedule(trimmed);
        }
        if ("GET_ALL_SCHEDULES".equalsIgnoreCase(trimmed)) {
            return handleGetAllSchedules();
        }
        if (trimmed.toUpperCase().startsWith("GET_ROUTE_ID|")) {
            return handleGetRouteId(trimmed);
        }
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
    private String handleGetAllUsers() {
        try {
            List<model.entity.User> users = userController.getAllUsers();
            return objectMapper.writeValueAsString(users);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleCreateUser(String command) {
        // CREATE_USER|username|password|fullName|email|roleId
        String[] parts = command.split("\\|", 6);
        if (parts.length < 6)
            return "ERROR|Sai định dạng";
        try {
            dto.ActionResponse res = userController.createUser(
                    parts[1].trim(), parts[2].trim(),
                    parts[3].trim(), parts[4].trim(), parts[5].trim());
            return objectMapper.writeValueAsString(res);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleChangeStatus(String command) {
        // CHANGE_STATUS|userId|status
        String[] parts = command.split("\\|", 3);
        if (parts.length < 3) return "ERROR|Sai định dạng";
        try {
            model.entity.enums.UserStatus status =
                    model.entity.enums.UserStatus.valueOf(parts[2].trim().toUpperCase());
            dto.ActionResponse res = userController.changeStatus(parts[1].trim(), status);
            return objectMapper.writeValueAsString(res);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleChangeRole(String command) {
        // CHANGE_ROLE|adminId|targetUserId|newRoleName
        String[] parts = command.split("\\|", 4);
        if (parts.length < 4) return "ERROR|Sai định dạng";
        try {
            dto.ActionResponse res = userController.changeUserRole(
                    parts[1].trim(), parts[2].trim(), parts[3].trim());
            return objectMapper.writeValueAsString(res);
        } catch (Exception e) {
            e.printStackTrace();
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
        System.out.println("Socket server: "+ parts[0] + ": " + parts[1]);
        if (parts.length < 2) return "ERROR|Invalid format";
        try {
            dto.SeatsInfoResponse res = ticketController.getSeatsInfoForSchedule(parts[1]);
            res.getSeats().forEach(seat -> {
                String carriageNum = seat.getCarriage() != null
                        ? String.valueOf(seat.getCarriage().getCarriageNumber())
                        : "null";
                System.out.println("Toa: " + carriageNum + "  ghe: " + seat.getSeatNumber());
            });
            System.out.println("Socket server - seat res: "+res.getBookedSeatIds());
            System.out.println("Socket server - seat res: " + objectMapper.writeValueAsString(res));

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

    private String handleGetCustomersBookedBetween(String command) {
        try {
            String[] parts = command.split("\\|");

            if (parts.length < 3) {
                return "ERROR|Invalid format";
            }

            LocalDate from = LocalDate.parse(parts[1].trim());
            LocalDate to   = LocalDate.parse(parts[2].trim());

            List<model.entity.Customer> customers =
                    customerController.getCustomersBookedBetween(from, to);

            return objectMapper.writeValueAsString(customers);

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetAllTrains() {
        try {
            List<model.entity.Train> trains = trainController.getAllTrains();
            return objectMapper.writeValueAsString(trains);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetTrainById(String command) {
        String[] parts = command.split("\\|", 2);
        if (parts.length < 2) return "ERROR|Invalid format";
        try {
            model.entity.Train train = trainController.getTrainById(parts[1].trim());
            if (train == null) return "ERROR|Không tìm thấy tàu";
            return objectMapper.writeValueAsString(train);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleCreateTrain(String command) {
        try {
            String payload = command.substring(command.indexOf('|') + 1);
            dto.CreateTrainRequest request =
                    objectMapper.readValue(payload, dto.CreateTrainRequest.class);
            model.entity.Train train = trainController.createTrain(request);
            return objectMapper.writeValueAsString(train);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleUpdateTrain(String command) {
        try {
            String payload = command.substring(command.indexOf('|') + 1);
            System.out.println("UPDATE_TRAIN payload: " + payload);

            UpdateTrainRequest request = objectMapper.readValue(payload, UpdateTrainRequest.class);
            System.out.println("Updating train: trainID=" + request.getTrainID() +
                    ", name=" + request.getTrainName());

            trainController.updateTrain(request);

            return objectMapper.writeValueAsString(
                    dto.ActionResponse.success("Cập nhật tàu thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            String errorMsg = "Lỗi cập nhật tàu: " + e.getMessage();
            System.err.println(errorMsg);
            try {
                return objectMapper.writeValueAsString(
                        dto.ActionResponse.fail(errorMsg));
            } catch (Exception ex) {
                return "ERROR|" + errorMsg;
            }
        }
    }

    private String handleDeleteTrain(String command) {
        String[] parts = command.split("\\|", 2);
        if (parts.length < 2) return "ERROR|Invalid format";
        try {
            trainController.deleteTrain(parts[1].trim());
            return objectMapper.writeValueAsString(
                    dto.ActionResponse.success("Xóa tàu thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleCreateSchedule(String command) {
        try {
            String payload = command.substring(command.indexOf('|') + 1);
            CreateScheduleRequest request = objectMapper.readValue(payload, CreateScheduleRequest.class);
            scheduleController.createSchedule(request);
            return objectMapper.writeValueAsString(
                    dto.ActionResponse.success("Tạo lịch trình thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleUpdateSchedule(String command) {
        try {
            String payload = command.substring(command.indexOf('|') + 1);
            UpdateScheduleRequest request = objectMapper.readValue(payload, UpdateScheduleRequest.class);
            scheduleController.updateSchedule(request);
            return objectMapper.writeValueAsString(
                    dto.ActionResponse.success("Cập nhật lịch trình thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetAllSchedules() {
        try {
            java.util.List<model.entity.Schedule> schedules = scheduleController.getAllSchedules();
            return objectMapper.writeValueAsString(schedules);
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }
    private String handleGetRouteId(String command) {
        String[] parts = command.split("\\|", 3);
        if (parts.length < 3) return "ERROR|Invalid format";
        try {
            String routeId = scheduleController.findRouteIdByStations(
                    parts[1].trim(), parts[2].trim());
            return routeId != null ? routeId : "ERROR|Route not found";
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }
    private String handleDeleteSchedule(String command) {
        String[] parts = command.split("\\|", 2);
        if (parts.length < 2) return "ERROR|Invalid format";
        try {
            scheduleController.deleteSchedule(parts[1].trim());
            return objectMapper.writeValueAsString(
                    dto.ActionResponse.success("Ngừng lịch trình thành công"));
        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleGetTicketsByCustomer(String command) {
        try {
            String[] parts = command.split("\\|");
            if (parts.length < 2) return "ERROR|Invalid format";

            String customerId = parts[1].trim();

            List<Ticket> tickets =
                    ticketController.getTicketsByCustomer(customerId);

            return objectMapper.writeValueAsString(tickets);

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleRevenueStatistics(String command) {
        try {
            String[] parts = command.split("\\|");

            RevenueStatisticsRequest req = new RevenueStatisticsRequest();
            req.setManagerID(parts[1].trim());
            req.setStartDate(LocalDate.parse(parts[2].trim()));
            req.setEndDate(LocalDate.parse(parts[3].trim()));

            RevenueStatisticsResponse res =
                    userController.revenueStatistics(req);

            return objectMapper.writeValueAsString(res);

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleSeatTypeRevenue(String command) {
        try {
            String[] parts = command.split("\\|");

            SeatTypeRevenueRequest req = new SeatTypeRevenueRequest();
            req.setManagerID(parts[1].trim());

            SeatTypeRevenueResponse res =
                    userController.seatTypeRevenue(req);

            return objectMapper.writeValueAsString(res);

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

    private String handleScheduleStatistics(String command) {
        try {
            String[] parts = command.split("\\|");

            ScheduleStatisticsRequest req = new ScheduleStatisticsRequest();
            req.setManagerID(parts[1].trim());

            ScheduleStatisticsResponse res =
                    userController.scheduleStatistics(req);

            return objectMapper.writeValueAsString(res);

        } catch (Exception e) {
            e.printStackTrace();
            return "ERROR|" + e.getMessage();
        }
    }

}