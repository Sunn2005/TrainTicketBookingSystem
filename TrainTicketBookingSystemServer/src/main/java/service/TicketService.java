package service;

import com.fasterxml.jackson.databind.ObjectMapper;
import dto.ActionResponse;
import dto.SellTicketRequest;
import model.entity.*;
import util.JPAUtil;
import model.entity.enums.CustomerType;
import model.entity.enums.PaymentStatus;
import model.entity.enums.TicketStatus;
import model.entity.enums.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import jakarta.persistence.PersistenceException;

import java.io.File;
import java.io.IOException;
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
import dao.TicketDAO;
import dao.PaymentDAO;
import dao.ScheduleDAO;
import dao.SeatDAO;




public class TicketService {
    private final TicketDAO ticketDAO = new TicketDAO();
    private final PaymentDAO paymentDAO = new PaymentDAO();
    private final ScheduleDAO scheduleDAO = new ScheduleDAO();
    private final SeatDAO seatDAO = new SeatDAO();
    private final VietQRService vietQRService = new VietQRService();

    private static final String FILE_PATH = "json/basePrice.json";
    private final ObjectMapper mapper = new ObjectMapper();
    private BasePrice basePrice;




    public TicketService() {
        {
            try {
                basePrice = mapper.readValue(new File(FILE_PATH), BasePrice.class);
            } catch (IOException e) {
                throw new RuntimeException(e);
            }
        }
    }

