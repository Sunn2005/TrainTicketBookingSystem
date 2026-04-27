package dao;

import model.entity.Customer;
import util.JPAUtil;
import jakarta.persistence.EntityManager;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
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

    public List<Customer> findCustomersBookedBetween(LocalDate from, LocalDate to) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            LocalDateTime fromDate = from.atStartOfDay();
            LocalDateTime toDate =
                    to.atTime(LocalTime.MAX);

            String jpql = """
            SELECT DISTINCT t.customer
            FROM Ticket t
            WHERE t.createAt BETWEEN :from AND :to
        """;

            return em.createQuery(jpql, Customer.class)
                    .setParameter("from", fromDate)
                    .setParameter("to", toDate)
                    .getResultList();
        } finally {
            em.close();
        }
    }

}
