package app;

import db.JPAUtil;
import entity.*;
import entity.enums.*;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class DataSeeder {
    public static void main(String[] args) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. Roles
            Role adminRole = new Role("ADMIN", "Quản trị viên");
            Role employeeRole = new Role("EMPLOYEE", "Nhân viên bán vé");
            em.persist(adminRole);
            em.persist(employeeRole);

            // 2. Stations (Ga tàu thực tế ở Việt Nam)
            List<Station> stations = Arrays.asList(
                    new Station("SGO", "Ga Sài Gòn", "Quận 3, TP. Hồ Chí Minh"),
                    new Station("NTR", "Ga Nha Trang", "Nha Trang, Khánh Hòa"),
                    new Station("DAN", "Ga Đà Nẵng", "Hải Châu, Đà Nẵng"),
                    new Station("HUE", "Ga Huế", "TP. Huế, Thừa Thiên Huế"),
                    new Station("HNO", "Ga Hà Nội", "Đống Đa, Hà Nội")
            );
            for (Station st : stations) em.persist(st);

            // 3. Trains (Các mác tàu phổ biến)
            List<Train> trains = Arrays.asList(
                    new Train("SE1", "Tàu Khách Bắc Nam SE1"),
                    new Train("SE2", "Tàu Khách Bắc Nam SE2"),
                    new Train("SE3", "Tàu Khách Bắc Nam SE3"),
                    new Train("SE4", "Tàu Khách Bắc Nam SE4"),
                    new Train("SNT1", "Tàu Sài Gòn - Nha Trang")
            );
            for (Train t : trains) em.persist(t);

            // 4. Customers (CCCD thực tế 12 số, Tên tiếng Việt)
            List<Customer> customers = Arrays.asList(
                    new Customer("079099001111", "Nguyễn Văn An", CustomerType.ADULT),
                    new Customer("001090002222", "Trần Thị Bình", CustomerType.CHILD),
                    new Customer("048095003333", "Lê Hoàng Châu", CustomerType.ELDERLY),
                    new Customer("075088004444", "Phạm Khắc Duy", CustomerType.STUDENT),
                    new Customer("001085005555", "Hoàng Ngọc Ánh", CustomerType.ADULT)
            );
            for (Customer c : customers) em.persist(c);

            // 5. Users (Tài khoản nhân viên bán vé)
            List<User> users = Arrays.asList(
                    new User("NV_001", "nguyenvana", "NV001@", "Nguyễn Văn A", employeeRole, UserStatus.ACTIVE),
                    new User("NV_002", "tranthib", "NV002@", "Trần Thị B", employeeRole, UserStatus.ACTIVE),
                    new User("NV_003", "lehoangc", "NV003@", "Lê Hoàng C", employeeRole, UserStatus.ACTIVE),
                    new User("NV_004", "phamkhacd", "NV004@", "Phạm Khắc D", employeeRole, UserStatus.ACTIVE),
                    new User("ADMIN_01", "admin", "ADMIN01@", "Quản Trị Viên", adminRole, UserStatus.ACTIVE)
            );
            for (User u : users) em.persist(u);

            // 6. Routes (Tuyến đường giữa các ga)
            List<Route> routes = Arrays.asList(
                    new Route("ROUTE_SGO_NTR", stations.get(0), stations.get(1), 411.0), // Sài Gòn - Nha Trang
                    new Route("ROUTE_NTR_DAN", stations.get(1), stations.get(2), 524.0), // Nha Trang - Đà Nẵng
                    new Route("ROUTE_DAN_HUE", stations.get(2), stations.get(3), 103.0), // Đà Nẵng - Huế
                    new Route("ROUTE_HUE_HNO", stations.get(3), stations.get(4), 688.0), // Huế - Hà Nội
                    new Route("ROUTE_HNO_SGO", stations.get(4), stations.get(0), 1726.0) // Hà Nội - Sài Gòn
            );
            for (Route r : routes) em.persist(r);

            // 7. Seats (Số ghế thực tế tương ứng với vị trí toa, ví dụ Toa 1 Ghế 10)
            List<Seat> seats = Arrays.asList(
                    new Seat("SEAT_SE1_T1G10", trains.get(0), "Toa 1 - Ghế 10", SeatType.SOFT_SEAT),
                    new Seat("SEAT_SE2_T2G15", trains.get(1), "Toa 2 - Ghế 15", SeatType.SOFT_SEAT),
                    new Seat("SEAT_SE3_T3G20", trains.get(2), "Toa 3 - Ghế 20", SeatType.SOFT_SLEEPER),
                    new Seat("SEAT_SE4_T4G25", trains.get(3), "Toa 4 - Ghế 25", SeatType.SOFT_SLEEPER),
                    new Seat("SEAT_SNT_T5G30", trains.get(4), "Toa 5 - Ghế 30", SeatType.SOFT_SLEEPER)
            );
            for (Seat s : seats) em.persist(s);

            // 8. Schedules (Lịch trình khởi hành vào những ngày tới)
            List<Schedule> schedules = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                LocalDateTime departureTime = LocalDateTime.now().plusDays(i + 1).withHour(19).withMinute(30).withSecond(0);
                LocalDateTime arrivalTime = departureTime.plusHours(8 + i); // Đi từ 8 đến 12 tiếng tùy tuyến dài ngắn
                Schedule s = new Schedule("SCH_2026_" + (i+1), trains.get(i), routes.get(i), departureTime, arrivalTime);
                em.persist(s);
                schedules.add(s);
            }

            // 9. Tickets (Vé tàu với giá tiền thật)
            double[] basePrices = {450000.0, 520000.0, 150000.0, 750000.0, 1800000.0};
            List<Ticket> tickets = new ArrayList<>();
            for (int i = 0; i < 5; i++) {
                Ticket t = new Ticket();
                t.setTicketID("TK_" + String.format("%06d", i + 1));
                t.setUser(users.get(i % users.size()));
                t.setCustomer(customers.get(i));
                t.setSchedule(schedules.get(i));
                t.setSeat(seats.get(i));
                t.setDiscount("0%");    // Có thể set 10% nếu là Sinh viên/Trẻ em
                t.setPrice(basePrices[i]);
                t.setFinalPrice(basePrices[i]);
                t.setTicketStatus(TicketStatus.PAID);
                em.persist(t);
                tickets.add(t);
            }

//            // 10. Payments & QR Payments (Thanh toán qua mã QR Momo/ZaloPay/VNPAY)
            for (int i = 0; i < 5; i++) {
                Payment p = new Payment();
                p.setPaymentID("PAY_" + String.format("%06d", i + 1));
                p.setTicket(tickets.get(i));
                p.setPaymentMethod("VNPAY-QR");
                p.setAmount(BigDecimal.valueOf(tickets.get(i).getFinalPrice()));
                p.setPaymentTime(LocalDateTime.now().minusHours(1));
                p.setPaymentStatus(PaymentStatus.SUCCESS);
                em.persist(p);
            }

            tx.commit();
            System.out.println("Successfully seeded 5 realistic records for all tables.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
