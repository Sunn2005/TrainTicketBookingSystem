package service;

import dao.TrainDAO;
import dto.CreateTrainRequest;
import dto.UpdateTrainRequest;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import model.entity.Train;
import model.entity.Carriage;
import model.entity.Seat;
import model.entity.enums.SeatType;
import util.JPAUtil;

import java.util.Map;
import java.util.UUID;

public class TrainService {

    public TrainService() {
    }

    public void createTrain(CreateTrainRequest request) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Train train = new Train();
            train.setTrainID("SE" + UUID.randomUUID().toString().substring(0, 8));
            train.setTrainName(request.getTrainName());
            em.persist(train);

            if (request.getDetail() != null) {
                for (Map.Entry<Integer, String> entry : request.getDetail().entrySet()) {
                    Integer carriageNum = entry.getKey();
                    String val = entry.getValue(); // e.g. "20-SOFT_SEAT"
                    if (val == null || !val.contains("-")) continue;

                    String[] parts = val.split("-");
                    int numSeats = Integer.parseInt(parts[0]);
                    SeatType seatType = SeatType.valueOf(parts[1]);

                    Carriage carriage = new Carriage();
                    carriage.setCarriageID("CAR_" + UUID.randomUUID().toString().substring(0, 8));
                    carriage.setTrain(train);
                    carriage.setCarriageNumber(carriageNum);
                    em.persist(carriage);

                    for (int i = 1; i <= numSeats; i++) {
                        Seat seat = new Seat();
                        seat.setSeatID("SEAT_" + UUID.randomUUID().toString().substring(0, 8));
                        seat.setCarriage(carriage);
                        seat.setSeatNumber(i);
                        seat.setSeatType(seatType);
                        em.persist(seat);
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error creating train", e);
        } finally {
            em.close();
        }
    }

    public void updateTrain(UpdateTrainRequest request) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Train train = em.find(Train.class, request.getTrainID());
            if (train == null) {
                throw new RuntimeException("Train not found");
            }

            if (request.getTrainName() != null) {
                train.setTrainName(request.getTrainName());
            }

            if (request.getDetail() != null) {
                // Delete old carriages and seats
                em.createQuery("DELETE FROM Seat s WHERE s.carriage.train = :train").setParameter("train", train).executeUpdate();
                em.createQuery("DELETE FROM Carriage c WHERE c.train = :train").setParameter("train", train).executeUpdate();

                for (Map.Entry<Integer, String> entry : request.getDetail().entrySet()) {
                    Integer carriageNum = entry.getKey();
                    String val = entry.getValue();
                    if (val == null || !val.contains("-")) continue;

                    String[] parts = val.split("-");
                    int numSeats = Integer.parseInt(parts[0]);
                    SeatType seatType = SeatType.valueOf(parts[1]);

                    Carriage carriage = new Carriage();
                    carriage.setCarriageID("CAR_" + UUID.randomUUID().toString().substring(0, 8));
                    carriage.setTrain(train);
                    carriage.setCarriageNumber(carriageNum);
                    em.persist(carriage);

                    for (int i = 1; i <= numSeats; i++) {
                        Seat seat = new Seat();
                        seat.setSeatID("SEAT_" + UUID.randomUUID().toString().substring(0, 8));
                        seat.setCarriage(carriage);
                        seat.setSeatNumber(i);
                        seat.setSeatType(seatType);
                        em.persist(seat);
                    }
                }
            }

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error updating train", e);
        } finally {
            em.close();
        }
    }

    public void deleteTrain(String trainID) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Train train = em.find(Train.class, trainID);
            if (train == null) {
                throw new RuntimeException("Train not found");
            }

            // Delete associated carriages and seats
            em.createQuery("DELETE FROM Seat s WHERE s.carriage.train = :train").setParameter("train", train).executeUpdate();
            em.createQuery("DELETE FROM Carriage c WHERE c.train = :train").setParameter("train", train).executeUpdate();
            em.remove(train);

            tx.commit();
        } catch (Exception e) {
            if (tx != null && tx.isActive()) {
                tx.rollback();
            }
            throw new RuntimeException("Error deleting train", e);
        } finally {
            em.close();
        }
    }
}
