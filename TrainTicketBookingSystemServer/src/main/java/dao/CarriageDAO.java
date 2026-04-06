package dao;

import model.entity.Carriage;

import java.util.List;

public class CarriageDAO extends BaseDAO<Carriage, String> {
    public CarriageDAO() {
        super(Carriage.class);
    }

    public List<Carriage> findByTrainId(String trainId) {
        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT c FROM Carriage c WHERE c.train.trainID = :trainId ORDER BY c.carriageNumber", Carriage.class)
                    .setParameter("trainId", trainId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}

