package iuh.fit.gui.statistics;

import dto.SeatTypeRevenueRequest;
import dto.SeatTypeRevenueResponse;
import iuh.fit.service.UserClientService;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.chart.PieChart;
import javafx.scene.control.*;

public class RevenueBySeatTypeController {

    @FXML private TableView<SeatTypeRevenueResponse.SeatTypeRevenueDetail> table;

    @FXML private TableColumn<SeatTypeRevenueResponse.SeatTypeRevenueDetail, String> colType;
    @FXML private TableColumn<SeatTypeRevenueResponse.SeatTypeRevenueDetail, String> colCount;
    @FXML private TableColumn<SeatTypeRevenueResponse.SeatTypeRevenueDetail, String> colRevenue;

    @FXML private Label totalRevenue;
    @FXML private PieChart pieChart;

    private final UserClientService service = new UserClientService();

    @FXML
    public void initialize() {
        colType.setCellValueFactory(c ->
                new SimpleStringProperty(c.getValue().getSeatType().name())
        );

        colCount.setCellValueFactory(c ->
                new SimpleStringProperty(String.valueOf(c.getValue().getSeatNumber()))
        );

        colRevenue.setCellValueFactory(c ->
                new SimpleStringProperty(
                        MoneyUtils.formatVND(c.getValue().getRevenue())
                )
        );

        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        loadData();
    }

    private void loadData() {
        try {
            SeatTypeRevenueRequest req = new SeatTypeRevenueRequest();
            req.setManagerID("USER-002");
            SeatTypeRevenueResponse res = service.seatTypeRevenue(req);
            if (res == null) return;
            var list = res.getDetails();
            table.setItems(FXCollections.observableArrayList(list));
            totalRevenue.setText(MoneyUtils.formatVND(res.getTotalRevenue()));
            ObservableList<PieChart.Data> pieData = FXCollections.observableArrayList();

            for (var d : list) {
                pieData.add(new PieChart.Data(
                        d.getSeatType().name(),
                        d.getRevenue()
                ));
            }

            pieChart.setData(pieData);

        } catch (Exception e) {
            e.printStackTrace();
            totalRevenue.setText("Lỗi tải dữ liệu");
        }
    }

    @FXML
    private void onRefresh() {
        loadData();
    }
}