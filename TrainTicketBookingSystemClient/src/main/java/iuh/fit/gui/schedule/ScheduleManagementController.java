package iuh.fit.gui.schedule;

import dto.CreateScheduleRequest;
import dto.UpdateScheduleRequest;
import iuh.fit.context.UserContext;
import iuh.fit.service.ScheduleClientService;
import iuh.fit.service.TrainClientService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import model.entity.Schedule;
import model.entity.Train;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.List;
import java.util.Map;

public class ScheduleManagementController {

    // ================= SEARCH SECTION =================
    @FXML private ComboBox<String> departureStationCombo;
    @FXML private ComboBox<String> arrivalStationCombo;
    @FXML private DatePicker travelDatePicker;
    @FXML private Button searchBtn;
    @FXML private Button refreshBtn;

    // ================= TABLE =================
    @FXML private TableView<ScheduleDisplayData> scheduleTable;
    @FXML private TableColumn<ScheduleDisplayData, String> colScheduleId;
    @FXML private TableColumn<ScheduleDisplayData, String> colTrain;
    @FXML private TableColumn<ScheduleDisplayData, String> colRoute;
    @FXML private TableColumn<ScheduleDisplayData, String> colDeparture;
    @FXML private TableColumn<ScheduleDisplayData, String> colArrival;
    @FXML private TableColumn<ScheduleDisplayData, String> colStatus;
    @FXML private TableColumn<ScheduleDisplayData, String> colAction;

    private final ScheduleClientService scheduleService =
            new ScheduleClientService();
    private final TrainClientService trainService =
            new TrainClientService();

    private final ObservableList<ScheduleDisplayData> scheduleList =
            FXCollections.observableArrayList();
    private final ObservableList<String> trainList =
            FXCollections.observableArrayList();
    private final ObservableList<String> stationList =
            FXCollections.observableArrayList();

    // Map stationName -> stationID
    private java.util.Map<String, String> stationNameToIdMap = new java.util.HashMap<>();

    @FXML
    private void initialize() {
        loadStations();
        setupTable();
        loadSchedules();

        travelDatePicker.setValue(LocalDate.now());
    }

    private void loadStations() {
        new Thread(() -> {
            try {
                // Lấy danh sách stations từ StationController (JAR)
                controller.StationController stationController = new controller.StationController();
                java.util.List<model.entity.Station> stations = stationController.getAllStations();

                Platform.runLater(() -> {
                    stationList.clear();
                    stationNameToIdMap.clear();
                    for (model.entity.Station station : stations) {
                        stationList.add(station.getStationName());
                        stationNameToIdMap.put(station.getStationName(), station.getStationID());
                    }
                    departureStationCombo.setItems(stationList);
                    arrivalStationCombo.setItems(stationList);
                    departureStationCombo.setValue("Ga Sài Gòn");
                    arrivalStationCombo.setValue("Ga Hà Nội");
                });
            } catch (Exception e) {
                System.err.println("Lỗi tải ga: " + e.getMessage());
                e.printStackTrace();
            }
        }).start();
    }

