package iuh.fit.gui.home;

import iuh.fit.context.UserContext;
import javafx.animation.KeyFrame;
import javafx.animation.Timeline;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.util.Duration;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

public class HomeHeaderController {
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

    private Timeline clockTimeline;

    @FXML
    private void initialize() {
        bindUserInfo();
        updateDateTime();
    }

    void startClock() {
        if (clockTimeline != null) {
            return;
        }
        clockTimeline = new Timeline(new KeyFrame(Duration.seconds(1), event -> updateDateTime()));
        clockTimeline.setCycleCount(Timeline.INDEFINITE);
        clockTimeline.play();
    }

    void stopClock() {
        if (clockTimeline != null) {
            clockTimeline.stop();
            clockTimeline = null;
        }
    }

    private void bindUserInfo() {
        UserContext userContext = UserContext.getInstance();

        String fullName = userContext.getFullName();
        if (fullName == null || fullName.isBlank()) {
            fullName = "Unknown User";
        }

        String role = userContext.getRole();
        if (role == null || role.isBlank()) {
            role = "Staff";
        }

        fullNameLabel.setText(fullName);
        roleLabel.setText(role);
        avatarLabel.setText(fullName.substring(0, 1).toUpperCase());
    }

    private void updateDateTime() {
        LocalDateTime now = LocalDateTime.now();
        dateLabel.setText(now.format(DATE_FORMAT));
        timeLabel.setText(now.format(TIME_FORMAT));
    }
}

