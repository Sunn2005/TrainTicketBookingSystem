package iuh.fit.gui.user.create_account;

import dto.ActionResponse;
import iuh.fit.service.UserClientService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.entity.User;
import model.entity.enums.UserStatus;

import java.util.List;

public class CreateAccountScreenController {

	// ── Form ──────────────────────────────────────────────────────────
	@FXML private TextField        usernameField;
	@FXML private Label            usernameError;
	@FXML private PasswordField    passwordField;
	@FXML private Label            passwordError;
	@FXML private PasswordField    confirmPasswordField;
	@FXML private Label            confirmPasswordError;
	@FXML private TextField        fullNameField;
	@FXML private Label            fullNameError;
	@FXML private TextField        emailField;
	@FXML private Label            emailError;
	@FXML private ComboBox<String> roleCombo;
	@FXML private ComboBox<String> statusCombo;
	@FXML private Label            formStatusLabel;
	@FXML private Button           createBtn;

	// ── Table ─────────────────────────────────────────────────────────
	@FXML private TableView<User>           userTable;
	@FXML private TableColumn<User, String> colUsername;
	@FXML private TableColumn<User, String> colFullName;
	@FXML private TableColumn<User, String> colEmail;
	@FXML private TableColumn<User, String> colRole;
	@FXML private TableColumn<User, String> colStatus;
	@FXML private TableColumn<User, String> colAction;
	@FXML private Label tableStatusLabel;

	private final UserClientService        userService = new UserClientService();
	private final ObservableList<User>     userList    = FXCollections.observableArrayList();

	// ── Regex ─────────────────────────────────────────────────────────
	private static final String REGEX_USERNAME = "^[a-zA-Z0-9_]{4,20}$";
	private static final String REGEX_FULLNAME = "^[\\p{L} ]+$";
	private static final String REGEX_EMAIL    = "^[\\w.+-]+@[\\w.-]+\\.[a-zA-Z]{2,}$";
	private static final String REGEX_PASSWORD = "^(?=.*[a-zA-Z])(?=.*\\d).{6,}$";

	// ─────────────────────────────────────────────────────────────────
	@FXML
	private void initialize() {
		roleCombo.setItems(FXCollections.observableArrayList("EMPLOYEE", "MANAGER"));
		roleCombo.setValue("EMPLOYEE");
		statusCombo.setItems(FXCollections.observableArrayList("ACTIVE", "INACTIVE"));
		statusCombo.setValue("ACTIVE");

		setupTable();
		loadUsers();

		usernameField.focusedProperty().addListener((o, old, focused) -> {
			if (!focused) validateUsername();
		});
		fullNameField.focusedProperty().addListener((o, old, focused) -> {
			if (!focused) validateFullName();
		});
		emailField.focusedProperty().addListener((o, old, focused) -> {
			if (!focused) validateEmail();
		});
		passwordField.focusedProperty().addListener((o, old, focused) -> {
			if (!focused) validatePassword();
		});
		confirmPasswordField.focusedProperty().addListener((o, old, focused) -> {
			if (!focused) validateConfirmPassword();
		});
	}

