package iuh.fit.gui.user;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.Alert;
import javafx.scene.control.Button;
import javafx.scene.control.ContentDisplay;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.PasswordField;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.Region;
import javafx.scene.layout.VBox;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;

public class UpdatePasswordScreenController {
	private static final DateTimeFormatter DATE_FORMAT = DateTimeFormatter.ofPattern("dd-MM-yyyy");

	@FXML
	private ListView<PendingUser> pendingUsersListView;

	@FXML
	private Label selectedUserLabel;

	@FXML
	private PasswordField newPasswordField;

	@FXML
	private PasswordField confirmPasswordField;

	@FXML
	private Label statusLabel;

	@FXML
	private Button confirmButton;

	private final ObservableList<PendingUser> pendingUsers = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		setupListView();
		setupPendingUsers();
		setFormEnabled(false);
		clearStatus();
	}

	@FXML
	private void onConfirmClick() {
		PendingUser selectedUser = pendingUsersListView.getSelectionModel().getSelectedItem();
		if (selectedUser == null) {
			showError("Vui lòng chọn tài khoản cần cập nhật.");
			return;
		}

		String newPassword = newPasswordField.getText() == null ? "" : newPasswordField.getText().trim();
		String confirmPassword = confirmPasswordField.getText() == null ? "" : confirmPasswordField.getText().trim();

		if (newPassword.isEmpty() || confirmPassword.isEmpty()) {
			showError("Vui lòng nhập đầy đủ mật khẩu mới và xác nhận mật khẩu.");
			return;
		}
		if (newPassword.length() < 6) {
			showError("Mật khẩu mới phải có ít nhất 6 ký tự.");
			return;
		}
		if (!newPassword.equals(confirmPassword)) {
			showError("Mật khẩu xác nhận không khớp.");
			return;
		}

		showSuccess("Đã cập nhật mật khẩu cho " + selectedUser.username() + " thành công.");
		confirmPasswordField.clear();
		newPasswordField.clear();
		showSuccessDialog(selectedUser.username());
	}

	private void setupPendingUsers() {
		pendingUsers.setAll(
				new PendingUser("User001", "Nguyễn Văn A", "Manager", LocalDate.now().minusDays(5)),
				new PendingUser("User002", "Nguyễn Văn B", "Employee", LocalDate.now().minusDays(4)),
				new PendingUser("User003", "Trần Thị C", "Employee", LocalDate.now().minusDays(3)),
				new PendingUser("User004", "Lê Văn D", "Employee", LocalDate.now().minusDays(2))
		);
	}

	private void setupListView() {
		pendingUsersListView.setItems(pendingUsers);
		pendingUsersListView.setCellFactory(listView -> new PendingUserCell());
		pendingUsersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedUser) -> {
			if (selectedUser == null) {
				selectedUserLabel.setText("Chưa chọn người dùng");
				setFormEnabled(false);
				return;
			}
			selectedUserLabel.setText(selectedUser.username() + " - " + selectedUser.fullName());
			setFormEnabled(true);
			clearStatus();
			newPasswordField.clear();
			confirmPasswordField.clear();
		});
	}

	private void setFormEnabled(boolean enabled) {
		newPasswordField.setDisable(!enabled);
		confirmPasswordField.setDisable(!enabled);
		confirmButton.setDisable(!enabled);
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

	private void showSuccessDialog(String username) {
		Alert alert = new Alert(Alert.AlertType.INFORMATION);
		alert.setTitle("Thông báo");
		alert.setHeaderText("Cập nhật thành công");
		alert.setContentText("Đã cập nhật mật khẩu mới cho tài khoản " + username + ".");
		alert.showAndWait();
	}

	private record PendingUser(String username, String fullName, String role, LocalDate requestDate) {
	}

	private static final class PendingUserCell extends ListCell<PendingUser> {
		@Override
		protected void updateItem(PendingUser item, boolean empty) {
			super.updateItem(item, empty);
			if (empty || item == null) {
				setText(null);
				setGraphic(null);
				return;
			}

			Label usernameLabel = new Label(item.username());
			usernameLabel.getStyleClass().add("user-item-username");

			Label nameLabel = new Label(item.fullName() + " (" + item.role() + ")");
			nameLabel.getStyleClass().add("user-item-name");

			VBox leftBox = new VBox(2, usernameLabel, nameLabel);

			Label dateLabel = new Label(item.requestDate().format(DATE_FORMAT));
			dateLabel.getStyleClass().add("user-item-date");

			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);

			HBox row = new HBox(10, leftBox, spacer, dateLabel);
			row.setFillHeight(true);

			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			setGraphic(row);
		}
	}
}
