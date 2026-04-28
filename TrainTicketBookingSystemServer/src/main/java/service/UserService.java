package service;

import dto.*;
import model.entity.Role;
import model.entity.User;
import model.entity.enums.UserStatus;
import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityTransaction;
import util.JPAUtil;

import java.util.UUID;
import dao.UserDAO;
import dao.RoleDAO;
import java.util.List;

public class UserService {
    private final UserDAO userDAO = new UserDAO();
    private final RoleDAO roleDAO = new RoleDAO();
    private static final List<PasswordResetRequestDTO> passwordResetRequests = new java.util.ArrayList<>();


    public UserService() {
    }

    public LoginResponse login(String userName, String password) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            User user = userDAO.findByUserName(userName).orElse(null);
            if (user == null) {
                return LoginResponse.fail("Tên đăng nhập không tồn tại.");
            }
            if (!user.getPassword().equals(password)) {
                return LoginResponse.fail("Mật khẩu không chính xác.");
            }
            if (user.getUserStatus() == UserStatus.INACTIVE) {
                return LoginResponse.fail("Tài khoản đã vô hiệu hoá.");
            }
            return LoginResponse.success(user.getUserID(), user.getFullName(), user.getRole().getRoleName());
        } catch (Exception e) {
            return LoginResponse.fail("Lỗi đăng nhập: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public ActionResponse createUser(String userName, String password,
                                     String fullName, String email, String roleId) {
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
            user.setEmail(email);
            user.setPassword(password);
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
    public List<User> getAllUsers() {
        return userDAO.findAllWithRole();
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

    public List<TransactionDTO> getTransactionHistory(String userId) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            User user = em.find(User.class, userId);
            if (user == null) {
                return java.util.Collections.emptyList();
            }

            String roleName = user.getRole() != null ? user.getRole().getRoleName() : "";
            List<model.entity.Ticket> tickets = new java.util.ArrayList<>();

            if ("MANAGER".equalsIgnoreCase(roleName) || "ADMIN".equalsIgnoreCase(roleName)) {
                tickets = em.createQuery("SELECT t FROM Ticket t ORDER BY t.createAt DESC", model.entity.Ticket.class).getResultList();
            } else if ("EMPLOYEE".equalsIgnoreCase(roleName)) {
                tickets = em.createQuery("SELECT t FROM Ticket t WHERE t.user.userID = :userId ORDER BY t.createAt DESC", model.entity.Ticket.class)
                        .setParameter("userId", userId)
                        .getResultList();
            } else {
                return java.util.Collections.emptyList();
            }

            List<TransactionDTO> result = new java.util.ArrayList<>();
            for (model.entity.Ticket t : tickets) {
                TransactionDTO dto = new TransactionDTO();
                dto.setTicketId(t.getTicketID());
                dto.setEmployeeName(t.getUser() != null ? t.getUser().getFullName() : null);
                dto.setCreatedAt(t.getCreateAt());

                if (t.getCustomer() != null) {
                    dto.setCustomerName(t.getCustomer().getFullName());
                    dto.setCustomerCccd(t.getCustomer().getCustomerID());
                }

                dto.setPrice(t.getFinalPrice());
                dto.setDiscount(t.getDiscount());

                if (t.getSchedule() != null && t.getSchedule().getTrain() != null && t.getSchedule().getRoute() != null) {
                    model.entity.Train train = t.getSchedule().getTrain();
                    model.entity.Route route = t.getSchedule().getRoute();
                    dto.setTrainName(train.getTrainName());
                    dto.setDepartureStation(route.getDepartureStation().getStationName());
                    dto.setArrivalStation(route.getArrivalStation().getStationName());
                    dto.setDepartureTime(t.getSchedule().getDepartureTime());
                    dto.setArrivalTime(t.getSchedule().getArrivalTime());
                }

                if (t.getSeat() != null) {
                    dto.setSeatNumber(t.getSeat().getSeatNumber());
                    if (t.getSeat().getCarriage() != null) {
                        dto.setCarriageNumber(t.getSeat().getCarriage().getCarriageNumber());
                    }
                }

                List<model.entity.Payment> payments = em.createQuery("SELECT p FROM Payment p WHERE p.ticket.ticketID = :ticketId", model.entity.Payment.class)
                        .setParameter("ticketId", t.getTicketID())
                        .getResultList();
                if (!payments.isEmpty()) {
                    dto.setPaymentMethod(payments.get(0).getPaymentMethod());
                } else {
                    dto.setPaymentMethod("N/A");
                }

                dto.setStatus(t.getTicketStatus());
                result.add(dto);
            }
            return result;
        } catch (Exception e) {
            e.printStackTrace();
            return java.util.Collections.emptyList();
        } finally {
            em.close();
        }
    }

    public RevenueStatisticsResponse revenueStatistics(RevenueStatisticsRequest request) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            User user = em.find(User.class, request.getManagerID());
            if (user == null || user.getRole() == null || !"MANAGER".equalsIgnoreCase(user.getRole().getRoleName())) {
                return new dto.RevenueStatisticsResponse(java.util.Collections.emptyMap());
            }

            String jpql = "SELECT t FROM Ticket t WHERE t.ticketStatus IN (model.entity.enums.TicketStatus.PAID, model.entity.enums.TicketStatus.USED)";

            java.time.LocalDateTime startOfDay = null;
            if (request.getStartDate() != null) {
                startOfDay = request.getStartDate().atStartOfDay();
                jpql += " AND t.createAt >= :startDate";
            }

            java.time.LocalDateTime endOfDay = null;
            if (request.getEndDate() != null) {
                endOfDay = request.getEndDate().atTime(23, 59, 59);
                jpql += " AND t.createAt <= :endDate";
            }

            jakarta.persistence.TypedQuery<model.entity.Ticket> query = em.createQuery(jpql, model.entity.Ticket.class);
            if (startOfDay != null) {
                query.setParameter("startDate", startOfDay);
            }
            if (endOfDay != null) {
                query.setParameter("endDate", endOfDay);
            }

            java.util.List<model.entity.Ticket> tickets = query.getResultList();

            java.util.Map<java.time.LocalDate, Double> statsMap = new java.util.TreeMap<>();

            for (model.entity.Ticket t : tickets) {
                java.time.LocalDate date = t.getCreateAt().toLocalDate();
                java.time.LocalDate keyDate = date;

                if (request.getStatisticType() != null) {
                    switch (request.getStatisticType()) {
                        case DAY:
                            keyDate = date;
                            break;
                        case MONTH:
                            keyDate = java.time.LocalDate.of(date.getYear(), date.getMonth(), 1);
                            break;
                        case YEAR:
                            keyDate = java.time.LocalDate.of(date.getYear(), 1, 1);
                            break;
                        case SEASON:
                            int month = date.getMonthValue();
                            int firstMonthOfSeason = ((month - 1) / 3) * 3 + 1;
                            keyDate = java.time.LocalDate.of(date.getYear(), firstMonthOfSeason, 1);
                            break;
                    }
                }

                statsMap.put(keyDate, statsMap.getOrDefault(keyDate, 0.0) + t.getFinalPrice());
            }

            return new dto.RevenueStatisticsResponse(statsMap);

        } catch (Exception e) {
            e.printStackTrace();
            return new dto.RevenueStatisticsResponse(java.util.Collections.emptyMap());
        } finally {
            em.close();
        }
    }

    public SeatTypeRevenueResponse seatTypeRevenue(SeatTypeRevenueRequest request) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            User user = em.find(User.class, request.getManagerID());
            if (user == null || user.getRole() == null || !"MANAGER".equalsIgnoreCase(user.getRole().getRoleName())) {
                return new dto.SeatTypeRevenueResponse(java.util.Collections.emptyList(), 0.0);
            }

            String jpql = "SELECT t.seat.seatType, COUNT(t), SUM(t.finalPrice) FROM Ticket t " +
                          "WHERE t.ticketStatus IN (model.entity.enums.TicketStatus.PAID, model.entity.enums.TicketStatus.USED) " +
                          "GROUP BY t.seat.seatType";

            java.util.List<Object[]> results = em.createQuery(jpql, Object[].class).getResultList();

            java.util.List<dto.SeatTypeRevenueResponse.SeatTypeRevenueDetail> details = new java.util.ArrayList<>();
            double totalRevenue = 0.0;

            for (Object[] row : results) {
                if (row[0] == null) continue;
                model.entity.enums.SeatType type = (model.entity.enums.SeatType) row[0];
                long seatNum = ((Number) row[1]).longValue();
                double rev = row[2] != null ? ((Number) row[2]).doubleValue() : 0.0;

                details.add(new dto.SeatTypeRevenueResponse.SeatTypeRevenueDetail(type, seatNum, rev));
                totalRevenue += rev;
            }

            return new dto.SeatTypeRevenueResponse(details, totalRevenue);
        } catch (Exception e) {
            e.printStackTrace();
            return new dto.SeatTypeRevenueResponse(java.util.Collections.emptyList(), 0.0);
        } finally {
            em.close();
        }
    }

    public ScheduleStatisticsResponse scheduleStatistics(dto.ScheduleStatisticsRequest request) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            User user = em.find(User.class, request.getManagerID());
            if (user == null || user.getRole() == null || !"MANAGER".equalsIgnoreCase(user.getRole().getRoleName())) {
                return new dto.ScheduleStatisticsResponse(java.util.Collections.emptyList());
            }

            java.util.List<model.entity.Schedule> schedules = em.createQuery("SELECT s FROM Schedule s WHERE s.scheduleStatus = model.entity.enums.ScheduleStatus.ENABLED", model.entity.Schedule.class).getResultList();
            java.util.List<dto.ScheduleStatisticsResponse.ScheduleStatisticDetail> details = new java.util.ArrayList<>();

            for (model.entity.Schedule s : schedules) {
                // Calculate total seats
                Long totalSeats = em.createQuery("SELECT COUNT(st) FROM Seat st WHERE st.carriage.train = :train", Long.class)
                                    .setParameter("train", s.getTrain())
                                    .getSingleResult();
                if (totalSeats == null) totalSeats = 0L;

                // Calculate total tickets and revenue for the schedule
                Object[] stats = (Object[]) em.createQuery("SELECT COUNT(t), SUM(t.finalPrice) FROM Ticket t WHERE t.schedule = :schedule AND t.ticketStatus IN (model.entity.enums.TicketStatus.PAID, model.entity.enums.TicketStatus.USED)", Object[].class)
                                              .setParameter("schedule", s)
                                              .getSingleResult();

                long totalTickets = (stats[0] != null) ? ((Number) stats[0]).longValue() : 0L;
                double totalRevenue = (stats[1] != null) ? ((Number) stats[1]).doubleValue() : 0.0;
                long availableSeats = totalSeats - totalTickets;
                if (availableSeats < 0) availableSeats = 0; // Safety guard

                double loadFactor = 0.0;
                if (totalSeats > 0) {
                    loadFactor = (double) totalTickets / totalSeats * 100.0;
                }

                String routeName = s.getRoute() != null && s.getRoute().getDepartureStation() != null && s.getRoute().getArrivalStation() != null
                    ? s.getRoute().getDepartureStation().getStationName() + " → " + s.getRoute().getArrivalStation().getStationName()
                    : "Unknown Route";

                details.add(new dto.ScheduleStatisticsResponse.ScheduleStatisticDetail(
                    s.getScheduleID(),
                    routeName,
                    totalTickets,
                    totalSeats,
                    availableSeats,
                    totalRevenue,
                    loadFactor
                ));
            }

            return new dto.ScheduleStatisticsResponse(details);
        } catch (Exception e) {
            e.printStackTrace();
            return new dto.ScheduleStatisticsResponse(java.util.Collections.emptyList());
        } finally {
            em.close();
        }
    }


    public ActionResponse requestPasswordReset(dto.PasswordResetRequestDTO request) {
        EntityManager em = JPAUtil.getEntityManager();
        try {
            User user = userDAO.findByID(request.getUserID()).orElse(null);
            if (user == null) {
                return new ActionResponse(false, "Người dùng không tồn tại");
            }
            if (user.getEmail() == null || !user.getEmail().trim().equalsIgnoreCase(request.getEmail().trim())) {
                return new ActionResponse(false, "Email không khớp với hệ thống");
            }
            if (!user.getFullName().trim().equalsIgnoreCase(request.getFullName().trim())) {
                return new ActionResponse(false, "Họ tên không khớp");
            }

            boolean exists = passwordResetRequests.stream()
                .anyMatch(r -> r.getUserID().equals(request.getUserID()));
            if (!exists) {
                passwordResetRequests.add(request);
            }
            return new ActionResponse(true, "Yêu cầu đã được gửi đến Admin");
        } catch (Exception e) {
            return new ActionResponse(false, "Lỗi: " + e.getMessage());
        } finally {
            em.close();
        }
    }

    public java.util.List<dto.PasswordResetRequestDTO> getPendingPasswordResets() {
        return new java.util.ArrayList<>(passwordResetRequests);
    }

    public ActionResponse resetPassword(String targetUserId, String newPassword) {
        EntityManager em = JPAUtil.getEntityManager();
        EntityTransaction tx = em.getTransaction();
        try {
            tx.begin();
            User user = userDAO.findByID(targetUserId).orElse(null);
            if (user == null) {
                return new ActionResponse(false, "Người dùng không tồn tại");
            }
            user.setPassword(newPassword);
            em.merge(user);
            tx.commit();
            
            passwordResetRequests.removeIf(r -> r.getUserID().equals(targetUserId));
            return new ActionResponse(true, "Cập nhật mật khẩu thành công");
        } catch (Exception e) {
            if (tx.isActive()) tx.rollback();
            return new ActionResponse(false, "Lỗi server: " + e.getMessage());
        } finally {
            em.close();
        }
    }
}
