package service;

import util.JPAUtil;
import model.entity.Customer;
import model.entity.Payment;
import model.entity.Schedule;
import model.entity.Seat;
import model.entity.Ticket;
import model.entity.User;
import model.entity.enums.CustomerType;
import model.entity.enums.PaymentStatus;
import model.entity.enums.TicketStatus;
import model.entity.enums.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import dto.ScheduleInfoResponse;

public class TicketService {
    public static class SellTicketRequest {
        private String sellerUserId;
        private String scheduleId;
        private String seatId;
        private String customerName;
        private String customerCccd;
        private CustomerType customerType;
        private String discount;
        private double price;
        private Double finalPrice;
        private String paymentMethod;
        private BigDecimal paymentAmount;
        private boolean paymentConfirmed;
        private String qrCode;

        public String getSellerUserId() {
            return sellerUserId;
        }

        public void setSellerUserId(String sellerUserId) {
            this.sellerUserId = sellerUserId;
        }

        public String getScheduleId() {
            return scheduleId;
        }

        public void setScheduleId(String scheduleId) {
            this.scheduleId = scheduleId;
        }

        public String getSeatId() {
            return seatId;
        }

        public void setSeatId(String seatId) {
            this.seatId = seatId;
        }

        public String getCustomerName() {
            return customerName;
        }

        public void setCustomerName(String customerName) {
            this.customerName = customerName;
        }

        public String getCustomerCccd() {
            return customerCccd;
        }

        public void setCustomerCccd(String customerCccd) {
            this.customerCccd = customerCccd;
        }

        public CustomerType getCustomerType() {
            return customerType;
        }

        public void setCustomerType(CustomerType customerType) {
            this.customerType = customerType;
        }

        public String getDiscount() {
            return discount;
        }

        public void setDiscount(String discount) {
            this.discount = discount;
        }

        public double getPrice() {
            return price;
        }

        public void setPrice(double price) {
            this.price = price;
        }

        public Double getFinalPrice() {
            return finalPrice;
        }

        public void setFinalPrice(Double finalPrice) {
            this.finalPrice = finalPrice;
        }

        public String getPaymentMethod() {
            return paymentMethod;
        }

        public void setPaymentMethod(String paymentMethod) {
            this.paymentMethod = paymentMethod;
        }

        public BigDecimal getPaymentAmount() {
            return paymentAmount;
        }

        public void setPaymentAmount(BigDecimal paymentAmount) {
            this.paymentAmount = paymentAmount;
        }

        public boolean isPaymentConfirmed() {
            return paymentConfirmed;
        }

        public void setPaymentConfirmed(boolean paymentConfirmed) {
            this.paymentConfirmed = paymentConfirmed;
        }

        public String getQrCode() {
            return qrCode;
        }

        public void setQrCode(String qrCode) {
            this.qrCode = qrCode;
        }
    }

    public static class SellTicketResponse {
        private boolean success;
        private String message;
        private String ticketId;
        private String paymentId;

        public SellTicketResponse(boolean success, String message, String ticketId, String paymentId) {
            this.success = success;
            this.message = message;
            this.ticketId = ticketId;
            this.paymentId = paymentId;
        }

        public static SellTicketResponse success(String ticketId, String paymentId) {
            return new SellTicketResponse(true, "Sell ticket successfully.", ticketId, paymentId);
        }

        public static SellTicketResponse fail(String message) {
            return new SellTicketResponse(false, message, null, null);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }

        public String getTicketId() {
            return ticketId;
        }

        public String getPaymentId() {
            return paymentId;
        }
    }

    public static class ActionResponse {
        private boolean success;
        private String message;

        public ActionResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ActionResponse success(String message) {
            return new ActionResponse(true, message);
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
    }

    public TicketService() {
    }

    public boolean createNewTicket(Ticket ticket) {
        if (ticket == null) {
            return false;
        }

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            em.merge(ticket);
            tx.commit();
            return true;
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return false;
        } finally {
            em.close();
        }
    }

