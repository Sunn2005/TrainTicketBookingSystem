package iuh.fit.gui.home;

import iuh.fit.App;
import iuh.fit.constance.AppTheme;
import iuh.fit.context.TicketContext;
import iuh.fit.context.UserContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.StackPane;
import javafx.scene.layout.VBox;
import javafx.stage.Stage;
import javafx.util.Duration;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;

public class HomeScreen {
    private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("EEEE, dd/MM/yyyy");
    private static final DateTimeFormatter TIME_FORMAT = DateTimeFormatter.ofPattern("HH:mm:ss");

    @FXML
    private Label avatarLabel;

    @FXML
    private Label fullNameLabel;

    @FXML
    private Label roleLabel;

    private final TicketContext ctx = TicketContext.getInstance();

    @FXML
    private Label dateLabel;

    @FXML
    private Label timeLabel;

    @FXML
    private StackPane contentContainer;

    @FXML
    private Button sellTicketButton;

    @FXML
    private Button exchangeTicketButton;

    @FXML
    private Button cancelTicketButton;

    @FXML
    private Button scheduleButton;

    @FXML
    private Button coachButton;

    @FXML
    private Button customerListButton;

    @FXML
    private Button updatePriceButton;

//    @FXML
//    private Button customerUpdateButton;

    @FXML
    private Button createAccountButton;

    @FXML
    private Button updatePasswordButton;

    private Timeline clockTimeline;
    private List<Button> menuButtons;

    @FXML
    private void initialize() {
        bindUserInfo();
        updateDateTime();

        menuButtons = List.of(
                sellTicketButton,
                exchangeTicketButton,
                cancelTicketButton,
                scheduleButton,
                coachButton,
                customerListButton,
//                customerUpdateButton,
                createAccountButton,
                updatePasswordButton,
                updatePriceButton
        );

        showSellTicketScreen();

        // Keep time in the header synced every second.
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateDateTime()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    @FXML
    private void showSellTicketScreen() {
        setActiveButton(sellTicketButton);
        try {
            ctx.setCurrentStep(TicketContext.BookingStep.OUTBOUND_SEARCH);
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/iuh/fit/gui/ticket/search/search-schedule-view.fxml"));
            contentContainer.getChildren().setAll((Node) loader.load());
        } catch (Exception e) {  // đổi IOException → Exception
            e.printStackTrace();  // thêm dòng này
            contentContainer.getChildren().setAll(
                    new Label("Không thể tải màn hình bán vé: " + e.getMessage()));
        }
    }

    @FXML
    private void showExchangeTicketScreen() {
        setActiveButton(exchangeTicketButton);
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/iuh/fit/gui/ticket/exchange/exchange-ticket-view.fxml"));
            contentContainer.getChildren().setAll((Node) loader.load());
        } catch (Exception e) {
            e.printStackTrace();
            contentContainer.getChildren().setAll(
                    new Label("Không thể tải màn hình đổi vé: " + e.getMessage()));
        }
    }


