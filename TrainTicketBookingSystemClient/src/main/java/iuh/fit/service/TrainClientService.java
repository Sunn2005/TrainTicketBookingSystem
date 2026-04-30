package iuh.fit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import dto.ActionResponse;
import dto.CreateTrainRequest;
import dto.UpdateScheduleRequest;
import dto.UpdateTrainRequest;
import iuh.fit.socketconfig.SocketClient;
import model.entity.Train;

import java.util.List;
import java.util.Map;

public class TrainClientService {

    private final SocketClient socketClient = new SocketClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public List<Map<String, Object>> getAllTrains() {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT, "GET_ALL_TRAINS");
            System.out.println("JSON từ Server: " + response); // Dòng này cực kỳ quan trọng để debug
            if (response == null || response.startsWith("ERROR")) return List.of();
            return objectMapper.readValue(response,
                    new TypeReference<List<Map<String, Object>>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public Map<String, Object> getTrainById(String trainId) {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "GET_TRAIN_BY_ID|" + trainId);
            if (response == null || response.startsWith("ERROR")) return null;
            return objectMapper.readValue(response,
                    new TypeReference<Map<String, Object>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public Train createTrain(CreateTrainRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "CREATE_TRAIN|" + json);
            if (response == null || response.startsWith("ERROR")) {
                System.err.println("Lỗi tạo tàu: " + response);
                return null;
            }
            return objectMapper.readValue(response, Train.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public ActionResponse updateTrain(UpdateTrainRequest request) {
        try {
            String json = objectMapper.writeValueAsString(request);
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "UPDATE_TRAIN|" + json); // Lệnh phải khớp với Case ở SocketServer

            if (response == null || response.startsWith("ERROR"))
                return dto.ActionResponse.fail(response != null ? response : "Lỗi kết nối");

            return objectMapper.readValue(response, dto.ActionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return dto.ActionResponse.fail("Lỗi: " + e.getMessage());
        }
    }

    public ActionResponse deleteTrain(String trainId) {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "DELETE_TRAIN|" + trainId);
            if (response == null || response.startsWith("ERROR"))
                return ActionResponse.fail(response != null ? response : "Lỗi kết nối");
            return objectMapper.readValue(response, ActionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResponse.fail("Lỗi kết nối: " + e.getMessage());
        }
    }
}