package dao;

import model.entity.User;

import java.util.List;
import java.util.Optional;

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

    public Optional<User> findByUserName(String userName) {
        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT u FROM User u WHERE u.userName = :userName", User.class)
                    .setParameter("userName", userName)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }
    public List<User> findAllWithRole() {
        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            List<User> users = em.createQuery(
                            "SELECT u FROM User u LEFT JOIN FETCH u.role ORDER BY u.createDate DESC",
                            User.class)
                    .getResultList();
            return users;
        } finally {
            em.close();
        }
    }
}
