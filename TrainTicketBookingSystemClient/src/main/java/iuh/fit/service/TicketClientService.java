package iuh.fit.service;

import controller.TicketController;
import dto.ActionResponse;
import dto.ScheduleInfoResponse;
import dto.SellTicketRequest;
import model.entity.Seat;
import model.entity.Station;

import java.time.LocalDate;
import java.util.List;

public class TicketClientService {
    private final TicketController delegate = new TicketController();

    public List<Station> getStations() {
        return new dao.StationDAO().findAll();
    }

    public List<ScheduleInfoResponse> getSchedulesWithAvailableSeats(
            String depId, String arrId, LocalDate date) {
        return delegate.getSchedulesWithAvailableSeats(depId, arrId, date);
    }

    public List<Seat> getAvailableSeats(String scheduleId) {
        List<Seat> seats = delegate.getAvailableSeats(scheduleId);
        // Ép load lazy fields trong cùng session
        for (Seat seat : seats) {
            seat.getCarriage().getCarriageNumber();
            seat.getSeatType().name();
        }
        return seats;
    }

    public ActionResponse sellTicket(SellTicketRequest request) {
        return delegate.sellTicket(request);
    }

    public ActionResponse cancelTicket(String ticketId, String cccd) {
        return delegate.cancelTicket(ticketId, cccd);
    }
}