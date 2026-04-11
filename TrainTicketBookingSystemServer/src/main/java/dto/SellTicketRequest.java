package dto;

import model.entity.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellTicketRequest {
    private String sellerUserId;
    private String scheduleId;
    private String seatId;

    private String customerName;
    private String customerCccd;
    private CustomerType customerType;

    private boolean isQRPaymentMethod;

}