    @FXML
    private void showCancelTicketScreen() {
        setActiveButton(cancelTicketButton);
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/iuh/fit/gui/ticket/cancel/cancel-ticket-view.fxml"));
            contentContainer.getChildren().setAll((Node) loader.load());
        } catch (Exception e) {
            e.printStackTrace();
            contentContainer.getChildren().setAll(
                    new Label("Không thể tải màn hình hủy vé: " + e.getMessage()));
        }
    }

    @FXML
    private void showScheduleScreen() {
        setActiveButton(scheduleButton);
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/iuh/fit/gui/schedule/schedule-management-view.fxml"));
            contentContainer.getChildren().setAll((Node) loader.load());
        } catch (Exception e) {
            e.printStackTrace();
            contentContainer.getChildren().setAll(
                    new Label("Không thể tải màn hình lịch trình chuyến tàu: " + e.getMessage()));
        }
    }

    @FXML
    private void showCoachScreen() {
        setActiveButton(coachButton);
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/iuh/fit/gui/train/train-management-view.fxml"));
            contentContainer.getChildren().setAll((Node) loader.load());
        } catch (Exception e) {
            e.printStackTrace();
            contentContainer.getChildren().setAll(
                    new Label("Không thể tải màn hình quản lý tàu: " + e.getMessage()));
        }
    }

    @FXML
    private void showCustomerListScreen() {
        setActiveButton(customerListButton);
        try {
            FXMLLoader loader = new FXMLLoader(
                    App.class.getResource(
                            "/iuh/fit/gui/customer/customer-management-view.fxml"
                    )
            );
            contentContainer.getChildren().setAll((Node) loader.load());
        } catch (Exception e) {
            e.printStackTrace();
            contentContainer.getChildren().setAll(
                    new Label("Không thể tải màn hình khách hàng: " + e.getMessage())
            );
        }
    }

    private void loadView(String fxml) {
        try {
            FXMLLoader loader = new FXMLLoader(
                    getClass().getResource(fxml)
            );

            Parent view = loader.load();

            contentContainer.getChildren().clear();
            contentContainer.getChildren().add(view);

        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @FXML
    private void showRevenueStats() {
        loadView("/iuh/fit/gui/statistics/revenue-by-time-view.fxml");
    }

    @FXML
    private void showSeatTypeStats() {
        loadView("/iuh/fit/gui/statistics/revenue-by-seat-type-view.fxml");
    }

    @FXML
    private void showScheduleStats() {
        loadView("/iuh/fit/gui/statistics/revenue-by-schedule-view.fxml");
    }

    @FXML
    private void showUpdatePriceScreen() {
        setActiveButton(updatePriceButton);
        loadView("/iuh/fit/gui/price/price-management-view.fxml");
    }

//    @FXML
//    private void showCustomerUpdateScreen() {
//        setActiveButton(customerUpdateButton);
//        contentContainer.getChildren().setAll(createSampleScreen(
//                "Cap nhat khach hang",
//                "Man hinh mau cap nhat thong tin ho so khach hang.",
//                "Yeu cau cap nhat", "16",
//                "Da xu ly", "13",
//                "Con lai", "3"
//        ));
//    }

    @FXML
    private void showCreateAccountScreen() {
        String role = UserContext.getInstance().getRole();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            setActiveButton(createAccountButton);
            // Hiện thông báo không có quyền
            VBox denied = new VBox(16);
            denied.setAlignment(javafx.geometry.Pos.CENTER);
            denied.setStyle("-fx-background-color:#f8faff;");
            Label icon = new Label("🔒");
            icon.setStyle("-fx-font-size:64px;");
            Label title = new Label("Không có quyền truy cập");
            title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#dc2626;");
            Label msg = new Label("Chỉ tài khoản ADMIN mới được quản lý tài khoản.");
            msg.setStyle("-fx-font-size:14px;-fx-text-fill:#555;");
            denied.getChildren().addAll(icon, title, msg);
            contentContainer.getChildren().setAll(denied);
            return;
        }

        setActiveButton(createAccountButton);
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/iuh/fit/gui/user/create-account/create-account-view.fxml"));
            contentContainer.getChildren().setAll((Node) loader.load());
        } catch (Exception e) {
            e.printStackTrace();
            contentContainer.getChildren().setAll(
                    new Label("Không thể tải màn hình tạo tài khoản: " + e.getMessage()));
        }
    }
    @FXML
    private void showUpdatePasswordScreen() {
        String role = UserContext.getInstance().getRole();
        if (!"ADMIN".equalsIgnoreCase(role)) {
            setActiveButton(updatePasswordButton);
            // Hiện thông báo không có quyền
            VBox denied = new VBox(16);
            denied.setAlignment(javafx.geometry.Pos.CENTER);
            denied.setStyle("-fx-background-color:#f8faff;");
            Label icon = new Label("🔒");
            icon.setStyle("-fx-font-size:64px;");
            Label title = new Label("Không có quyền truy cập");
            title.setStyle("-fx-font-size:22px;-fx-font-weight:bold;-fx-text-fill:#dc2626;");
            Label msg = new Label("Chỉ tài khoản ADMIN mới được quản lý tài khoản.");
            msg.setStyle("-fx-font-size:14px;-fx-text-fill:#555;");
            denied.getChildren().addAll(icon, title, msg);
            contentContainer.getChildren().setAll(denied);
            return;
        }
        setActiveButton(updatePasswordButton);
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/iuh/fit/gui/user/update-password/update-password-view.fxml"));
            contentContainer.getChildren().setAll((Node) loader.load());
        } catch (Exception e) {
            e.printStackTrace();
            contentContainer.getChildren().setAll(
                    new Label("Không thể tải màn hình cập nhật mật khẩu: " + e.getMessage()));
        }
    }

    private Node createSampleScreen(
            String title,
            String subtitle,
            String metric1Label,
            String metric1Value,
            String metric2Label,
            String metric2Value,
            String metric3Label,
            String metric3Value
    ) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("screen-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("screen-subtitle");

        HBox cardRow = new HBox(12);
        cardRow.getChildren().addAll(
                createMetricCard(metric1Label, metric1Value),
                createMetricCard(metric2Label, metric2Value),
                createMetricCard(metric3Label, metric3Value)
        );

        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        Label panelTitle = new Label("Noi dung mau");
        panelTitle.getStyleClass().add("panel-title");

        VBox placeholder = new VBox(6);
        placeholder.getStyleClass().add("placeholder");
        VBox.setVgrow(placeholder, Priority.ALWAYS);

        Label info1 = new Label("- Day la khu vuc content se thay doi theo menu sidebar.");
        info1.getStyleClass().add("placeholder-text");
        Label info2 = new Label("- Ban co the thay bang TableView/Form that trong buoc tiep theo.");
        info2.getStyleClass().add("placeholder-text");
        Label info3 = new Label("- Mau nay giup team test dieu huong truoc khi gan du lieu that.");
        info3.getStyleClass().add("placeholder-text");
        placeholder.getChildren().addAll(info1, info2, info3);

        panel.getChildren().addAll(panelTitle, new Separator(), placeholder);

        VBox root = new VBox(16);
        root.getStyleClass().add("screen-root");
        root.getChildren().addAll(titleLabel, subtitleLabel, cardRow, panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
        return root;
    }

    private VBox createMetricCard(String label, String value) {
        Label metricLabel = new Label(label);
        metricLabel.getStyleClass().add("card-label");

        Label metricValue = new Label(value);
        metricValue.getStyleClass().add("card-value");

        VBox card = new VBox(6, metricLabel, metricValue);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }

    private void setActiveButton(Button activeButton) {
        for (Button button : menuButtons) {
            button.getStyleClass().remove("menu-button-active");
        }
        activeButton.getStyleClass().add("menu-button-active");
    }

    private void bindUserInfo() {
        UserContext userContext = UserContext.getInstance();
        String fullName = userContext.getFullName().isBlank() ? "Unknown User" : userContext.getFullName();
        String role = userContext.getRole().isBlank() ? "Staff" : userContext.getRole();

        fullNameLabel.setText(fullName);
        roleLabel.setText(role);
        avatarLabel.setText(fullName.substring(0, 1).toUpperCase());
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DATE_FORMAT));
        timeLabel.setText(now.format(TIME_FORMAT));
    }

    @FXML
    private void onLogoutButtonClick() {
        if (clockTimeline != null) {
            clockTimeline.stop();
        }

        UserContext.getInstance().clear();

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/iuh/fit/gui/login/login-view.fxml"));
            Scene loginScene = new Scene(loader.load(), 1366, 768);
            AppTheme.applyTo(loginScene);

            Stage stage = (Stage) contentContainer.getScene().getWindow();
            stage.setTitle("Train Ticket Socket Client");
            stage.setScene(loginScene);
        } catch (IOException e) {
            contentContainer.getChildren().setAll(new Label("Khong the mo man hinh dang nhap."));
        }
    }
}
