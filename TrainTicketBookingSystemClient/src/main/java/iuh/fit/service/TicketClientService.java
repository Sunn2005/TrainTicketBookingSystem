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
import iuh.fit.socketconfig.SocketClient;

import java.time.LocalDate;
import java.util.List;

public class TicketClientService {
    private final TicketController delegate;
    private final SocketClient socketClient = new SocketClient();
    private final ObjectMapper objectMapper = new ObjectMapper();

    public TicketClientService() {
        this.delegate = new TicketController();
    }

    public TicketClientService(TicketController delegate) {
        this.delegate = delegate;
    }

    public List<ScheduleInfoResponse> getSchedulesWithAvailableSeats(String departureStation, String destinationStation, LocalDate date) {
        return delegate.getSchedulesWithAvailableSeats(departureStation, destinationStation, date);
    }

    public List<Seat> getAvailableSeats(String scheduleId) {
        return delegate.getAvailableSeats(scheduleId);
    }

    public ActionResponse sellTicket(SellTicketRequest request) {
        return delegate.sellTicket(request);
    }

    public ActionResponse cancelTicket(String ticketId, String reason) {
        return delegate.cancelTicket(ticketId, reason);
    }

    public ActionResponse exchangeTicket(String ticketId, String newScheduleId, String newSeatId) {
        return delegate.exchangeTicket(ticketId, newScheduleId, newSeatId);
    }

    public ActionResponse updatePaymentStatus(String ticketId, PaymentStatus status) {
        return delegate.updatePaymentStatus(ticketId, status);
    }

    public List<Station> getAllStations() throws Exception {
        String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, "GET_ALL_STATIONS");
        if (response == null || response.startsWith("ERROR") || "No response".equals(response)) {
            throw new Exception("Lỗi khi lấy danh sách ga: " + response);
        }
        return objectMapper.readValue(response, new TypeReference<List<Station>>(){});
    }
}