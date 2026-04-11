package dao;

import model.entity.Customer;
import util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.util.List;

public class CustomerDAO extends BaseDAO<Customer, String> {
    public CustomerDAO() {
        super(Customer.class);
    }

    public List<Customer> searchCustomers(String keyword) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            String jpql = "SELECT c FROM Customer c WHERE c.customerID = :keyword OR LOWER(c.fullName) LIKE LOWER(:likeKeyword)";
            return em.createQuery(jpql, Customer.class)
                    .setParameter("keyword", keyword)
                    .setParameter("likeKeyword", "%" + keyword + "%")
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
