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
import dto.PasswordResetRequestDTO;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.stream.Collectors;
import model.entity.User;

public class UpdatePasswordScreenController {
	@FXML
	private ListView<PendingUser> pendingUsersListView;

	@FXML
	private Label selectedUserIdLabel;

	@FXML
	private Label selectedFullNameLabel;

	@FXML
	private Label selectedRoleLabel;

	@FXML
	private Label selectedEmailLabel;

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
					showSuccess("Đã cập nhật mật khẩu cho " + selectedUser.userName() + " thành công.");
					confirmPasswordField.clear();
					newPasswordField.clear();
					showSuccessDialog(selectedUser.userName());
					loadPendingUsers();
				} else {
					String message = response != null ? response.getMessage() : "Không thể cập nhật mật khẩu.";
					showError(message);
				}
			});
		}).start();
	}

	private void loadPendingUsers() {
		new Thread(() -> {
			List<PasswordResetRequestDTO> requests = userService.getPendingPasswordResets();
			List<User> users = userService.getAllUsers();
			Map<String, String> usernameById = users.stream()
					.filter(Objects::nonNull)
					.filter(u -> u.getUserID() != null && u.getUserName() != null)
					.collect(Collectors.toMap(User::getUserID, User::getUserName, (a, b) -> a));
			List<PendingUser> mappedUsers = requests.stream()
					.filter(Objects::nonNull)
					.filter(this::isEmployeeOrManager)
					.map(req -> toPendingUser(req, usernameById))
					.collect(Collectors.toList());

			Platform.runLater(() -> {
				pendingUsers.setAll(mappedUsers);
				if (mappedUsers.isEmpty()) {
					showError("Không có yêu cầu cấp lại mật khẩu.");
					clearSelectedUserDetails();
					setFormEnabled(false);
				} else {
					clearStatus();
				}
			});
		}).start();
	}

	private boolean isEmployeeOrManager(PasswordResetRequestDTO request) {
		if (request.getRole() == null) {
			return false;
		}
		String roleName = request.getRole().toUpperCase(Locale.ROOT);
		return "EMPLOYEE".equals(roleName) || "MANAGER".equals(roleName);
	}

	private PendingUser toPendingUser(PasswordResetRequestDTO request, Map<String, String> usernameById) {
		String userId = request.getUserID() == null ? "" : request.getUserID();
		String userName = usernameById.getOrDefault(userId, userId);
		String fullName = request.getFullName() == null ? "" : request.getFullName();
		String roleName = request.getRole() == null ? "" : request.getRole();
		String email = request.getEmail() == null ? "" : request.getEmail();
		return new PendingUser(userId, userName, fullName, roleName, email);
	}

	private void setupListView() {
		pendingUsersListView.setItems(pendingUsers);
		pendingUsersListView.setCellFactory(listView -> new PendingUserCell());
		pendingUsersListView.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, selectedUser) -> {
			if (selectedUser == null) {
				clearSelectedUserDetails();
				setFormEnabled(false);
				return;
			}
			selectedUserIdLabel.setText(selectedUser.userName());
			selectedFullNameLabel.setText(selectedUser.fullName());
			selectedRoleLabel.setText(selectedUser.role());
			selectedEmailLabel.setText(selectedUser.email());
			setFormEnabled(true);
			clearStatus();
			newPasswordField.clear();
			confirmPasswordField.clear();
		});
	}

	private void clearSelectedUserDetails() {
		selectedUserIdLabel.setText("—");
		selectedFullNameLabel.setText("—");
		selectedRoleLabel.setText("—");
		selectedEmailLabel.setText("—");
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

	private record PendingUser(String userId, String userName, String fullName, String role, String email) {
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

			Label usernameLabel = new Label(item.userName());
			usernameLabel.getStyleClass().add("user-item-username");

			Label nameLabel = new Label(item.fullName() + " (" + item.role() + ")");
			nameLabel.getStyleClass().add("user-item-name");

			VBox leftBox = new VBox(2, usernameLabel, nameLabel);

			Label metaLabel = new Label(item.email());
			metaLabel.getStyleClass().add("user-item-meta");

			Region spacer = new Region();
			HBox.setHgrow(spacer, Priority.ALWAYS);

			HBox row = new HBox(10, leftBox, spacer, metaLabel);
			row.setFillHeight(true);

			setContentDisplay(ContentDisplay.GRAPHIC_ONLY);
			setGraphic(row);
		}
	}
}
