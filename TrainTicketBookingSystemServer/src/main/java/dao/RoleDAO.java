package dao;

import model.entity.Role;
import java.util.Optional;

public class RoleDAO extends BaseDAO<Role, String> {
    public RoleDAO() {
        super(Role.class);
    }

    public Optional<Role> findByRoleName(String roleName) {
        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT r FROM Role r WHERE r.roleName = :roleName", Role.class)
                    .setParameter("roleName", roleName)
                    .getResultStream()
                    .findFirst();
        } finally {
            em.close();
        }
    }
}
