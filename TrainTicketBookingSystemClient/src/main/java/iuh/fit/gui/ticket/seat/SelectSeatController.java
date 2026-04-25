package iuh.fit.gui.ticket.seat;

import iuh.fit.App;
import iuh.fit.context.TicketContext;
import iuh.fit.constance.AppTheme;
import iuh.fit.dto.SeatsInfoResponse;
import iuh.fit.service.TicketClientService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.layout.*;
import model.entity.Seat;
import model.entity.enums.CustomerType;
import model.entity.enums.SeatType;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

public class SelectSeatController {

    @FXML private Label      breadcrumbLabel;
    @FXML private HBox       segmentTabBox;
    @FXML private Button     outboundTab;
    @FXML private Button     returnTab;
    @FXML private VBox       carriageSection;
    @FXML private Label      carriageMapLabel;
    @FXML private HBox       carriageBox;
    @FXML private VBox       seatSection;
    @FXML private Label      seatMapTitle;
    @FXML private GridPane   seatGrid;
    @FXML private Label      statusLabel;
    @FXML private Label      countLabel;
    @FXML private Button     nextBtn;
    @FXML private VBox       cartBox;
    @FXML private Label      cartEmptyLabel;
    @FXML private VBox       cartTotalBox;
    @FXML private Label      cartTotalLabel;

    private final TicketClientService ticketService = new TicketClientService();
    private final TicketContext ctx = TicketContext.getInstance();

    private static final int MAX = 6;
    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter SHORT =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    private String activeSegment = "outbound";
    private Map<Integer, List<Seat>> carriageMap = new TreeMap<>();
    private Map<Integer, List<Seat>> allCarriageMap = new TreeMap<>(); // Tất cả ghế (available + booked)
    private Set<String> bookedSeatIds = new HashSet<>(); // Ghế đã đặt
    private int selectedCarriage = -1;
    private final Map<String, Button> seatBtnMap = new HashMap<>();

    @FXML
    private void initialize() {
        // Hiện tab khứ hồi
        if (ctx.isRoundTrip() && ctx.getReturnSchedule() != null) {
            segmentTabBox.setVisible(true);
            segmentTabBox.setManaged(true);
        }
        loadSegment("outbound");
    }

    @FXML private void onTabOutbound() { loadSegment("outbound"); }
    @FXML private void onTabReturn()   { loadSegment("return"); }

    private void loadSegment(String segment) {
        activeSegment = segment;
        updateTabs();

        var schedule = segment.equals("outbound")
                ? ctx.getOutboundSchedule() : ctx.getReturnSchedule();
        if (schedule == null) return;

        ctx.setCurrentDistance(schedule.getDistance());

        breadcrumbLabel.setText(
                (segment.equals("outbound") ? "Chiều đi — " : "Chiều về — ")
                        + schedule.getTrainName() + "  "
                        + schedule.getDepartureStationName()
                        + " → " + schedule.getArrivalStationName()
                        + "  " + (schedule.getDepartureTime() != null
                        ? schedule.getDepartureTime().format(SHORT) : "--"));

        seatSection.setVisible(false); seatSection.setManaged(false);
        carriageBox.getChildren().clear();
        seatBtnMap.clear();
        statusLabel.setText("Đang tải ghế...");

        new Thread(() -> {
            try {
                // Lấy tất cả ghế (available + booked)
                SeatsInfoResponse seatsInfo = ticketService.getSeatsInfoForSchedule(schedule.getScheduleId());
                
                Platform.runLater(() -> {
                    statusLabel.setText("");
                    bookedSeatIds = new HashSet<>(seatsInfo.getBookedSeatIds() != null ? seatsInfo.getBookedSeatIds() : List.of());
                    
                    // Tổ chức ghế theo toa
                    allCarriageMap = seatsInfo.getSeats().stream().collect(Collectors.groupingBy(
                            s -> s.getCarriage().getCarriageNumber(),
                            TreeMap::new, Collectors.toList()));
                    
                    if (allCarriageMap.isEmpty()) {
                        showError("Không có ghế."); return;
                    }
                    buildCarriageMap();
                    // Mặc định mở toa đầu tiên
                    int first = ((TreeMap<Integer, List<Seat>>) allCarriageMap).firstKey();
                    showSeatMap(first);
                    updateCart();
                });
            } catch (Exception e) {
                Platform.runLater(() -> showError("Lỗi tải ghế: " + e.getMessage()));
            }
        }).start();
    }

