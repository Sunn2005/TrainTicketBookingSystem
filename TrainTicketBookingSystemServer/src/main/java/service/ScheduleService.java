package service;

import dao.ScheduleDAO;
import dto.CreateScheduleRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.entity.Route;
import model.entity.Schedule;
import model.entity.Train;
import util.JPAUtil;
import model.entity.User;

import java.time.LocalDate;
import java.util.List;
import java.util.UUID;

public class ScheduleService {

    public ScheduleService() {
    }

    private final ScheduleDAO scheduleDAO = new ScheduleDAO();

    // Thêm method này
    public List<Schedule> getAllSchedules() {
        return scheduleDAO.findAll();
    }

    // Thêm method này
    public List<Schedule> searchSchedules(String departureStationId, String arrivalStationId, LocalDate travelDate) {
        return scheduleDAO.searchSchedules(departureStationId, arrivalStationId, travelDate);
    }

    public void createSchedule(CreateScheduleRequest request) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User user = em.find(User.class, request.getManagerID());
            if (user == null || user.getRole() == null || !"MANAGER".equalsIgnoreCase(user.getRole().getRoleName())) {
                throw new RuntimeException("Only managers can create schedules");
            }

            Train train = em.find(Train.class, request.getTrainID());
            if (train == null) {
                throw new RuntimeException("Train not found");
            }

            Route route = em.find(Route.class, request.getRouteID());
            if (route == null) {
                throw new RuntimeException("Route not found");
            }

            // Trong hàm createSchedule
            Schedule schedule = new Schedule();
            schedule.setScheduleID(generateNextScheduleID(em)); // Sử dụng hàm tự sinh mã
            schedule.setTrain(train);
            schedule.setRoute(route);
            schedule.setDepartureTime(request.getDepartureTime());
            schedule.setArrivalTime(request.getArrivalTime());
            schedule.setScheduleStatus(model.entity.enums.ScheduleStatus.ENABLED);

            em.persist(schedule);

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error creating schedule", e);
        } finally {
            em.close();
        }
    }

    public void updateSchedule(dto.UpdateScheduleRequest request) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            User user = em.find(User.class, request.getManagerID());
            if (user == null || user.getRole() == null || !"MANAGER".equalsIgnoreCase(user.getRole().getRoleName())) {
                throw new RuntimeException("Only managers can update schedules");
            }

            Schedule schedule = em.find(Schedule.class, request.getScheduleID());
            if (schedule == null) {
                throw new RuntimeException("Schedule not found");
            }

            if (request.getTrainID() != null) {
                Train train = em.find(Train.class, request.getTrainID());
                if (train == null) {
                    throw new RuntimeException("Train not found");
                }
                schedule.setTrain(train);
            }

            if (request.getRouteID() != null) {
                Route route = em.find(Route.class, request.getRouteID());
                if (route == null) {
                    throw new RuntimeException("Route not found");
                }
                schedule.setRoute(route);
            }

            if (request.getDepartureTime() != null) {
                schedule.setDepartureTime(request.getDepartureTime());
            }

            if (request.getArrivalTime() != null) {
                schedule.setArrivalTime(request.getArrivalTime());
            }

            em.merge(schedule);

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error updating schedule", e);
        } finally {
            em.close();
        }
    }

    public void deleteSchedule(String scheduleID) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Schedule schedule = em.find(Schedule.class, scheduleID);
            if (schedule == null) {
                throw new RuntimeException("Schedule not found");
            }

            schedule.setScheduleStatus(model.entity.enums.ScheduleStatus.DISABLED);
            em.merge(schedule);

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error deleting schedule", e);
        } finally {
            em.close();
        }
    }
    public String findRouteIdByStations(String depStationId, String arrStationId) {
        return scheduleDAO.findRouteIdByStations(depStationId, arrStationId);
    }
    private String generateNextScheduleID(EntityManager em) {
        try {
            // Lấy mã SCH lớn nhất hiện có
            String jpql = "SELECT s.scheduleID FROM Schedule s WHERE s.scheduleID LIKE 'SCH-%' ORDER BY s.scheduleID DESC";
            List<String> results = em.createQuery(jpql, String.class)
                    .setMaxResults(1)
                    .getResultList();

            if (results.isEmpty()) {
                return "SCH-001";
            }

            String lastID = results.get(0); // Ví dụ: "SCH-005"
            int lastNumber = Integer.parseInt(lastID.split("-")[1]);
            return String.format("SCH-%03d", lastNumber + 1);
        } catch (Exception e) {
            return "SCH-" + java.util.UUID.randomUUID().toString().substring(0, 5);
        }
    }
}
