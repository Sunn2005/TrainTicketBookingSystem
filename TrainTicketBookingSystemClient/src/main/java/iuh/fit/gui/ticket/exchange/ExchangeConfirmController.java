package iuh.fit.gui.ticket.exchange;

import dto.ActionResponse;
import iuh.fit.App;
import iuh.fit.constance.AppTheme;
import iuh.fit.context.TicketContext;
import iuh.fit.service.TicketClientService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.entity.Seat;
import model.entity.enums.CustomerType;
import model.entity.enums.SeatType;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Locale;

public class ExchangeConfirmController {

    // ── Step bar ──────────────────────────────────────────────────────────────
    @FXML private Label stepLookup;
    @FXML private Label stepChoose;
    @FXML private Label stepConfirm;

    // ── Old ticket ────────────────────────────────────────────────────────────
    @FXML private Label oldTicketIdLabel;
    @FXML private Label oldSeatLabel;
    @FXML private Label oldPriceLabel;

    // ── New info ──────────────────────────────────────────────────────────────
    @FXML private Label newTrainLabel;
    @FXML private Label newRouteLabel;
    @FXML private Label newDepartureLabel;
    @FXML private Label newSeatLabel;
    @FXML private Label newPriceLabel;

    // ── Fee ───────────────────────────────────────────────────────────────────
    @FXML private Label feeRuleLabel;
    @FXML private Label feeAmountLabel;

    // ── Actions ───────────────────────────────────────────────────────────────
    @FXML private Button confirmBtn;
    @FXML private Label  statusLabel;

    @FXML private VBox  resultBox;
    @FXML private Label resultIcon;
    @FXML private Label resultTitle;
    @FXML private Label resultMessage;

    // ── Deps ──────────────────────────────────────────────────────────────────
    private final TicketClientService ticketService = new TicketClientService();
    private final TicketContext ctx = TicketContext.getInstance();
    private static final NumberFormat VND =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    private void initialize() {
        setStep(3);
        populateOldInfo();
        populateNewInfo();
        feeRuleLabel.setText("Phí sẽ được tính khi xác nhận");
        feeAmountLabel.setText("—");

        // Ẩn result box ban đầu
        resultBox.setVisible(false);
        resultBox.setManaged(false);

        boolean ready = ctx.getOutboundSchedule() != null
                && !ctx.getOutboundSeats().isEmpty();
        confirmBtn.setDisable(!ready);
        if (!ready) showError("Vui lòng quay lại chọn chuyến và ghế mới.");
    }

    // ── Populate ──────────────────────────────────────────────────────────────
    private void populateOldInfo() {
        oldTicketIdLabel.setText(
                ctx.getExchangeTicketId() != null ? ctx.getExchangeTicketId() : "—");

        // Load chi tiết vé cũ từ server (background)
        String ticketId = ctx.getExchangeTicketId();
        if (ticketId == null || ticketId.isBlank()) return;

        new Thread(() -> {
            var ticket = ticketService.getTicketById(ticketId);
            Platform.runLater(() -> {
                if (ticket == null) return;
                oldTicketIdLabel.setText(ticket.getTicketID());
                if (ticket.getSeat() != null) {
                    var s = ticket.getSeat();
                    String toa  = s.getCarriage() != null
                            ? String.valueOf(s.getCarriage().getCarriageNumber()) : "?";
                    String type = s.getSeatType() != null ? s.getSeatType().name() : "?";
                    oldSeatLabel.setText(
                            "Ghế " + s.getSeatNumber() + " • Toa " + toa + " • " + type);
                }
                oldPriceLabel.setText(VND.format((long) ticket.getPrice()) + " đ");
            });
        }).start();
    }

    private void populateNewInfo() {
        var schedule = ctx.getOutboundSchedule();
        if (schedule == null) {
            newTrainLabel.setText("—"); newRouteLabel.setText("—");
            newDepartureLabel.setText("—"); newSeatLabel.setText("(Chưa chọn)");
            newPriceLabel.setText("—"); return;
        }
        newTrainLabel.setText(schedule.getTrainId()
                + (schedule.getTrainName() != null ? " — " + schedule.getTrainName() : ""));
        newRouteLabel.setText(schedule.getDepartureStationName()
                + " → " + schedule.getArrivalStationName());
        newDepartureLabel.setText(schedule.getDepartureTime() != null
                ? schedule.getDepartureTime().format(DT) : "—");

        List<Seat> seats = ctx.getOutboundSeats();
        if (seats.isEmpty()) {
            newSeatLabel.setText("(Chưa chọn ghế)"); newPriceLabel.setText("—"); return;
        }
        StringBuilder sb = new StringBuilder();
        double total = 0;
        for (Seat s : seats) {
            if (!sb.isEmpty()) sb.append(", ");
            sb.append("Ghế ").append(s.getSeatNumber());
            if (s.getCarriage() != null)
                sb.append(" Toa ").append(s.getCarriage().getCarriageNumber());
            total += TicketContext.calcPrice(
                    ctx.getDistance(), s.getSeatType(), CustomerType.ADULT);
        }
        newSeatLabel.setText(sb.toString());
        newPriceLabel.setText(VND.format((long) total) + " đ");
    }