    private void updateTabs() {
        if (outboundTab == null) return;
        outboundTab.getStyleClass().removeAll("segment-tab", "segment-tab-active");
        returnTab.getStyleClass().removeAll("segment-tab", "segment-tab-active");
        if ("outbound".equals(activeSegment)) {
            outboundTab.getStyleClass().add("segment-tab-active");
            returnTab.getStyleClass().add("segment-tab");
        } else {
            outboundTab.getStyleClass().add("segment-tab");
            returnTab.getStyleClass().add("segment-tab-active");
        }
    }

    private void buildCarriageMap() {
        carriageBox.getChildren().clear();
        var schedule = "outbound".equals(activeSegment)
                ? ctx.getOutboundSchedule() : ctx.getReturnSchedule();
        carriageMapLabel.setText("Sơ đồ toa — "
                + (schedule != null ? schedule.getTrainName() : "")
                + "  (Tối đa " + MAX + " ghế/lần đặt)");

        VBox locoBox = new VBox(3);
        locoBox.setAlignment(Pos.CENTER);
        ImageView locoIcon = loadIcon("/iuh/fit/img/Toa_Dau.png", 72, 44);
        Label locoFallback = new Label("TOA DAU");
        locoFallback.setStyle("-fx-font-size:12px; -fx-text-fill: #0077c8; -fx-font-weight: bold;");
        Label locoText = new Label(schedule != null ? schedule.getTrainId() : "Tàu");
        locoText.setStyle("-fx-font-size:12px; -fx-font-weight: bold;");
        locoBox.getChildren().addAll(locoIcon != null ? locoIcon : locoFallback, locoText);
        carriageBox.getChildren().add(locoBox);

        for (var entry : allCarriageMap.entrySet()) {
            int num = entry.getKey();
            List<Seat> seats = entry.getValue();
            boolean sel = num == selectedCarriage;
            SeatType dom = dominant(seats);

            VBox box = new VBox(3);
            box.setAlignment(Pos.CENTER);
            box.setPadding(new Insets(4, 8, 4, 8));
            box.getStyleClass().add(sel ? "carriage-box-selected" : "carriage-box");

            String iconPath;
            iconPath = dom == SeatType.SOFT_SLEEPER
                    ? "/iuh/fit/img/Toa_Giuong_Nam.png"
                    : "/iuh/fit/img/Toa_Ghe_Mem.png";
            ImageView icon = loadIcon(iconPath, 58, 40);
            Label fb = new Label("TOA");
            fb.setStyle("-fx-font-size:12px; -fx-font-weight: bold;");

            Label lbl = new Label(String.valueOf(num));
            lbl.getStyleClass().add(sel ? "carriage-number-sel" : "carriage-number");

            box.getChildren().addAll(icon != null ? icon : fb, lbl);
            box.setOnMouseClicked(_ -> onClickCarriage(num));
            box.setStyle("-fx-cursor:hand;");
            carriageBox.getChildren().add(box);
        }
    }

    private void onClickCarriage(int num) {
        showSeatMap(num);
    }

