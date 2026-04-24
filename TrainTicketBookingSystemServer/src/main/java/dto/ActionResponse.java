package dto;

public class ActionResponse {
    private boolean success;
    private String message;
    private String ticketIds;
    private String paymentIds;
    private double total;
    private String qrUrl;

    public ActionResponse() {
    }

    public ActionResponse(boolean success, String message) {
        this.success = success;
        this.message = message;
    }

    public ActionResponse(boolean success, String message, String ticketIds, String paymentIds, double total, String qrUrl) {
        this.success = success;
        this.message = message;
        this.ticketIds = ticketIds;
        this.paymentIds = paymentIds;
        this.total = total;
        this.qrUrl = qrUrl;
    }

    public static ActionResponse success(String message) {
        return new ActionResponse(true, message);
    }

    public static ActionResponse success(String message, String ticketIds, String paymentIds, double total, String qrUrl) {
        return new ActionResponse(true, message, ticketIds, paymentIds, total, qrUrl);
    }

    public static ActionResponse fail(String message) {
        return new ActionResponse(false, message);
    }

    public boolean isSuccess() {
        return success;
    }

    public String getMessage() {
        return message;
    }

    public String getTicketIds() {
        return ticketIds;
    }

    public String getPaymentIds() {
        return paymentIds;
    }

    public double getTotal() {
        return total;
    }

    public String getQrUrl() {
        return qrUrl;
    }
}
