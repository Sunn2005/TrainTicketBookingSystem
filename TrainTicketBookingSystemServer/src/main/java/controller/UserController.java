package controller;

import dto.*;
import model.entity.enums.UserStatus;
import service.UserService;

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
     * API Đăng nhập
     */
    public LoginResponse login(String userName, String password) {
        return userService.login(userName, password);
    }

    /**
     * API Tạo mới User
     */
    public ActionResponse createUser(String userName, String password, String fullName, String roleId) {
        return userService.createUser(userName, password, fullName, roleId);
    }

    /**
     * API Đổi mật khẩu
     */
    public ActionResponse changePassword(String userId, String oldPassword, String newPassword) {
        return userService.changePassword(userId, oldPassword, newPassword);
    }

    /**
     * API Đổi trạng thái (UserStatus)
     */
    public ActionResponse changeStatus(String userId, UserStatus status) {
        return userService.changeStatus(userId, status);
    }

    /**
     * API Thay đổi Tên đầy đủ
     */
    public ActionResponse changeFullName(String userId, String newFullName) {
        return userService.changeFullName(userId, newFullName);
    }

    /**
     * API Thay đổi chức vụ User (Chỉ ADMIN)
     */
    public ActionResponse changeUserRole(String adminId, String targetUserId, String newRoleName) {
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
    public RevenueStatisticsResponse revenueStatistics(RevenueStatisticsRequest request) {
        return userService.revenueStatistics(request);
    }

    /**
     * API Thống kê doanh thu theo loại vé (ghế mềm, giường mềm) (Dành cho MANAGER)
     */
    public SeatTypeRevenueResponse seatTypeRevenue(SeatTypeRevenueRequest request) {
        return userService.seatTypeRevenue(request);
    }

    /**
     * API Thống kê doanh thu theo lịch trình tàu (Dành cho MANAGER)
     */
    public ScheduleStatisticsResponse scheduleStatistics(ScheduleStatisticsRequest request) {
        return userService.scheduleStatistics(request);
    }

}
