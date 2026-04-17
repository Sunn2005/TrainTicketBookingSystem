package iuh.fit;

import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

public final class WindowSizing {
    private WindowSizing() {
    }

    public static void applyHalfScreen(Stage stage) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();

        stage.setMaximized(false);
        stage.setWidth(bounds.getWidth() * 0.75);
        stage.setHeight(bounds.getHeight() * 0.75);
        stage.centerOnScreen();
    }

    public static void applyFullScreen(Stage stage) {
        stage.setMaximized(true);
    }
}

