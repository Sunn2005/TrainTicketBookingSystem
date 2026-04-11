package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateScheduleRequest {
    private String scheduleID;
    private String managerID;
    private String trainID;
    private String routeID;
    private LocalDateTime departureTime;
    private LocalDateTime arrivalTime;
}

