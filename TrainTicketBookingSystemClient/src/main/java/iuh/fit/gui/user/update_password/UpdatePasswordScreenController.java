package iuh.fit.gui.user.update_password;

import dto.ActionResponse;
import iuh.fit.service.UserClientService;
import javafx.application.Platform;
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
import model.entity.User;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

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

	private final UserClientService userService = new UserClientService();
	private final ObservableList<PendingUser> pendingUsers = FXCollections.observableArrayList();

	@FXML
	private void initialize() {
		setupListView();
		loadPendingUsers();
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

		new Thread(() -> {
			ActionResponse response = userService.resetPassword(selectedUser.userId(), newPassword);
			Platform.runLater(() -> {
				if (response != null && response.isSuccess()) {
					showSuccess("Đã cập nhật mật khẩu cho " + selectedUser.username() + " thành công.");
					confirmPasswordField.clear();
					newPasswordField.clear();
					showSuccessDialog(selectedUser.username());
				} else {
					String message = response != null ? response.getMessage() : "Không thể cập nhật mật khẩu.";
					showError(message);
				}
			});
		}).start();
	}

	private void loadPendingUsers() {
		new Thread(() -> {
			List<User> users = userService.getAllUsers();
			List<PendingUser> mappedUsers = users.stream()
					.filter(Objects::nonNull)
					.filter(this::isEmployeeOrManager)
					.map(this::toPendingUser)
					.collect(Collectors.toList());

			Platform.runLater(() -> {
				pendingUsers.setAll(mappedUsers);
				if (mappedUsers.isEmpty()) {
					showError("Không có tài khoản Employee/Manager để hiển thị.");
				} else {
					clearStatus();
				}
			});
		}).start();
	}

	private boolean isEmployeeOrManager(User user) {
		if (user.getRole() == null || user.getRole().getRoleName() == null) {
			return false;
		}
		String roleName = user.getRole().getRoleName().toUpperCase(Locale.ROOT);
		return "EMPLOYEE".equals(roleName) || "MANAGER".equals(roleName);
	}

	private PendingUser toPendingUser(User user) {
		String username = user.getUserName() == null ? "" : user.getUserName();
		String fullName = user.getFullName() == null ? "" : user.getFullName();
		String roleName = user.getRole() != null && user.getRole().getRoleName() != null ? user.getRole().getRoleName() : "";
		LocalDate requestDate = user.getCreateDate() != null ? user.getCreateDate().toLocalDate() : LocalDate.now();
		return new PendingUser(user.getUserID(), username, fullName, roleName, requestDate);
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

	private record PendingUser(String userId, String username, String fullName, String role, LocalDate requestDate) {
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
