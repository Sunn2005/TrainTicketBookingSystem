package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.enums.SeatType;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class SeatTypeRevenueResponse {
    private List<SeatTypeRevenueDetail> details;
    private double totalRevenue;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class SeatTypeRevenueDetail {
        private SeatType seatType;
        private long seatNumber; // số lượng vé/ghế đã bán
        private double revenue;  // doanh thu của loại ghế
    }
}
