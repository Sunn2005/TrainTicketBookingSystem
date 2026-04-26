package iuh.fit.gui.user.create_account;

import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;

public class CreateAccountScreenController {

	@FXML
	private TextField usernameField;

	@FXML
	private TextField fullNameField;

	@FXML
	private TextField roleField;

	@FXML
	private PasswordField passwordField;

	@FXML
	private PasswordField confirmPasswordField;

	@FXML
	private Label statusLabel;

	@FXML
	private Button createButton;

	@FXML
	private void initialize() {
		clearStatus();
	}

	@FXML
	private void onCreateClick() {
		String username = usernameField.getText() == null ? "" : usernameField.getText().trim();
		String fullName = fullNameField.getText() == null ? "" : fullNameField.getText().trim();
		String role = roleField.getText() == null ? "" : roleField.getText().trim();
		String password = passwordField.getText() == null ? "" : passwordField.getText().trim();
		String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText().trim();

		if (username.isEmpty() || fullName.isEmpty() || role.isEmpty() || password.isEmpty() || confirmPassword.isEmpty()) {
			showError("Vui lòng nhập đầy đủ thông tin.");
			return;
		}
		if (password.length() < 6) {
			showError("Mật khẩu phải có ít nhất 6 ký tự.");
			return;
		}
		if (!password.equals(confirmPassword)) {
			showError("Mật khẩu xác nhận không khớp.");
			return;
		}
		if (!role.equals("Manager") && !role.equals("Employee")) {
			showError("Vai trò phải là Manager hoặc Employee.");
			return;
		}

		showSuccess("Đã tạo tài khoản cho " + username + " thành công.");
		clearFields();
		showSuccessDialog(username);
	}

	private void showSuccess(String message) {
		statusLabel.getStyleClass().removeAll("error", "success");
		statusLabel.getStyleClass().add("success");
		statusLabel.setText(message);
	}

	private void showError(String message) {
		statusLabel.getStyleClass().removeAll("error", "success");
		statusLabel.getStyleClass().add("error");
		statusLabel.setText(message);
	}

	private void clearStatus() {
		statusLabel.getStyleClass().removeAll("error", "success");
		statusLabel.setText("");
	}

	private void clearFields() {
		usernameField.clear();
		fullNameField.clear();
		roleField.clear();
		passwordField.clear();
		confirmPasswordField.clear();
	}

	private void showSuccessDialog(String username) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Thông báo");
		alert.setHeaderText("Tạo tài khoản thành công");
		alert.setContentText("Đã tạo tài khoản mới cho " + username + ".");
		alert.showAndWait();
	}
}
