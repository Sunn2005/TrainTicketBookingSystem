package dao;

import model.entity.Schedule;

public class ScheduleDAO extends BaseDAO<Schedule, String> {
    public ScheduleDAO() {
        super(Schedule.class);
    }
}
