package iuh.fit.gui.statistics;

import dto.ScheduleStatisticsRequest;
import dto.ScheduleStatisticsResponse;
import iuh.fit.context.UserContext;
import iuh.fit.service.UserClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;

import java.util.List;
import java.util.stream.Collectors;

public class RevenueByScheduleController {

    @FXML private TableView<ScheduleStatisticsResponse.ScheduleStatisticDetail> table;
    @FXML private TableColumn<ScheduleStatisticsResponse.ScheduleStatisticDetail, String> colId;
    @FXML private TableColumn<ScheduleStatisticsResponse.ScheduleStatisticDetail, String> colRoute;
    @FXML private TableColumn<ScheduleStatisticsResponse.ScheduleStatisticDetail, String> colTotal;
    @FXML private TableColumn<ScheduleStatisticsResponse.ScheduleStatisticDetail, String> colEmpty;
    @FXML private TableColumn<ScheduleStatisticsResponse.ScheduleStatisticDetail, String> colRevenue;

    @FXML private BarChart<String, Number> barChart;
    @FXML private ComboBox<String> cbRoute;
    @FXML private Label lblTotalRevenue;

    private final UserClientService service = new UserClientService();
    private List<ScheduleStatisticsResponse.ScheduleStatisticDetail> allData;

    @FXML
    public void initialize() {

        if (!isManager()) {
            showNoPermission();
            return;
        }

        colId.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getScheduleId()));
        colRoute.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getRouteName()));
        colTotal.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getTotalSeats())));
        colEmpty.setCellValueFactory(c -> new SimpleStringProperty(String.valueOf(c.getValue().getAvailableSeats())));
        colRevenue.setCellValueFactory(c ->
                new SimpleStringProperty(MoneyUtils.formatVND(c.getValue().getTotalRevenue()))
        );

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);
        barChart.setAnimated(false);
        barChart.setLegendVisible(false);

        cbRoute.setOnAction(e -> filter());

        onLoad();
    }

    public void onLoad() {

        if (!isManager()) return;

        ScheduleStatisticsRequest req = new ScheduleStatisticsRequest();
        req.setManagerID(UserContext.getInstance().getUserID());

        ScheduleStatisticsResponse res = service.scheduleStatistics(req);

        allData = res.getDetails();

        loadRoutes();
        applyData(allData);
    }

    private void loadRoutes() {
        cbRoute.getItems().clear();
        cbRoute.getItems().add("Tất cả");

        for (var d : allData) {
            if (!cbRoute.getItems().contains(d.getRouteName())) {
                cbRoute.getItems().add(d.getRouteName());
            }
        }

        cbRoute.getSelectionModel().selectFirst();
    }

    private void filter() {
        if (allData == null) return;

        String route = cbRoute.getValue();

        List<ScheduleStatisticsResponse.ScheduleStatisticDetail> filtered =
                allData.stream()
                        .filter(d -> route == null
                                || route.equals("Tất cả")
                                || d.getRouteName().equals(route))
                        .collect(Collectors.toList());

        applyData(filtered);
    }

    private void applyData(List<ScheduleStatisticsResponse.ScheduleStatisticDetail> data) {

        table.setItems(FXCollections.observableArrayList(data));

        barChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        double total = 0;

        for (var d : data) {
            series.getData().add(new XYChart.Data<>(d.getScheduleId(), d.getTotalRevenue()));
            total += d.getTotalRevenue();
        }

        barChart.getData().add(series);
        lblTotalRevenue.setText(MoneyUtils.formatVND(total));
    }

    private boolean isManager() {
        String role = UserContext.getInstance().getRole();
        return "ROLE-002".equals(role) || "MANAGER".equalsIgnoreCase(role);
    }

    private void showNoPermission() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Không có quyền");
        a.setHeaderText(null);
        a.setContentText("Chỉ MANAGER (ROLE-002) được xem thống kê");
        a.showAndWait();
    }
}