package app;

import controller.TicketController;
import util.JPAUtil;
import model.entity.Seat;
import model.entity.enums.CustomerType;
import service.TicketService;
import dto.ScheduleInfoResponse;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.util.List;

public class App {
//    public static void main(String[] args) {
//        EntityManager em = JPAUtil.getEntityManager();
//    }
    public static void main(String[] args) {
        System.out.println("====== BẮT ĐẦU TEST TICKET CONTROLLER ======");
        TicketController controller = new TicketController();

        // 1. Test lấy danh sách lịch trình và chỗ trống (Ví dụ tìm SGO đi NTR ngày mai)
        // Lưu ý: Sửa ga đi (SGO), ga đến (NTR) và Ngày (LocalDate) cho khớp với DB của bạn
        LocalDate travelDate = LocalDate.now().plusDays(1);
        System.out.println("\n--- 1. TÌM CHUYẾN TÀU (SGO -> NTR) NGÀY " + travelDate + " ---");

        List<ScheduleInfoResponse> schedules = controller.getSchedulesWithAvailableSeats("SGO", "NTR", travelDate);
        if (schedules.isEmpty()) {
            System.out.println("Không tìm thấy chuyến tàu nào. (Hãy đảm bảo bạn đã chạy DataSeeder mới nhất)");
        } else {
            for (ScheduleInfoResponse s : schedules) {
                System.out.printf("Mã Lịch Trình: %s | Tàu: %s | %s -> %s | Khởi hành: %s | Chỗ trống: %d%n",
                        s.getScheduleId(), s.getTrainName(),
                        s.getDepartureStationName(), s.getArrivalStationName(),
                        s.getDepartureTime(), s.getAvailableSeatCount());
            }

            // Lấy ID của lịch trình đầu tiên để test tiếp
            String testScheduleId = schedules.get(0).getScheduleId();

            // 2. Test lấy danh sách các ghế cụ thể còn trống của một chuyến tàu
            System.out.println("\n--- 2. DANH SÁCH CÁC GHẾ CÒN TRỐNG CỦA LỊCH TRÌCH [" + testScheduleId + "] ---");
            List<Seat> seats = controller.getAvailableSeats(testScheduleId);
            for (Seat seat : seats) {
                System.out.printf("ID Ghế: %s | Số ghế: %s | Loại: %s%n",
                        seat.getSeatID(), seat.getSeatNumber(), seat.getSeatType());
            }

            // 3. Test bán vé (Booking)
            if (!seats.isEmpty()) {
                System.out.println("\n--- 3. TEST BÁN VÉ CHO GHẾ [" + seats.get(0).getSeatNumber() + "] ---");

                TicketService.SellTicketRequest request = new TicketService.SellTicketRequest();
                request.setSellerUserId("NV_001");               // Mã Seller khớp DB DataSeeder
                request.setScheduleId(testScheduleId);         // Mã lịch trình
                request.setSeatId(seats.get(0).getSeatID());   // Mã ghế
                request.setCustomerName("Khách Hàng Test API");
                request.setCustomerCccd("012345678901");
                request.setCustomerType(CustomerType.ADULT);
                request.setDiscount("0%");
                request.setPrice(450000);
                request.setFinalPrice(450000.0);
                request.setPaymentMethod("Tiền mặt");
                request.setPaymentAmount(BigDecimal.valueOf(450000));
                request.setPaymentConfirmed(true);             // Đã thanh toán => Vé sẽ chuyển Status PAID

                TicketService.SellTicketResponse response = controller.sellTicket(request);
                if (response.isSuccess()) {
                    System.out.println("✅ Bán vé THÀNH CÔNG!");
                    System.out.println("Mã Vé: " + response.getTicketId());
                    System.out.println("Mã Thanh toán: " + response.getPaymentId());

                    // Đếm lại số ghế sau khi bán
                    int leftSeats = controller.getAvailableSeats(testScheduleId).size();
                    System.out.println("=> Số ghế trống còn lại hiện tại: " + leftSeats);
                } else {
                    System.out.println("❌ Bán vé THẤT BẠI: " + response.getMessage());
                }
            } else {
                System.out.println("Không còn ghế trống để test chức năng bán vé.");
            }
        }

        System.out.println("\n====== KẾT THÚC TEST ======");
        JPAUtil.getEntityManager().close(); // Clean up connections
    }
}
