package controller;

import model.entity.Station;
import service.StationService;
import java.util.List;

public class  StationController {
    private final StationService stationService;

    public StationController() {
        this.stationService = new StationService();
    }

    /**
     * Lấy danh sách dùng chung cho cả Ga đi (Departure) và Ga đến (Arrival)
     * @return Danh sách tất cả các ga hiện có trong hệ thống
     */
    public List<Station> getAllStations() {
        return stationService.getAllStations();
    }
}