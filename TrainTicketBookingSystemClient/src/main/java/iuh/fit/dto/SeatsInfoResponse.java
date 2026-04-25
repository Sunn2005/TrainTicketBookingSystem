package iuh.fit.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.Seat;

import java.util.List;

@Data
public class SeatsInfoResponse {
    private List<Seat> seats;
    private List<String> bookedSeatIds;

    public SeatsInfoResponse() {
    }

    public SeatsInfoResponse(List<Seat> seats, List<String> bookedSeatIds) {
        this.seats = seats;
        this.bookedSeatIds = bookedSeatIds;
    }

    public List<Seat> getSeats() {
        return seats;
    }

    public void setSeats(List<Seat> seats) {
        this.seats = seats;
    }

    public List<String> getBookedSeatIds() {
        return bookedSeatIds;
    }

    public void setBookedSeatIds(List<String> bookedSeatIds) {
        this.bookedSeatIds = bookedSeatIds;
    }
}