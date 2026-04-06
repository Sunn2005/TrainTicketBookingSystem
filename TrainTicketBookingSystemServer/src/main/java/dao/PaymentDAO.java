package dao;

import model.entity.Payment;
import java.util.List;

public class PaymentDAO extends BaseDAO<Payment, String> {
    public PaymentDAO() {
        super(Payment.class);
    }

    public List<Payment> findPaymentsByTicketId(String ticketId) {
        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            return em.createQuery("SELECT p FROM Payment p WHERE p.ticket.ticketID = :ticketId", Payment.class)
                    .setParameter("ticketId", ticketId)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