    private void showSeatMap(int num) {
        selectedCarriage = num;
        buildCarriageMap();

        List<Seat> seats = allCarriageMap.get(num);
        if (seats == null || seats.isEmpty()) return;

        SeatType dom = dominant(seats);
        String typeName = dom == SeatType.SOFT_SLEEPER ? "Giường mềm" : "Ghế mềm";
        double price = TicketContext.calcPrice(ctx.getDistance(), dom, CustomerType.ADULT);
        seatMapTitle.setText("Toa " + num + " — " + typeName
                + "  |  Từ " + money(price) + " đ/ghế");

        seatGrid.getChildren().clear();
        seatBtnMap.clear();

        List<Seat> sorted = seats.stream()
                .sorted(Comparator.comparingInt(Seat::getSeatNumber)).toList();
        int rows = Math.max(1, (int) Math.ceil(sorted.size() / 4.0));

        for (int i = 0; i < sorted.size(); i++) {
            Seat seat = sorted.get(i);
            int row = i % rows;
            int col = switch (i / rows) {
                case 0 -> 0; case 1 -> 1; case 2 -> 3; default -> 4;
            };
            Button btn = new Button(String.valueOf(seat.getSeatNumber()));
            
            boolean isBooked = bookedSeatIds.contains(seat.getSeatID());
            boolean isSelected = getActiveSeats().stream().anyMatch(s -> s.getSeatID().equals(seat.getSeatID()));
            
            if (isBooked) {
                // Ghế đã đặt - đỏ, không click được
                btn.getStyleClass().add("seat-btn-booked");
                btn.setDisable(true);
                btn.setStyle("-fx-background-color: #dc3545; -fx-text-fill: white; -fx-cursor: not-allowed;");
            } else if (isSelected) {
                // Ghế đang chọn - xanh lá
                btn.getStyleClass().add("seat-btn-selected");
                btn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            } else {
                // Ghế trống - xám, clickable
                btn.getStyleClass().add("seat-btn-available");
                btn.setStyle("-fx-background-color: #a0a0a0; -fx-text-fill: white;");
            }
            
            btn.setPrefSize(48, 42);
            if (!isBooked) {
                btn.setOnAction(e -> toggleSeat(seat, btn));
            }
            seatGrid.add(btn, col, row);
            seatBtnMap.put(seat.getSeatID(), btn);
        }

        // Lối đi
        for (int r = 0; r < rows; r++) {
            Region aisle = new Region();
            aisle.setPrefWidth(20);
            seatGrid.add(aisle, 2, r);
        }

        // Restore ghế đã chọn
        getActiveSeats().forEach(s -> {
            Button b = seatBtnMap.get(s.getSeatID());
            if (b != null && !bookedSeatIds.contains(s.getSeatID())) {
                b.getStyleClass().remove("seat-btn-available");
                b.getStyleClass().add("seat-btn-selected");
                b.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
            }
        });

        seatSection.setVisible(true);
        seatSection.setManaged(true);
        updateCount();
    }

    private void toggleSeat(Seat seat, Button btn) {
        // Không thể chọn ghế đã đặt
        if (bookedSeatIds.contains(seat.getSeatID())) {
            return;
        }
        
        List<Seat> active = getActiveSeats();
        boolean already = active.stream()
                .anyMatch(s -> s.getSeatID().equals(seat.getSeatID()));
        if (already) {
            active.removeIf(s -> s.getSeatID().equals(seat.getSeatID()));
            btn.getStyleClass().remove("seat-btn-selected");
            btn.getStyleClass().add("seat-btn-available");
            btn.setStyle("-fx-background-color: #a0a0a0; -fx-text-fill: white;");
        } else {
            if (active.size() >= MAX) {
                showError("Tối đa " + MAX + " ghế!"); return;
            }
            statusLabel.setText("");
            active.add(seat);
            btn.getStyleClass().remove("seat-btn-available");
            btn.getStyleClass().add("seat-btn-selected");
            btn.setStyle("-fx-background-color: #28a745; -fx-text-fill: white;");
        }
        updateCount();
        updateCart();
    }

    private void updateCount() {
        List<Seat> active = getActiveSeats();
        int cnt = active.size();
        countLabel.setText(cnt > 0 ? "Đã chọn " + cnt + "/" + MAX + " ghế" : "");

        boolean outDone = !ctx.getOutboundSeats().isEmpty();
        boolean retDone = !ctx.isRoundTrip() || !ctx.getReturnSeats().isEmpty();
        nextBtn.setVisible(outDone && retDone);
        nextBtn.setManaged(outDone && retDone);
    }

