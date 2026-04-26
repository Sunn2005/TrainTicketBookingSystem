package iuh.fit.service;

import controller.TicketController;
import dto.ActionResponse;
import dto.ScheduleInfoResponse;
import dto.SellRoundTripRequest;
import dto.SellRoundTripResponse;
import dto.SellTicketRequest;
import iuh.fit.dto.SeatsInfoResponse;
import model.entity.Payment;
import model.entity.Seat;
import model.entity.Station;
import model.entity.Ticket;
import model.entity.enums.PaymentStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import iuh.fit.socketconfig.SocketClient;

import java.time.LocalDate;
import java.util.List;

public class TicketClientService {
    private final TicketController delegate;
    private final SocketClient socketClient = new SocketClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public TicketClientService() {
        this.delegate = new TicketController();
    }

    public TicketClientService(TicketController delegate) {
        this.delegate = delegate;
    }

    public List<ScheduleInfoResponse> getSchedulesWithAvailableSeats(String departureStation, String destinationStation, LocalDate date) {
        try {
            String message = "GET_SCHEDULES|" + departureStation + "|" + destinationStation + "|" + date.toString();
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, message);
            if (response == null || response.startsWith("ERROR") || "No response".equals(response)) {
                System.err.println("Lỗi lấy lịch trình: " + response);
                return List.of();
            }
            return objectMapper.readValue(response, new TypeReference<List<ScheduleInfoResponse>>(){});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Seat> getAvailableSeats(String scheduleId) {
        try {
            String message = "GET_SEATS|" + scheduleId;
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, message);
            if (response == null || response.startsWith("ERROR") || "No response".equals(response)) {
                System.err.println("Lỗi lấy ghế: " + response);
                return List.of();
            }
            return objectMapper.readValue(response, new TypeReference<List<Seat>>(){});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public SeatsInfoResponse getSeatsInfoForSchedule(String scheduleId) {
        try {
            String message = "GET_ALL_SEATS_INFO|" + scheduleId;
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, message);
            if (response == null || "No response".equals(response)) {
                throw new RuntimeException("Không nhận được phản hồi từ server");
            }
            if (response.startsWith("ERROR")) {
                throw new RuntimeException("Lỗi server khi lấy thông tin ghế: " + response);
            }
            return objectMapper.readValue(response, SeatsInfoResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi khi tải dữ liệu ghế: " + e.getMessage(), e);
        }
    }

    public ActionResponse sellTicket(SellTicketRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, "SELL_TICKET|" + json);
            if (response == null || response.startsWith("ERROR")) {
                return ActionResponse.fail("Lỗi khi bán vé: " + response);
            }
            return objectMapper.readValue(response, ActionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResponse.fail("Lỗi khi kết nối tới server: " + e.getMessage());
        }
    }

    public ActionResponse sellRoundTrip(SellRoundTripRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, "SELL_ROUND_TRIP|" + json);
            if (response == null || response.startsWith("ERROR")) {
                return ActionResponse.fail("Lỗi khi bán vé khứ hồi: " + response);
            }
            return objectMapper.readValue(response, SellRoundTripResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResponse.fail("Lỗi khi kết nối tới server: " + e.getMessage());
        }
    }

    public Ticket getTicketById(String ticketId) {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT, "GET_TICKET|" + ticketId);
            if (response == null || response.startsWith("ERROR")
                    || "No response".equals(response)) {
                System.err.println("Lỗi lấy vé: " + response);
                return null;
            }
            return objectMapper.readValue(response, Ticket.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ActionResponse cancelTicket(String ticketId, String cccd) {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "CANCEL_TICKET|" + ticketId + "|" + cccd);
            if (response == null || response.startsWith("ERROR")
                    || "No response".equals(response)) {
                return ActionResponse.fail("Lỗi hủy vé: " + response);
            }
            return objectMapper.readValue(response, ActionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResponse.fail("Lỗi kết nối: " + e.getMessage());
        }
    }

    public ActionResponse exchangeTicket(String ticketId, String newScheduleId, String newSeatId) {
        try {
            String message = "EXCHANGE_TICKET|" + ticketId + "|" + newScheduleId + "|" + newSeatId;
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, message);
            if (response == null || response.startsWith("ERROR")
                    || "No response".equals(response)) {
                return ActionResponse.fail("Lỗi đổi vé: " + response);
            }
            return objectMapper.readValue(response, ActionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResponse.fail("Lỗi kết nối: " + e.getMessage());
        }
    }
    public Payment getPaymentByTicketId(String ticketId) {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT, "GET_PAYMENT|" + ticketId);
            if (response == null || response.startsWith("ERROR") || "No response".equals(response)) {
                System.err.println("Lỗi lấy payment: " + response);
                return null;
            }
            return objectMapper.readValue(response, Payment.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ActionResponse updatePaymentStatus(String ticketId, PaymentStatus status) {
        try {
            String message = "UPDATE_PAYMENT_STATUS|" + ticketId + "|" + status.name();
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, message);
            if (response == null || response.startsWith("ERROR")) {
                return ActionResponse.fail("Lỗi khi cập nhật trạng thái thanh toán: " + response);
            }
            return objectMapper.readValue(response, ActionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResponse.fail("Lỗi khi kết nối tới server: " + e.getMessage());
        }
    }

    public ActionResponse updateTicketStatus(String ticketId, model.entity.enums.TicketStatus status) {
        try {
            String message = "UPDATE_TICKET_STATUS|" + ticketId + "|" + status.name();
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, message);
            if (response == null || response.startsWith("ERROR")) {
                return ActionResponse.fail("Lỗi khi cập nhật trạng thái vé: " + response);
            }
            return objectMapper.readValue(response, ActionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResponse.fail("Lỗi khi kết nối tới server: " + e.getMessage());
        }
    }

    public List<Station> getAllStations() throws Exception {
        String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, "GET_ALL_STATIONS");
        if (response == null || response.startsWith("ERROR") || "No response".equals(response)) {
            throw new Exception("Lỗi khi lấy danh sách ga: " + response);
        }
        return objectMapper.readValue(response, new TypeReference<List<Station>>(){});
    }
}