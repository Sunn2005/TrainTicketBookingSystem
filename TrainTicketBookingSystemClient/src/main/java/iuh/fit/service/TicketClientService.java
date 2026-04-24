package iuh.fit.service;

import controller.TicketController;
import dto.ActionResponse;
import dto.ScheduleInfoResponse;
import dto.SellTicketRequest;
import model.entity.Seat;
import model.entity.Station;
import model.entity.enums.PaymentStatus;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;
import iuh.fit.socketconfig.SocketClient;

import java.time.LocalDate;
import java.util.List;

public class TicketClientService {
    private final TicketController delegate;
    private final SocketClient socketClient = new SocketClient();
    private final ObjectMapper objectMapper = new ObjectMapper().registerModule(new JavaTimeModule()).disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

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

    public ActionResponse cancelTicket(String ticketId, String reason) {
        return delegate.cancelTicket(ticketId, reason);
    }

    public ActionResponse exchangeTicket(String ticketId, String newScheduleId, String newSeatId) {
        return delegate.exchangeTicket(ticketId, newScheduleId, newSeatId);
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