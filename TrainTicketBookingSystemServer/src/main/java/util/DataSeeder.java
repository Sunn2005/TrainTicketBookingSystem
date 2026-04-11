package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.entity.*;
import model.entity.enums.CustomerType;
import model.entity.enums.UserStatus;
import model.entity.enums.*;

import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import dto.CreateTrainRequest;
import service.TrainService;

public class DataSeeder {
    public static void main(String[] args) {
        System.out.println("Starting Data Seeder...");
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();

        try {
            tx.begin();

            // 1. Role (Chỉ có 3 role)
            Role rAdmin = new Role("ROLE-001", "ADMIN");
            Role rManager = new Role("ROLE-002", "MANAGER");
            Role rEmployee = new Role("ROLE-003", "EMPLOYEE");
            em.persist(rAdmin); em.persist(rManager); em.persist(rEmployee);

            // 2. User
            User u1 = new User("USER-001", "admin1", "admin123", "Nguyễn Quản Trị", "system", LocalDateTime.now(), rAdmin, UserStatus.ACTIVE);
            User u2 = new User("USER-002", "manager1", "man123", "Phạm Trưởng Ga", "admin1", LocalDateTime.now(), rManager, UserStatus.ACTIVE);
            User u3 = new User("USER-003", "emp1", "emp123", "Lê Nhân Viên Một", "admin1", LocalDateTime.now(), rEmployee, UserStatus.ACTIVE);
            User u4 = new User("USER-004", "emp2", "emp123", "Trần Ban Vé", "admin1", LocalDateTime.now(), rEmployee, UserStatus.ACTIVE);
            User u5 = new User("USER-005", "emp3", "emp123", "Vũ Hướng Dẫn", "admin1", LocalDateTime.now(), rEmployee, UserStatus.ACTIVE);
            em.persist(u1); em.persist(u2); em.persist(u3); em.persist(u4); em.persist(u5);

            // 3. Station
            Station s1 = new Station("STA-001", "Ga Sài Gòn", "Hồ Chí Minh");
            Station s2 = new Station("STA-002", "Ga Nha Trang", "Khánh Hòa");
            Station s3 = new Station("STA-003", "Ga Đà Nẵng", "Đà Nẵng");
            Station s4 = new Station("STA-004", "Ga Huế", "Thừa Thiên Huế");
            Station s5 = new Station("STA-005", "Ga Hà Nội", "Hà Nội");
            em.persist(s1); em.persist(s2); em.persist(s3); em.persist(s4); em.persist(s5);

            // 4. Route
            Route rt1 = new Route("ROU-001", s1, s5, 1726.0); // Sài Gòn -> Hà Nội
            Route rt2 = new Route("ROU-002", s5, s1, 1726.0); // Hà Nội -> Sài Gòn
            Route rt3 = new Route("ROU-003", s1, s3, 935.0);  // Sài Gòn -> Đà Nẵng
            Route rt4 = new Route("ROU-004", s3, s5, 791.0);  // Đà Nẵng -> Hà Nội
            Route rt5 = new Route("ROU-005", s1, s2, 411.0);  // Sài Gòn -> Nha Trang
            em.persist(rt1); em.persist(rt2); em.persist(rt3); em.persist(rt4); em.persist(rt5);

            // 5. Train
            Train tr1 = new Train("TRA-001", "SE1", new java.util.ArrayList<>());
            Train tr2 = new Train("TRA-002", "SE2", new java.util.ArrayList<>());
            Train tr3 = new Train("TRA-003", "SE3", new java.util.ArrayList<>());
            Train tr4 = new Train("TRA-004", "SE4", new java.util.ArrayList<>());
            Train tr5 = new Train("TRA-005", "SE5", new java.util.ArrayList<>());
            em.persist(tr1); em.persist(tr2); em.persist(tr3); em.persist(tr4); em.persist(tr5);

            // 6. Carriage
            java.util.List<Carriage> carriages = new java.util.ArrayList<>();
            int carIndex = 1;
            for (Train t : java.util.List.of(tr1, tr2, tr3, tr4, tr5)) {
                for (int i = 1; i <= 2; i++) {
                    Carriage car = new Carriage(String.format("CAR-%03d", carIndex++), t, i, new java.util.ArrayList<>());
                    em.persist(car);
                    carriages.add(car);
                    t.getCarriages().add(car); // Liên kết Carriage vào Train
                }
            }

            // 7. Seat
            java.util.List<Seat> seats = new java.util.ArrayList<>();
            int seatIndex = 1;
            for (Carriage car : carriages) {
                for (int i = 1; i <= 4; i++) {
                    SeatType type = (i % 2 == 0) ? SeatType.SOFT_SLEEPER : SeatType.SOFT_SEAT;
                    Seat seat = new Seat(String.format("SEA-%03d", seatIndex++), car, i, type);
                    em.persist(seat);
                    seats.add(seat);
                    car.getSeats().add(seat); // Liên kết Seat vào Carriage
                }
            }

            // 8. Customer
            Customer c1 = new Customer("CUS-001", "Trần Khách Một", CustomerType.ADULT);
            Customer c2 = new Customer("CUS-002", "Lê Khách Sinh Viên", CustomerType.STUDENT);
            Customer c3 = new Customer("CUS-003", "Phạm Lão Thành", CustomerType.ELDERLY);
            Customer c4 = new Customer("CUS-004", "Vũ Khách Trẻ Em", CustomerType.CHILD);
            Customer c5 = new Customer("CUS-005", "Hồ Khách Hai", CustomerType.ADULT);
            em.persist(c1); em.persist(c2); em.persist(c3); em.persist(c4); em.persist(c5);

            // 9. Schedule
            LocalDateTime now = LocalDateTime.now();
            Schedule sch1 = new Schedule("SCH-001", tr1, rt1, now.plusDays(1), now.plusDays(2).plusHours(10), ScheduleStatus.ENABLED);
            Schedule sch2 = new Schedule("SCH-002", tr2, rt2, now.plusDays(2), now.plusDays(3).plusHours(8), ScheduleStatus.ENABLED);
            Schedule sch3 = new Schedule("SCH-003", tr3, rt3, now.plusDays(1).plusHours(5), now.plusDays(1).plusHours(20), ScheduleStatus.ENABLED);
            Schedule sch4 = new Schedule("SCH-004", tr4, rt4, now.plusDays(3), now.plusDays(4), ScheduleStatus.ENABLED);
            Schedule sch5 = new Schedule("SCH-005", tr5, rt5, now.minusDays(1), now.plusHours(8), ScheduleStatus.ENABLED);
            em.persist(sch1); em.persist(sch2); em.persist(sch3); em.persist(sch4); em.persist(sch5);

            // 10. Ticket
            Ticket tk1 = new Ticket("TIC-001", u3, c1, sch1, seats.get(0), "0%", 1800000, 1800000, now, TicketStatus.PAID);
            Ticket tk2 = new Ticket("TIC-002", u4, c2, sch1, seats.get(1), "10%", 1800000, 1620000, now, TicketStatus.PENDING);
            Ticket tk3 = new Ticket("TIC-003", u5, c3, sch2, seats.get(4), "15%", 1800000, 1530000, now, TicketStatus.PAID);
            Ticket tk4 = new Ticket("TIC-004", u3, c4, sch5, seats.get(16), "25%", 1000000, 750000, now.minusDays(2), TicketStatus.USED);
            Ticket tk5 = new Ticket("TIC-005", u4, c5, sch4, seats.get(12), "0%", 900000, 900000, now, TicketStatus.CANCELLED);
            em.persist(tk1); em.persist(tk2); em.persist(tk3); em.persist(tk4); em.persist(tk5);

            // 11. Payment
            Payment p1 = new Payment("PAY-001", tk1, "VNPAY", java.math.BigDecimal.valueOf(1800000), now, PaymentStatus.SUCCESS);
            Payment p3 = new Payment("PAY-003", tk3, "MOMO", java.math.BigDecimal.valueOf(1530000), now, PaymentStatus.SUCCESS);
            Payment p4 = new Payment("PAY-004", tk4, "CASH", java.math.BigDecimal.valueOf(750000), now.minusDays(1), PaymentStatus.SUCCESS);
            Payment p5 = new Payment("PAY-005", tk5, "VNPAY", java.math.BigDecimal.valueOf(900000), now, PaymentStatus.REFUNDED);
            em.persist(p1); em.persist(p3); em.persist(p4); em.persist(p5);

            tx.commit();
            System.out.println("Data Seeder executed successfully. Realistic sample data inserted!");

        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
