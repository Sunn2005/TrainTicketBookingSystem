package iuh.fit.gui.home;

import iuh.fit.App;
import iuh.fit.constance.AppTheme;
import iuh.fit.context.UserContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
    private Button customerUpdateButton;

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
                customerUpdateButton
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
        contentContainer.getChildren().setAll(createSampleScreen(
                "Doi ve",
                "Man hinh mau cho doi ve theo ma dat cho.",
                "Yeu cau moi", "9",
                "Dang xu ly", "5",
                "Da hoan tat", "42"
        ));
    }

    @FXML
    private void showCancelTicketScreen() {
        setActiveButton(cancelTicketButton);
        contentContainer.getChildren().setAll(createSampleScreen(
                "Huy ve",
                "Man hinh mau cho huy ve va tinh phi hoan.",
                "Yeu cau huy", "7",
                "Da duyet", "31",
                "Hoan tien", "14,700,000 VND"
        ));
    }

    @FXML
    private void showScheduleScreen() {
        setActiveButton(scheduleButton);
        contentContainer.getChildren().setAll(createSampleScreen(
                "Lich trinh",
                "Man hinh mau quan ly lich trinh tau chay.",
                "Tau dang chay", "18",
                "Cho khoi hanh", "11",
                "Tre gio", "2"
        ));
    }

    @FXML
    private void showCoachScreen() {
        setActiveButton(coachButton);
        contentContainer.getChildren().setAll(createSampleScreen(
                "Toa tau",
                "Man hinh mau quan ly toa, ghe va trang thai.",
                "Tong toa", "126",
                "Con trong", "34",
                "Bao tri", "6"
        ));
    }

    @FXML
    private void showCustomerListScreen() {
        setActiveButton(customerListButton);
        contentContainer.getChildren().setAll(createSampleScreen(
                "Danh sach khach hang",
                "Man hinh mau tra cuu va loc khach hang.",
                "Tong KH", "5,280",
                "Moi tuan nay", "126",
                "VIP", "420"
        ));
    }

    @FXML
    private void showCustomerUpdateScreen() {
        setActiveButton(customerUpdateButton);
        contentContainer.getChildren().setAll(createSampleScreen(
                "Cap nhat khach hang",
                "Man hinh mau cap nhat thong tin ho so khach hang.",
                "Yeu cau cap nhat", "16",
                "Da xu ly", "13",
                "Con lai", "3"
        ));
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
