package app;

import jakarta.persistence.EntityManager;
import util.JPAUtil;

public class TestConnectDatabase {
    public static void main(String[] args) {
        EntityManager em = JPAUtil.getEntityManager();
    }
}
