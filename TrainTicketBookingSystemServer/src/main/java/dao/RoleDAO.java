package dao;

import model.entity.Role;

public class RoleDAO extends BaseDAO<Role, String> {
    public RoleDAO() {
        super(Role.class);
    }
}
