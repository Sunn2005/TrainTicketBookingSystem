package dao;

import model.entity.Schedule;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

public class ScheduleDAO extends BaseDAO<Schedule, String> {
    public ScheduleDAO() {
        super(Schedule.class);
    }

    public List<Schedule> searchSchedules(String departureStationId, String arrivalStationId, LocalDate travelDate) {
        LocalDateTime from = travelDate.atStartOfDay();
        LocalDateTime to = travelDate.plusDays(1).atStartOfDay();

        jakarta.persistence.EntityManager em = util.JPAUtil.getEntityManager();
        try {
            return em.createQuery(
                            "SELECT s FROM Schedule s "
                                    + "WHERE s.route.departureStation.stationID = :departureId "
                                    + "AND s.route.arrivalStation.stationID = :arrivalId "
                                    + "AND s.departureTime >= :fromTime "
                                    + "AND s.departureTime < :toTime "
                                    + "AND s.scheduleStatus = model.entity.enums.ScheduleStatus.ENABLED "
                                    + "ORDER BY s.departureTime", Schedule.class)
                    .setParameter("departureId", departureStationId)
                    .setParameter("arrivalId", arrivalStationId)
                    .setParameter("fromTime", from)
                    .setParameter("toTime", to)
                    .getResultList();
        } finally {
            em.close();
        }
    }
}
