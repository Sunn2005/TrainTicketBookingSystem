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

import java.util.List;
import java.util.Map;
import java.util.UUID;

public class TrainService {

    public TrainService() {
    }
    private final TrainDAO trainDAO = new TrainDAO();


    public List<Train> getAllTrains() {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Query 1: Load trains + carriages
            List<Train> trains = em.createQuery(
                            "SELECT DISTINCT t FROM Train t LEFT JOIN FETCH t.carriages",
                            Train.class)
                    .getResultList();

            // Query 2: Load seats cho từng carriage
            for (Train train : trains) {
                if (train.getCarriages() != null) {
                    for (var carriage : train.getCarriages()) {
                        // Ép load seats trong cùng session
                        em.createQuery(
                                        "SELECT c FROM Carriage c LEFT JOIN FETCH c.seats WHERE c.carriageID = :id",
                                        model.entity.Carriage.class)
                                .setParameter("id", carriage.getCarriageID())
                                .getResultList();
                    }
                }
            }
            return trains;
        } finally {
            em.close();
        }
    }

    public Train getTrainById(String trainId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            // Query 1: Load train + carriages
            Train train = em.createQuery(
                            "SELECT DISTINCT t FROM Train t LEFT JOIN FETCH t.carriages WHERE t.trainID = :id",
                            Train.class)
                    .setParameter("id", trainId)
                    .getResultStream()
                    .findFirst()
                    .orElse(null);

            if (train == null) return null;

            // Query 2: Load seats cho từng carriage
            if (train.getCarriages() != null) {
                for (var carriage : train.getCarriages()) {
                    em.createQuery(
                                    "SELECT c FROM Carriage c LEFT JOIN FETCH c.seats WHERE c.carriageID = :id",
                                    model.entity.Carriage.class)
                            .setParameter("id", carriage.getCarriageID())
                            .getResultList();
                }
            }
            return train;
        } finally {
            em.close();
        }
    }

    public Train createTrain(CreateTrainRequest request) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            Train train = new Train();
            // THAY THẾ UUID BẰNG HÀM TỰ SINH MÃ:
            train.setTrainID(generateNextTrainID(em));
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
            return train;
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

            // 1. Tìm thực thể tàu hiện tại
            Train train = em.find(Train.class, request.getTrainID());
            if (train == null) throw new RuntimeException("Train not found: " + request.getTrainID());

            // 2. Cập nhật thông tin cơ bản
            if (request.getTrainName() != null) {
                train.setTrainName(request.getTrainName());
            }

            // 3. Xóa thủ công Seats và Carriages bằng Query
            // Bước này xóa trực tiếp trong DB nhưng Hibernate Cache vẫn giữ bản sao cũ
            em.createQuery("DELETE FROM Seat s WHERE s.carriage.train.trainID = :tid")
                    .setParameter("tid", train.getTrainID()).executeUpdate();
            em.createQuery("DELETE FROM Carriage c WHERE c.train.trainID = :tid")
                    .setParameter("tid", train.getTrainID()).executeUpdate();

            // 4. QUAN TRỌNG: Xóa sạch bộ nhớ đệm (Persistence Context)
            // Điều này bắt Hibernate phải quên đi các đối tượng Seat/Carriage cũ để không cố gắng xóa chúng lần nữa khi commit
            em.flush();
            em.clear();

            // 5. Nạp lại thực thể Train vào Context mới sau khi đã clear()
            train = em.find(Train.class, request.getTrainID());

            // 6. Thêm lại chi tiết toa và ghế mới
            if (request.getDetail() != null) {
                for (Map.Entry<Integer, String> entry : request.getDetail().entrySet()) {
                    String val = entry.getValue();
                    if (val == null || !val.contains("-")) continue;

                    String[] parts = val.split("-");
                    int numSeats = Integer.parseInt(parts[0]);
                    model.entity.enums.SeatType seatType = model.entity.enums.SeatType.valueOf(parts[1]);

                    Carriage carriage = new Carriage();
                    carriage.setCarriageID("CAR_" + java.util.UUID.randomUUID().toString().substring(0, 8));
                    carriage.setTrain(train);
                    carriage.setCarriageNumber(entry.getKey());
                    em.persist(carriage);

                    for (int i = 1; i <= numSeats; i++) {
                        Seat seat = new Seat();
                        seat.setSeatID("SEAT_" + java.util.UUID.randomUUID().toString().substring(0, 8));
                        seat.setCarriage(carriage);
                        seat.setSeatNumber(i);
                        seat.setSeatType(seatType);
                        em.persist(seat);
                    }
                }
            }

            tx.commit(); // Sẽ không còn lỗi Unexpected row count
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw e;
        } finally {
            em.close();
        }
    }

    public void deleteTrain(String trainID) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();

            // 1. Xóa tất cả Ghế của các toa thuộc tàu này
            em.createQuery("DELETE FROM Seat s WHERE s.carriage.train.trainID = :tid")
                    .setParameter("tid", trainID)
                    .executeUpdate();

            // 2. Xóa tất cả Toa của tàu này
            em.createQuery("DELETE FROM Carriage c WHERE c.train.trainID = :tid")
                    .setParameter("tid", trainID)
                    .executeUpdate();

            // 3. Cuối cùng mới xóa Tàu
            Train train = em.find(Train.class, trainID);
            if (train != null) {
                em.remove(train);
            }

            tx.commit();
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            throw new RuntimeException("Không thể xóa tàu: Tàu này có thể đã được gắn vào Lịch trình/Vé", e);
        } finally {
            em.close();
        }
    }

    private String generateNextTrainID(EntityManager em) {
        // Tìm mã tàu có định dạng TRA-XXX lớn nhất
        String lastID = em.createQuery(
                        "SELECT t.trainID FROM Train t WHERE t.trainID LIKE 'TRA-%' ORDER BY t.trainID DESC", String.class)
                .setMaxResults(1)
                .getResultStream()
                .findFirst()
                .orElse("TRA-000"); // Nếu chưa có tàu nào thì bắt đầu từ 000

        // Tách phần số: "TRA-005" -> 5
        int nextNumber = Integer.parseInt(lastID.substring(4)) + 1;

        // Trả về định dạng mới: 6 -> "TRA-006"
        return String.format("TRA-%03d", nextNumber);
    }
}
