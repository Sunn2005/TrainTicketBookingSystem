package iuh.fit.gui.login;

import iuh.fit.App;
import iuh.fit.constance.AppTheme;
import iuh.fit.context.UserContext;
import iuh.fit.socketconfig.SocketClient;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.control.PasswordField;
import javafx.scene.control.TextField;
import javafx.stage.Stage;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class LoginScreen {
    private static final String HOST = SocketClient.HOST;
    private static final int PORT = SocketClient.PORT;

    private final SocketClient socketClient = new SocketClient();

    @FXML
    private Label welcomeText;

    @FXML
    private TextField messageField;

    @FXML
    private TextField filePathField;

    @FXML
    private TextField usernameField;

    @FXML
    private PasswordField passwordField;

    @FXML
    protected void onLoginButtonClick() {
        //String username = usernameField.getText();
        String username = "QL001";
        //String password = passwordField.getText();
        String password = "QL001@";

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            welcomeText.setText("Nhap day du username va password.");
            return;
        }

        String command = "LOGIN|" + username.trim() + "|" + password.trim();
        try {
            String rawResponse = socketClient.sendRawMessage(HOST, PORT, command);
            LoginResult loginResult = parseLoginResponse(rawResponse);

            if (loginResult.success()) {
                UserContext.getInstance().setUser(loginResult.userID(), loginResult.fullName(), loginResult.role());
                openHomeScreen();
                return;
            }

            welcomeText.setText(loginResult.message());
        } catch (IOException e) {
            welcomeText.setText("Khong the ket noi server tai " + HOST + ":" + PORT);
        }
    }

    @FXML
    protected void onSendButtonClick() {
        String message = messageField.getText();
        if (message == null || message.isBlank()) {
            welcomeText.setText("Nhap message de gui den server.");
            return;
        }

        try {
            String serverResponse = socketClient.sendMessage(HOST, PORT, message.trim());
            welcomeText.setText(serverResponse);
        } catch (IOException e) {
            welcomeText.setText("Khong the ket noi server tai " + HOST + ":" + PORT);
        }
    }

    @FXML
    protected void onSendFileButtonClick() {
        String filePath = filePathField.getText();
        if (filePath == null || filePath.isBlank()) {
            welcomeText.setText("Nhap duong dan file .txt.");
            return;
        }

        try {
            List<String> lines = Files.readAllLines(Path.of(filePath.trim()));
            List<String> messages = lines.stream()
                    .map(String::trim)
                    .filter(line -> !line.isEmpty())
                    .toList();

            if (messages.isEmpty()) {
                welcomeText.setText("File khong co message hop le.");
                return;
            }

            List<String> responses = socketClient.sendMessages(HOST, PORT, messages);
            welcomeText.setText("Da gui " + messages.size() + " message. Phan hoi cuoi: " + responses.getLast());
        } catch (IOException e) {
            welcomeText.setText("Khong doc duoc file hoac khong the ket noi server.");
        }
    }

    private void openHomeScreen() throws IOException {
        FXMLLoader loader = new FXMLLoader(App.class.getResource("/iuh/fit/gui/home/home-view.fxml"));
        Scene homeScene = new Scene(loader.load(), 1366, 768);
        AppTheme.applyTo(homeScene);

        Stage stage = (Stage) usernameField.getScene().getWindow();
        stage.setTitle("Train Ticket Booking - Home");
        stage.setScene(homeScene);
    }

    private LoginResult parseLoginResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return new LoginResult(false, "", "", "", "Khong nhan duoc phan hoi login tu server.");
        }

        String[] parts = rawResponse.split("\\|");
        if (parts.length >= 5 && "LOGIN_SUCCESS".equalsIgnoreCase(parts[0])) {
            return new LoginResult(true, parts[1], parts[2], parts[3], "Login thanh cong");
        }
        if (parts.length >= 2 && "LOGIN_FAIL".equalsIgnoreCase(parts[0])) {
            return new LoginResult(false, "", "", "", "Login that bai: " + parts[1]);
        }
        return new LoginResult(false, "", "", "", "Phan hoi login khong hop le: " + rawResponse);
    }

    private record LoginResult(boolean success, String userID, String fullName, String role, String message) {
    }
}
