package iuh.fit.service;

import controller.UserController;
import dto.*;
import model.entity.enums.UserStatus;

import java.util.List;

public class UserClientService {
    private final UserController delegate;

    public UserClientService() {
        this.delegate = new UserController();
    }

    public UserClientService(UserController delegate) {
        this.delegate = delegate;
    }

    public LoginResponse login(String userName, String password) {
        return delegate.login(userName, password);
    }

    public ActionResponse createUser(String username, String password, String fullName, String roleName) {
        return delegate.createUser(username, password, fullName, roleName);
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
}