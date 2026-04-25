package iuh.fit.gui.ticket.exchange;

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
import model.entity.Payment;
import model.entity.Ticket;
import model.entity.enums.PaymentStatus;
import model.entity.enums.TicketStatus;

import java.io.IOException;
import java.text.NumberFormat;
import java.util.Locale;

public class ExchangeTicketController {

    // ── Step bar ──────────────────────────────────────────────────────────────
    @FXML private Label stepLookup;
    @FXML private Label stepChoose;
    @FXML private Label stepConfirm;

    // ── Form ──────────────────────────────────────────────────────────────────
    @FXML private TextField ticketIdField;
    @FXML private TextField cccdField;
    @FXML private Button    lookupBtn;
    @FXML private Label     statusLabel;

    // ── Ticket card ───────────────────────────────────────────────────────────
    @FXML private VBox   ticketInfoCard;
    @FXML private Label  lblTicketId;
    @FXML private Label  lblTrain;
    @FXML private Label  lblRoute;
    @FXML private Label  lblDeparture;
    @FXML private Label  lblSeat;
    @FXML private Label  lblPrice;
    @FXML private Label  lblPaymentStatus;
    @FXML private Label  lblTicketStatus;
    @FXML private Button continueBtn;

    // ── Deps ──────────────────────────────────────────────────────────────────
    private final TicketClientService ticketService = new TicketClientService();
    private final TicketContext       ctx           = TicketContext.getInstance();
    private static final NumberFormat VND =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final java.time.format.DateTimeFormatter DT =
            java.time.format.DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    private Ticket  foundTicket  = null;
    private Payment foundPayment = null; // ← lưu payment riêng

    // ─────────────────────────────────────────────────────────────────────────
    @FXML
    private void initialize() {
        setStep(1);
        hideCard();
        if (ctx.isExchangeMode()
                && ctx.getExchangeTicketId() != null
                && !ctx.getExchangeTicketId().isBlank()) {
            ticketIdField.setText(ctx.getExchangeTicketId());
        }
    }

    // ── Tra cứu vé ────────────────────────────────────────────────────────────
    @FXML
    private void onLookup() {
        String ticketId = ticketIdField.getText().trim();
        String cccd     = cccdField.getText().trim();

        if (ticketId.isEmpty()) {
            showError("Vui lòng nhập mã vé.");
            return;
        }

        clearStatus();
        lookupBtn.setDisable(true);
        lookupBtn.setText("Đang tra cứu…");
        hideCard();

        new Thread(() -> {
            // Gọi song song cả ticket và payment
            Ticket  ticket  = ticketService.getTicketById(ticketId);
            Payment payment = ticket != null
                    ? ticketService.getPaymentByTicketId(ticketId)
                    : null;

            Platform.runLater(() -> {
                lookupBtn.setDisable(false);
                lookupBtn.setText("Tra cứu");

                if (ticket == null) {
                    showError("Không tìm thấy vé: " + ticketId);
                    return;
                }

                // Kiểm tra CCCD
                if (!cccd.isEmpty()
                        && ticket.getCustomer() != null
                        && !cccd.equals(ticket.getCustomer().getCustomerID())) {
                    showError("CCCD không khớp với vé này.");
                    return;
                }

                // Validate dùng cả ticket + payment
                String err = validateTicket(ticket, payment);
                if (err != null) {
                    showError(err);
                    return;
                }

                foundTicket  = ticket;
                foundPayment = payment;
                populateCard(ticket, payment);
                showCard();
            });
        }).start();
    }

    // ── Validate ──────────────────────────────────────────────────────────────

