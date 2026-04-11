# Hướng Dẫn Sử Dụng Controller Classes Phía Client

## Tổng Quan

Phía client có 6 lớp controller (wrapper) để gọi các service từ server JAR. Mỗi lớp đại diện cho một domain khác nhau và cung cấp các API để thao tác dữ liệu.

**Các lớp controller có sẵn:**

- `UserClientController` - Quản lý người dùng, đăng nhập, quyền
- `CustomerClientController` - Quản lý thông tin khách hàng
- `TicketClientController` - Quản lý vé tàu, đặt vé, hủy vé
- `TrainClientController` - Quản lý tàu, lệnh tạo/sửa/xóa tàu
- `ScheduleClientController` - Quản lý lịch trình tàu
- `PriceClientController` - Quản lý giá cơ bản

---

## 1. UserClientController - Quản Lý Người Dùng

### Import & Khởi Tạo

```java
import iuh.fit.client.controller.UserClientController;

// Khởi tạo mặc định (lấy UserService ngầm)
UserClientController userController = new UserClientController();

// Hoặc khởi tạo với UserService tùy chỉnh (nếu cần)
// UserClientController userController = new UserClientController(myUserService);
```

### Các Phương Thức Chính

#### 1. Login - Đăng nhập

```java
String userName = "admin";
String password = "123456";

LoginResponse response = userController.login(userName, password);

if (response.isSuccess()) {
    System.out.println("Đăng nhập thành công!");
    System.out.println("UserID: " + response.getUserID());
    System.out.println("Tên: " + response.getFullName());
    System.out.println("Quyền: " + response.getRole());
    System.out.println("Tin nhắn: " + response.getMessage());
} else {
    System.out.println("Đăng nhập thất bại: " + response.getMessage());
}
```

#### 2. Tạo User Mới

```java
ActionResponse response = userController.createUser(
    "newuser",      // username
    "password123",  // password
    "Nguyen Van A", // fullName
    "EMPLOYEE"      // roleName (ADMIN, MANAGER, EMPLOYEE)
);

if (response.isSuccess()) {
    System.out.println("Tạo user thành công!");
} else {
    System.out.println("Lỗi: " + response.getMessage());
}
```

#### 3. Đổi Mật Khẩu

```java
ActionResponse response = userController.changePassword(
    "userId123",    // userId
    "oldPassword",  // oldPassword
    "newPassword"   // newPassword
);

if (response.isSuccess()) {
    System.out.println("Đổi mật khẩu thành công!");
}
```

#### 4. Đổi Trạng Thái User

```java
import model.entity.enums.UserStatus;

ActionResponse response = userController.changeStatus(
    "userId123",
    UserStatus.ACTIVE  // Trạng thái: ACTIVE, INACTIVE
);
```

#### 5. Đổi Tên Đầy Đủ

```java
ActionResponse response = userController.changeFullName(
    "userId123",
    "Tran Van B"
);
```

#### 6. Đổi Quyền User (chỉ ADMIN)

```java
ActionResponse response = userController.changeUserRole(
    "adminId",        // ID của admin
    "targetUserId",   // ID user cần thay đổi quyền
    "MANAGER"         // Quyền mới (ADMIN, MANAGER, EMPLOYEE)
);
```

#### 7. Lấy Lịch Sử Giao Dịch

```java
List<TransactionDTO> transactions = userController.getTransactionHistory("userId123");

for (TransactionDTO t : transactions) {
    System.out.println("Giao dịch: " + t.getId() + " - " + t.getAmount());
}
```

#### 8. Thống Kê Doanh Thu

```java
import dto.RevenueStatisticsRequest;
import dto.RevenueStatisticsResponse;

RevenueStatisticsRequest request = new RevenueStatisticsRequest();
request.setStartDate(LocalDate.of(2026, 1, 1));
request.setEndDate(LocalDate.of(2026, 12, 31));
request.setStatisticType(StatisticType.YEARLY); // DAILY, MONTHLY, QUARTERLY, YEARLY

RevenueStatisticsResponse response = userController.revenueStatistics(request);
System.out.println("Doanh thu: " + response.getTotal());
```

