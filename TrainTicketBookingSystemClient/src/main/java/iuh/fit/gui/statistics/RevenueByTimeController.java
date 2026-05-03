package iuh.fit.gui.statistics;

import dto.RevenueStatisticsRequest;
import dto.RevenueStatisticsResponse;
import iuh.fit.context.UserContext;
import iuh.fit.service.UserClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.LineChart;
import javafx.scene.chart.XYChart;
import javafx.scene.control.*;
import model.entity.enums.StatisticType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Map;
import javafx.util.StringConverter;

public class RevenueByTimeController {

    @FXML private DatePicker fromDate;
    @FXML private DatePicker toDate;

    @FXML private Label totalRevenue;
    @FXML private Label avgRevenue;

    @FXML private TableView<Map.Entry<LocalDate, Double>> table;
    @FXML private TableColumn<Map.Entry<LocalDate, Double>, String> colDate;
    @FXML private TableColumn<Map.Entry<LocalDate, Double>, String> colRevenue;
    @FXML private LineChart<String, Number> lineChart;

    private final UserClientService service = new UserClientService();

        private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    public void initialize() {

        configureDatePicker(fromDate);
        configureDatePicker(toDate);

        colDate.setCellValueFactory(c ->
            new SimpleStringProperty(
                c.getValue().getKey() != null
                    ? DATE_FMT.format(c.getValue().getKey())
                    : ""
            )
        );

        colRevenue.setCellValueFactory(c ->
                new SimpleStringProperty(
                        MoneyUtils.formatVND(c.getValue().getValue())
                )
        );

        lineChart.setAnimated(false); // tránh lag
        lineChart.setLegendVisible(false);

        CategoryAxis xAxis = (CategoryAxis) lineChart.getXAxis();
        xAxis.setTickLabelRotation(45); // xoay chữ
        xAxis.setTickLabelGap(10);      // khoảng cách

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_ALL_COLUMNS);

    }

    private void configureDatePicker(DatePicker datePicker) {
        datePicker.setConverter(new StringConverter<LocalDate>() {
            @Override
            public String toString(LocalDate date) {
                return date != null ? DATE_FMT.format(date) : "";
            }

            @Override
            public LocalDate fromString(String value) {
                return (value == null || value.trim().isEmpty())
                        ? null
                        : LocalDate.parse(value.trim(), DATE_FMT);
            }
        });
        datePicker.setPromptText("dd/MM/yyyy");
    }

    @FXML
    public void onLoad() {

        if (!isManager()) {
            showNoPermission();
            return;
        }

        RevenueStatisticsRequest req = new RevenueStatisticsRequest();
        req.setManagerID(UserContext.getInstance().getUserID());
        req.setStartDate(fromDate.getValue());
        req.setEndDate(toDate.getValue());

        RevenueStatisticsResponse res = service.revenueStatistics(req);

        Map<LocalDate, Double> map = res.getResponseMap();

        table.setItems(FXCollections.observableArrayList(map.entrySet()));

        double total = map.values().stream().mapToDouble(Double::doubleValue).sum();
        totalRevenue.setText(MoneyUtils.formatVND(total));

        double avg = map.isEmpty() ? 0 : total / map.size();
        avgRevenue.setText(MoneyUtils.formatVND(avg));

        lineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .forEach(e ->
                    series.getData().add(new XYChart.Data<>(DATE_FMT.format(e.getKey()), e.getValue()))
                );

        lineChart.getData().add(series);
    }

    private boolean isManager() {
        String role = UserContext.getInstance().getRole();
        return "ROLE-002".equals(role) || "MANAGER".equalsIgnoreCase(role);
    }

    private void showNoPermission() {
        Alert a = new Alert(Alert.AlertType.WARNING);
        a.setTitle("Không có quyền");
        a.setContentText("Chỉ MANAGER được xem thống kê");
        a.showAndWait();
    }
}