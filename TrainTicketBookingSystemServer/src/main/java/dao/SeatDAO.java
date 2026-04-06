package dao;

import model.entity.Seat;

public class SeatDAO extends BaseDAO<Seat, String> {
    public SeatDAO() {
        super(Seat.class);
    }
}
