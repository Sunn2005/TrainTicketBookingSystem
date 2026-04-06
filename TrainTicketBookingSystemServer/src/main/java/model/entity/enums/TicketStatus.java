package model.entity.enums;

public enum TicketStatus {
    PENDING,     //(đang giữ chỗ, chưa thanh toán)
    PAID,        //(đã thanh toán)
    CANCELLED,    //(đã hủy)
    EXPIRED,     //(quá thời gian giữ vé)
    USED         //(đã sử dụng / đã lên tàu)
}