    // ── Fee calculation ───────────────────────────────────────────────────────
    private void calculateFee() {
        var schedule = ctx.getOutboundSchedule();
        List<Seat> seats = ctx.getOutboundSeats();

        double basePrice = 0;
        for (Seat s : seats)
            basePrice += TicketContext.calcPrice(
                    ctx.getDistance(), s.getSeatType(), CustomerType.ADULT);

        long hoursLeft = Long.MAX_VALUE;
        if (schedule != null && schedule.getDepartureTime() != null) {
            hoursLeft = java.time.Duration
                    .between(LocalDateTime.now(), schedule.getDepartureTime())
                    .toHours();
        }

        double rate;
        String rule;
        if (hoursLeft >= 24)      { rate = 0.00; rule = "Còn hơn 24 giờ → Miễn phí (0%)"; }
        else if (hoursLeft >= 6)  { rate = 0.10; rule = "Còn " + hoursLeft + "h → Phí 10%"; }
        else if (hoursLeft >= 2)  { rate = 0.20; rule = "Còn " + hoursLeft + "h → Phí 20%"; }
        else                      { rate = 0.30; rule = "Dưới 2 giờ → Phí 30%"; }

        double fee = basePrice * rate;
        feeRuleLabel.setText(rule);
        feeAmountLabel.setText(VND.format((long) fee) + " đ");

        boolean ready = schedule != null && !seats.isEmpty();
        confirmBtn.setDisable(!ready);
        if (!ready) showError("Vui lòng quay lại chọn chuyến và ghế mới.");
    }

    // ── Actions ───────────────────────────────────────────────────────────────
    @FXML
    private void onConfirm() {
        String ticketId  = ctx.getExchangeTicketId();
        var    schedule  = ctx.getOutboundSchedule();
        List<Seat> seats = ctx.getOutboundSeats();

        if (ticketId == null || schedule == null || seats.isEmpty()) {
            showError("Thông tin không đầy đủ."); return;
        }

        Seat newSeat = seats.get(0);
        confirmBtn.setDisable(true);
        confirmBtn.setText("Đang xử lý…");
        clearStatus();

        new Thread(() -> {
            ActionResponse res = ticketService.exchangeTicket(
                    ticketId,
                    schedule.getScheduleId(),
                    newSeat.getSeatID());

            Platform.runLater(() -> {
                confirmBtn.setDisable(false);
                confirmBtn.setText("Xác nhận đổi vé");

                if (res != null && res.isSuccess()) {
                    ctx.setExchangeMode(false);
                    ctx.setExchangeTicketId(null);
                    ctx.getOutboundSeats().clear();
                    ctx.setOutboundSchedule(null);

                    // Hiện màn hình kết quả thành công
                    showResultBox(true, res.getMessage());
                } else {
                    showError(res != null ? res.getMessage() : "Lỗi kết nối");
                }
            });
        }).start();
    }

    private void showResultBox(boolean success, String message) {
        // Ẩn nút confirm
        confirmBtn.setVisible(false);
        confirmBtn.setManaged(false);

        // Hiện result box
        resultBox.setVisible(true);
        resultBox.setManaged(true);

        if (success) {
            resultIcon.setText("✔");
            resultIcon.setStyle("-fx-text-fill:#16a34a; -fx-font-size:48px;");
            resultTitle.setText("Đổi vé thành công!");
            resultTitle.setStyle(
                    "-fx-text-fill:#16a34a; -fx-font-size:20px; -fx-font-weight:bold;");
            // Hiện chi tiết phí từ server
            feeRuleLabel.setText("Đã xác nhận bởi server");
            feeAmountLabel.setText(message);
        } else {
            resultIcon.setText("✗");
            resultIcon.setStyle("-fx-text-fill:#dc2626; -fx-font-size:48px;");
            resultTitle.setText("Đổi vé thất bại");
            resultTitle.setStyle(
                    "-fx-text-fill:#dc2626; -fx-font-size:20px; -fx-font-weight:bold;");
        }
        resultMessage.setText(message);
        resultMessage.setStyle("-fx-font-size:13px; -fx-text-fill:#555; -fx-text-alignment:center;");
    }
    @FXML private void onBack() {
        navigateTo("/iuh/fit/gui/ticket/seat/select-seat-view.fxml");
    }

    @FXML private void onCancel() {
        ctx.setExchangeMode(false);
        ctx.setExchangeTicketId(null);
        ctx.getOutboundSeats().clear();
        ctx.setOutboundSchedule(null);
        navigateTo("/iuh/fit/gui/ticket/search/search-schedule-view.fxml");
    }

    // ── Utils ─────────────────────────────────────────────────────────────────
    private void setStep(int step) {
        String active   = "-fx-background-color:#0077c8;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:5 16 5 16;";
        String inactive = "-fx-background-color:#e0e0e0;-fx-text-fill:#888;"
                + "-fx-background-radius:20;-fx-padding:5 16 5 16;";
        stepLookup.setStyle(step == 1 ? active : inactive);
        stepChoose.setStyle(step == 2 ? active : inactive);
        stepConfirm.setStyle(step == 3 ? active : inactive);
    }

    private void clearStatus() { statusLabel.setText(""); statusLabel.setStyle(""); }
    private void showError(String msg) {
        statusLabel.setText("⚠ " + msg);
        statusLabel.setStyle("-fx-text-fill:#dc2626;");
    }
    private void showSuccess(String msg) {
        statusLabel.setText("✔ " + msg);
        statusLabel.setStyle("-fx-text-fill:#16a34a;");
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource(fxml));
            StackPane content = (StackPane) confirmBtn.getScene()
                    .lookup("#contentContainer");
            if (content != null) {
                AppTheme.applyTo(content.getScene());
                content.getChildren().setAll(root);
            }
        } catch (IOException e) { showError("Lỗi: " + e.getMessage()); }
    }
}