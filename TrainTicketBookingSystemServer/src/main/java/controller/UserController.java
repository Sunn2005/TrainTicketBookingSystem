package controller;

import model.entity.enums.UserStatus;
import service.UserService;

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
}