    private void setupTable() {
        colScheduleId.setCellValueFactory(cell ->
                new SimpleStringProperty(safe(cell.getValue().getScheduleId())));

        colTrain.setCellValueFactory(cell ->
                new SimpleStringProperty(safe(cell.getValue().getTrainName())));

        colRoute.setCellValueFactory(cell ->
                new SimpleStringProperty(safe(cell.getValue().getRoute())));

        colDeparture.setCellValueFactory(cell ->
                new SimpleStringProperty(safe(cell.getValue().getDepartureTime())));

        colArrival.setCellValueFactory(cell ->
                new SimpleStringProperty(safe(cell.getValue().getArrivalTime())));

        colStatus.setCellValueFactory(cell ->
                new SimpleStringProperty(safe(cell.getValue().getStatus())));

        // Trong setupTable()
        colAction.setCellFactory(column -> new TableCell<>() {
            private final Button editBtn = new Button("Sửa");
            private final Button stopBtn = new Button("Ngừng");
            private final HBox box = new HBox(8, editBtn, stopBtn);

            {
                box.setAlignment(Pos.CENTER);

                // Style cho nút Sửa
                editBtn.setStyle(
                        "-fx-padding: 6 16 6 16; " +
                        "-fx-background-color: #151C35; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
                );
                editBtn.setOnMouseEntered(e -> editBtn.setStyle(
                        "-fx-padding: 6 16 6 16; " +
                        "-fx-background-color: #0f2050; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
                ));
                editBtn.setOnMouseExited(e -> editBtn.setStyle(
                        "-fx-padding: 6 16 6 16; " +
                        "-fx-background-color: #151C35; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
                ));

                // Style cho nút Ngừng
                stopBtn.setStyle(
                        "-fx-padding: 6 16 6 16; " +
                        "-fx-background-color: #dc2626; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
                );
                stopBtn.setOnMouseEntered(e -> stopBtn.setStyle(
                        "-fx-padding: 6 16 6 16; " +
                        "-fx-background-color: #b91c1c; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
                ));
                stopBtn.setOnMouseExited(e -> stopBtn.setStyle(
                        "-fx-padding: 6 16 6 16; " +
                        "-fx-background-color: #dc2626; " +
                        "-fx-text-fill: white; " +
                        "-fx-font-size: 12px; " +
                        "-fx-background-radius: 4; " +
                        "-fx-cursor: hand;"
                ));

                // Sự kiện Sửa
                editBtn.setOnAction(e -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        showScheduleForm(getTableView().getItems().get(index));
                    }
                });

                // Sự kiện Ngừng
                stopBtn.setOnAction(e -> {
                    int index = getIndex();
                    if (index >= 0 && index < getTableView().getItems().size()) {
                        handleDeleteSchedule(getTableView().getItems().get(index));
                    }
                });
            }

            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                setGraphic(empty ? null : box);
            }
        });

        scheduleTable.setItems(scheduleList);

        // Add row selection handler
        scheduleTable.setRowFactory(tv -> {
            TableRow<ScheduleDisplayData> row = new TableRow<ScheduleDisplayData>() {
                @Override
                protected void updateItem(ScheduleDisplayData item, boolean empty) {
                    super.updateItem(item, empty);
                    if (empty || item == null) {
                        setOnMouseClicked(null);
                    } else {
                        setOnMouseClicked(event -> {
                            if (event.getClickCount() == 1) {
                                showScheduleDetails(item);
                            }
                        });
                    }
                }
            };
            return row;
        });
    }

    private void loadSchedules() {
        new Thread(() -> {
            try {
                List<Schedule> schedules = scheduleService.getAllSchedules();

                List<ScheduleDisplayData> displayData = schedules.stream()
                        .map(s -> new ScheduleDisplayData(
                                safe(s.getScheduleID()),
                                safe(s.getTrain() != null ? s.getTrain().getTrainName() : "N/A"),
                                safe(s.getRoute() != null ? s.getRoute().getDepartureStation().getStationName() + " - " +
                                        s.getRoute().getArrivalStation().getStationName() : "N/A"),
                                safe(s.getDepartureTime() != null ? s.getDepartureTime().toString() : "N/A"),
                                safe(s.getArrivalTime() != null ? s.getArrivalTime().toString() : "N/A"),
                                safe(s.getScheduleStatus() != null ? s.getScheduleStatus().toString() : "N/A")
                        ))
                        .toList();

                Platform.runLater(() -> {
                    scheduleList.setAll(displayData);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Error loading schedules: " + e.getMessage());
                    showAlert("Lỗi khi tải lịch trình: " + e.getMessage() +
                             "\n\nVui lòng đảm bảo Server đã implement method getAllSchedules()",
                             Alert.AlertType.ERROR);
                });
            }
        }).start();
    }

    @FXML
    private void onSearch() {
        String departure = departureStationCombo.getValue();
        String arrival = arrivalStationCombo.getValue();
        LocalDate travelDate = travelDatePicker.getValue();

        if (departure == null || arrival == null || travelDate == null) {
            showAlert("Vui lòng điền đầy đủ thông tin tìm kiếm", Alert.AlertType.WARNING);
            return;
        }

        new Thread(() -> {
            // Mock search - in real implementation, call service
            List<ScheduleDisplayData> filtered = scheduleList.stream()
                    .filter(s -> s.getRoute().contains(departure) && s.getRoute().contains(arrival))
                    .toList();

            Platform.runLater(() -> {
                scheduleList.setAll(filtered);
            });
        }).start();
    }

    @FXML
    private void onRefresh() {
        departureStationCombo.setValue("Hà Nội");
        arrivalStationCombo.setValue("Sài Gòn");
        travelDatePicker.setValue(LocalDate.now());
        loadSchedules();
    }

    private void showScheduleForm(ScheduleDisplayData data) {
        boolean isNew = (data == null);
        Stage dialogStage = new Stage();
        dialogStage.setTitle(isNew ? "Thêm Lịch Trình Mới" : "Cập Nhật Lịch Trình");
        dialogStage.initModality(javafx.stage.Modality.APPLICATION_MODAL);
        dialogStage.setWidth(700);
        dialogStage.setHeight(650);

        // Form Container
        VBox form = new VBox(15);
        form.setPadding(new Insets(20));
        form.setStyle("-fx-background-color: white;");

        // Title
        Label titleLabel = new Label(isNew ? "Tạo Lịch Trình Mới" : "Cập Nhật Lịch Trình");
        titleLabel.setStyle("-fx-font-size: 18px; -fx-font-weight: bold; -fx-text-fill: #0a1f7a;");

        // 1. Chọn Tàu
        VBox trainSection = new VBox(8);
        Label trainLabel = new Label("Chọn Tàu *");
        trainLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5f6f95;");
        ComboBox<String> trainCombo = new ComboBox<>();
        trainCombo.setPrefWidth(Double.MAX_VALUE);
        trainCombo.setStyle("-fx-font-size: 13px; -fx-padding: 9 12 9 12;");
        trainCombo.setPromptText("Chọn tàu...");
        loadTrainsIntoCombo(trainCombo);
        trainSection.getChildren().addAll(trainLabel, trainCombo);

        // 2. Ga khởi hành
        VBox depStationSection = new VBox(8);
        Label depStationLabel = new Label("Ga Khởi Hành *");
        depStationLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5f6f95;");
        ComboBox<String> depStationCombo = new ComboBox<>();
        depStationCombo.setItems(stationList);
        depStationCombo.setPrefWidth(Double.MAX_VALUE);
        depStationCombo.setStyle("-fx-font-size: 13px; -fx-padding: 9 12 9 12;");
        depStationCombo.setPromptText("Chọn ga khởi hành...");
        depStationSection.getChildren().addAll(depStationLabel, depStationCombo);

        // 3. Ga đến
        VBox arrStationSection = new VBox(8);
        Label arrStationLabel = new Label("Ga Đến *");
        arrStationLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5f6f95;");
        ComboBox<String> arrStationCombo = new ComboBox<>();
        arrStationCombo.setItems(stationList);
        arrStationCombo.setPrefWidth(Double.MAX_VALUE);
        arrStationCombo.setStyle("-fx-font-size: 13px; -fx-padding: 9 12 9 12;");
        arrStationCombo.setPromptText("Chọn ga đến...");
        arrStationSection.getChildren().addAll(arrStationLabel, arrStationCombo);

        // 4. Thời gian khởi hành
        HBox depTimeSection = new HBox(10);
        Label depTimeLabel = new Label("Thời Gian Khởi Hành *");
        depTimeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5f6f95;");
        depTimeLabel.setPrefWidth(150);
        DatePicker depDate = new DatePicker(LocalDate.now());
        depDate.setStyle("-fx-font-size: 13px; -fx-padding: 9 12 9 12;");
        Spinner<Integer> depHour = new Spinner<>(0, 23, 8);
        depHour.setPrefWidth(70);
        depHour.setStyle("-fx-font-size: 13px;");
        Spinner<Integer> depMin = new Spinner<>(0, 59, 0);
        depMin.setPrefWidth(70);
        depMin.setStyle("-fx-font-size: 13px;");
        depTimeSection.getChildren().addAll(
                depTimeLabel, depDate,
                new Label("Giờ:"), depHour,
                new Label("Phút:"), depMin
        );

        // 5. Thời gian đến dự kiến
        HBox arrTimeSection = new HBox(10);
        Label arrTimeLabel = new Label("Thời Gian Đến Dự Kiến *");
        arrTimeLabel.setStyle("-fx-font-size: 13px; -fx-font-weight: bold; -fx-text-fill: #5f6f95;");
        arrTimeLabel.setPrefWidth(150);
        DatePicker arrDate = new DatePicker(LocalDate.now());
        arrDate.setStyle("-fx-font-size: 13px; -fx-padding: 9 12 9 12;");
        Spinner<Integer> arrHour = new Spinner<>(0, 23, 12);
        arrHour.setPrefWidth(70);
        arrHour.setStyle("-fx-font-size: 13px;");
        Spinner<Integer> arrMin = new Spinner<>(0, 59, 0);
        arrMin.setPrefWidth(70);
        arrMin.setStyle("-fx-font-size: 13px;");
        arrTimeSection.getChildren().addAll(
                arrTimeLabel, arrDate,
                new Label("Giờ:"), arrHour,
                new Label("Phút:"), arrMin
        );

        // Form Layout
        VBox formContent = new VBox(15);
        formContent.getChildren().addAll(
                titleLabel,
                new Separator(),
                trainSection,
                depStationSection,
                arrStationSection,
                depTimeSection,
                arrTimeSection
        );

        ScrollPane scrollPane = new ScrollPane(formContent);
        scrollPane.setFitToWidth(true);
        scrollPane.setStyle("-fx-background-color: white;");
        form.getChildren().add(scrollPane);
        VBox.setVgrow(scrollPane, Priority.ALWAYS);

        // Buttons
        HBox buttonBox = new HBox(10);
        buttonBox.setAlignment(Pos.CENTER_RIGHT);
        buttonBox.setPadding(new Insets(15, 0, 0, 0));

        Button submitBtn = new Button(isNew ? "Tạo Lịch Trình" : "Lưu Thay Đổi");
        submitBtn.setStyle(
                "-fx-padding: 10 30 10 30; " +
                "-fx-font-size: 13px; " +
                "-fx-font-weight: bold; " +
                "-fx-background-color: #151C35; " +
                "-fx-text-fill: white; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;"
        );
        submitBtn.setOnAction(e -> {
            if (validateForm(trainCombo, depStationCombo, arrStationCombo)) {
                handleScheduleSubmit(isNew, trainCombo, depStationCombo, arrStationCombo,
                        depDate, depHour, depMin, arrDate, arrHour, arrMin, dialogStage, data);
            }
        });

        Button cancelBtn = new Button("Hủy");
        cancelBtn.setStyle(
                "-fx-padding: 10 30 10 30; " +
                "-fx-font-size: 13px; " +
                "-fx-background-color: transparent; " +
                "-fx-text-fill: #5f6f95; " +
                "-fx-border-color: #dbe4ff; " +
                "-fx-border-radius: 4; " +
                "-fx-background-radius: 4; " +
                "-fx-cursor: hand;"
        );
        cancelBtn.setOnAction(e -> dialogStage.close());

        buttonBox.getChildren().addAll(cancelBtn, submitBtn);
        form.getChildren().add(buttonBox);

        Scene scene = new Scene(form);
        dialogStage.setScene(scene);
        dialogStage.show();
    }

    private void loadTrainsIntoCombo(ComboBox<String> trainCombo) {
        new Thread(() -> {
            try {
                List<Map<String, Object>> trains = trainService.getAllTrains();
                List<String> trainNames = trains.stream()
                        .map(t -> {
                            String id = safe(t.get("trainID"));
                            String name = safe(t.get("trainName"));
                            return id + " | " + name;
                        })
                        .toList();

                Platform.runLater(() -> {
                    trainList.setAll(trainNames);
                    trainCombo.setItems(trainList);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    System.err.println("Lỗi tải danh sách tàu: " + e.getMessage());
                });
            }
        }).start();
    }

    private boolean validateForm(ComboBox<String> trainCombo, ComboBox<String> depStation,
                                  ComboBox<String> arrStation) {
        if (trainCombo.getValue() == null || trainCombo.getValue().isEmpty()) {
            showAlert("Vui lòng chọn tàu!", Alert.AlertType.WARNING);
            return false;
        }
        if (depStation.getValue() == null || depStation.getValue().isEmpty()) {
            showAlert("Vui lòng chọn ga khởi hành!", Alert.AlertType.WARNING);
            return false;
        }
        if (arrStation.getValue() == null || arrStation.getValue().isEmpty()) {
            showAlert("Vui lòng chọn ga đến!", Alert.AlertType.WARNING);
            return false;
        }
        if (depStation.getValue().equals(arrStation.getValue())) {
            showAlert("Ga khởi hành và ga đến không được trùng nhau!", Alert.AlertType.WARNING);
            return false;
        }
        return true;
    }

    private void handleScheduleSubmit(boolean isNew, ComboBox<String> trainCombo,
                                      ComboBox<String> depStationCombo, ComboBox<String> arrStationCombo,
                                      DatePicker depDate, Spinner<Integer> depHour, Spinner<Integer> depMin,
                                      DatePicker arrDate, Spinner<Integer> arrHour, Spinner<Integer> arrMin,
                                      Stage dialogStage, ScheduleDisplayData data) {
        new Thread(() -> {
            try {
                String trainId = trainCombo.getValue().split("\\|")[0].trim();
                String depStationName = depStationCombo.getValue();
                String arrStationName = arrStationCombo.getValue();

                // Lấy stationID từ stationName
                String depStationId = stationNameToIdMap.get(depStationName);
                String arrStationId = stationNameToIdMap.get(arrStationName);

                if (depStationId == null || arrStationId == null) {
                    Platform.runLater(() -> {
                        showAlert("Lỗi: Không tìm thấy ID ga!", Alert.AlertType.ERROR);
                    });
                    return;
                }

                // Tạo routeID từ stationID
                String routeId = scheduleService.findRouteIdByStations(depStationId, arrStationId);

                if (routeId == null) {
                    Platform.runLater(() ->
                            showAlert("Không tìm thấy tuyến đường từ "
                                            + depStationName + " → " + arrStationName
                                            + ".\nTuyến đường này chưa tồn tại trong hệ thống.",
                                    Alert.AlertType.ERROR));
                    return;
                }

                LocalDateTime depTime = LocalDateTime.of(depDate.getValue(),
                        LocalTime.of(depHour.getValue(), depMin.getValue()));
                LocalDateTime arrTime = LocalDateTime.of(arrDate.getValue(),
                        LocalTime.of(arrHour.getValue(), arrMin.getValue()));

                String managerId = UserContext.getInstance().getUserID();

                if (isNew) {
                    CreateScheduleRequest req = new CreateScheduleRequest();
                    req.setTrainID(trainId);
                    req.setRouteID(routeId);
                    req.setDepartureTime(depTime);
                    req.setArrivalTime(arrTime);
                    req.setManagerID(managerId);
                    System.out.println("Creating schedule with: trainId=" + trainId +
                                     ", routeId=" + routeId + ", depTime=" + depTime +
                                     ", arrTime=" + arrTime + ", managerId=" + managerId);
                    scheduleService.createSchedule(req);
                } else {
                    UpdateScheduleRequest req = new UpdateScheduleRequest();
                    req.setScheduleID(data.getScheduleId());
                    req.setTrainID(trainId);
                    req.setRouteID(routeId);
                    req.setDepartureTime(depTime);
                    req.setArrivalTime(arrTime);
                    req.setManagerID(managerId);
                    System.out.println("Updating schedule with: scheduleId=" + data.getScheduleId() +
                                     ", trainId=" + trainId + ", routeId=" + routeId + ", managerId=" + managerId);
                    scheduleService.updateSchedule(req);
                }

                Platform.runLater(() -> {
                    showAlert("Thành công!", Alert.AlertType.INFORMATION);
                    dialogStage.close();
                    loadSchedules();
                });
            } catch (Exception e) {
                e.printStackTrace();
                Platform.runLater(() -> {
                    showAlert("Lỗi: " + e.getMessage(), Alert.AlertType.ERROR);
                    e.printStackTrace();
                });
            }
        }).start();
    }

    private void handleDeleteSchedule(ScheduleDisplayData data) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION);
        confirmAlert.setTitle("Xác Nhận Ngừng Lịch Trình");
        confirmAlert.setHeaderText("Ngừng Lịch Trình");
        confirmAlert.setContentText("Bạn có chắc chắn muốn ngừng lịch trình này không?\n\n" +
                "Mã lịch trình: " + data.getScheduleId() + "\n" +
                "Tàu: " + data.getTrainName());

        confirmAlert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    try {
                        scheduleService.deleteSchedule(data.getScheduleId());
                        Platform.runLater(() -> {
                            showAlert("Ngừng lịch trình thành công!", Alert.AlertType.INFORMATION);
                            loadSchedules();
                        });
                    } catch (Exception e) {
                        Platform.runLater(() -> {
                            showAlert("Lỗi: " + e.getMessage(), Alert.AlertType.ERROR);
                        });
                    }
                }).start();
            }
        });
    }

    @FXML
    private void onAddSchedule() {
        showScheduleForm(null);
    }

    private void showScheduleDetails(ScheduleDisplayData schedule) {
        Stage detailStage = new Stage();
        detailStage.setTitle("Chi Tiết Lịch Trình");
        detailStage.initModality(Modality.APPLICATION_MODAL);
        detailStage.setWidth(650);
        detailStage.setHeight(500);

        VBox content = new VBox(15);
        content.setPadding(new Insets(25));
        content.setStyle("-fx-background-color: white;");

        Label titleLabel = new Label("Chi Tiết Lịch Trình");
        titleLabel.setStyle("-fx-font-size: 20px; -fx-font-weight: bold; -fx-text-fill: #0a1f7a;");

        VBox detailBox = new VBox(12);
        detailBox.setStyle("-fx-border-color: #dbe4ff; -fx-border-radius: 8; -fx-background-color: #f8faff; -fx-padding: 20; -fx-border-width: 1;");
        
        detailBox.getChildren().addAll(
                createDetailRow("Mã Lịch Trình:", schedule.getScheduleId()),
                createDetailRow("Tàu:", schedule.getTrainName()),
                createDetailRow("Tuyến Đường:", schedule.getRoute()),
                createDetailRow("Khởi Hành:", schedule.getDepartureTime()),
                createDetailRow("Dự Kiến Đến:", schedule.getArrivalTime()),
                createDetailRow("Trạng Thái:", schedule.getStatus())
        );

        content.getChildren().addAll(titleLabel, new Separator(), detailBox);

        ScrollPane scrollPane = new ScrollPane(content);
        scrollPane.setFitToWidth(true);

        Scene scene = new Scene(scrollPane);
        detailStage.setScene(scene);
        detailStage.show();
    }

    private HBox createDetailRow(String label, String value) {
        HBox box = new HBox(15);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(10, 0, 10, 0));

        Label labelText = new Label(label);
        labelText.setStyle(
                "-fx-font-weight: bold; " +
                "-fx-text-fill: #5f6f95; " +
                "-fx-min-width: 140; " +
                "-fx-font-size: 13px;"
        );

        Label valueText = new Label(value != null ? value : "N/A");
        valueText.setStyle(
                "-fx-text-fill: #0a1f7a; " +
                "-fx-font-size: 13px;"
        );
        valueText.setWrapText(true);
        HBox.setHgrow(valueText, Priority.ALWAYS);

        box.getChildren().addAll(labelText, valueText);
        return box;
    }

    private void showAlert(String message, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    private String safe(String val) {
        return val == null ? "" : val;
    }

    private String safe(Object val) {
        return val == null ? "" : val.toString();
    }

    // Inner class for display data
    public static class ScheduleDisplayData {
        private final String scheduleId;
        private final String trainName;
        private final String route;
        private final String departureTime;
        private final String arrivalTime;
        private final String status;

        public ScheduleDisplayData(String scheduleId, String trainName, String route,
                                  String departureTime, String arrivalTime, String status) {
            this.scheduleId = scheduleId;
            this.trainName = trainName;
            this.route = route;
            this.departureTime = departureTime;
            this.arrivalTime = arrivalTime;
            this.status = status;
        }

        public String getScheduleId() { return scheduleId; }
        public String getTrainName() { return trainName; }
        public String getRoute() { return route; }
        public String getDepartureTime() { return departureTime; }
        public String getArrivalTime() { return arrivalTime; }
        public String getStatus() { return status; }
    }
}


