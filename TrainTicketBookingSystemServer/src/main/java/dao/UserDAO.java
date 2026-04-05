package dao;

import entity.User;

public class UserDAO extends BaseDAO<User, String> {
    public UserDAO() {
        super(User.class);
    }
}
