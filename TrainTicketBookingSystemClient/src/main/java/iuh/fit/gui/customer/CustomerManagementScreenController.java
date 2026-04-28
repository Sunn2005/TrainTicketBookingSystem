package iuh.fit.gui.customer;

import iuh.fit.service.CustomerClientService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
        import model.entity.Customer;
import model.entity.enums.CustomerType;

import java.time.LocalDate;
import java.util.List;
import java.util.stream.Collectors;

public class CustomerManagementScreenController {

    // ================= HEADER =================
    @FXML private Label totalCustomerLabel;
    @FXML private DatePicker fromDatePicker;
    @FXML private DatePicker toDatePicker;

    // ================= SEARCH =================
    @FXML private ComboBox<String> searchTypeCombo;
    @FXML private TextField keywordField;
    @FXML private Button searchBtn;
    @FXML private Button refreshBtn;

    // ================= TABLE =================
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colType;

    private final CustomerClientService customerService =
            new CustomerClientService();

    private final ObservableList<Customer> customerList =
            FXCollections.observableArrayList();

    // =========================================================
    @FXML
    private void initialize() {
        searchTypeCombo.setItems(FXCollections.observableArrayList(
                "Mã khách hàng",
                "Tên khách hàng",
                "CCCD"
        ));
        searchTypeCombo.setValue("Mã khách hàng");

        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());

        setupTable();
        loadCustomers();

        fromDatePicker.setOnAction(e -> loadByDate());
        toDatePicker.setOnAction(e -> loadByDate());
    }

    // =========================================================
    private void setupTable() {

        colId.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        safe(cell.getValue().getCustomerID())
                ));

        colName.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        safe(cell.getValue().getFullName())
                ));

        colType.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        convertType(cell.getValue().getCustomerType())
                ));

        customerTable.setItems(customerList);

        // Highlight selected row
        customerTable.setRowFactory(tv -> {
            TableRow<Customer> row = new TableRow<>();
            row.selectedProperty().addListener((obs, oldVal, selected) -> {
                if (selected) {
                    row.setStyle(
                            "-fx-background-color:#8d97d9;" +
                                    "-fx-text-fill:white;"
                    );
                } else {
                    row.setStyle("");
                }
            });
            return row;
        });
    }

    // =========================================================
    private void loadCustomers() {

        new Thread(() -> {
            List<Customer> customers = customerService.getAllCustomers();

            Platform.runLater(() -> {
                customerList.setAll(customers);
                totalCustomerLabel.setText(
                        "Tổng khách hàng: " + customers.size()
                );
            });

        }).start();
    }

    private void loadByDate() {
        LocalDate from = fromDatePicker.getValue();
        LocalDate to   = toDatePicker.getValue();

        if (from == null || to == null) return;
        if (from.isAfter(to)) return;

        new Thread(() -> {
            List<Customer> list = customerService.getCustomersBookedBetween(from, to);

            Platform.runLater(() -> {
                customerList.setAll(list);
                totalCustomerLabel.setText(
                        "Tổng khách hàng mới: " + list.size()
                );
            });
        }).start();
    }

    // =========================================================
    @FXML
    private void onSearch() {

        String keyword = keywordField.getText().trim();

        if (keyword.isEmpty()) {
            loadCustomers();
            return;
        }

        new Thread(() -> {

            List<Customer> customers = customerService.getAllCustomers();

            String searchType = searchTypeCombo.getValue();

            List<Customer> filtered = customers.stream()
                    .filter(c -> matchCustomer(c, searchType, keyword))
                    .collect(Collectors.toList());

            Platform.runLater(() -> {
                customerList.setAll(filtered);
                totalCustomerLabel.setText(
                        "Tổng khách hàng: " + filtered.size()
                );
            });

        }).start();
    }

    // =========================================================
    @FXML
    private void onRefresh() {
        keywordField.clear();
        loadCustomers();
    }

    // =========================================================
    private boolean matchCustomer(Customer c,
                                  String type,
                                  String keyword) {

        keyword = keyword.toLowerCase();

        return switch (type) {

            case "Mã khách hàng" ->
                    safe(c.getCustomerID())
                            .toLowerCase()
                            .contains(keyword);

            case "Tên khách hàng" ->
                    safe(c.getFullName())
                            .toLowerCase()
                            .contains(keyword);

            case "CCCD" ->
                    safe(c.getCustomerID())
                            .toLowerCase()
                            .contains(keyword);

            default -> false;
        };
    }

    // =========================================================
    private String convertType(CustomerType type) {

        if (type == null) return "—";

        return switch (type) {
            case ADULT -> "Người lớn";
            case STUDENT -> "Sinh viên";
            case CHILD -> "Trẻ em";
            case ELDERLY -> "Người cao tuổi";
        };
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}