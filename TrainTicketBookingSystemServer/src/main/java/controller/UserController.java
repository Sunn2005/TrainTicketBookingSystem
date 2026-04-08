package controller;

import model.entity.enums.UserStatus;
import service.UserService;
import dto.TransactionDTO;
import java.util.List;

public class UserController {

    private final UserService userService;

    public UserController() {
        this.userService = new UserService();
    }

    public UserController(UserService userService) {
        this.userService = userService;
    }

    /**
     * API Tạo mới User
     */
    public UserService.ActionResponse createUser(String userName, String password, String fullName, String roleId) {
        return userService.createUser(userName, password, fullName, roleId);
    }

    /**
     * API Đổi mật khẩu
     */
    public UserService.ActionResponse changePassword(String userId, String oldPassword, String newPassword) {
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    /**
     * API Đổi trạng thái (UserStatus)
     */
    public UserService.ActionResponse changeStatus(String userId, UserStatus status) {
        return userService.changeStatus(userId, status);
    }

    /**
     * API Thay đổi Tên đầy đủ
     */
    public UserService.ActionResponse changeFullName(String userId, String newFullName) {
        return userService.changeFullName(userId, newFullName);
    }

    /**
     * API Thay đổi chức vụ User (Chỉ ADMIN)
     */
    public UserService.ActionResponse changeUserRole(String adminId, String targetUserId, String newRoleName) {
        return userService.changeUserRole(adminId, targetUserId, newRoleName);
    }

    /**
     * API Lấy lịch sử giao dịch (Toàn bộ nếu là MANAGER, hoặc của chính User nếu là EMPLOYEE)
     */
    public List<TransactionDTO> getTransactionHistory(String userID) {
        return userService.getTransactionHistory(userID);
    }

    /**
     * API Thống kê doanh thu theo ngày, tháng, năm, quý (Dành cho MANAGER)
     */
    public dto.RevenueStatisticsResponse revenueStatistics(dto.RevenueStatisticsRequest request) {
        return userService.revenueStatistics(request);
    }
}