#### 9. Thống Kê Doanh Thu Theo Loại Ghế

```java
import dto.SeatTypeRevenueRequest;
import dto.SeatTypeRevenueResponse;

SeatTypeRevenueRequest request = new SeatTypeRevenueRequest();
request.setStartDate(LocalDate.of(2026, 1, 1));
request.setEndDate(LocalDate.of(2026, 12, 31));

SeatTypeRevenueResponse response = userController.seatTypeRevenue(request);
for (SeatTypeRevenueDetail detail : response.getDetails()) {
    System.out.println("Loại ghế: " + detail.getSeatType() + " - Doanh thu: " + detail.getRevenue());
}
```

#### 10. Thống Kê Doanh Thu Theo Lịch Trình

```java
import dto.ScheduleStatisticsRequest;
import dto.ScheduleStatisticsResponse;

ScheduleStatisticsRequest request = new ScheduleStatisticsRequest();
request.setStartDate(LocalDate.of(2026, 1, 1));
request.setEndDate(LocalDate.of(2026, 12, 31));

ScheduleStatisticsResponse response = userController.scheduleStatistics(request);
for (ScheduleStatisticDetail detail : response.getDetails()) {
    System.out.println("Lịch trình: " + detail.getScheduleId() + " - Doanh thu: " + detail.getRevenue());
}
```

---

## 2. CustomerClientController - Quản Lý Khách Hàng

### Import & Khởi Tạo

```java
import iuh.fit.client.controller.CustomerClientController;

CustomerClientController customerController = new CustomerClientController();
```

### Các Phương Thức

#### 1. Lấy Khách Hàng Theo ID

```java
Customer customer = customerController.getCustomerById("customerId123");

if (customer != null) {
    System.out.println("ID: " + customer.getId());
    System.out.println("Tên: " + customer.getName());
    System.out.println("CCCD: " + customer.getIdentityNumber());
    System.out.println("Email: " + customer.getEmail());
}
```

#### 2. Lấy Tất Cả Khách Hàng

```java
List<Customer> customers = customerController.getAllCustomers();

for (Customer customer : customers) {
    System.out.println(customer.getName() + " - " + customer.getIdentityNumber());
}
```

---

## 3. TicketClientController - Quản Lý Vé Tàu

### Import & Khởi Tạo

```java
import iuh.fit.client.controller.TicketClientController;
import java.time.LocalDate;

TicketClientController ticketController = new TicketClientController();
```

### Các Phương Thức

#### 1. Lấy Lịch Trình Có Ghế Trống

```java
List<ScheduleInfoResponse> schedules = ticketController.getSchedulesWithAvailableSeats(
    "Hà Nội",      // departureStation
    "TP.HCM",      // destinationStation
    LocalDate.of(2026, 4, 15)  // date
);

for (ScheduleInfoResponse schedule : schedules) {
    System.out.println("Lịch trình: " + schedule.getScheduleId());
    System.out.println("Tàu: " + schedule.getTrainId());
    System.out.println("Ghế trống: " + schedule.getAvailableSeatCount());
}
```

#### 2. Lấy Danh Sách Ghế Trống

```java
List<Seat> seats = ticketController.getAvailableSeats("scheduleId123");

for (Seat seat : seats) {
    System.out.println("Ghế: " + seat.getSeatNumber() + " - Loại: " + seat.getSeatType());
}
```

#### 3. Bán Vé

```java
import dto.SellTicketRequest;

SellTicketRequest request = new SellTicketRequest();
request.setCustomerId("customerId123");
request.setScheduleId("scheduleId123");

// Thêm chi tiết vé
SellTicketRequest.TicketDetail detail = new SellTicketRequest.TicketDetail();
detail.setSeatId("seatId123");
detail.setPrice(500000);
request.addTicketDetail(detail);

ActionResponse response = ticketController.sellTicket(request);

if (response.isSuccess()) {
    System.out.println("Bán vé thành công!");
    System.out.println("ID vé: " + response.getExtraData());
} else {
    System.out.println("Lỗi: " + response.getMessage());
}
```

