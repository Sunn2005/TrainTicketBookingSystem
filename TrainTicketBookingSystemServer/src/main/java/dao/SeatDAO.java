package dao;

import model.entity.Seat;
import java.util.List;

public class SeatDAO extends BaseDAO<Seat, String> {
    public SeatDAO() {
        super(Seat.class);
    }

    public List<Seat> findByTrainId(String trainId) {
        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT s FROM Seat s WHERE s.carriage.train.trainID = :trainId ORDER BY s.carriage.carriageNumber, s.seatNumber", Seat.class)
                    .setParameter("trainId", trainId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
