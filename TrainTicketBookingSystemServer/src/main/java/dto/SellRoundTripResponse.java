package dto;

import lombok.*;

import java.util.List;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class SellRoundTripResponse extends ActionResponse {
    private List<String> outboundTicketIds;
    private List<String> returnTicketIds;

    public SellRoundTripResponse(boolean success, String message, String ticketIds, String paymentIds,
                                 double total, String qrUrl,
                                 List<String> outboundTicketIds, List<String> returnTicketIds) {
        super(success, message, ticketIds, paymentIds, total, qrUrl);
        this.outboundTicketIds = outboundTicketIds;
        this.returnTicketIds = returnTicketIds;
    }

    public static SellRoundTripResponse from(ActionResponse base,
                                             List<String> outboundTicketIds,
                                             List<String> returnTicketIds) {
        return new SellRoundTripResponse(base.isSuccess(), base.getMessage(),
                base.getTicketIds(), base.getPaymentIds(), base.getTotal(), base.getQrUrl(),
                outboundTicketIds, returnTicketIds);
    }
}
