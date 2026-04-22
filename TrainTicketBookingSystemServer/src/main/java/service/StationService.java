package service;

import dao.StationDAO;
import model.entity.Station;
import java.util.List;

public class StationService {
    private final StationDAO stationDAO;

    public StationService() {
        this.stationDAO = new StationDAO();
    }

    // Lấy toàn bộ danh sách các ga
    public List<Station> getAllStations() {
        return stationDAO.findAll();
    }
}