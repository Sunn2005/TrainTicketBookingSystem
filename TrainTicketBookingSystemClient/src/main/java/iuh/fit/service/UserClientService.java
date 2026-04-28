package iuh.fit.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import controller.UserController;
import dto.*;
import iuh.fit.socketconfig.SocketClient;
import model.entity.User;
import model.entity.enums.UserStatus;

import java.util.List;

public class UserClientService {
    private final UserController delegate;
    private final SocketClient socketClient = new SocketClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS)
            .configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);

    public UserClientService() {
        this.delegate = new UserController();
    }

    public UserClientService(UserController delegate) {
        this.delegate = delegate;
    }

    public LoginResponse login(String userName, String password) {
        return delegate.login(userName, password);
    }

    public ActionResponse createUser(String username, String password,
                                     String fullName, String email, String roleId) {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "CREATE_USER|" + username + "|" + password
                            + "|" + fullName + "|" + email + "|" + roleId);
            if (response == null || response.startsWith("ERROR"))
                return ActionResponse.fail(response != null ? response : "Lỗi kết nối");
            return objectMapper.readValue(response, ActionResponse.class);
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResponse.fail("Lỗi kết nối: " + e.getMessage());
        }
    }
    public List<model.entity.User> getAllUsers() {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT, "GET_ALL_USERS");
            if (response == null || response.startsWith("ERROR")) return List.of();
            return objectMapper.readValue(response,
                    new TypeReference<List<User>>() {});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
    public ActionResponse changePassword(String username, String oldPassword, String newPassword) {
        return delegate.changePassword(username, oldPassword, newPassword);
    }

    public ActionResponse changeStatus(String username, UserStatus userStatus) {
        return delegate.changeStatus(username, userStatus);
    }

    public ActionResponse changeFullName(String username, String newFullName) {
        return delegate.changeFullName(username, newFullName);
    }

    public ActionResponse changeUserRole(String adminUsername, String targetUsername, String newRoleName) {
        return delegate.changeUserRole(adminUsername, targetUsername, newRoleName);
    }

    public List<TransactionDTO> getTransactionHistory(String username) {
        return delegate.getTransactionHistory(username);
    }

    public RevenueStatisticsResponse revenueStatistics(RevenueStatisticsRequest request) {
        return delegate.revenueStatistics(request);
    }

    public SeatTypeRevenueResponse seatTypeRevenue(SeatTypeRevenueRequest request) {
        return delegate.seatTypeRevenue(request);
    }

    public ScheduleStatisticsResponse scheduleStatistics(ScheduleStatisticsRequest request) {
        return delegate.scheduleStatistics(request);
    }

    public ActionResponse requestPasswordReset(PasswordResetRequestDTO request) {
        return delegate.requestPasswordReset(request);
    }

    public List<PasswordResetRequestDTO> getPendingPasswordResets() {
        return delegate.getPendingPasswordResets();
    }

    public ActionResponse resetPassword(String targetUserId, String newPassword) {
        try {
            String response = socketClient.sendMessage(
                    SocketClient.HOST, SocketClient.PORT,
                    "RESET_PASSWORD|" + targetUserId + "|" + newPassword);

            if (response == null) {
                return ActionResponse.fail("Lỗi kết nối");
            }
            if (response.startsWith("SUCCESS|")) {
                return ActionResponse.success(response.substring("SUCCESS|".length()));
            }
            if (response.startsWith("ERROR|")) {
                return ActionResponse.fail(response.substring("ERROR|".length()));
            }
            if (response.startsWith("ERROR")) {
                return ActionResponse.fail(response);
            }
            return ActionResponse.fail("Phản hồi không hợp lệ từ server");
        } catch (Exception e) {
            e.printStackTrace();
            return ActionResponse.fail("Lỗi kết nối: " + e.getMessage());
        }
    }
}