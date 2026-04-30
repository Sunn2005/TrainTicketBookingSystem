package iuh.fit.gui.scheduleandtrain;

import iuh.fit.App;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;

public class ScheduleTrainManagementController {

    @FXML
    private TabPane tabPane;

    @FXML
    private Tab scheduleTab;

    @FXML
    private Tab trainTab;

    @FXML
    private void initialize() {
        loadScheduleTab();
        loadTrainTab();
    }

    private void loadScheduleTab() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/iuh/fit/gui/schedule/schedule-management-view.fxml"));
            Node content = loader.load();
            scheduleTab.setContent(content);
        } catch (Exception e) {
            System.err.println("Error loading schedule tab: " + e.getMessage());
            e.printStackTrace();
        }
    }

    private void loadTrainTab() {
        try {
            FXMLLoader loader = new FXMLLoader(App.class.getResource(
                    "/iuh/fit/gui/train/train-management-view.fxml"));
            Node content = loader.load();
            trainTab.setContent(content);
        } catch (Exception e) {
            System.err.println("Error loading train tab: " + e.getMessage());
            e.printStackTrace();
        }
    }
}


