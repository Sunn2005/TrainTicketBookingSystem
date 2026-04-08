package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.enums.StatisticType;

import java.time.LocalDate;

@Data
@AllArgsConstructor
@NoArgsConstructor

public class RevenueStatisticsRequest {
    private String managerID;
    private LocalDate startDate;
    private LocalDate endDate;

    private StatisticType statisticType;
}
