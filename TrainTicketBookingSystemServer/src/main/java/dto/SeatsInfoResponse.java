package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.Seat;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatsInfoResponse {
    private List<Seat> seats;
    private List<String> bookedSeatIds;
}
