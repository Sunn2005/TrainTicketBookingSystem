package controller;

import entity.Schedule;
import entity.Seat;
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

    public List<Schedule> searchSchedules(String departureStationId, String arrivalStationId, LocalDate travelDate) {
        return ticketService.searchSchedules(departureStationId, arrivalStationId, travelDate);
    }

    public List<ScheduleInfoResponse> getSchedulesWithAvailableSeats(String departureStationId, String arrivalStationId, LocalDate travelDate) {
        return ticketService.getSchedulesWithAvailableSeats(departureStationId, arrivalStationId, travelDate);
    }

    public List<Seat> getAvailableSeats(String scheduleId) {
        return ticketService.findAvailableSeats(scheduleId);
    }

    public TicketService.SellTicketResponse sellTicket(TicketService.SellTicketRequest request) {
        return ticketService.sellTicket(request);
    }

    public TicketService.ActionResponse cancelTicket(String ticketId, String cccd) {
        return ticketService.cancelTicket(ticketId, cccd);
    }

    public TicketService.ActionResponse exchangeTicket(String ticketId, String newScheduleId, String newSeatId) {
        return ticketService.exchangeTicket(ticketId, newScheduleId, newSeatId);
    }
}
