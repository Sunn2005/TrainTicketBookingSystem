package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleStatisticsResponse {

    private List<ScheduleStatisticDetail> details;

    @Data
    @AllArgsConstructor
    @NoArgsConstructor
    public static class ScheduleStatisticDetail {
        private String scheduleId;
        private String routeName; // e.g. "Sài Gòn → Hà Nội"
        private long totalTickets;
        private long totalSeats;
        private long availableSeats;
        private double totalRevenue;
        private double loadFactor; // in %
    }
}
