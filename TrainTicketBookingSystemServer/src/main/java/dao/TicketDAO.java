package dao;
import model.entity.Ticket;
import model.entity.enums.TicketStatus;

import java.util.List;

public class TicketDAO extends BaseDAO<Ticket, String> {
    public TicketDAO() {
        super(Ticket.class);
    }

    public boolean isSeatBooked(String scheduleId, String seatId) {
        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            Long count = em.createQuery(
                            "SELECT COUNT(t) FROM Ticket t "
                                    + "WHERE t.schedule.scheduleID = :scheduleId "
                                    + "AND t.seat.seatID = :seatId "
                                    + "AND t.ticketStatus <> :cancelledStatus", Long.class)
                    .setParameter("scheduleId", scheduleId)
                    .setParameter("seatId", seatId)
                    .setParameter("cancelledStatus", TicketStatus.CANCELLED)
                    .getSingleResult();
            return count != null && count > 0;
        } finally {
            em.close();
        }
    }

    public List<String> getBookedSeatIds(String scheduleId) {
        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT t.seat.seatID FROM Ticket t "
                                    + "WHERE t.schedule.scheduleID = :scheduleId "
                                    + "AND t.ticketStatus <> :cancelledStatus", String.class)
                    .setParameter("scheduleId", scheduleId)
                    .setParameter("cancelledStatus", TicketStatus.CANCELLED)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
