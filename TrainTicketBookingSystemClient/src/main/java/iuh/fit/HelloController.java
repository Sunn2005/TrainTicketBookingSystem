package iuh.fit;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;

public class HelloController {
    private static final String HOST = "127.0.0.1";
    private static final int PORT = 9999;

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
    private TextField passwordField;

    @FXML
    protected void onLoginButtonClick() {
        String username = usernameField.getText();
        String password = passwordField.getText();

        if (username == null || username.isBlank() || password == null || password.isBlank()) {
            welcomeText.setText("Nhap day du username va password.");
            return;
        }

        String command = "LOGIN|" + username.trim() + "|" + password.trim();
        try {
            String rawResponse = socketClient.sendRawMessage(HOST, PORT, command);
            welcomeText.setText(parseLoginResponse(rawResponse));
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

    private String parseLoginResponse(String rawResponse) {
        if (rawResponse == null || rawResponse.isBlank()) {
            return "Khong nhan duoc phan hoi login tu server.";
        }

        String[] parts = rawResponse.split("\\|");
        if (parts.length >= 5 && "LOGIN_SUCCESS".equalsIgnoreCase(parts[0])) {
            return "Login thanh cong: userID=" + parts[1]
                    + ", fullName=" + parts[2]
                    + ", role=" + parts[3]
                    + ", message=" + parts[4];
        }
        if (parts.length >= 2 && "LOGIN_FAIL".equalsIgnoreCase(parts[0])) {
            return "Login that bai: " + parts[1];
        }
        return "Phan hoi login khong hop le: " + rawResponse;
    }
}
