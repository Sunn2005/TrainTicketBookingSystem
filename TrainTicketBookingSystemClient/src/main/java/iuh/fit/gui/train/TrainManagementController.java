package iuh.fit.gui.train;

import dto.ActionResponse;
import dto.CreateTrainRequest;
import dto.UpdateTrainRequest;
import iuh.fit.service.TrainClientService;
import javafx.application.Platform;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;
import model.entity.Train;

import java.util.Comparator;
import java.util.List;
import java.util.Map;

public class TrainManagementController {

    // ================= HEADER =================
    @FXML private Button addButton;

    // ================= TABLE =================
    @FXML private TableView<Map<String, Object>>           trainTable;
    @FXML private TableColumn<Map<String, Object>, String> colTrainId;
    @FXML private TableColumn<Map<String, Object>, String> colTrainName;
    @FXML private TableColumn<Map<String, Object>, String> colCarriageCount;
    @FXML private TableColumn<Map<String, Object>, String> colAction;

    private final TrainClientService trainService = new TrainClientService();
    private final ObservableList<Map<String, Object>> trainList =
            FXCollections.observableArrayList();

    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void initialize() {
        setupTrainTable();
        loadTrains();
    }

    // ── Setup Table ───────────────────────────────────────────────────
    private void setupTrainTable() {
        colTrainId.setCellValueFactory(c ->
                new SimpleStringProperty(getStr(c.getValue(), "trainID")));
        colTrainName.setCellValueFactory(c ->
                new SimpleStringProperty(getStr(c.getValue(), "trainName")));
        colCarriageCount.setCellValueFactory(c ->
                new SimpleStringProperty(
                        String.valueOf(getCarriages(c.getValue()).size())));

        colAction.setCellValueFactory(c -> new SimpleStringProperty(""));
        colAction.setCellFactory(col -> new TableCell<>() {
            private final Button updateBtn = createStyledButton(
                    "Cập nhật", "-fx-background-color:#151C35;");
            private final Button deleteBtn = createStyledButton(
                    "Xóa", "-fx-background-color:#dc2626;");
            {
                updateBtn.setOnAction(e ->
                        handleUpdateTrain(getTableView().getItems().get(getIndex())));
                deleteBtn.setOnAction(e ->
                        handleDeleteTrain(getTableView().getItems().get(getIndex())));
            }
            @Override
            protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty);
                if (empty) { setGraphic(null); return; }
                HBox box = new HBox(8, updateBtn, deleteBtn);
                setGraphic(box);
            }
        });

        // Click row để xem chi tiết
        trainTable.setRowFactory(tv -> {
            TableRow<Map<String, Object>> row = new TableRow<>();
            row.setOnMouseClicked(e -> {
                if (!row.isEmpty()) showTrainDetailsDialog(row.getItem());
            });
            return row;
        });

        trainTable.setItems(trainList);
    }

    // ── Load trains ───────────────────────────────────────────────────
    private void loadTrains() {
        new Thread(() -> {
            List<Map<String, Object>> trains = trainService.getAllTrains();
            Platform.runLater(() -> trainList.setAll(trains));
        }).start();
    }

    // ── Chi tiết tàu ─────────────────────────────────────────────────
    private void showTrainDetailsDialog(Map<String, Object> train) {
        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle("Chi Tiết: " + getStr(train, "trainName"));
        dialog.getDialogPane().setPrefWidth(700);
        dialog.getDialogPane().setPrefHeight(500);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding:20;-fx-background-color:#f8faff;");

        Label title = new Label("Chi Tiết: " + getStr(train, "trainName"));
        title.setStyle("-fx-font-size:16px;-fx-font-weight:bold;-fx-text-fill:#0a1f7a;");

        Label idLbl = new Label("Mã Tàu: " + getStr(train, "trainID"));
        idLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#5f6f95;");

        List<Map<String, Object>> carriages = getCarriages(train);
        Label cntLbl = new Label("Số Toa: " + carriages.size());
        cntLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#5f6f95;");

        Label secTitle = new Label("DANH SÁCH TOA");
        secTitle.setStyle(
                "-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#0a1f7a;");

        TableView<Map<String, Object>> table = new TableView<>();
        table.setPrefHeight(300);

        TableColumn<Map<String, Object>, String> colNum   = new TableColumn<>("Toa Số");
        TableColumn<Map<String, Object>, String> colType  = new TableColumn<>("Loại Ghế");
        TableColumn<Map<String, Object>, String> colCount = new TableColumn<>("Số Ghế");

        colNum.setCellValueFactory(c ->
                new SimpleStringProperty(getStr(c.getValue(), "carriageNumber")));
        colType.setCellValueFactory(c -> {
            List<Map<String, Object>> seats = getSeats(c.getValue());
            String type = seats.isEmpty() ? "N/A" : getStr(seats.get(0), "seatType");
            return new SimpleStringProperty(type);
        });
        colCount.setCellValueFactory(c ->
                new SimpleStringProperty(
                        String.valueOf(getSeats(c.getValue()).size())));

        colNum.setPrefWidth(100);
        colType.setPrefWidth(200);
        colCount.setPrefWidth(100);
        table.getColumns().addAll(colNum, colType, colCount);

        List<Map<String, Object>> sorted = carriages.stream()
                .sorted(Comparator.comparingInt(c ->
                        ((Number) c.get("carriageNumber")).intValue()))
                .toList();
        table.setItems(FXCollections.observableArrayList(sorted));

        content.getChildren().addAll(
                title, new Separator(), idLbl, cntLbl, secTitle, table);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setStyle("-fx-background-color:#f8faff;-fx-border-color:transparent;");
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ── Thêm / Cập nhật tàu ──────────────────────────────────────────
    @FXML
    private void onAddTrain() { showTrainForm(null); }

    private void handleUpdateTrain(Map<String, Object> train) {
        showTrainForm(train);
    }

    private void showTrainForm(Map<String, Object> train) {
        boolean isNew = train == null;

        Dialog<Void> dialog = new Dialog<>();
        dialog.setTitle(isNew ? "Thêm tàu mới" : "Cập nhật tàu");
        dialog.getDialogPane().setPrefWidth(700);
        dialog.getDialogPane().setPrefHeight(500);

        VBox content = new VBox(15);
        content.setStyle("-fx-padding:20;-fx-background-color:#f8faff;");

        Label titleLbl = new Label(isNew ? "Thêm Tàu Mới" : "Cập Nhật Tàu");
        titleLbl.setStyle(
                "-fx-font-size:18px;-fx-font-weight:bold;-fx-text-fill:#0a1f7a;");

        Label idLbl = new Label(isNew
                ? "Mã tàu sẽ được tạo tự động"
                : "Mã tàu: " + getStr(train, "trainID"));
        idLbl.setStyle("-fx-font-size:13px;-fx-text-fill:#5f6f95;");

        TextField trainNameField = createStyledTextField("Tên tàu");
        if (!isNew) trainNameField.setText(getStr(train, "trainName"));

        // ── Carriage form ──
        Label carriagesLbl = new Label("Quản lý toa tàu");
        carriagesLbl.setStyle(
                "-fx-font-size:14px;-fx-font-weight:bold;-fx-text-fill:#0a1f7a;");

        // Danh sách toa đã thêm
        ObservableList<Map<String, Object>> carriageList =
                FXCollections.observableArrayList();
        TableView<Map<String, Object>> carriageTable = new TableView<>();
        carriageTable.setPrefHeight(180);

        TableColumn<Map<String, Object>, String> cNum  = new TableColumn<>("Toa số");
        TableColumn<Map<String, Object>, String> cType = new TableColumn<>("Loại ghế");
        TableColumn<Map<String, Object>, String> cCnt  = new TableColumn<>("Số ghế");
        TableColumn<Map<String, Object>, String> cDel  = new TableColumn<>("");

        cNum.setCellValueFactory(c ->
                new SimpleStringProperty(getStr(c.getValue(), "carriageNumber")));
        cType.setCellValueFactory(c ->
                new SimpleStringProperty(getStr(c.getValue(), "seatType")));
        cCnt.setCellValueFactory(c ->
                new SimpleStringProperty(getStr(c.getValue(), "seatCount")));
        cDel.setCellFactory(col -> new TableCell<>() {
            private final Button rm = new Button("✕");
            { rm.setStyle("-fx-background-color:#fee2e2;-fx-text-fill:#dc2626;"
                    + "-fx-cursor:hand;-fx-background-radius:4;"); }
            @Override protected void updateItem(String item, boolean empty) {
                super.updateItem(item, empty); setGraphic(empty ? null : rm);
                rm.setOnAction(e -> carriageList.remove(getIndex()));
            }
        });

        cNum.setPrefWidth(80); cType.setPrefWidth(160);
        cCnt.setPrefWidth(80); cDel.setPrefWidth(50);
        carriageTable.getColumns().addAll(cNum, cType, cCnt, cDel);
        carriageTable.setItems(carriageList);

        // Nếu update thì load toa cũ
        if (!isNew) {
            getCarriages(train).forEach(c -> {
                Map<String, Object> row = new java.util.LinkedHashMap<>();
                row.put("carriageNumber", getStr(c, "carriageNumber"));
                List<Map<String, Object>> seats = getSeats(c);
                row.put("seatType", seats.isEmpty() ? "N/A"
                        : getStr(seats.get(0), "seatType"));
                row.put("seatCount", String.valueOf(seats.size()));
                carriageList.add(row);
            });
        }

        // Form thêm toa
        Spinner<Integer> numSpinner  = new Spinner<>(1, 20, 1);
        Spinner<Integer> cntSpinner  = new Spinner<>(1, 100, 20);
        ComboBox<String> typeCombo   = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(
                "SOFT_SEAT", "SOFT_SLEEPER"));
        typeCombo.setValue("SOFT_SEAT");

        Button addCarriageBtn = new Button("+ Thêm toa");
        addCarriageBtn.setStyle(
                "-fx-background-color:#0b1f84;-fx-text-fill:white;"
                        + "-fx-font-size:12px;-fx-padding:6 12 6 12;"
                        + "-fx-cursor:hand;-fx-background-radius:4;");
        addCarriageBtn.setOnAction(e -> {
            Map<String, Object> row = new java.util.LinkedHashMap<>();
            row.put("carriageNumber", String.valueOf(numSpinner.getValue()));
            row.put("seatType",       typeCombo.getValue());
            row.put("seatCount",      String.valueOf(cntSpinner.getValue()));
            carriageList.add(row);
        });

        HBox addCarriageRow = new HBox(10,
                createDetailRow("Toa số:",    numSpinner),
                createDetailRow("Loại ghế:",  typeCombo),
                createDetailRow("Số ghế:",    cntSpinner),
                addCarriageBtn);
        addCarriageRow.setAlignment(javafx.geometry.Pos.CENTER_LEFT);

        // Submit
        Button submitBtn = new Button(isNew ? "Thêm tàu" : "Cập nhật tàu");
        submitBtn.setStyle(
                "-fx-background-color:#151C35;-fx-text-fill:white;"
                        + "-fx-font-size:13px;-fx-padding:10 20 10 20;"
                        + "-fx-cursor:hand;-fx-background-radius:4;");
        submitBtn.setOnAction(e -> {
            String name = trainNameField.getText().trim();
            if (name.isEmpty()) {
                showAlert("Vui lòng nhập tên tàu", Alert.AlertType.WARNING);
                return;
            }
            if (carriageList.isEmpty()) {
                showAlert("Vui lòng thêm ít nhất 1 toa", Alert.AlertType.WARNING);
                return;
            }

            // Tạo Map chi tiết toa/ghế dùng chung cho cả 2 trường hợp
            Map<Integer, String> detailMap = new java.util.LinkedHashMap<>();
            for (Map<String, Object> c : carriageList) {
                int num = Integer.parseInt(getStr(c, "carriageNumber"));
                String cnt = getStr(c, "seatCount");
                String typ = getStr(c, "seatType");
                detailMap.put(num, cnt + "-" + typ);
            }

            new Thread(() -> {
                final boolean[] success = {false};
                final String[] errorMsg = {""};
                try {
                    if (isNew) {
                        // TRƯỜNG HỢP THÊM MỚI
                        dto.CreateTrainRequest req = new dto.CreateTrainRequest();
                        req.setTrainName(name);
                        req.setDetail(detailMap);

                        Train result = trainService.createTrain(req);
                        success[0] = (result != null);
                        if (!success[0]) errorMsg[0] = "Không thể tạo tàu";
                    } else {
                        // TRƯỜNG HỢP CẬP NHẬT
                        UpdateTrainRequest updateReq = new UpdateTrainRequest();
                        // Lấy lại ID cũ (Ví dụ: TRA-005) để Server tìm đúng bản ghi cần sửa
                        updateReq.setTrainID(getStr(train, "trainID"));
                        updateReq.setTrainName(name);
                        updateReq.setDetail(detailMap);

                        System.out.println("Updating train: " + updateReq.getTrainID() +
                                         " with name: " + name);

                        ActionResponse resp = trainService.updateTrain(updateReq);
                        if (resp != null) {
                            success[0] = resp.isSuccess();
                            errorMsg[0] = resp.getMessage();
                            System.out.println("Update response: success=" + success[0] + ", msg=" + errorMsg[0]);
                        } else {
                            success[0] = false;
                            errorMsg[0] = "Không nhận được phản hồi từ server";
                        }
                    }
                } catch (Exception ex) {
                    success[0] = false;
                    errorMsg[0] = ex.getMessage();
                    ex.printStackTrace();
                }

                Platform.runLater(() -> {
                    if (success[0]) {
                        showAlert("Lưu dữ liệu tàu thành công!", Alert.AlertType.INFORMATION);
                        dialog.close();
                        loadTrains(); // Tải lại bảng để cập nhật dữ liệu mới nhất
                    } else {
                        showAlert("Lỗi: " + (errorMsg[0].isEmpty() ? "Không xác định" : errorMsg[0]),
                                 Alert.AlertType.ERROR);
                    }
                });
            }).start();
        });

        content.getChildren().addAll(
                titleLbl, new Separator(),
                idLbl,
                createDetailRow("Tên tàu:", trainNameField),
                carriagesLbl,
                addCarriageRow,
                carriageTable,
                submitBtn);

        ScrollPane scroll = new ScrollPane(content);
        scroll.setStyle("-fx-background-color:#f8faff;-fx-border-color:transparent;");
        dialog.getDialogPane().setContent(scroll);
        dialog.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        dialog.showAndWait();
    }

    // ── Xóa tàu ──────────────────────────────────────────────────────
    private void handleDeleteTrain(Map<String, Object> train) {
        Alert confirm = new Alert(Alert.AlertType.CONFIRMATION);
        confirm.setTitle("Xác nhận xóa");
        confirm.setHeaderText(null);
        confirm.setContentText(
                "Bạn có chắc muốn xóa tàu " + getStr(train, "trainName") + "?");
        confirm.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) {
                new Thread(() -> {
                    ActionResponse resp =
                            trainService.deleteTrain(getStr(train, "trainID"));
                    Platform.runLater(() -> {
                        showAlert(resp.isSuccess() ? "Xóa tàu thành công"
                                        : "Lỗi: " + resp.getMessage(),
                                resp.isSuccess()
                                        ? Alert.AlertType.INFORMATION
                                        : Alert.AlertType.ERROR);
                        if (resp.isSuccess()) loadTrains();
                    });
                }).start();
            }
        });
    }

    // ── Helpers ───────────────────────────────────────────────────────
    private String getStr(Map<String, Object> map, String key) {
        Object val = map.get(key);
        return val != null ? val.toString() : "";
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getCarriages(Map<String, Object> train) {
        Object c = train.get("carriages");
        if (c instanceof List) return (List<Map<String, Object>>) c;
        return List.of();
    }

    @SuppressWarnings("unchecked")
    private List<Map<String, Object>> getSeats(Map<String, Object> carriage) {
        Object s = carriage.get("seats");
        if (s instanceof List) return (List<Map<String, Object>>) s;
        return List.of();
    }

    private Button createStyledButton(String text, String style) {
        Button btn = new Button(text);
        btn.setStyle(style
                + "-fx-text-fill:white;-fx-font-size:12px;"
                + "-fx-padding:6 12 6 12;-fx-cursor:hand;"
                + "-fx-background-radius:4;");
        btn.setPrefWidth(80);
        return btn;
    }

    private TextField createStyledTextField(String prompt) {
        TextField f = new TextField();
        f.setPromptText(prompt);
        f.setStyle("-fx-font-size:13px;-fx-background-color:#f8faff;"
                + "-fx-border-color:#dbe4ff;-fx-border-radius:4;"
                + "-fx-background-radius:4;-fx-padding:8 12 8 12;"
                + "-fx-text-fill:#0a1f7a;");
        f.setPrefHeight(35);
        return f;
    }

    private HBox createDetailRow(String label, Node control) {
        HBox box = new HBox(8);
        box.setAlignment(javafx.geometry.Pos.CENTER_LEFT);
        Label lbl = new Label(label);
        lbl.setStyle("-fx-font-weight:bold;-fx-text-fill:#5f6f95;-fx-min-width:80;");
        if (!(control instanceof Spinner) && !(control instanceof ComboBox))
            HBox.setHgrow(control, Priority.ALWAYS);
        box.getChildren().addAll(lbl, control);
        return box;
    }

    private void showAlert(String msg, Alert.AlertType type) {
        Alert alert = new Alert(type);
        alert.setTitle("Thông báo");
        alert.setHeaderText(null);
        alert.setContentText(msg);
        alert.showAndWait();
    }
}