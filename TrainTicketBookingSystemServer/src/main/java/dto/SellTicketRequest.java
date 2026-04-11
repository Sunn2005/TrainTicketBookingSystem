package dto;

import model.entity.enums.CustomerType;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellTicketRequest {
    private String sellerUserId;
    private boolean isQRPaymentMethod;
    private List<TicketDetail> tickets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketDetail {
        private String scheduleId;
        private String seatId;
        private String customerName;
        private String customerCccd;
        private CustomerType customerType;
    }
}
