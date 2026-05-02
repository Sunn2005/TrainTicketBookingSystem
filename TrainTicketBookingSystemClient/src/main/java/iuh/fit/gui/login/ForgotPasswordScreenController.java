package iuh.fit.gui.login;

import dto.ActionResponse;
import dto.PasswordResetRequestDTO;
import iuh.fit.service.UserClientService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ComboBox;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

public class ForgotPasswordScreenController {
    private static final String REGEX_EMAIL = "^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";

    @FXML
    private TextField usernameField;

    @FXML
    private TextField fullNameField;

    @FXML
    private ComboBox<String> roleCombo;

    @FXML
    private TextField emailField;

    @FXML
    private Label statusLabel;

    @FXML
    private Button submitButton;

    private final UserClientService userService = new UserClientService();

    @FXML
    private void initialize() {
        roleCombo.setItems(FXCollections.observableArrayList("EMPLOYEE", "MANAGER"));
        roleCombo.getSelectionModel().selectFirst();
        clearStatus();
    }

    @FXML
    private void onSubmit() {
        String userId = safeTrim(usernameField.getText());
        String fullName = safeTrim(fullNameField.getText());
        String role = roleCombo.getValue() == null ? "" : roleCombo.getValue().trim();
        String email = safeTrim(emailField.getText());

        if (userId.isEmpty()) {
            showError("Vui lòng nhập User ID.");
            return;
        }
        if (fullName.isEmpty()) {
            showError("Vui lòng nhập họ và tên.");
            return;
        }
        if (!"EMPLOYEE".equals(role) && !"MANAGER".equals(role)) {
            showError("Vai trò chỉ chấp nhận EMPLOYEE hoặc MANAGER.");
            return;
        }
        if (email.isEmpty()) {
            showError("Vui lòng nhập email.");
            return;
        }
        if (!email.matches(REGEX_EMAIL)) {
            showError("Email không đúng định dạng.");
            return;
        }

        setFormEnabled(false);
        clearStatus();

        PasswordResetRequestDTO request = new PasswordResetRequestDTO(userId, fullName, role, email);
        new Thread(() -> {
            ActionResponse response = userService.requestPasswordReset(request);
            Platform.runLater(() -> {
                setFormEnabled(true);
                if (response != null && response.isSuccess()) {
                    showSuccess(response.getMessage());
                    clearForm();
                } else {
                    String message = response != null ? response.getMessage() : "Không thể gửi yêu cầu.";
                    showError(message);
                }
            });
        }).start();
    }

    @FXML
    private void onClose() {
        Stage stage = (Stage) submitButton.getScene().getWindow();
        stage.close();
    }

    private void setFormEnabled(boolean enabled) {
        usernameField.setDisable(!enabled);
        fullNameField.setDisable(!enabled);
        roleCombo.setDisable(!enabled);
        emailField.setDisable(!enabled);
        submitButton.setDisable(!enabled);
    }

    private void clearForm() {
        usernameField.clear();
        fullNameField.clear();
        emailField.clear();
        roleCombo.getSelectionModel().selectFirst();
    }

    private void showError(String message) {
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add("status-error");
        statusLabel.setText(message);
    }

    private void showSuccess(String message) {
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.getStyleClass().add("status-success");
        statusLabel.setText(message);
    }

    private void clearStatus() {
        statusLabel.getStyleClass().removeAll("status-success", "status-error");
        statusLabel.setText("");
    }

    private String safeTrim(String value) {
        return value == null ? "" : value.trim();
    }
}