    public ActionResponse sellTicket(SellTicketRequest request) {
        if (request == null) {
            return ActionResponse.fail("Request is required.");
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

            if (request.getTickets() == null || request.getTickets().isEmpty()) {
                return ActionResponse.fail("No tickets to sell.");
            }

            List<Ticket> savedTickets = new ArrayList<>();
            double totalFinalPrice = 0.0;
            List<String> ticketIds = new ArrayList<>();

            for (SellTicketRequest.TicketDetail detail : request.getTickets()) {
                Schedule schedule = em.find(Schedule.class, detail.getScheduleId());
                if (schedule == null || schedule.getScheduleStatus() == model.entity.enums.ScheduleStatus.DISABLED) {
                    throw new IllegalArgumentException("Không tìm thấy lịch trình hoặc lịch trình đã bị vô hiệu hóa.");
                }

                // Seat validation
                Seat seat = null;
                if (detail.getSeatId() != null) {
                    seat = em.find(Seat.class, detail.getSeatId());
                    if (seat == null) {
                        throw new IllegalArgumentException("Seat not found: " + detail.getSeatId());
                    }

                    if (!seat.getCarriage().getTrain().getTrainID().equals(schedule.getTrain().getTrainID())) {
                        throw new IllegalArgumentException("Seat does not belong to schedule's train.");
                    }

                    if (ticketDAO.isSeatBooked(detail.getScheduleId(), detail.getSeatId())) {
                        throw new IllegalStateException("Ghế đã có người đặt trên chuyến này.");
                    }
                }

                // Customer
                Customer customer = em.find(Customer.class, detail.getCustomerCccd());
                if (customer == null) {
                    customer = new Customer();
                    customer.setCustomerID(detail.getCustomerCccd());
                }
                customer.setFullName(detail.getCustomerName());
                CustomerType cType = detail.getCustomerType() == null ? CustomerType.ADULT : detail.getCustomerType();
                customer.setCustomerType(cType);
                customer = em.merge(customer);

                double distance = schedule.getRoute().getDistance();
                double seatFee = 0;
                if (seat != null) {
                    if (seat.getSeatType() == model.entity.enums.SeatType.SOFT_SEAT) {
                        seatFee = this.basePrice.getSoftSeatFee();
                    } else if (seat.getSeatType() == model.entity.enums.SeatType.SOFT_SLEEPER) {
                        seatFee = this.basePrice.getSoftSleeperFee();
                    }
                }
                double basePriceVal = distance * this.basePrice.getPricePerDistance() + seatFee;

                double calculatedFinalPrice = basePriceVal;
                String computedDiscount = "0%";

                switch (cType) {
                    case CHILD:
                        calculatedFinalPrice = basePriceVal * this.basePrice.getChildDiscount();
                        computedDiscount = String.format("%.0f%%", (1 - this.basePrice.getChildDiscount()) * 100);
                        break;
                    case STUDENT:
                        calculatedFinalPrice = basePriceVal * this.basePrice.getStudentDiscount();
                        computedDiscount = String.format("%.0f%%", (1 - this.basePrice.getStudentDiscount()) * 100);
                        break;
                    case ELDERLY:
                        calculatedFinalPrice = basePriceVal * this.basePrice.getElderlyDiscount();
                        computedDiscount = String.format("%.0f%%", (1 - this.basePrice.getElderlyDiscount()) * 100);
                        break;
                    case ADULT:
                    default:
                        calculatedFinalPrice = basePriceVal;
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
                ticket.setPrice(basePriceVal);
                ticket.setFinalPrice(calculatedFinalPrice);
                ticket.setCreateAt(LocalDateTime.now());
                ticket.setTicketStatus(TicketStatus.PENDING);
                em.persist(ticket);

                Payment payment = new Payment();
                payment.setPaymentID(generateId("PAY"));
                payment.setTicket(ticket);
                payment.setPaymentMethod(request.isQRPaymentMethod() ? "QR_CODE" : "CASH");
                payment.setAmount(BigDecimal.valueOf(calculatedFinalPrice));
                payment.setPaymentTime(LocalDateTime.now());
                payment.setPaymentStatus(PaymentStatus.PENDING);
                em.persist(payment);

                savedTickets.add(ticket);
                ticketIds.add(ticket.getTicketID());
                totalFinalPrice += calculatedFinalPrice;
            }

            tx.commit();

            String joinedIds = String.join(", ", ticketIds);
            if (request.isQRPaymentMethod()) {
                String paymentDescription = "Thanh toan ve " + joinedIds;
                if (paymentDescription.length() > 50) {
                    paymentDescription = paymentDescription.substring(0, 47) + "...";
                }
                String qrUrl = vietQRService.generateQRCodeUrl(totalFinalPrice, paymentDescription);
                return ActionResponse.success(joinedIds + " - URL_QR: " + qrUrl);
            } else {
                return ActionResponse.success(joinedIds);
            }

        } catch (IllegalArgumentException | IllegalStateException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return ActionResponse.fail(e.getMessage());
        } catch (PersistenceException e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return ActionResponse.fail("Cannot create ticket. Data constraint was violated.");
        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            return ActionResponse.fail("Unexpected error while selling ticket.");
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
            List<Payment> payments = paymentDAO.findPaymentsByTicketId(ticketId);
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
            if (newSchedule == null || newSchedule.getScheduleStatus() == model.entity.enums.ScheduleStatus.DISABLED) {
                return ActionResponse.fail("Không tìm thấy lịch trình mới hoặc lịch trình đã bị vô hiệu hóa.");
            }

            Seat newSeat = em.find(Seat.class, newSeatId);
            if (newSeat == null) {
                return ActionResponse.fail("Không tìm thấy thông tin ghế mới: " + newSeatId);
            }

            if (!newSeat.getCarriage().getTrain().getTrainID().equals(newSchedule.getTrain().getTrainID())) {
                return ActionResponse.fail("Ghế đổi không nằm trên tàu của chuyến đi mới.");
            }

            if (ticketDAO.isSeatBooked(newScheduleId, newSeatId)) {
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

    public ActionResponse updatePaymentStatus(String paymentId, PaymentStatus status) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            Payment payment = em.find(Payment.class, paymentId);
            if (payment == null) {
                return ActionResponse.fail("Không tìm thấy giao dịch (Payment): " + paymentId);
            }

            payment.setPaymentStatus(status);
            em.merge(payment);

            Ticket ticket = payment.getTicket();
            if (ticket != null) {
                if (status == PaymentStatus.SUCCESS && ticket.getTicketStatus() == TicketStatus.PENDING) {
                    ticket.setTicketStatus(TicketStatus.PAID);
                    em.merge(ticket);
                } else if (status == PaymentStatus.FAILED && ticket.getTicketStatus() == TicketStatus.PENDING) {
                    ticket.setTicketStatus(TicketStatus.CANCELLED);
                    em.merge(ticket);
                }
            }

            tx.commit();
            return ActionResponse.success("Cập nhật trạng thái thanh toán thành công: " + status);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return ActionResponse.fail("Lỗi khi cập nhật trạng thái thanh toán: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public List<Schedule> searchSchedules(String departureStationId, String arrivalStationId, LocalDate travelDate) {
        if (travelDate == null) {
            throw new IllegalArgumentException("travelDate is required.");
        }

        return scheduleDAO.searchSchedules(departureStationId, arrivalStationId, travelDate);
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
            if (schedule == null || schedule.getScheduleStatus() == model.entity.enums.ScheduleStatus.DISABLED) {
                return java.util.Collections.emptyList();
            }

            List<Seat> allSeats = seatDAO.findByTrainId(schedule.getTrain().getTrainID());
            List<String> bookedSeatIds = ticketDAO.getBookedSeatIds(scheduleId);

            Set<String> bookedSet = new HashSet<>(bookedSeatIds);
            return allSeats.stream()
                    .filter(seat -> !bookedSet.contains(seat.getSeatID()))
                    .toList();
        } finally {
            em.close();
        }
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