    private String validateTicket(Ticket t, Payment payment) {
        if (t.getTicketStatus() == TicketStatus.CANCELLED)
            return "Vé đã hủy, không thể đổi.";

        if (t.getTicketStatus() == TicketStatus.USED)
            return "Vé đã sử dụng, không thể đổi.";

        if (t.getTicketStatus() != TicketStatus.PAID)
            return "Vé chưa thanh toán (trạng thái: "
                    + t.getTicketStatus() + "), không thể đổi.";

        if ("EXCHANGED".equalsIgnoreCase(t.getDiscount()))
            return "Vé này đã được đổi 1 lần, không thể đổi thêm.";

        return null;
    }
    // ── Hiển thị thông tin vé lên card ───────────────────────────────────────
    private void populateCard(Ticket t, Payment payment) {
        lblTicketId.setText(t.getTicketID());

        if (t.getSchedule() != null) {
            var sch = t.getSchedule();
            lblTrain.setText(sch.getTrain() != null
                    ? sch.getTrain().getTrainID() + " — " + sch.getTrain().getTrainName()
                    : "—");
            lblRoute.setText(sch.getRoute() != null
                    ? sch.getRoute().getDepartureStation().getStationName()
                    + " → " + sch.getRoute().getArrivalStation().getStationName()
                    : "—");
            lblDeparture.setText(sch.getDepartureTime() != null
                    ? sch.getDepartureTime().format(DT) : "—");
        } else {
            lblTrain.setText("—");
            lblRoute.setText("—");
            lblDeparture.setText("—");
        }

        if (t.getSeat() != null) {
            var s = t.getSeat();
            String type = s.getSeatType() != null ? s.getSeatType().name() : "?";
            String toa  = s.getCarriage() != null
                    ? String.valueOf(s.getCarriage().getCarriageNumber()) : "?";
            lblSeat.setText("Ghế " + s.getSeatNumber() + " • Toa " + toa + " • " + type);
        } else {
            lblSeat.setText("—");
        }

        lblPrice.setText(VND.format((long) t.getPrice()) + " đ");

        // FIX: lấy PaymentStatus từ Payment, không từ Ticket
        lblPaymentStatus.setText(
                payment != null && payment.getPaymentStatus() != null
                        ? payment.getPaymentStatus().name() : "—");

        lblTicketStatus.setText(
                t.getTicketStatus() != null ? t.getTicketStatus().name() : "—");
    }

    // ── Tiếp tục sang bước 2 ─────────────────────────────────────────────────
    @FXML
    private void onContinue() {
        if (foundTicket == null) return;

        ctx.setExchangeMode(true);
        ctx.setExchangeTicketId(foundTicket.getTicketID());
        ctx.getOutboundSeats().clear();
        ctx.getReturnSeats().clear();
        ctx.getPassengers().clear();
        ctx.setOutboundSchedule(null);
        ctx.setReturnSchedule(null);
        ctx.setRoundTrip(false);

        navigateTo("/iuh/fit/gui/ticket/search/search-schedule-view.fxml");
    }

    // ── Hủy bỏ ───────────────────────────────────────────────────────────────
    @FXML
    private void onCancel() {
        ctx.setExchangeMode(false);
        ctx.setExchangeTicketId(null);
        navigateTo("/iuh/fit/gui/ticket/search/search-schedule-view.fxml");
    }

    // ── UI Helpers ────────────────────────────────────────────────────────────
    private void setStep(int step) {
        String active   = "-fx-background-color:#0077c8;-fx-text-fill:white;"
                + "-fx-font-weight:bold;-fx-background-radius:20;-fx-padding:5 16 5 16;";
        String inactive = "-fx-background-color:#e0e0e0;-fx-text-fill:#888;"
                + "-fx-background-radius:20;-fx-padding:5 16 5 16;";
        stepLookup.setStyle(step == 1 ? active : inactive);
        stepChoose.setStyle(step == 2 ? active : inactive);
        stepConfirm.setStyle(step == 3 ? active : inactive);
    }

    private void showCard() {
        ticketInfoCard.setVisible(true);  ticketInfoCard.setManaged(true);
        continueBtn.setVisible(true);     continueBtn.setManaged(true);
    }

    private void hideCard() {
        ticketInfoCard.setVisible(false); ticketInfoCard.setManaged(false);
        continueBtn.setVisible(false);    continueBtn.setManaged(false);
    }

    private void clearStatus() {
        statusLabel.setText("");
        statusLabel.setStyle("");
    }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill:#dc2626;");
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource(fxml));
            StackPane content = (StackPane) ticketIdField.getScene()
                    .lookup("#contentContainer");
            if (content != null) {
                AppTheme.applyTo(content.getScene());
                content.getChildren().setAll(root);
            }
        } catch (IOException e) {
            showError("Lỗi chuyển màn hình: " + e.getMessage());
        }
    }
}