package iuh.fit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.ActionResponse;
import dto.CreateScheduleRequest;
import dto.UpdateScheduleRequest;
import iuh.fit.socketconfig.SocketClient;
import model.entity.Schedule;

import java.util.List;

public class ScheduleClientService {

    private final SocketClient socketClient = new SocketClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<Schedule> getAllSchedules() {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT, "GET_ALL_SCHEDULES");
            if (response == null || response.startsWith("ERROR")) {
                System.err.println("Lỗi lấy lịch trình: " + response);
                return List.of();
            }
            return objectMapper.readValue(response,
                    new TypeReference<List<Schedule>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public void createSchedule(CreateScheduleRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "CREATE_SCHEDULE|" + json);
            if (response == null || response.startsWith("ERROR"))
                throw new RuntimeException("Lỗi tạo lịch trình: " + response);
            ActionResponse resp = objectMapper.readValue(response, ActionResponse.class);
            if (!resp.isSuccess())
                throw new RuntimeException(resp.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi kết nối: " + e.getMessage(), e);
        }
    }

    public void updateSchedule(UpdateScheduleRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "UPDATE_SCHEDULE|" + json);
            if (response == null || response.startsWith("ERROR"))
                throw new RuntimeException("Lỗi cập nhật lịch trình: " + response);
            ActionResponse resp = objectMapper.readValue(response, ActionResponse.class);
            if (!resp.isSuccess())
                throw new RuntimeException(resp.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi kết nối: " + e.getMessage(), e);
        }
    }

    public void deleteSchedule(String scheduleId) {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "DELETE_SCHEDULE|" + scheduleId);
            if (response == null || response.startsWith("ERROR"))
                throw new RuntimeException("Lỗi xóa lịch trình: " + response);
            ActionResponse resp = objectMapper.readValue(response, ActionResponse.class);
            if (!resp.isSuccess())
                throw new RuntimeException(resp.getMessage());
        } catch (RuntimeException e) {
            throw e;
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException("Lỗi kết nối: " + e.getMessage(), e);
        }
    }

    public String findRouteIdByStations(String depStationId, String arrStationId) {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "GET_ROUTE_ID|" + depStationId + "|" + arrStationId);
            if (response == null || response.startsWith("ERROR")) return null;
            return response;
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }
}