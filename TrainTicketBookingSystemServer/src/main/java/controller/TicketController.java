package controller;

import dto.ActionResponse;
import dto.SellTicketRequest;
import model.entity.Schedule;
import model.entity.Seat;
import model.entity.Ticket;
import model.entity.enums.PaymentStatus;
import service.TicketService;
import dto.ScheduleInfoResponse;

import java.time.LocalDate;
import java.util.List;

public class TicketController {
    private final TicketService ticketService;

    public TicketController() {
        this.ticketService = new TicketService();
    }

    public TicketController(TicketService ticketService) {
        this.ticketService = ticketService;
    }

//    public List<Schedule> searchSchedules(String departureStationId, String arrivalStationId, LocalDate travelDate) {
//        return ticketService.searchSchedules(departureStationId, arrivalStationId, travelDate);
//    }

    public List<ScheduleInfoResponse> getSchedulesWithAvailableSeats(String departureStationId, String arrivalStationId, LocalDate travelDate) {
        return ticketService.getSchedulesWithAvailableSeats(departureStationId, arrivalStationId, travelDate);
    }

    public List<Seat> getAvailableSeats(String scheduleId) {
        return ticketService.findAvailableSeats(scheduleId);
    }

    public ActionResponse sellTicket(SellTicketRequest request) {
        return ticketService.sellTicket(request);
    }

    public ActionResponse cancelTicket(String ticketId, String cccd) {
        return ticketService.cancelTicket(ticketId, cccd);
    }

    public ActionResponse exchangeTicket(String ticketId, String newScheduleId, String newSeatId) {
        return ticketService.exchangeTicket(ticketId, newScheduleId, newSeatId);
    }

    public ActionResponse updatePaymentStatus(String paymentId, PaymentStatus status) {
        return ticketService.updatePaymentStatus(paymentId, status);
    }

    public ActionResponse updateTicketStatus(String ticketId, model.entity.enums.TicketStatus status) {
        return ticketService.updateTicketStatus(ticketId, status);
    }

    public Ticket getTicketById(String ticketId) {
        return ticketService.getTicketById(ticketId);
    }

}
