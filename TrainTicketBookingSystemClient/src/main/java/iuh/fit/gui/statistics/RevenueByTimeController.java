package iuh.fit.gui.statistics;

import dto.RevenueStatisticsRequest;
import dto.RevenueStatisticsResponse;
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
import java.util.Map;

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

    @FXML
    public void initialize() {

        colDate.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getKey().toString())
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

    @FXML
    public void onLoad() {

        RevenueStatisticsRequest req = new RevenueStatisticsRequest();
        req.setManagerID("USER-002"); // manager của bạn
        req.setStartDate(fromDate.getValue());
        req.setEndDate(toDate.getValue());
        req.setStatisticType(StatisticType.DAY);

        RevenueStatisticsResponse res = service.revenueStatistics(req);

        Map<LocalDate, Double> map = res.getResponseMap();

        // set table
        table.setItems(FXCollections.observableArrayList(map.entrySet()));

        // tính tổng
        double total = map.values().stream().mapToDouble(Double::doubleValue).sum();

        totalRevenue.setText(MoneyUtils.formatVND(total));
        // trung bình
        double avg = map.isEmpty() ? 0 : total / map.size();
        avgRevenue.setText(MoneyUtils.formatVND(avg));

        lineChart.getData().clear();

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        series.setName("Doanh thu");

        map.entrySet().stream()
                .sorted(Map.Entry.comparingByKey()) // sort theo ngày
                .forEach(entry -> {
                    series.getData().add(
                            new XYChart.Data<>(entry.getKey().toString(), entry.getValue())
                    );
                });

        lineChart.getData().add(series);
    }
}