package controller;

import dto.CreateScheduleRequest;
import dto.UpdateScheduleRequest;
import service.ScheduleService;

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
}