#### 4. Hủy Vé

```java
ActionResponse response = ticketController.cancelTicket(
    "ticketId123",    // ticketId
    "Lý do hủy"      // reason
);

if (response.isSuccess()) {
    System.out.println("Hủy vé thành công!");
}
```

#### 5. Đổi Vé

```java
ActionResponse response = ticketController.exchangeTicket(
    "ticketId123",    // ticketId
    "newScheduleId",  // newScheduleId
    "newSeatId"       // newSeatId
);

if (response.isSuccess()) {
    System.out.println("Đổi vé thành công!");
}
```

#### 6. Cập Nhật Trạng Thái Thanh Toán

```java
import model.entity.enums.PaymentStatus;

ActionResponse response = ticketController.updatePaymentStatus(
    "ticketId123",
    PaymentStatus.COMPLETED  // PENDING, COMPLETED, FAILED
);
```

---

## 4. TrainClientController - Quản Lý Tàu

### Import & Khởi Tạo

```java
import iuh.fit.client.controller.TrainClientController;
import dto.CreateTrainRequest;
import dto.UpdateTrainRequest;

TrainClientController trainController = new TrainClientController();
```

### Các Phương Thức

#### 1. Tạo Tàu Mới

```java
CreateTrainRequest request = new CreateTrainRequest();
request.setTrainId("TRAIN001");
request.setTrainCode("T001");
request.setName("Tàu Hà Nội - TP.HCM");
request.setStatus("ACTIVE");

trainController.createTrain(request);
System.out.println("Tàu được tạo thành công!");
```

#### 2. Cập Nhật Tàu

```java
UpdateTrainRequest request = new UpdateTrainRequest();
request.setTrainId("TRAIN001");
request.setName("Tàu Hà Nội - TP.HCM Express");
request.setStatus("ACTIVE");

trainController.updateTrain(request);
System.out.println("Tàu được cập nhật thành công!");
```

#### 3. Xóa Tàu

```java
trainController.deleteTrain("TRAIN001");
System.out.println("Tàu được xóa thành công!");
```

---

## 5. ScheduleClientController - Quản Lý Lịch Trình

### Import & Khởi Tạo

```java
import iuh.fit.client.controller.ScheduleClientController;
import dto.CreateScheduleRequest;
import dto.UpdateScheduleRequest;
import java.time.LocalDateTime;

ScheduleClientController scheduleController = new ScheduleClientController();
```

### Các Phương Thức

#### 1. Tạo Lịch Trình

```java
CreateScheduleRequest request = new CreateScheduleRequest();
request.setTrainId("TRAIN001");
request.setRouteId("ROUTE001");
request.setDepartureTime(LocalDateTime.of(2026, 4, 15, 8, 0));
request.setArrivalTime(LocalDateTime.of(2026, 4, 15, 16, 30));

scheduleController.createSchedule(request);
System.out.println("Lịch trình được tạo thành công!");
```

#### 2. Cập Nhật Lịch Trình

```java
UpdateScheduleRequest request = new UpdateScheduleRequest();
request.setScheduleId("SCHEDULE001");
request.setTrainId("TRAIN002");
request.setDepartureTime(LocalDateTime.of(2026, 4, 16, 8, 0));

scheduleController.updateSchedule(request);
System.out.println("Lịch trình được cập nhật thành công!");
```

#### 3. Xóa Lịch Trình

```java
scheduleController.deleteSchedule("SCHEDULE001");
System.out.println("Lịch trình được xóa thành công!");
```

---

## 6. PriceClientController - Quản Lý Giá

### Import & Khởi Tạo

```java
import iuh.fit.client.controller.PriceClientController;
import model.entity.BasePrice;

PriceClientController priceController = new PriceClientController();
```

### Các Phương Thức

