package iuh.fit.gui.customer;

import iuh.fit.service.CustomerClientService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import model.entity.Customer;
import model.entity.Ticket;
import model.entity.enums.CustomerType;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.stream.Collectors;
import javafx.util.StringConverter;

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
    @FXML private Label customerTypeStatsLabel;


    // ================= TABLE =================
    @FXML private TableView<Customer> customerTable;
    @FXML private TableColumn<Customer, String> colId;
    @FXML private TableColumn<Customer, String> colName;
    @FXML private TableColumn<Customer, String> colType;

    @FXML private TableView<Ticket> ticketTable;
    @FXML private TableColumn<Ticket, String> colTicketId;
    @FXML private TableColumn<Ticket, String> colSchedule;
    @FXML private TableColumn<Ticket, String> colSeat;
    @FXML private TableColumn<Ticket, String> colPrice;
    @FXML private TableColumn<Ticket, String> colStatus;


    private final ObservableList<Ticket> ticketList =
            FXCollections.observableArrayList();

    private final CustomerClientService customerService =
            new CustomerClientService();

    private final ObservableList<Customer> customerList =
            FXCollections.observableArrayList();

        private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy");

    @FXML
    private void initialize() {
        searchTypeCombo.setItems(FXCollections.observableArrayList(
                "Mã khách hàng",
                "Tên khách hàng",
                "CCCD"
        ));
        searchTypeCombo.setValue("Mã khách hàng");

        configureDatePicker(fromDatePicker);
        configureDatePicker(toDatePicker);
        fromDatePicker.setValue(LocalDate.now().minusMonths(1));
        toDatePicker.setValue(LocalDate.now());

        setupTable();
        loadCustomers();

        customerTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);
        ticketTable.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY);

        fromDatePicker.setOnAction(e -> loadByDate());
        toDatePicker.setOnAction(e -> loadByDate());

        customerTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, selectedCustomer) -> {

                    if (selectedCustomer != null) {
                        loadTicketsByCustomer(selectedCustomer.getCustomerID());
                    }
                });

        setupTicketTable();
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
                updateCustomerTypeStats(customers);
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
                updateCustomerTypeStats(list);
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
                updateCustomerTypeStats(filtered);
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

    private void setupTicketTable() {

        colTicketId.setCellValueFactory(cell ->
                new SimpleStringProperty(cell.getValue().getTicketID())
        );

        colSchedule.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        cell.getValue().getSchedule().getScheduleID()
                )
        );

        colSeat.setCellValueFactory(cell -> {
            Ticket t = cell.getValue();

            if (t.getSeat() == null) {
                return new SimpleStringProperty("—");
            }

            return new SimpleStringProperty(
                    "Ghế " + t.getSeat().getSeatNumber()
            );
        });

        colPrice.setCellValueFactory(cell ->
                new SimpleStringProperty(
                        String.valueOf(cell.getValue().getFinalPrice())
                )
        );

        colStatus.setCellValueFactory(cell -> {
            Ticket t = cell.getValue();

            if (t.getTicketStatus() == null) {
                return new SimpleStringProperty("—");
            }

            return new SimpleStringProperty(
                    t.getTicketStatus().name()
            );
        });

        ticketTable.setItems(ticketList);
    }

    private void handleRowClick() {

        customerTable.getSelectionModel().selectedItemProperty()
                .addListener((obs, oldVal, selectedCustomer) -> {

                    if (selectedCustomer != null) {
                        loadTicketsByCustomer(selectedCustomer.getCustomerID());
                    }
                });
    }

    private void loadTicketsByCustomer(String customerId) {

        new Thread(() -> {

            List<Ticket> tickets =
                    customerService.getTicketsByCustomer(customerId);

            Platform.runLater(() -> {
                ticketList.setAll(tickets);
            });

        }).start();
    }

    private void updateCustomerTypeStats(List<Customer> list) {

        long adult = list.stream()
                .filter(c -> c.getCustomerType() == CustomerType.ADULT)
                .count();

        long student = list.stream()
                .filter(c -> c.getCustomerType() == CustomerType.STUDENT)
                .count();

        long child = list.stream()
                .filter(c -> c.getCustomerType() == CustomerType.CHILD)
                .count();

        long elderly = list.stream()
                .filter(c -> c.getCustomerType() == CustomerType.ELDERLY)
                .count();

        customerTypeStatsLabel.setText(
                "Người lớn: " + adult +
                        "      |       Sinh viên: " + student +
                        "      |       Trẻ em: " + child +
                        "      |       Người cao tuổi: " + elderly
        );
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }
}