	// ── Setup Table ───────────────────────────────────────────────────
	private void setupTable() {
		colUsername.setCellValueFactory(c ->
				new SimpleStringProperty(c.getValue().getUserName()));
		colFullName.setCellValueFactory(c ->
				new SimpleStringProperty(c.getValue().getFullName()));
		colEmail.setCellValueFactory(c ->
				new SimpleStringProperty(c.getValue().getEmail() != null
						? c.getValue().getEmail() : "—"));
		colRole.setCellValueFactory(c ->
				new SimpleStringProperty(c.getValue().getRole() != null
						? c.getValue().getRole().getRoleName() : "—"));
		colStatus.setCellValueFactory(c ->
				new SimpleStringProperty(c.getValue().getUserStatus() != null
						? c.getValue().getUserStatus().name() : "—"));

		// Màu status
		colStatus.setCellFactory(col -> new TableCell<>() {
			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty || item == null) { setText(null); setStyle(""); return; }
				setText(item);
				setStyle("ACTIVE".equals(item)
						? "-fx-text-fill:#16a34a;-fx-font-weight:bold;"
						: "-fx-text-fill:#dc2626;-fx-font-weight:bold;");
			}
		});

		// Cột hành động
		colAction.setCellFactory(col -> new TableCell<>() {
			private final Button btn = new Button();
			{
				btn.setStyle("-fx-font-size:11px;-fx-cursor:hand;"
						+ "-fx-padding:4 10 4 10;-fx-background-radius:4;");
				btn.setOnAction(e -> {
					User user = getTableView().getItems().get(getIndex());
					toggleStatus(user);
				});
			}

			@Override
			protected void updateItem(String item, boolean empty) {
				super.updateItem(item, empty);
				if (empty) { setGraphic(null); return; }
				User user = getTableView().getItems().get(getIndex());
				boolean isActive = user.getUserStatus() == UserStatus.ACTIVE;
				btn.setText(isActive ? "Vô hiệu hóa" : "Kích hoạt");
				btn.setStyle("-fx-font-size:11px;-fx-cursor:hand;"
						+ "-fx-padding:4 10 4 10;-fx-background-radius:4;"
						+ (isActive
						? "-fx-background-color:#fee2e2;-fx-text-fill:#dc2626;"
						+ "-fx-border-color:#fca5a5;-fx-border-width:1;"
						: "-fx-background-color:#dcfce7;-fx-text-fill:#16a34a;"
						+ "-fx-border-color:#86efac;-fx-border-width:1;"));
				setGraphic(btn);
			}
		});

		userTable.setItems(userList);
	}

	private void loadUsers() {
		new Thread(() -> {
			List<User> users = userService.getAllUsers();
			Platform.runLater(() -> {
				// Lọc bỏ null
				userList.setAll(users.stream()
						.filter(u -> u != null && u.getUserName() != null)
						.toList());
			});
		}).start();
	}
	private void toggleStatus(User user) {
		UserStatus newStatus = user.getUserStatus() == UserStatus.ACTIVE
				? UserStatus.INACTIVE : UserStatus.ACTIVE;

		new Thread(() -> {
			ActionResponse resp = userService.changeStatus(user.getUserID(), newStatus);
			Platform.runLater(() -> {
				if (resp.isSuccess()) {
					user.setUserStatus(newStatus);
					userTable.refresh();
					// Dùng Alert thay vì label để không nhầm với form
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Cập nhật thành công");
					alert.setHeaderText(null);
					alert.setContentText("Đã "
							+ (newStatus == UserStatus.ACTIVE ? "kích hoạt" : "vô hiệu hóa")
							+ " tài khoản: " + user.getUserName());
					alert.showAndWait();
				} else {
					Alert alert = new Alert(Alert.AlertType.ERROR);
					alert.setTitle("Lỗi");
					alert.setHeaderText(null);
					alert.setContentText(resp.getMessage());
					alert.showAndWait();
				}
			});
		}).start();
	}

	// ── Validate ──────────────────────────────────────────────────────
	private boolean validateUsername() {
		String val = usernameField.getText().trim();
		if (val.isEmpty()) {
			setError(usernameField, usernameError,
					"Không được để trống. VD: nv006");
			return false;
		}
		if (!val.matches(REGEX_USERNAME)) {
			setError(usernameField, usernameError,
					"4-20 ký tự, chỉ gồm chữ cái, số và dấu _ (VD: nv006)");
			return false;
		}
		clearError(usernameField, usernameError);
		return true;
	}

	private boolean validateFullName() {
		String val = fullNameField.getText().trim();
		if (val.isEmpty()) {
			setError(fullNameField, fullNameError,
					"Không được để trống. VD: Nguyễn Văn A");
			return false;
		}
		if (!val.matches(REGEX_FULLNAME)) {
			setError(fullNameField, fullNameError,
					"Chỉ được chứa chữ cái và khoảng trắng. VD: Nguyễn Văn A");
			return false;
		}
		clearError(fullNameField, fullNameError);
		return true;
	}

	private boolean validateEmail() {
		String val = emailField.getText().trim();
		if (val.isEmpty()) {
			setError(emailField, emailError,
					"Không được để trống. VD: example@gmail.com");
			return false;
		}
		if (!val.matches(REGEX_EMAIL)) {
			setError(emailField, emailError,
					"Sai định dạng email. VD: example@gmail.com");
			return false;
		}
		clearError(emailField, emailError);
		return true;
	}

	private boolean validatePassword() {
		String val = passwordField.getText().trim();
		if (val.isEmpty()) {
			setError(passwordField, passwordError, "Không được để trống");
			return false;
		}
		if (!val.matches(REGEX_PASSWORD)) {
			setError(passwordField, passwordError,
					"Tối thiểu 6 ký tự, phải có cả chữ cái và số. VD: abc123");
			return false;
		}
		clearError(passwordField, passwordError);
		return true;
	}

	private boolean validateConfirmPassword() {
		String val  = confirmPasswordField.getText().trim();
		String pass = passwordField.getText().trim();
		if (val.isEmpty()) {
			setError(confirmPasswordField, confirmPasswordError,
					"Vui lòng xác nhận mật khẩu");
			return false;
		}
		if (!val.equals(pass)) {
			setError(confirmPasswordField, confirmPasswordError,
					"Mật khẩu xác nhận không khớp");
			return false;
		}
		clearError(confirmPasswordField, confirmPasswordError);
		return true;
	}

	// ── Tạo tài khoản ────────────────────────────────────────────────
	@FXML
	private void onCreate() {
		boolean valid = validateUsername()
				& validateFullName()
				& validateEmail()
				& validatePassword()
				& validateConfirmPassword();

		if (!valid) return;

		String username  = usernameField.getText().trim();
		String password  = passwordField.getText().trim();
		String fullName  = fullNameField.getText().trim();
		String email     = emailField.getText().trim();
		String role      = roleCombo.getValue();
		String statusStr = statusCombo.getValue();

		String roleId = switch (role) {
			case "MANAGER"  -> "ROLE-002";
			case "EMPLOYEE" -> "ROLE-003";
			default         -> "ROLE-003";
		};

		createBtn.setDisable(true);
		createBtn.setText("Đang tạo...");
		formStatusLabel.setText("");

		new Thread(() -> {
			ActionResponse resp = userService.createUser(
					username, password, fullName, email, roleId);

			if (resp.isSuccess() && "INACTIVE".equals(statusStr)) {
				String msg = resp.getMessage();
				if (msg != null && msg.contains("ID: ")) {
					String userId = msg.substring(msg.indexOf("ID: ") + 4).trim();
					userService.changeStatus(userId, UserStatus.INACTIVE);
				}
			}

			Platform.runLater(() -> {
				createBtn.setDisable(false);
				createBtn.setText("Tạo tài khoản");

				if (resp.isSuccess()) {
					// ── Alert thông báo thành công ────────────────
					Alert alert = new Alert(Alert.AlertType.INFORMATION);
					alert.setTitle("Tạo tài khoản thành công");
					alert.setHeaderText(null);
					alert.setContentText(
							"✔ Tài khoản [" + username + "] đã được tạo thành công!\n"
									+ "Vai trò: " + role + "\n"
									+ "Trạng thái: " + statusStr);
					alert.showAndWait();

					showFormSuccess("✔ Tạo tài khoản [" + username + "] thành công!");
					clearForm();
					loadUsers();
				} else {
					showFormError(resp.getMessage());
				}
			});
		}).start();
	}

	@FXML
	private void onClear() { clearForm(); }

	@FXML
	private void onRefresh() { loadUsers(); }

	// ── Helpers ───────────────────────────────────────────────────────
	private void setError(Control field, Label errorLabel, String msg) {
		field.setStyle("-fx-border-color:#dc2626;-fx-border-width:1.5;"
				+ "-fx-border-radius:4;-fx-background-radius:4;"
				+ "-fx-background-color:#fff5f5;");
		errorLabel.setText("⚠ " + msg);
		errorLabel.setVisible(true);
		errorLabel.setManaged(true);
	}

	private void clearError(Control field, Label errorLabel) {
		field.setStyle("");
		errorLabel.setText("");
		errorLabel.setVisible(false);
		errorLabel.setManaged(false);
	}

	private void showFormError(String msg) {
		formStatusLabel.setText("⚠ " + msg);
		formStatusLabel.setStyle("-fx-text-fill:#dc2626;-fx-font-size:12px;");
	}

	private void showFormSuccess(String msg) {
		formStatusLabel.setText(msg);
		formStatusLabel.setStyle(
				"-fx-text-fill:#16a34a;" +
						"-fx-font-size:13px;" +
						"-fx-font-weight:bold;" +
						"-fx-background-color:#dcfce7;" +
						"-fx-background-radius:4;" +
						"-fx-padding:8 12 8 12;" +
						"-fx-border-color:#86efac;" +
						"-fx-border-radius:4;" +
						"-fx-border-width:1;");
		formStatusLabel.setVisible(true);
		formStatusLabel.setManaged(true);
	}

	private void clearForm() {
		usernameField.clear();
		fullNameField.clear();
		emailField.clear();
		passwordField.clear();
		confirmPasswordField.clear();
		roleCombo.setValue("EMPLOYEE");
		statusCombo.setValue("ACTIVE");
		formStatusLabel.setText("");
		formStatusLabel.setVisible(false);
		formStatusLabel.setManaged(false);
		clearError(usernameField, usernameError);
		clearError(fullNameField, fullNameError);
		clearError(emailField, emailError);
		clearError(passwordField, passwordError);
		clearError(confirmPasswordField, confirmPasswordError);
	}
}