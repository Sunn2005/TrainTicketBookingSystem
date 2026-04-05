package dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SellTicketResponse {
    private boolean success;
    private String message;
    private String ticketId;
    private String paymentId;

    public static SellTicketResponse success(String ticketId, String paymentId) {
        return new SellTicketResponse(true, "Sell ticket successfully.", ticketId, paymentId);
    }

    public static SellTicketResponse fail(String message) {
        return new SellTicketResponse(false, message, null, null);
    }
}

