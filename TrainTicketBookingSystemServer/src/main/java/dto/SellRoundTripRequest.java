package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellRoundTripRequest {
    private String sellerUserId;
    private boolean isQRPaymentMethod;
    private List<TicketDetail> outboundTickets;
    private List<TicketDetail> returnTickets;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class TicketDetail {
        private String scheduleId;
        private String seatId;
        private String customerName;
        private String customerCccd;
        private model.entity.enums.CustomerType customerType;
    }

    public SellTicketRequest toSellTicketRequest() {
        List<SellTicketRequest.TicketDetail> combined = new ArrayList<>();
        if (outboundTickets != null) {
            for (TicketDetail detail : outboundTickets) {
                combined.add(toTicketDetail(detail));
            }
        }
        if (returnTickets != null) {
            for (TicketDetail detail : returnTickets) {
                combined.add(toTicketDetail(detail));
            }
        }
        return new SellTicketRequest(sellerUserId, isQRPaymentMethod, combined);
    }

    private SellTicketRequest.TicketDetail toTicketDetail(TicketDetail detail) {
        return new SellTicketRequest.TicketDetail(
                detail.getScheduleId(),
                detail.getSeatId(),
                detail.getCustomerName(),
                detail.getCustomerCccd(),
                detail.getCustomerType()
        );
    }

    public boolean hasReturnTrip() {
        return returnTickets != null && !returnTickets.isEmpty();
    }

    public List<String> allTicketKeys() {
        if (outboundTickets == null && returnTickets == null) {
            return Collections.emptyList();
        }
        List<String> keys = new ArrayList<>();
        if (outboundTickets != null) {
            for (TicketDetail detail : outboundTickets) {
                keys.add(detail.getScheduleId() + ":" + detail.getSeatId());
            }
        }
        if (returnTickets != null) {
            for (TicketDetail detail : returnTickets) {
                keys.add(detail.getScheduleId() + ":" + detail.getSeatId());
            }
        }
        return keys;
    }
}
