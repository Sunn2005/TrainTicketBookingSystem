package dao;

import entity.Ticket;

public class TicketDAO extends BaseDAO<Ticket, String> {
    public TicketDAO() {
        super(Ticket.class);
    }
}
