package iuh.fit.gui.ticket.search;

import dto.ScheduleInfoResponse;
import iuh.fit.App;
import iuh.fit.constance.AppTheme;
import iuh.fit.gui.ticket.TicketContext;
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
import model.entity.enums.CustomerType;
import model.entity.enums.SeatType;

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
                List<Station> stations = ticketService.getStations();
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

        if (!goList.isEmpty()) {
            Label title = new Label("→  CHUYẾN ĐI");
            title.getStyleClass().add("section-segment-title");
            HBox cards = new HBox(10);
            cards.setAlignment(Pos.CENTER_LEFT);
            goList.forEach(s -> cards.getChildren().add(buildCard(s, "outbound")));
            VBox sec = new VBox(8, title, cards);
            trainSectionsBox.getChildren().add(sec);
        }

        if (isRound && !retList.isEmpty()) {
            Label title = new Label("←  CHUYẾN VỀ");
            title.getStyleClass().add("section-segment-title-return");
            HBox cards = new HBox(10);
            cards.setAlignment(Pos.CENTER_LEFT);
            retList.forEach(s -> cards.getChildren().add(buildCard(s, "return")));
            VBox sec = new VBox(8, title, cards);
            trainSectionsBox.getChildren().add(sec);
        }
    }

    private VBox buildCard(ScheduleInfoResponse s, String segment) {
        boolean isSel = ("outbound".equals(segment) && ctx.getOutboundSchedule() != null
                && ctx.getOutboundSchedule().getScheduleId().equals(s.getScheduleId()))
                || ("return".equals(segment) && ctx.getReturnSchedule() != null
                && ctx.getReturnSchedule().getScheduleId().equals(s.getScheduleId()));

        VBox card = new VBox(4);
        card.setAlignment(Pos.CENTER);
        card.setPrefWidth(148);
        card.setPadding(new Insets(10, 8, 10, 8));
        card.getStyleClass().add(isSel ? "train-card-selected" : "train-card");

        Label nameLabel = new Label(s.getTrainId());
        nameLabel.getStyleClass().add(isSel ? "train-card-name-sel" : "train-card-name");

        ImageView icon = loadIcon(
                isSel ? "/iuh/fit/img/train_blue.png" : "/iuh/fit/img/train_gray.png",
                80, 58);
        Label fallback = new Label(isSel ? "🚆" : "🚂");
        fallback.setStyle("-fx-font-size: 36px;");

        Label depInfo = new Label("TG đi   " + fmt(s, true));
        depInfo.getStyleClass().add("train-card-info");
        Label arrInfo = new Label("TG đến " + fmt(s, false));
        arrInfo.getStyleClass().add("train-card-info");

        double minPrice = TicketContext.calcPrice(
                ctx.getDistance(), SeatType.SOFT_SEAT, CustomerType.ADULT);
        Label priceLabel = new Label("Từ " + money(minPrice) + " đ");
        priceLabel.getStyleClass().add("train-card-price");

        VBox availBox = new VBox(1);
        availBox.setAlignment(Pos.CENTER);
        Label aTitle = new Label("Chỗ trống");
        aTitle.getStyleClass().add("train-card-info");
        Label aVal = new Label(String.valueOf(s.getAvailableSeatCount()));
        aVal.getStyleClass().add(s.getAvailableSeatCount() == 0
                ? "seats-booked" : "seats-available");
        availBox.getChildren().addAll(aTitle, aVal);

        Label wheels = new Label("⚙  ⚙");
        wheels.setStyle("-fx-font-size:13px;-fx-text-fill:#f5c842;");

        card.getChildren().addAll(nameLabel,
                icon != null ? icon : fallback,
                depInfo, arrInfo, priceLabel, availBox, wheels);

        if (s.getAvailableSeatCount() > 0) {
            card.setOnMouseClicked(e -> onSelectTrain(s, segment));
            card.setStyle("-fx-cursor: hand;");
        } else {
            card.setOpacity(0.5);
        }
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

    private ImageView loadIcon(String path, double w, double h) {
        try {
            var st = getClass().getResourceAsStream(path);
            if (st == null) return null;
            ImageView iv = new ImageView(new Image(st));
            iv.setFitWidth(w); iv.setFitHeight(h); iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) { return null; }
    }

    private String money(double v) { return CURRENCY.format((long) v); }

    private void showError(Label l, String msg) {
        l.setText(msg); l.setStyle("-fx-text-fill: #dc2626;");
    }
}