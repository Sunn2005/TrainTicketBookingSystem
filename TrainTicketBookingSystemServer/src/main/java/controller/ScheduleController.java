package controller;

import dto.CreateScheduleRequest;
import dto.UpdateScheduleRequest;
import model.entity.Schedule;
import service.ScheduleService;

import java.time.LocalDate;
import java.util.List;

public class ScheduleController {
    private final ScheduleService scheduleService;

    public ScheduleController() {
        this.scheduleService = new ScheduleService();
    }

    public void createSchedule(CreateScheduleRequest request) {
        scheduleService.createSchedule(request);
    }

    public void updateSchedule(UpdateScheduleRequest request) {
        scheduleService.updateSchedule(request);
    }

    public void deleteSchedule(String scheduleID) {
        scheduleService.deleteSchedule(scheduleID);
    }

    // Thêm method này
    public List<Schedule> getAllSchedules() {
        return scheduleService.getAllSchedules();
    }

    // Thêm method này
    public List<Schedule> searchSchedules(String departureStationId, String arrivalStationId, LocalDate travelDate) {
        return scheduleService.searchSchedules(departureStationId, arrivalStationId, travelDate);
    }

    public String findRouteIdByStations(String depStationId, String arrStationId) {
        return scheduleService.findRouteIdByStations(depStationId, arrStationId);
    }
}
