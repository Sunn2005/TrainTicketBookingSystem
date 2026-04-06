package dao;

import model.entity.User;

public class UserDAO extends BaseDAO<User, String> {
    public UserDAO() {
        super(User.class);
    }

    public boolean existsByUserName(String userName) {
        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            Long count = em.createQuery("SELECT COUNT(u) FROM User u WHERE u.userName = :userName", Long.class)
                    .setParameter("userName", userName)
                    .getSingleResult();
            return count > 0;
        } finally {
            em.close();
        }
    }
}