    public SellTicketResponse sellTicket(SellTicketRequest request) {
        if (request == null) {
            return SellTicketResponse.fail("Request is required.");
        }

        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            User seller = em.find(User.class, request.getSellerUserId());
            if (seller == null) {
                throw new IllegalArgumentException("Seller not found: " + request.getSellerUserId());
            }
            if (seller.getUserStatus() != UserStatus.ACTIVE) {
                throw new IllegalArgumentException("Seller is inactive: " + request.getSellerUserId());
            }

            Schedule schedule = em.find(Schedule.class, request.getScheduleId());
            if (schedule == null) {
                throw new IllegalArgumentException("Schedule not found: " + request.getScheduleId());
            }

            Seat seat = em.find(Seat.class, request.getSeatId());
            if (seat == null) {
                throw new IllegalArgumentException("Seat not found: " + request.getSeatId());
            }

            if (!seat.getTrain().getTrainID().equals(schedule.getTrain().getTrainID())) {
                throw new IllegalArgumentException("Seat does not belong to schedule's train.");
            }

            Long bookedCount = em.createQuery(
                            "SELECT COUNT(t) FROM Ticket t "
                                    + "WHERE t.schedule.scheduleID = :scheduleId "
                                    + "AND t.seat.seatID = :seatId "
                                    + "AND t.ticketStatus <> :cancelledStatus", Long.class)
                    .setParameter("scheduleId", request.getScheduleId())
                    .setParameter("seatId", request.getSeatId())
                    .setParameter("cancelledStatus", TicketStatus.CANCELLED)
                    .getSingleResult();

            if (bookedCount != null && bookedCount > 0) {
                throw new IllegalStateException("Seat is already booked for this schedule.");
            }

            // Reuse customerID as CCCD for now because Customer entity has no dedicated cccd field.
            Customer customer = em.find(Customer.class, request.getCustomerCccd());
            if (customer == null) {
                customer = new Customer();
                customer.setCustomerID(request.getCustomerCccd());
            }
            customer.setFullName(request.getCustomerName());
            CustomerType cType = request.getCustomerType() == null ? CustomerType.ADULT : request.getCustomerType();
            customer.setCustomerType(cType);
            customer = em.merge(customer);

            // Xử lý logic Giảm giá theo đối tượng (Dựa theo nghiệp vụ)
            // - Trẻ em (Dưới 6 tuổi: không mua vé ngồi chung, 6-10 tuổi: Giảm 25%)
            // - Sinh viên: Giảm 10%
            // - Người cao tuổi (>= 60 tuổi): Giảm 15%
            // - Người lớn: Giá gốc
            double basePrice = request.getPrice();
            double calculatedFinalPrice = basePrice;
            String computedDiscount = "0%";

            switch (cType) {
                case CHILD:
                    calculatedFinalPrice = basePrice * 0.75; // Giảm 25%
                    computedDiscount = "25%";
                    break;
                case STUDENT:
                    calculatedFinalPrice = basePrice * 0.90; // Giảm 10%
                    computedDiscount = "10%";
                    break;
                case ELDERLY:
                    calculatedFinalPrice = basePrice * 0.85; // Giảm 15%
                    computedDiscount = "15%";
                    break;
                case ADULT:
                default:
                    calculatedFinalPrice = basePrice;
                    computedDiscount = "0%";
                    break;
            }

            Ticket ticket = new Ticket();
            ticket.setTicketID(generateId("TICKET"));
            ticket.setUser(seller);
            ticket.setCustomer(customer);
            ticket.setSchedule(schedule);
            ticket.setSeat(seat);
            ticket.setDiscount(computedDiscount);
            ticket.setPrice(basePrice);
            ticket.setFinalPrice(calculatedFinalPrice);
            ticket.setTicketStatus(request.isPaymentConfirmed() ? TicketStatus.PAID : TicketStatus.PENDING);
            em.persist(ticket);

            Payment payment = new Payment();
            payment.setPaymentID(generateId("PAY"));
            payment.setTicket(ticket);
            payment.setPaymentMethod(request.getPaymentMethod());
            // Ép buộc số tiền thanh toán phải bằng với giá đã tính phần trăm từ máy chủ
            payment.setAmount(BigDecimal.valueOf(calculatedFinalPrice));
            payment.setPaymentTime(LocalDateTime.now());
            payment.setPaymentStatus(request.isPaymentConfirmed() ? PaymentStatus.SUCCESS : PaymentStatus.PENDING);
            em.persist(payment);

            tx.commit();
            return SellTicketResponse.success(ticket.getTicketID(), payment.getPaymentID());
        } catch (IllegalArgumentException | IllegalStateException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return SellTicketResponse.fail(e.getMessage());
        } catch (PersistenceException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return SellTicketResponse.fail("Cannot create ticket. Data constraint was violated.");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return SellTicketResponse.fail("Unexpected error while selling ticket.");
        } finally {
            em.close();
        }
    }

    public ActionResponse cancelTicket(String ticketId, String cccd) {
        if (cccd == null || cccd.trim().isEmpty()) {
            return ActionResponse.fail("Cần phải cung cấp CCCD để thực hiện huỷ vé.");
        }
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Ticket ticket = em.find(Ticket.class, ticketId);
            if (ticket == null) {
                return ActionResponse.fail("Không tìm thấy Vé: " + ticketId);
            }
            if (ticket.getTicketStatus() == TicketStatus.CANCELLED) {
                return ActionResponse.fail("Vé này đã bị huỷ từ trước.");
            }
            if (ticket.getTicketStatus() == TicketStatus.USED) {
                return ActionResponse.fail("Vé đã sử dụng, không thể huỷ.");
            }
            if (ticket.getCustomer() == null || !cccd.equals(ticket.getCustomer().getCustomerID())) {
                return ActionResponse.fail("CCCD không khớp với thông tin khách hàng trên vé.");
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime departureTime = ticket.getSchedule().getDepartureTime();
            if (now.isAfter(departureTime) || now.isEqual(departureTime)) {
                return ActionResponse.fail("Không thể huỷ vé sau khi tàu đã khởi hành.");
            }

            long minutesToDeparture = ChronoUnit.MINUTES.between(now, departureTime);

            if (minutesToDeparture < 240) { // < 4 giờ
                return ActionResponse.fail("Không thể huỷ vé trước giờ khởi hành dưới 4 tiếng.");
            }

            double feePercentage;
            if (minutesToDeparture < 1440) { // Từ 4h -> dưới 24h
                feePercentage = 0.20;
            } else {                         // Từ 24h trở lên
                feePercentage = 0.10;
            }

            double refundAmount = ticket.getFinalPrice() * (1 - feePercentage);

            ticket.setTicketStatus(TicketStatus.CANCELLED);
            em.merge(ticket);

            // Cập nhật trạng thái thanh toán sang REFUNDED (hoàn tiền)
            List<Payment> payments = em.createQuery("SELECT p FROM Payment p WHERE p.ticket.ticketID = :ticketId", Payment.class)
                    .setParameter("ticketId", ticketId)
                    .getResultList();
            for (Payment p : payments) {
                if (p.getPaymentStatus() == PaymentStatus.SUCCESS || p.getPaymentStatus() == PaymentStatus.PENDING) {
                    p.setPaymentStatus(PaymentStatus.REFUNDED);
                    em.merge(p);
                }
            }

            tx.commit();
            return ActionResponse.success(String.format("Huỷ vé thành công. Phí huỷ vé là: %.0f%%. Số tiền hoàn lại: %,.0f VNĐ", feePercentage * 100, refundAmount));
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return ActionResponse.fail("Lỗi khi huỷ vé: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public ActionResponse exchangeTicket(String ticketId, String newScheduleId, String newSeatId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Ticket ticket = em.find(Ticket.class, ticketId);
            if (ticket == null) {
                return ActionResponse.fail("Không tìm thấy Vé: " + ticketId);
            }
            if (ticket.getTicketStatus() != TicketStatus.PAID) {
                return ActionResponse.fail("Chỉ được đổi khi vé đã thanh toán (PAID).");
            }

            // Kiểm tra đổi 1 lần duy nhất (Ta đánh dấu trạng thái bằng chữ 'EXCHANGED' trong cột discount - hoặc một cờ tuỳ chỉnh)
            if ("EXCHANGED".equalsIgnoreCase(ticket.getDiscount())) {
                return ActionResponse.fail("Vé này đã được đổi 1 lần trước đó, không thể đổi thêm.");
            }

            LocalDateTime now = LocalDateTime.now();
            LocalDateTime departureTime = ticket.getSchedule().getDepartureTime();
            if (now.isAfter(departureTime) || now.isEqual(departureTime)) {
                return ActionResponse.fail("Không thể đổi vé sau khi tàu đã khởi hành.");
            }

            long minutesToDeparture = ChronoUnit.MINUTES.between(now, departureTime);

            if (minutesToDeparture < 60) {
                return ActionResponse.fail("Không thể đổi vé trước giờ khởi hành dưới 1 tiếng.");
            }

            double feePercentage = 0;
            if (minutesToDeparture < 240) {         // Từ 1h -> dưới 4h (4*60=240)
                feePercentage = 0.20;
            } else if (minutesToDeparture < 1440) { // Từ 4h -> dưới 24h (24*60=1440)
                feePercentage = 0.10;
            } else {                                // Từ 24h trở lên
                feePercentage = 0.05;
            }

            double exchangeFee = ticket.getPrice() * feePercentage;

            Schedule newSchedule = em.find(Schedule.class, newScheduleId);
            if (newSchedule == null) {
                return ActionResponse.fail("Không tìm thấy lịch trình mới: " + newScheduleId);
            }

            Seat newSeat = em.find(Seat.class, newSeatId);
            if (newSeat == null) {
                return ActionResponse.fail("Không tìm thấy thông tin ghế mới: " + newSeatId);
            }

            if (!newSeat.getTrain().getTrainID().equals(newSchedule.getTrain().getTrainID())) {
                return ActionResponse.fail("Ghế đổi không nằm trên tàu của chuyến đi mới.");
            }

            Long bookedCount = em.createQuery(
                            "SELECT COUNT(t) FROM Ticket t "
                                    + "WHERE t.schedule.scheduleID = :scheduleId "
                                    + "AND t.seat.seatID = :seatId "
                                    + "AND t.ticketStatus <> :cancelledStatus", Long.class)
                    .setParameter("scheduleId", newScheduleId)
                    .setParameter("seatId", newSeatId)
                    .setParameter("cancelledStatus", TicketStatus.CANCELLED)
                    .getSingleResult();

            if (bookedCount != null && bookedCount > 0) {
                return ActionResponse.fail("Ghế mới đã có người đặt, vui lòng chọn ghế trống khác.");
            }

            ticket.setSchedule(newSchedule);
            ticket.setSeat(newSeat);
            ticket.setDiscount("EXCHANGED"); // Đánh dấu là đã đổi lần đầu

            // Xử lý giá vé mới và phí đổi vé tuỳ chọn
            // Tuỳ nghiệp vụ hiện tại: Khách hàng có thể trả tiền phí bằng tiền mặt ở quầy
            // Dưới đây chỉ in thông báo ra Message, ko thay đổi Amount Payment 1-1 cũ
            em.merge(ticket);

            tx.commit();
            return ActionResponse.success(String.format("Đổi vé thành công. Phí đổi vé là: %,.0f VNĐ (%.0f%% giá vé).", exchangeFee, feePercentage * 100));
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return ActionResponse.fail("Lỗi khi đổi vé: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public List<Schedule> searchSchedules(String departureStationId, String arrivalStationId, LocalDate travelDate) {
        if (travelDate == null) {
            throw new IllegalArgumentException("travelDate is required.");
        }

        LocalDateTime from = travelDate.atStartOfDay();
        LocalDateTime to = travelDate.plusDays(1).atStartOfDay();

        EntityManager em = JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM Schedule s "
                                    + "WHERE s.route.departureStation.stationID = :departureId "
                                    + "AND s.route.arrivalStation.stationID = :arrivalId "
                                    + "AND s.departureTime >= :fromTime "
                                    + "AND s.departureTime < :toTime "
                                    + "ORDER BY s.departureTime", Schedule.class)
                    .setParameter("departureId", departureStationId)
                    .setParameter("arrivalId", arrivalStationId)
                    .setParameter("fromTime", from)
                    .setParameter("toTime", to)
                    .getResultList();
        } finally {
            em.close();
        }
    }

    public List<ScheduleInfoResponse> getSchedulesWithAvailableSeats(String departureStationId, String arrivalStationId, LocalDate travelDate) {
        List<Schedule> schedules = searchSchedules(departureStationId, arrivalStationId, travelDate);
        List<ScheduleInfoResponse> result = new ArrayList<>();

        for (Schedule s : schedules) {
            int availableSeatsCount = findAvailableSeats(s.getScheduleID()).size();

            ScheduleInfoResponse dto = new ScheduleInfoResponse(
                    s.getScheduleID(),
                    s.getTrain().getTrainID(),
                    s.getTrain().getTrainName(),
                    s.getRoute().getDepartureStation().getStationName(),
                    s.getRoute().getArrivalStation().getStationName(),
                    s.getDepartureTime(),
                    s.getArrivalTime(),
                    availableSeatsCount
            );

            result.add(dto);
        }

        return result;
    }

    public List<Seat> findAvailableSeats(String scheduleId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            Schedule schedule = em.find(Schedule.class, scheduleId);
            if (schedule == null) {
                throw new IllegalArgumentException("Schedule not found: " + scheduleId);
            }

            List<Seat> allSeats = em.createQuery(
                            "SELECT s FROM Seat s WHERE s.train.trainID = :trainId ORDER BY s.seatNumber", Seat.class)
                    .setParameter("trainId", schedule.getTrain().getTrainID())
                    .getResultList();

            List<String> bookedSeatIds = em.createQuery(
                            "SELECT t.seat.seatID FROM Ticket t "
                                    + "WHERE t.schedule.scheduleID = :scheduleId "
                                    + "AND t.ticketStatus <> :cancelledStatus", String.class)
                    .setParameter("scheduleId", scheduleId)
                    .setParameter("cancelledStatus", TicketStatus.CANCELLED)
                    .getResultList();

            Set<String> bookedSet = new HashSet<>(bookedSeatIds);
            return allSeats.stream()
                    .filter(seat -> !bookedSet.contains(seat.getSeatID()))
                    .toList();
        } finally {
            em.close();
        }
    }

    private double resolveFinalPrice(SellTicketRequest request) {
        if (request.getFinalPrice() != null) {
            return request.getFinalPrice();
        }

        return request.getPrice();
    }

    private BigDecimal resolveAmount(SellTicketRequest request, Ticket ticket) {
        if (request.getPaymentAmount() != null) {
            return request.getPaymentAmount();
        }

        return BigDecimal.valueOf(ticket.getFinalPrice());
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
