package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import model.entity.enums.TicketStatus;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class TransactionDTO {
    private String ticketId;
    private String employeeName;
    private LocalDateTime createdAt;

    private String customerName;
    private String customerCccd;

    private Double price;
    private String discount; // e.g., "10%" or flat amount

    private String trainName;
    private int carriageNumber;
    private int seatNumber;
    private String departureStation;  // e.g., "SE1: Ha Noi - Sai Gon"
    private String arrivalStation;
    private LocalDateTime departureTime;// e.g., "SE1: Ha Noi - Sai Gon"
    private LocalDateTime arrivalTime;// e.g., "SE1: Ha Noi - Sai Gon"

    private String paymentMethod;
    private TicketStatus status;
}

