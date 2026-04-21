package iuh.fit.service;

import controller.UserController;
import dao.UserDAO;
import dto.*;
import model.entity.User;
import model.entity.enums.UserStatus;

import java.util.Comparator;
import java.util.List;
import java.util.Locale;
import java.util.stream.Collectors;

public class UserClientService {
    private final UserController delegate;
    private final UserDAO userDAO;

    public UserClientService() {
        this.delegate = new UserController();
        this.userDAO = new UserDAO();
    }

    public UserClientService(UserController delegate) {
        this.delegate = delegate;
        this.userDAO = new UserDAO();
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