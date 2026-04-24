package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class ScheduleInfoResponse {
    private String scheduleId;
    private String trainId;
    private String trainName;
    private String departureStationName;
    private String arrivalStationName;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
    private int availableSeatCount;
    private double distance;
}

