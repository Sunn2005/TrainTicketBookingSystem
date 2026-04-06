package dao;

import model.entity.Station;

public class StationDAO extends BaseDAO<Station, String> {
    public StationDAO() {
        super(Station.class);
    }
}