#### 1. Lấy Giá Cơ Bản

```java
BasePrice basePrice = priceController.getBasePrice();

if (basePrice != null) {
    System.out.println("Giá cơ bản: " + basePrice.getPrice());
    System.out.println("Ngày cập nhật: " + basePrice.getUpdatedDate());
}
```

#### 2. Cập Nhật Giá Cơ Bản

```java
BasePrice basePrice = new BasePrice();
basePrice.setPrice(500000);
basePrice.setUpdatedDate(LocalDateTime.now());

boolean success = priceController.updateBasePrice(basePrice);

if (success) {
    System.out.println("Giá cơ bản được cập nhật thành công!");
} else {
    System.out.println("Cập nhật giá cơ bản thất bại!");
}
```

---

## Template Sử Dụng Trong JavaFX

### Ví dụ: Sử dụng UserClientController trong Controller JavaFX

```java
package iuh.fit.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import iuh.fit.client.controller.UserClientController;
import dto.LoginResponse;

public class LoginFormController {
    private final UserClientController userController = new UserClientController();

    @FXML
    private TextField usernameField;

    @FXML
    private TextField passwordField;

    @FXML
    private Label statusLabel;

    @FXML
    protected void onLoginClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        try {
            LoginResponse response = userController.login(username, password);

            if (response.isSuccess()) {
                statusLabel.setText("Đăng nhập thành công: " + response.getFullName());
                // Chuyển sang màn hình tiếp theo
            } else {
                statusLabel.setText("Đăng nhập thất bại: " + response.getMessage());
            }
        } catch (Exception e) {
            statusLabel.setText("Lỗi: " + e.getMessage());
        }
    }
}
```

---

## Các Lưu Ý Quan Trọng

### 1. Exception Handling

Luôn wrap gọi controller trong try-catch để bắt các exception:

```java
try {
    LoginResponse response = userController.login("admin", "123456");
} catch (NullPointerException e) {
    System.out.println("Lỗi null: " + e.getMessage());
} catch (Exception e) {
    System.out.println("Lỗi chung: " + e.getMessage());
}
```

### 2. Kiểm Tra Response

Luôn kiểm tra response trước khi lấy dữ liệu:

```java
ActionResponse response = userController.createUser(...);

if (response != null && response.isSuccess()) {
    System.out.println("Thành công!");
} else if (response != null) {
    System.out.println("Thất bại: " + response.getMessage());
} else {
    System.out.println("Null response từ server!");
}
```

### 3. Quản Lý Kết Nối

- Tất cả controller wrapper gọi service từ server JAR qua database thực.
- Đảm bảo database được khởi tạo trước khi gọi controller.
- Lỗi database sẽ báo lỗi từ service.

### 4. Xử Lý Null

Luôn kiểm tra null trước khi truy cập property:

```java
Customer customer = customerController.getCustomerById("id123");

if (customer != null) {
    String name = customer.getName();  // Safely access
}
```

### 5. Input Validation

Luôn validate dữ liệu đầu vào trước khi guyị controller:

```java
String username = usernameField.getText().trim();
String password = passwordField.getText().trim();

if (username.isEmpty() || password.isEmpty()) {
    statusLabel.setText("Username/password không được rỗng!");
    return;
}

LoginResponse response = userController.login(username, password);
```

---

## Tóm Tắt Quy Trình Sử Dụng

1. **Import** class controller cần dùng
2. **Khởi tạo** instance của controller
3. **Chuẩn bị** request/parameter cần gửi
4. **Gọi** method tương ứng
5. **Wrap trong try-catch** để bắt exception
6. **Kiểm tra** response.isSuccess() hoặc response != null
7. **Xử lý** response (lấy dữ liệu hoặc hiển thị lỗi)

---

## Liên Hệ Support

Nếu gặp vấn đề:

- Kiểm tra log terminal server để xem thông báo chi tiết
- Kiểm tra connection tới database
- Kiểm tra database đã được khởi tạo dữ liệu chưa
