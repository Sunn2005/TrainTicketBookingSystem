package iuh.fit.service;

import controller.ScheduleController;
import dto.CreateScheduleRequest;
import dto.UpdateScheduleRequest;

public class ScheduleClientService {
    private final ScheduleController delegate;

    public ScheduleClientService() {
        this.delegate = new ScheduleController();
    }

    public ScheduleClientService(ScheduleController delegate) {
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