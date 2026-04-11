package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.entity.*;
import model.entity.enums.CustomerType;
import model.entity.enums.UserStatus;

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

            // Seed Roles
            Role roleAdmin = em.find(Role.class, "ROLE-ADMIN");
            if (roleAdmin == null) {
                roleAdmin = new Role("ROLE-ADMIN", "ADMIN");
                em.persist(roleAdmin);
            }

            Role roleManager = em.find(Role.class, "ROLE-MANAGER");
            if (roleManager == null) {
                roleManager = new Role("ROLE-MANAGER", "MANAGER");
                em.persist(roleManager);
            }

            Role roleEmployee = em.find(Role.class, "ROLE-EMPLOYEE");
            if (roleEmployee == null) {
                roleEmployee = new Role("ROLE-EMPLOYEE", "EMPLOYEE");
                em.persist(roleEmployee);
            }

            // Seed Users
            User admin = em.createQuery("SELECT u FROM User u WHERE u.userName = :username", User.class)
                    .setParameter("username", "admin")
                    .getResultStream().findFirst().orElse(null);
            if (admin == null) {
                admin = new User();
                admin.setUserID("USER-ADMIN");
                admin.setUserName("admin");
                admin.setPassword("admin123");
                admin.setFullName("Administrator");
                admin.setUserStatus(UserStatus.ACTIVE);
                admin.setRole(roleAdmin);
                em.persist(admin);
            }

            User manager = em.createQuery("SELECT u FROM User u WHERE u.userName = :username", User.class)
                    .setParameter("username", "manager")
                    .getResultStream().findFirst().orElse(null);
            if (manager == null) {
                manager = new User();
                manager.setUserID("USER-MANAGER");
                manager.setUserName("manager");
                manager.setPassword("manager123");
                manager.setFullName("System Manager");
                manager.setUserStatus(UserStatus.ACTIVE);
                manager.setRole(roleManager);
                em.persist(manager);
            }

            User employee = em.createQuery("SELECT u FROM User u WHERE u.userName = :username", User.class)
                    .setParameter("username", "employee")
                    .getResultStream().findFirst().orElse(null);
            if (employee == null) {
                employee = new User();
                employee.setUserID("USER-001");
                employee.setUserName("Employee B");
                employee.setPassword("employee123");
                employee.setFullName("Tran Thi b");
                employee.setUserStatus(UserStatus.ACTIVE);
                employee.setRole(roleEmployee);
                em.persist(employee);
            }

            // Seed Stations
            Station hanoi = em.find(Station.class, "STA-HANOI");
            if (hanoi == null) {
                hanoi = new Station("STA-HANOI", "Hà Nội", "Hà Nội");
                em.persist(hanoi);
            }

            Station hcm = em.find(Station.class, "STA-HCM");
            if (hcm == null) {
                hcm = new Station("STA-HCM", "Hồ Chí Minh", "TP. HCM");
                em.persist(hcm);
            }

            Station danang = em.find(Station.class, "STA-DANANG");
            if (danang == null) {
                danang = new Station("STA-DANANG", "Đà Nẵng", "Đà Nẵng");
                em.persist(danang);
            }

            // Seed Routes
            Route hn_hcm = em.find(Route.class, "RT-HN-HCM");
            if (hn_hcm == null) {
                hn_hcm = new Route("RT-HN-HCM", hanoi, hcm, 1726.0);
                em.persist(hn_hcm);
            }

            Route hcm_hn = em.find(Route.class, "RT-HCM-HN");
            if (hcm_hn == null) {
                hcm_hn = new Route("RT-HCM-HN", hcm, hanoi, 1726.0);
                em.persist(hcm_hn);
            }

            Route hn_dn = em.find(Route.class, "RT-HN-DN");
            if (hn_dn == null) {
                hn_dn = new Route("RT-HN-DN", hanoi, danang, 791.0);
                em.persist(hn_dn);
            }

            // Seed a mock Ticket to test QR code
            model.entity.Ticket mockTicket = em.find(model.entity.Ticket.class, "TICKET-MOCK-QR");
            if (mockTicket == null) {
                System.out.println("Creating mock ticket for QR testing...");

                // Create a mock Train
                Train mockTrain = em.find(Train.class, "TR-001");
                if (mockTrain == null) {
                    mockTrain = new Train("TR-001", "SE1", null);
                    em.persist(mockTrain);
                }

                // Create a mock Schedule
                Schedule mockSchedule = em.find(Schedule.class, "SCH-001");
                if (mockSchedule == null) {
                    mockSchedule = new Schedule("SCH-001", mockTrain, hn_hcm,
                        LocalDateTime.now().plusDays(1), LocalDateTime.now().plusDays(2),
                        model.entity.enums.ScheduleStatus.ENABLED);
                    em.persist(mockSchedule);
                }

                // Create a mock Customer
                model.entity.Customer mockCustomer = em.find(model.entity.Customer.class, "CCCD-003");
                if (mockCustomer == null) {
                    mockCustomer = new model.entity.Customer("CCCD-003", "Nguyễn Văn A", CustomerType.CHILD);
                    em.persist(mockCustomer);
                }

                Carriage mockCarriage = em.find(Carriage.class, "CARRIAGE-001");
                if (mockCarriage == null) {
                    mockCarriage = new Carriage("CARRIAGE-001", mockTrain, 1, new java.util.ArrayList<>());
                    em.persist(mockCarriage);
                }

                Seat mockSeat = em.find(Seat.class, "SEAT-004");
                if (mockSeat == null) {
                    mockSeat = new Seat("SEAT-004", mockCarriage, 1, model.entity.enums.SeatType.SOFT_SEAT);
                    em.persist(mockSeat);
                }

                Seat mockSeat2 = em.find(Seat.class, "SEAT-005");
                if (mockSeat2 == null) {
                    mockSeat2 = new Seat("SEAT-005", mockCarriage, 2, model.entity.enums.SeatType.SOFT_SEAT);
                    em.persist(mockSeat2);
                }

                mockTicket = new model.entity.Ticket();
                mockTicket.setTicketID("TICKET-004");
                mockTicket.setUser(employee); // sold by employee
                mockTicket.setCustomer(mockCustomer);
                mockTicket.setSchedule(mockSchedule);
                mockTicket.setSeat(mockSeat); // set the mock seat
                mockTicket.setDiscount("25%");
                mockTicket.setPrice(1500000.0);
                mockTicket.setFinalPrice(1125000.0); // 1.125M VND (Child ticket)
                mockTicket.setCreateAt(LocalDateTime.now());
                mockTicket.setTicketStatus(model.entity.enums.TicketStatus.PENDING);
                em.persist(mockTicket);

                model.entity.Ticket mockTicket2 = new model.entity.Ticket();
                mockTicket2.setTicketID("TICKET-005");
                mockTicket2.setUser(employee); // sold by employee
                mockTicket2.setCustomer(mockCustomer); // same customer
                mockTicket2.setSchedule(mockSchedule);
                mockTicket2.setSeat(mockSeat2); // another seat
                mockTicket2.setDiscount("0%");
                mockTicket2.setPrice(1500000.0);
                mockTicket2.setFinalPrice(1500000.0); // 1.5M VND (Adult representation)
                mockTicket2.setCreateAt(LocalDateTime.now());
                mockTicket2.setTicketStatus(model.entity.enums.TicketStatus.PENDING);
                em.persist(mockTicket2);

                // Generate QR URL just to test logging it
                service.VietQRService qrService = new service.VietQRService();
                double totalAmount = mockTicket.getFinalPrice() + mockTicket2.getFinalPrice();
                String ticketIds = mockTicket.getTicketID() + ", " + mockTicket2.getTicketID();
                String qrUrl = qrService.generateQRCodeUrl(totalAmount, "Thanh toan ve tau " + ticketIds);
                System.out.println("Generated Mock Multi-Ticket QR Code URL (Sold by employee): " + qrUrl);
                System.out.println("Total Amount to Pay: " + (long)totalAmount + " VND");
            }

            tx.commit();
            System.out.println("Data seeded successfully!");

        } catch (Exception e) {
            if (tx.isActive()) {
                tx.rollback();
            }
            System.err.println("Error seeding data: " + e.getMessage());
            e.printStackTrace();
        } finally {
            em.close();
        }
    }
}
