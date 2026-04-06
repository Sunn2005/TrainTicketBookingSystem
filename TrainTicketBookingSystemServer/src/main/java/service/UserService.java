package service;

import model.entity.Role;
import model.entity.User;
import model.entity.enums.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import util.JPAUtil;

import java.util.UUID;
import dao.UserDAO;
import dao.RoleDAO;

public class UserService {

    public static class ActionResponse {
        private boolean success;
        private String message;

        public ActionResponse(boolean success, String message) {
            this.success = success;
            this.message = message;
        }

        public static ActionResponse success(String message) {
            return new ActionResponse(true, message);
        }

        public static ActionResponse fail(String message) {
            return new ActionResponse(false, message);
        }

        public boolean isSuccess() {
            return success;
        }

        public String getMessage() {
            return message;
        }
    }

    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();

    public UserService() {
    }

    public ActionResponse createUser(String userName, String password, String fullName, String roleId) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Check if username already exists
            if (userDAO.existsByUserName(userName)) {
                return ActionResponse.fail("Tên đăng nhập đã tồn tại.");
            }

            Role role = em.find(Role.class, roleId);
            if (role == null) {
                return ActionResponse.fail("Không tìm thấy vai trò (Role): " + roleId);
            }

            User user = new User();
            user.setUserID(generateId("USER"));
            user.setUserName(userName);
            user.setPassword(password); // in real system, should hash password
            user.setFullName(fullName);
            user.setRole(role);
            user.setUserStatus(UserStatus.ACTIVE);

            em.persist(user);
            tx.commit();
            return ActionResponse.success("Tạo người dùng thành công với ID: " + user.getUserID());
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return ActionResponse.fail("Lỗi khi tạo người dùng: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public ActionResponse changePassword(String userId, String oldPassword, String newPassword) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User user = em.find(User.class, userId);
            if (user == null) {
                return ActionResponse.fail("Không tìm thấy người dùng.");
            }
            if (!user.getPassword().equals(oldPassword)) {
                return ActionResponse.fail("Mật khẩu cũ không chính xác.");
            }
            user.setPassword(newPassword);
            em.merge(user);
            tx.commit();
            return ActionResponse.success("Đổi mật khẩu thành công.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return ActionResponse.fail("Lỗi khi đổi mật khẩu: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public ActionResponse changeStatus(String userId, UserStatus status) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User user = em.find(User.class, userId);
            if (user == null) {
                return ActionResponse.fail("Không tìm thấy người dùng.");
            }
            user.setUserStatus(status);
            em.merge(user);
            tx.commit();
            return ActionResponse.success("Đổi trạng thái thành công.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return ActionResponse.fail("Lỗi khi đổi trạng thái: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public ActionResponse changeFullName(String userId, String newFullName) {
        if (newFullName == null || newFullName.trim().isEmpty()) {
            return ActionResponse.fail("Tên đầy đủ không được để trống.");
        }
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User user = em.find(User.class, userId);
            if (user == null) {
                return ActionResponse.fail("Không tìm thấy người dùng.");
            }
            user.setFullName(newFullName);
            em.merge(user);
            tx.commit();
            return ActionResponse.success("Đổi tên thành công.");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return ActionResponse.fail("Lỗi khi đổi tên: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public ActionResponse changeUserRole(String adminId, String targetUserId, String newRoleName) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            // Validate admin
            User admin = em.find(User.class, adminId);
            if (admin == null || admin.getRole() == null || !"ADMIN".equalsIgnoreCase(admin.getRole().getRoleName())) {
                return ActionResponse.fail("Chỉ có ADMIN mới được quyền đổi chức vụ người dùng.");
            }

            User targetUser = em.find(User.class, targetUserId);
            if (targetUser == null) {
                return ActionResponse.fail("Không tìm thấy người dùng cần đổi quyền.");
            }

            model.entity.Role newRole = roleDAO.findByRoleName(newRoleName).orElse(null);
            if (newRole == null) {
                return ActionResponse.fail("Vai trò không hợp lệ: " + newRoleName);
            }

            targetUser.setRole(newRole);
            em.merge(targetUser);

            tx.commit();
            return ActionResponse.success("Thay đổi chức vụ thành công thành: " + newRoleName);
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return ActionResponse.fail("Lỗi khi thay đổi chức vụ: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    private String generateId(String prefix) {
        return prefix + "-" + UUID.randomUUID().toString().replace("-", "").substring(0, 12).toUpperCase();
    }
}
