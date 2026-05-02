package iuh.fit.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.*;
import iuh.fit.socketconfig.SocketClient;

public class StatisticsClientService {

    private final SocketClient socketClient = new SocketClient();
    private final ObjectMapper mapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    // ================= DOANH THU =================
    public RevenueStatisticsResponse getRevenue(RevenueStatisticsRequest req) {
        try {
            String json = mapper.writeValueAsString(req);
            String res = socketClient.sendMessage(
                    SocketClient.HOST,
                    SocketClient.PORT,
                    "REVENUE_STATISTICS|" + json
            );

            return mapper.readValue(res, RevenueStatisticsResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================= LOẠI GHẾ =================
    public SeatTypeRevenueResponse getSeatTypeRevenue(SeatTypeRevenueRequest req) {
        try {
            String json = mapper.writeValueAsString(req);
            String res = socketClient.sendMessage(
                    SocketClient.HOST,
                    SocketClient.PORT,
                    "SEAT_TYPE_REVENUE|" + json
            );

            return mapper.readValue(res, SeatTypeRevenueResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    // ================= LỊCH TRÌNH =================
    public ScheduleStatisticsResponse getScheduleStats(ScheduleStatisticsRequest req) {
        try {
            String json = mapper.writeValueAsString(req);
            String res = socketClient.sendMessage(
                    SocketClient.HOST,
                    SocketClient.PORT,
                    "SCHEDULE_STATISTICS|" + json
            );

            return mapper.readValue(res, ScheduleStatisticsResponse.class);

        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}