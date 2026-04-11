package iuh.fit.controller;

import controller.TicketController;
import dto.ActionResponse;
import dto.ScheduleInfoResponse;
import dto.SellTicketRequest;
import model.entity.Seat;
import model.entity.enums.PaymentStatus;

import java.time.LocalDate;
import java.util.List;

public class TicketClientController {
    private final TicketController delegate;

    public TicketClientController() {
        this.delegate = new TicketController();
    }

    public TicketClientController(TicketController delegate) {
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
}