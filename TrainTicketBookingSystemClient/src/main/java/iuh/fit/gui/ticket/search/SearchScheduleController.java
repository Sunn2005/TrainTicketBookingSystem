package iuh.fit.gui.ticket.search;

import dto.ScheduleInfoResponse;
import iuh.fit.App;
import iuh.fit.context.TicketContext;
import iuh.fit.constance.AppTheme;
import iuh.fit.service.TicketClientService;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import javafx.scene.layout.StackPane;
import model.entity.Station;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class SearchScheduleController {

    @FXML private HBox       resultHeaderBox;
    @FXML private Label      resultHeaderLabel;
    @FXML private ScrollPane trainScrollPane;
    @FXML private VBox       trainSectionsBox;
    @FXML private Label      statusLabel;
    @FXML private Label      searchStatusLabel;
    @FXML private ComboBox<String> departureCombo;
    @FXML private ComboBox<String> arrivalCombo;
    @FXML private DatePicker  departureDatePicker;
    @FXML private DatePicker  returnDatePicker;
    @FXML private RadioButton oneWayRadio;
    @FXML private RadioButton roundTripRadio;
    @FXML private Button      searchButton;

    private final TicketClientService ticketService = new TicketClientService();
    private final TicketContext ctx = TicketContext.getInstance();
    private final Map<String, String> stationNameToId = new LinkedHashMap<>();
    private final Map<String, String> scheduleSegmentMap = new HashMap<>();
    private List<ScheduleInfoResponse> goList  = new ArrayList<>();
    private List<ScheduleInfoResponse> retList = new ArrayList<>();
    private ToggleGroup tripToggle;

    private static final DateTimeFormatter SHORT   = DateTimeFormatter.ofPattern("dd/MM HH:mm");
    private static final DateTimeFormatter DATEFMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    @FXML
    private void initialize() {
        departureDatePicker.setValue(LocalDate.now());
        tripToggle = new ToggleGroup();
        oneWayRadio.setToggleGroup(tripToggle);
        roundTripRadio.setToggleGroup(tripToggle);
        oneWayRadio.setSelected(true);
        returnDatePicker.setDisable(true);
        roundTripRadio.selectedProperty().addListener((obs, o, n) -> {
            returnDatePicker.setDisable(!n);
            if (!n) returnDatePicker.setValue(null);
        });
        loadStations();
    }

    private void loadStations() {
        new Thread(() -> {
            try {
                List<Station> stations = ticketService.getAllStations();
                Platform.runLater(() -> {
                    ObservableList<String> names = FXCollections.observableArrayList();
                    for (Station s : stations) {
                        stationNameToId.put(s.getStationName(), s.getStationID());
                        names.add(s.getStationName());
                    }
                    departureCombo.setItems(names);
                    arrivalCombo.setItems(names);
                    // Restore từ context
                    if (ctx.getDepartureStationName() != null)
                        departureCombo.setValue(ctx.getDepartureStationName());
                    if (ctx.getArrivalStationName() != null)
                        arrivalCombo.setValue(ctx.getArrivalStationName());
                    if (ctx.getDepartureDate() != null)
                        departureDatePicker.setValue(ctx.getDepartureDate());
                });
            } catch (Exception e) {
                Platform.runLater(() ->
                        showError(searchStatusLabel, "Không tải được ga: " + e.getMessage()));
            }
        }).start();
    }

    @FXML
    private void onSearch() {
        String depId  = stationNameToId.get(departureCombo.getValue());
        String arrId  = stationNameToId.get(arrivalCombo.getValue());
        LocalDate depDate = departureDatePicker.getValue();
        boolean isRound   = roundTripRadio.isSelected();
        LocalDate retDate = returnDatePicker.getValue();

        if (depId == null || arrId == null || depDate == null) {
            showError(searchStatusLabel, "Vui lòng chọn đầy đủ thông tin."); return;
        }
        if (depId.equals(arrId)) {
            showError(searchStatusLabel, "Ga đi và ga đến không được trùng."); return;
        }
        if (isRound && retDate == null) {
            showError(searchStatusLabel, "Vui lòng chọn ngày về."); return;
        }

        // Lưu vào context
        ctx.setDepartureStationId(depId);
        ctx.setDepartureStationName(departureCombo.getValue());
        ctx.setArrivalStationId(arrId);
        ctx.setArrivalStationName(arrivalCombo.getValue());
        ctx.setDepartureDate(depDate);
        ctx.setReturnDate(isRound ? retDate : null);
        ctx.setRoundTrip(isRound);
        ctx.getOutboundSeats().clear();
        ctx.getReturnSeats().clear();
        ctx.getPassengers().clear();

        searchButton.setDisable(true);
        searchButton.setText("Đang tìm...");
        searchStatusLabel.setText("");
        trainSectionsBox.getChildren().clear();

        new Thread(() -> {
            try {
                List<ScheduleInfoResponse> go =
                        ticketService.getSchedulesWithAvailableSeats(depId, arrId, depDate);
                List<ScheduleInfoResponse> ret = isRound && retDate != null
                        ? ticketService.getSchedulesWithAvailableSeats(arrId, depId, retDate)
                        : new ArrayList<>();

                Platform.runLater(() -> {
                    searchButton.setDisable(false);
                    searchButton.setText("Tìm kiếm");

                    if (go.isEmpty() && ret.isEmpty()) {
                        showError(searchStatusLabel, "Không tìm thấy chuyến tàu phù hợp.");
                        return;
                    }

                    goList  = go;
                    retList = ret;
                    scheduleSegmentMap.clear();
                    go.forEach(s  -> scheduleSegmentMap.put(s.getScheduleId(), "outbound"));
                    ret.forEach(s -> scheduleSegmentMap.put(s.getScheduleId(), "return"));

                    String header = "Chiều đi: ngày " + depDate.format(DATEFMT)
                            + "  " + ctx.getDepartureStationName()
                            + " → " + ctx.getArrivalStationName();
                    if (isRound && retDate != null)
                        header += "   |   Chiều về: ngày " + retDate.format(DATEFMT);
                    resultHeaderLabel.setText(header);
                    resultHeaderBox.setVisible(true);
                    resultHeaderBox.setManaged(true);

                    buildSections(isRound);
                    trainScrollPane.setVisible(true);
                    trainScrollPane.setManaged(true);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    searchButton.setDisable(false);
                    searchButton.setText("Tìm kiếm");
                    showError(searchStatusLabel, "Lỗi: " + e.getMessage());
                });
            }
        }).start();
    }

    private void buildSections(boolean isRound) {
        trainSectionsBox.getChildren().clear();
        trainSectionsBox.setSpacing(20);

        if (!goList.isEmpty()) {
            String lblText = "TUYẾN " + ctx.getDepartureStationName().toUpperCase() + " → " + ctx.getArrivalStationName().toUpperCase() + ", NGÀY " + ctx.getDepartureDate().format(DATEFMT);
            Label title = new Label(lblText.toUpperCase());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0077c8;");
            
            HBox titleBox = new HBox(title);
            titleBox.setPadding(new Insets(15, 10, 15, 10));
            titleBox.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #0077c8; -fx-border-width: 0 0 0 4px;");

            VBox cards = new VBox(10);
            cards.setAlignment(Pos.TOP_CENTER);
            goList.forEach(s -> cards.getChildren().add(buildCard(s, "outbound")));
            VBox sec = new VBox(10, titleBox, cards);
            sec.setPadding(new Insets(10));
            sec.setStyle("-fx-background-color: transparent;");
            trainSectionsBox.getChildren().add(sec);
        }

        if (isRound && !retList.isEmpty()) {
            String lblText = "TUYẾN " + ctx.getArrivalStationName().toUpperCase() + " → " + ctx.getDepartureStationName().toUpperCase() + ", NGÀY " + ctx.getReturnDate().format(DATEFMT);
            Label title = new Label(lblText.toUpperCase());
            title.setStyle("-fx-font-weight: bold; -fx-font-size: 16px; -fx-text-fill: #0077c8;");

            HBox titleBox = new HBox(title);
            titleBox.setPadding(new Insets(15, 10, 15, 10));
            titleBox.setStyle("-fx-background-color: #e8f4f8; -fx-border-color: #0077c8; -fx-border-width: 0 0 0 4px;");

            VBox cards = new VBox(10);
            cards.setAlignment(Pos.TOP_CENTER);
            retList.forEach(s -> cards.getChildren().add(buildCard(s, "return")));
            VBox sec = new VBox(10, titleBox, cards);
            sec.setPadding(new Insets(10));
            sec.setStyle("-fx-background-color: transparent;");
            trainSectionsBox.getChildren().add(sec);
        }
    }

    private Pane buildCard(ScheduleInfoResponse s, String segment) {
        boolean isSel = ("outbound".equals(segment) && ctx.getOutboundSchedule() != null
                && ctx.getOutboundSchedule().getScheduleId().equals(s.getScheduleId()))
                || ("return".equals(segment) && ctx.getReturnSchedule() != null
                && ctx.getReturnSchedule().getScheduleId().equals(s.getScheduleId()));

        HBox card = new HBox(20);
        card.setAlignment(Pos.CENTER_LEFT);
        card.setPadding(new Insets(15, 20, 15, 20));
        card.setStyle("-fx-background-color: white; -fx-background-radius: 8px; -fx-border-color: " + (isSel ? "#ffc107" : "#e0e0e0") + "; -fx-border-radius: 8px; -fx-border-width: " + (isSel ? "2px" : "1px") + "; -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.1), 5, 0, 0, 2);");
        
        // 1. Logo / Train ID
        VBox trainIdBox = new VBox(5);
        trainIdBox.setAlignment(Pos.CENTER);
        trainIdBox.setPrefWidth(100);
        Label vnrLabel = new Label("VND");
        vnrLabel.setStyle("-fx-text-fill: #0077c8; -fx-font-weight: bold; -fx-font-style: italic;");
        Label trId = new Label(s.getTrainId());
        trId.setStyle("-fx-font-weight: bold; -fx-font-size: 16px;");
        trainIdBox.getChildren().addAll(vnrLabel, trId);

        // 2. Schedule Info
        VBox scheduleBox = new VBox(5);
        scheduleBox.setAlignment(Pos.CENTER);
        HBox.setHgrow(scheduleBox, Priority.ALWAYS);
        
        HBox timeBox = new HBox(20);
        timeBox.setAlignment(Pos.CENTER);
        
        VBox depBox = new VBox(5);
        depBox.setAlignment(Pos.CENTER);
        Label depTime = new Label(s.getDepartureTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        depTime.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label depDate = new Label(s.getDepartureTime().format(DateTimeFormatter.ofPattern("dd/MM")));
        HBox dH = new HBox(5, depTime, depDate);
        dH.setAlignment(Pos.CENTER);
        Label depStation = new Label(s.getDepartureStationName());
        depStation.setStyle("-fx-font-weight: bold;");
        depBox.getChildren().addAll(dH, depStation);
        
        VBox midBox = new VBox(2);
        midBox.setAlignment(Pos.CENTER);
        ImageView trainIcon = new ImageView(new Image(App.class.getResourceAsStream("img/train.png")));
        trainIcon.setFitHeight(30);
        trainIcon.setFitWidth(30);
        trainIcon.setPreserveRatio(true);
        long minutes = java.time.Duration.between(s.getDepartureTime(), s.getArrivalTime()).toMinutes();
        String durStr = (minutes/60) + " giờ " + (minutes%60) + " phút";
        Label durLabel = new Label(durStr);
        durLabel.setStyle("-fx-text-fill: gray; -fx-font-size: 12px;");
        Label line = new Label("-----------------------");
        line.setStyle("-fx-text-fill: #ccc;");
        midBox.getChildren().addAll(trainIcon, line, durLabel);

        VBox arrBox = new VBox(5);
        arrBox.setAlignment(Pos.CENTER);
        Label arrTime = new Label(s.getArrivalTime().format(DateTimeFormatter.ofPattern("HH:mm")));
        arrTime.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 15px;");
        Label arrDate = new Label(s.getArrivalTime().format(DateTimeFormatter.ofPattern("dd/MM")));
        HBox aH = new HBox(5, arrTime, arrDate);
        aH.setAlignment(Pos.CENTER);
        Label arrStation = new Label(s.getArrivalStationName());
        arrStation.setStyle("-fx-font-weight: bold;");
        arrBox.getChildren().addAll(aH, arrStation);
        
        timeBox.getChildren().addAll(depBox, midBox, arrBox);
        scheduleBox.getChildren().add(timeBox);

        // 3. Seats
        VBox seatBox = new VBox(5);
        seatBox.setAlignment(Pos.CENTER);
        seatBox.setPrefWidth(120);
        Label seatL = new Label("Chỗ còn");
        seatL.setStyle("-fx-font-weight: bold;");
        Label seatCount = new Label(String.valueOf(s.getAvailableSeatCount()));
        seatCount.setStyle("-fx-text-fill: red; -fx-font-weight: bold; -fx-font-size: 16px;");
        seatBox.getChildren().addAll(seatL, seatCount);

        // 4. Select button
        Button btn = new Button(isSel ? "Đã chọn" : "→ Chọn");
        btn.setStyle(isSel ? "-fx-background-color: #28a745; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;" : "-fx-background-color: #0077c8; -fx-text-fill: white; -fx-font-weight: bold; -fx-cursor: hand;");
        btn.setPrefWidth(100);
        btn.setPrefHeight(35);
        btn.setOnAction(e -> onSelectTrain(s, segment));
        
        if (s.getAvailableSeatCount() <= 0) {
            btn.setDisable(true);
            btn.setText("Hết chỗ");
            card.setOpacity(0.6);
        }

        card.getChildren().addAll(trainIdBox, scheduleBox, seatBox, btn);
        return card;
    }

    private void onSelectTrain(ScheduleInfoResponse s, String segment) {
        if ("outbound".equals(segment)) {
            ctx.setOutboundSchedule(s);
            ctx.getOutboundSeats().clear();
        } else {
            ctx.setReturnSchedule(s);
            ctx.getReturnSeats().clear();
        }
        ctx.getPassengers().clear();

        // Rebuild để highlight
        buildSections(ctx.isRoundTrip());

        // Chuyển sang SelectSeat nếu đã chọn đủ
        boolean ready = ctx.getOutboundSchedule() != null
                && (!ctx.isRoundTrip() || ctx.getReturnSchedule() != null);
        if (ready) navigateTo("/iuh/fit/gui/ticket/seat/select-seat-view.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource(fxml));
            StackPane content = (StackPane) resultHeaderBox.getScene()
                    .lookup("#contentContainer");
            if (content != null) {
                javafx.scene.Scene scene = content.getScene();
                content.getChildren().setAll(root);
                AppTheme.applyTo(scene);
            }
        } catch (IOException e) {
            showError(statusLabel, "Lỗi chuyển màn hình: " + e.getMessage());
        }
    }

    private String fmt(ScheduleInfoResponse s, boolean dep) {
        var t = dep ? s.getDepartureTime() : s.getArrivalTime();
        return t != null ? t.format(SHORT) : "--";
    }

    private String money(double v) { return CURRENCY.format((long) v); }

    private void showError(Label l, String msg) {
        l.setText(msg); l.setStyle("-fx-text-fill: #dc2626;");
    }
}