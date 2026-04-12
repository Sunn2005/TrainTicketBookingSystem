package iuh.fit;

import iuh.fit.constance.AppTheme;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.stage.Stage;

import java.io.IOException;

public class App extends Application {
    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader fxmlLoader = new FXMLLoader(App.class.getResource("/iuh/fit/gui/login/login-view.fxml"));
        Scene scene = new Scene(fxmlLoader.load(), 1366, 768);
        AppTheme.applyTo(scene);

        stage.setTitle("Train Ticket Socket Client");
        stage.setScene(scene);
        stage.show();
    }
}
