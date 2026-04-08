package util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.entity.Role;
import model.entity.Route;
import model.entity.Schedule;
import model.entity.Station;
import model.entity.Train;
import model.entity.User;
import model.entity.enums.UserStatus;

import java.time.LocalDateTime;
import java.util.HashMap;
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

            // 1. Roles
            Role adminRole = new Role("ADMIN", "Administrator");
            Role managerRole = new Role("MANAGER", "Manager");
            Role sellerRole = new Role("EMPLOYEE", "Ticket Seller");
            em.persist(adminRole);
            em.persist(managerRole);
            em.persist(sellerRole);

            // 2. Users
            User admin = new User("ADMIN_01", "admin", "admin123", "System Admin", "SYSTEM", LocalDateTime.now(), adminRole, UserStatus.ACTIVE);
            User manager = new User("MANAGER_01", "admin", "admin123", "System Admin", "ADMIN_01", LocalDateTime.now(), managerRole, UserStatus.ACTIVE);
            User seller = new User("EMPLOYEE_01", "seller", "seller123", "Ticket Seller 1", "ADMIN_01", LocalDateTime.now(), sellerRole, UserStatus.ACTIVE);
            em.persist(admin);
            em.persist(manager);
            em.persist(seller);

            // 3. Stations
            Station sgo = new Station("SGO", "Sài Gòn", "TP. Hồ Chí Minh");
            Station ntr = new Station("NTR", "Nha Trang", "Khánh Hòa");
            Station dan = new Station("DAN", "Đà Nẵng", "Đà Nẵng");
            Station hno = new Station("HNO", "Hà Nội", "Hà Nội");
            em.persist(sgo);
            em.persist(ntr);
            em.persist(dan);
            em.persist(hno);

            for (int i = 5; i <= 50; i++) {
                Station st = new Station("ST" + i, "Station " + i, "City " + i);
                em.persist(st);
            }

            // 4. Routes
            Route r1 = new Route("ROUTE_SGO_NTR", sgo, ntr, 411.0);
            Route r2 = new Route("ROUTE_NTR_DAN", ntr, dan, 524.0);
            Route r3 = new Route("ROUTE_DAN_HNO", dan, hno, 791.0);
            Route r4 = new Route("ROUTE_SGO_HNO", sgo, hno, 1726.0);
            em.persist(r1);
            em.persist(r2);
            em.persist(r3);
            em.persist(r4);

            for (int i = 5; i <= 50; i++) {
                Route r = new Route("ROUTE_" + i, sgo, hno, 100.0 + (i * 10));
                em.persist(r);
            }

            tx.commit();
            System.out.println("Basic data seeded successfully.");
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }

        // 5. Trains using TrainService
        System.out.println("Seeding Trains using TrainService...");
        try {
            TrainService trainService = new TrainService();
            // Train 1
            Map<Integer, String> t1Detail = new HashMap<>();
            t1Detail.put(1, "10-SOFT_SEAT");
            t1Detail.put(2, "20-SOFT_SLEEPER");
            trainService.createTrain(new CreateTrainRequest(t1Detail, "SE1"));

            // Train 2
            Map<Integer, String> t2Detail = new HashMap<>();
            t2Detail.put(1, "20-SOFT_SEAT");
            trainService.createTrain(new CreateTrainRequest(t2Detail, "SE2"));

            for (int i = 3; i <= 50; i++) {
                Map<Integer, String> tDetail = new HashMap<>();
                tDetail.put(1, "30-SOFT_SEAT");
                tDetail.put(2, "40-SOFT_SLEEPER");
                trainService.createTrain(new CreateTrainRequest(tDetail, "TRN" + i));
            }

            System.out.println("Trains seeded successfully.");
        } catch (Exception e) {
            e.printStackTrace();
        }

        // 6. Schedules
        System.out.println("Seeding Schedules...");
        em = JPAUtil.getEntityManager();
        tx = em.getTransaction();
        try {
            tx.begin();

            // Get data back
            Train se1 = em.createQuery("SELECT t FROM Train t WHERE t.trainName = 'SE1'", Train.class).setMaxResults(1).getSingleResult();
            Train se2 = em.createQuery("SELECT t FROM Train t WHERE t.trainName = 'SE2'", Train.class).setMaxResults(1).getSingleResult();
            Route routeSgoHno = em.find(Route.class, "ROUTE_SGO_HNO");
            Route routeSgoNtr = em.find(Route.class, "ROUTE_SGO_NTR");

            // Tomorrow schedules
            LocalDateTime tomorrow1930 = LocalDateTime.now().plusDays(1).withHour(19).withMinute(30).withSecond(0).withNano(0);
            LocalDateTime arrTomorrow1930 = tomorrow1930.plusHours(33); // approx time to HN

            Schedule sch1 = new Schedule("SCH_1", se1, routeSgoHno, tomorrow1930, arrTomorrow1930);
            Schedule sch2 = new Schedule("SCH_2", se2, routeSgoNtr, tomorrow1930, tomorrow1930.plusHours(8));

            em.persist(sch1);
            em.persist(sch2);

            for (int i = 3; i <= 50; i++) {
                Route r = em.find(Route.class, "ROUTE_" + i);
                if (r == null) r = routeSgoHno;
                Train t = em.createQuery("SELECT t FROM Train t WHERE t.trainName = 'TRN" + i + "'", Train.class).setMaxResults(1).getResultStream().findFirst().orElse(se1);
                Schedule sch = new Schedule("SCH_" + i, t, r, tomorrow1930.plusHours(i), tomorrow1930.plusHours(i + 10));
                em.persist(sch);
            }

            tx.commit();
            System.out.println("Schedules seeded successfully.");
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            e.printStackTrace();
        } finally {
            em.close();
        }

        System.out.println("Data Seeder completed!");
    }
}
