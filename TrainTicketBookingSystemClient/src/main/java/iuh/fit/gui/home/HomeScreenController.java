package iuh.fit.gui.home;

import iuh.fit.App;
import iuh.fit.WindowSizing;
import iuh.fit.constance.AppTheme;
import iuh.fit.context.UserContext;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Scene;
import javafx.scene.control.Label;
import javafx.scene.layout.StackPane;
import javafx.stage.Stage;

import java.io.IOException;

public class HomeScreenController {
    @FXML
    private StackPane contentContainer;

    @FXML
    private HomeHeaderController headerController;

    @FXML
    private HomeSidebarController sidebarController;

    private static final String DEFAULT_ROUTE = HomeContentFactory.ROUTE_SELL_TICKET;

    @FXML
    private void initialize() {
        sidebarController.setRouteHandler(this::showContent);
        sidebarController.setLogoutHandler(this::onLogoutButtonClick);
        sidebarController.selectMenu(DEFAULT_ROUTE);

        showContent(DEFAULT_ROUTE);
        headerController.startClock();
    }

    private void onLogoutButtonClick() {
        headerController.stopClock();

        UserContext.getInstance().clear();

        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource("/iuh/fit/gui/login/login-view.fxml"));
            Scene loginScene = new Scene(loader.load());
            AppTheme.applyTo(loginScene);

            Stage stage = (Stage) contentContainer.getScene().getWindow();
            stage.setTitle("Train Ticket Socket Client");
            stage.setScene(loginScene);
            WindowSizing.applyHalfScreen(stage);
        } catch (IOException e) {
            contentContainer.getChildren().setAll(new Label("Khong the mo man hinh dang nhap."));
        }
    }

    private void showContent(String route) {
        contentContainer.getChildren().setAll(HomeContentFactory.createContent(route));
    }
}
