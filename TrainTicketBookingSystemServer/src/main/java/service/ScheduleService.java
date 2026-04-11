package service;

import dto.CreateScheduleRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.entity.Route;
import model.entity.Schedule;
import model.entity.Train;
import util.JPAUtil;
import model.entity.User;

import java.util.UUID;

public class ScheduleService {

    public ScheduleService() {
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

            Schedule schedule = new Schedule();
            schedule.setScheduleID("SCH_" + UUID.randomUUID().toString().substring(0, 8));
            schedule.setTrain(train);
            schedule.setRoute(route);
            schedule.setDepartureTime(request.getDepartureTime());
            schedule.setArrivalTime(request.getArrivalTime());

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

            em.remove(schedule);

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
}
