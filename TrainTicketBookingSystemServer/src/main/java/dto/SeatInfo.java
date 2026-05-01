package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.enums.SeatType;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatInfo {
    private String seatID;
    private int seatNumber;
    private SeatType seatType;
    private CarriageInfo carriage;
}
