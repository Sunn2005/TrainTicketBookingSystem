package iuh.fit.controller;

import controller.ScheduleController;
import dto.CreateScheduleRequest;
import dto.UpdateScheduleRequest;

public class ScheduleClientController {
    private final ScheduleController delegate;

    public ScheduleClientController() {
        this.delegate = new ScheduleController();
    }

    public ScheduleClientController(ScheduleController delegate) {
        this.delegate = delegate;
    }

    public void createSchedule(CreateScheduleRequest request) {
        delegate.createSchedule(request);
    }

    public void updateSchedule(UpdateScheduleRequest request) {
        delegate.updateSchedule(request);
    }

    public void deleteSchedule(String scheduleId) {
        delegate.deleteSchedule(scheduleId);
    }
}