    private void updateCart() {
        cartBox.getChildren().clear();
        double total = 0;
        double dist = ctx.getDistance();
        boolean hasAny = false;

        if (!ctx.getOutboundSeats().isEmpty() && ctx.getOutboundSchedule() != null) {
            hasAny = true;
            Label seg = new Label("→ " + ctx.getOutboundSchedule().getTrainId());
            seg.getStyleClass().add("cart-segment-label");
            cartBox.getChildren().add(seg);
            for (Seat s : ctx.getOutboundSeats()) {
                double p = TicketContext.calcPrice(dist, s.getSeatType(), CustomerType.ADULT);
                total += p;
                cartBox.getChildren().add(buildCartRow(s, p, ctx.getOutboundSeats()));
            }
        }

        if (!ctx.getReturnSeats().isEmpty() && ctx.getReturnSchedule() != null) {
            hasAny = true;
            Label seg = new Label("← " + ctx.getReturnSchedule().getTrainId());
            seg.getStyleClass().add("cart-segment-label-return");
            cartBox.getChildren().add(seg);
            for (Seat s : ctx.getReturnSeats()) {
                double p = TicketContext.calcPrice(dist, s.getSeatType(), CustomerType.ADULT);
                total += p;
                cartBox.getChildren().add(buildCartRow(s, p, ctx.getReturnSeats()));
            }
        }

        if (!hasAny) {
            cartBox.getChildren().add(cartEmptyLabel);
            cartTotalBox.setVisible(false); cartTotalBox.setManaged(false);
        } else {
            cartTotalLabel.setText(money(total) + " đ");
            cartTotalBox.setVisible(true); cartTotalBox.setManaged(true);
        }
    }

    private HBox buildCartRow(Seat seat, double price, List<Seat> parent) {
        HBox row = new HBox(6);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(2, 0, 2, 0));

        String t = seat.getSeatType() == SeatType.SOFT_SLEEPER ? "Giường" : "Ghế";
        Label l = new Label(t + " " + seat.getSeatNumber()
                + " • Toa " + seat.getCarriage().getCarriageNumber());
        l.getStyleClass().add("cart-seat");
        l.setWrapText(true);
        HBox.setHgrow(l, Priority.ALWAYS);

        Label lp = new Label(money(price) + " đ");
        lp.getStyleClass().add("cart-price");

        Button rm = new Button("✕");
        rm.getStyleClass().add("cart-remove-btn");
        rm.setOnAction(e -> {
            parent.removeIf(s -> s.getSeatID().equals(seat.getSeatID()));
            Button b = seatBtnMap.get(seat.getSeatID());
            if (b != null) {
                b.getStyleClass().remove("seat-btn-selected");
                b.getStyleClass().add("seat-btn-available");
                b.setStyle("-fx-background-color: #a0a0a0; -fx-text-fill: white;");
            }
            updateCart(); updateCount();
        });

        row.getChildren().addAll(l, lp, rm);
        return row;
    }

    @FXML
    private void onNext() {
        navigateTo("/iuh/fit/gui/ticket/passenger/passenger-info-view.fxml");
    }

    @FXML
    private void onBackToSearch() {
        navigateTo("/iuh/fit/gui/ticket/search/search-schedule-view.fxml");
    }

    private List<Seat> getActiveSeats() {
        return "outbound".equals(activeSegment)
                ? ctx.getOutboundSeats() : ctx.getReturnSeats();
    }

    private SeatType dominant(List<Seat> seats) {
        return seats.stream()
                .collect(Collectors.groupingBy(Seat::getSeatType, Collectors.counting()))
                .entrySet().stream().max(Map.Entry.comparingByValue())
                .map(Map.Entry::getKey).orElse(SeatType.SOFT_SEAT);
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource(fxml));
            StackPane content = (StackPane) seatGrid.getScene()
                    .lookup("#contentContainer");
            if (content != null) {
                AppTheme.applyTo(content.getScene());
                content.getChildren().setAll(root);
            }
        } catch (IOException e) {
            showError("Lỗi chuyển màn hình: " + e.getMessage());
        }
    }

    private ImageView loadIcon(String path, double w, double h) {
        try {
            var s = getClass().getResourceAsStream(path);
            if (s == null) return null;
            ImageView iv = new ImageView(new Image(s));
            iv.setFitWidth(w); iv.setFitHeight(h); iv.setPreserveRatio(true);
            return iv;
        } catch (Exception e) { return null; }
    }

    private String money(double v) { return CURRENCY.format((long) v); }
    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill:#dc2626;");
    }
}
