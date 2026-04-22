package iuh.fit.gui.ticket.cancle;

import dto.ActionResponse;
import iuh.fit.service.TicketClientService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.entity.Ticket;
import model.entity.enums.TicketStatus;

import java.text.NumberFormat;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.time.temporal.ChronoUnit;
import java.util.Locale;

public class CancelTicketController {

    // ── Search ───────────────────────────────────────────────────────
    @FXML private TextField ticketIdField;
    @FXML private Button    searchBtn;
    @FXML private Label     searchStatusLabel;

    // ── Ticket info ───────────────────────────────────────────────────
    @FXML private VBox  ticketInfoBox;
    @FXML private Label statusBadge;
    @FXML private Label trainLabel;
    @FXML private Label routeLabel;
    @FXML private Label timeLabel;
    @FXML private Label ticketIdLabel;
    @FXML private Label createdAtLabel;
    @FXML private Label customerNameLabel;
    @FXML private Label customerCccdLabel;
    @FXML private Label seatLabel;
    @FXML private Label priceLabel;

    // ── Policy ───────────────────────────────────────────────────────
    @FXML private VBox  policyBox;
    @FXML private Label timeRemainingLabel;
    @FXML private Label feePolicyLabel;
    @FXML private Label feeAmountLabel;
    @FXML private Label refundAmountLabel;

    // ── Confirm ──────────────────────────────────────────────────────
    @FXML private VBox      confirmBox;
    @FXML private TextField cccdField;
    @FXML private Label     confirmStatusLabel;
    @FXML private Button    cancelBtn;

    // ── Result ───────────────────────────────────────────────────────
    @FXML private VBox  resultBox;
    @FXML private Label resultIcon;
    @FXML private Label resultTitle;
    @FXML private Label resultMessage;

    // ── State ────────────────────────────────────────────────────────
    private final TicketClientService ticketService = new TicketClientService();
    private Ticket currentTicket;

    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter DT_FMT =
            DateTimeFormatter.ofPattern("HH:mm  dd/MM/yyyy");
    private static final DateTimeFormatter DATE_FMT =
            DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm");

    // ─────────────────────────────────────────────────────────────────
    @FXML
    private void initialize() {
        // Auto uppercase ticket ID
        ticketIdField.textProperty().addListener((obs, o, n) -> {
            if (n != null && !n.equals(n.toUpperCase()))
                ticketIdField.setText(n.toUpperCase());
        });
    }

    // ═══════════════════════════════════════════════════════════════
    //  TRA CỨU VÉ
    // ═══════════════════════════════════════════════════════════════
    @FXML
    private void onSearch() {
        String ticketId = ticketIdField.getText().trim();
        if (ticketId.isEmpty()) {
            showSearchError("Vui lòng nhập mã vé."); return;
        }

        searchBtn.setDisable(true);
        searchBtn.setText("Đang tra cứu...");
        hideAll();

        new Thread(() -> {
            try {
                Ticket ticket = ticketService.getTicketById(ticketId);
                Platform.runLater(() -> {
                    searchBtn.setDisable(false);
                    searchBtn.setText("🔍  Tra cứu");
                    if (ticket == null) {
                        showSearchError("Không tìm thấy vé: " + ticketId);
                    } else {
                        currentTicket = ticket;
                        showTicketInfo(ticket);
                    }
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    searchBtn.setDisable(false);
                    searchBtn.setText("🔍  Tra cứu");
                    showSearchError("Lỗi tra cứu: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showTicketInfo(Ticket ticket) {
        // ── Status badge ──
        TicketStatus status = ticket.getTicketStatus();
        statusBadge.setText(status.name());
        statusBadge.getStyleClass().removeAll(
                "status-badge-paid", "status-badge-pending",
                "status-badge-cancelled", "status-badge-used");
        statusBadge.getStyleClass().add(switch (status) {
            case PAID      -> "status-badge-paid";
            case PENDING   -> "status-badge-pending";
            case CANCELLED -> "status-badge-cancelled";
            case USED      -> "status-badge-used";
        });

        // ── Train info ──
        var schedule = ticket.getSchedule();
        trainLabel.setText(schedule.getTrain().getTrainName()
                + "  (" + schedule.getTrain().getTrainID() + ")");
        routeLabel.setText(
                schedule.getRoute().getDepartureStation().getStationName()
                        + "  →  "
                        + schedule.getRoute().getArrivalStation().getStationName());
        timeLabel.setText(
                (schedule.getDepartureTime() != null
                        ? schedule.getDepartureTime().format(DT_FMT) : "--")
                        + "  →  "
                        + (schedule.getArrivalTime() != null
                        ? schedule.getArrivalTime().format(DT_FMT) : "--"));

        // ── Detail grid ──
        ticketIdLabel.setText(ticket.getTicketID());
        createdAtLabel.setText(ticket.getCreateAt() != null
                ? ticket.getCreateAt().format(DATE_FMT) : "--");
        customerNameLabel.setText(ticket.getCustomer().getFullName());
        customerCccdLabel.setText(ticket.getCustomer().getCustomerID());

        String seatTypeName = switch (ticket.getSeat().getSeatType()) {
            case SOFT_SEAT    -> "Ghế mềm";
            case SOFT_SLEEPER -> "Giường mềm";
        };
        seatLabel.setText("Ghế " + ticket.getSeat().getSeatNumber()
                + " • Toa " + ticket.getSeat().getCarriage().getCarriageNumber()
                + " • " + seatTypeName);

        priceLabel.setText(money(ticket.getFinalPrice()) + " đ");

        // Hiện ticket info
        ticketInfoBox.setVisible(true);
        ticketInfoBox.setManaged(true);

        // ── Kiểm tra có thể hủy không ──
        checkCancelEligibility(ticket);
    }

    private void checkCancelEligibility(Ticket ticket) {
        TicketStatus status = ticket.getTicketStatus();

        // Không hủy được
        if (status == TicketStatus.CANCELLED) {
            showPolicyError("Vé này đã bị hủy từ trước.");
            return;
        }
        if (status == TicketStatus.USED) {
            showPolicyError("Vé đã sử dụng, không thể hủy.");
            return;
        }

        LocalDateTime now = LocalDateTime.now();
        LocalDateTime departure = ticket.getSchedule().getDepartureTime();

        if (departure == null || now.isAfter(departure) || now.isEqual(departure)) {
            showPolicyError("Tàu đã khởi hành, không thể hủy vé.");
            return;
        }

        long minutes = ChronoUnit.MINUTES.between(now, departure);

        if (minutes < 240) { // < 4 tiếng
            showPolicyError("Không thể hủy vé dưới 4 tiếng trước khởi hành.");
            return;
        }

        // Tính phí
        double feePercent;
        String feeDesc;
        if (minutes < 1440) { // 4h - 24h
            feePercent = 0.20;
            feeDesc = "Phí hủy 20% (hủy từ 4 đến 24 tiếng trước khởi hành)";
        } else { // > 24h
            feePercent = 0.10;
            feeDesc = "Phí hủy 10% (hủy trước 24 tiếng)";
        }

        double fee    = ticket.getFinalPrice() * feePercent;
        double refund = ticket.getFinalPrice() - fee;

        // Thời gian còn lại
        long hours = minutes / 60;
        long mins  = minutes % 60;
        String timeLeft = hours > 0
                ? hours + " giờ " + mins + " phút trước khởi hành"
                : mins + " phút trước khởi hành";

        // Hiện policy
        policyBox.setVisible(true);
        policyBox.setManaged(true);
        timeRemainingLabel.setText("⏱ Còn " + timeLeft);
        feePolicyLabel.setText(feeDesc);
        feeAmountLabel.setText(money(fee) + " đ  (" + (int)(feePercent * 100) + "%)");
        refundAmountLabel.setText(money(refund) + " đ");

        // Hiện confirm
        confirmBox.setVisible(true);
        confirmBox.setManaged(true);
    }

    private void showPolicyError(String msg) {
        // Hiện policy box với thông báo lỗi
        policyBox.setVisible(true);
        policyBox.setManaged(true);
        timeRemainingLabel.setText("⚠ " + msg);
        timeRemainingLabel.setStyle("-fx-text-fill: #dc2626; -fx-font-weight: bold;");
        feePolicyLabel.setText("");
        feeAmountLabel.setText("--");
        refundAmountLabel.setText("--");
        // Không hiện confirm box
    }

    // ═══════════════════════════════════════════════════════════════
    //  HỦY VÉ
    // ═══════════════════════════════════════════════════════════════
    @FXML
    private void onCancelTicket() {
        String cccd = cccdField.getText().trim();
        if (cccd.isEmpty()) {
            showConfirmError("Vui lòng nhập CCCD để xác minh."); return;
        }
        if (cccd.length() < 9) {
            showConfirmError("CCCD không hợp lệ."); return;
        }
        if (currentTicket == null) return;

        // Confirm dialog
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Xác nhận hủy vé");
        alert.setHeaderText("Bạn có chắc muốn hủy vé "
                + currentTicket.getTicketID() + "?");
        alert.setContentText(
                "Thao tác này KHÔNG THỂ hoàn tác!\n\n"
                        + "Phí hủy sẽ được khấu trừ theo chính sách.\n"
                        + "Tiền hoàn lại sẽ được xử lý sau.");

        alert.showAndWait().ifPresent(btn -> {
            if (btn == ButtonType.OK) performCancel(cccd);
        });
    }

    private void performCancel(String cccd) {
        cancelBtn.setDisable(true);
        cancelBtn.setText("Đang xử lý...");

        new Thread(() -> {
            try {
                ActionResponse resp = ticketService.cancelTicket(
                        currentTicket.getTicketID(), cccd);
                Platform.runLater(() -> {
                    cancelBtn.setDisable(false);
                    cancelBtn.setText("✗  Xác nhận hủy vé");
                    showResult(resp);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    cancelBtn.setDisable(false);
                    cancelBtn.setText("✗  Xác nhận hủy vé");
                    showConfirmError("Lỗi hệ thống: " + e.getMessage());
                });
            }
        }).start();
    }

    // ═══════════════════════════════════════════════════════════════
    //  RESULT
    // ═══════════════════════════════════════════════════════════════
    private void showResult(ActionResponse resp) {
        ticketInfoBox.setVisible(false); ticketInfoBox.setManaged(false);
        policyBox.setVisible(false);     policyBox.setManaged(false);
        confirmBox.setVisible(false);    confirmBox.setManaged(false);
        resultBox.setVisible(true);      resultBox.setManaged(true);

        if (resp.isSuccess()) {
            resultIcon.setText("✔");
            resultIcon.setStyle("-fx-text-fill: #16a34a;");
            resultTitle.setText("Hủy vé thành công!");
            resultTitle.setStyle("-fx-text-fill: #16a34a;");
            resultMessage.setText(resp.getMessage());
        } else {
            resultIcon.setText("✗");
            resultIcon.setStyle("-fx-text-fill: #dc2626;");
            resultTitle.setText("Hủy vé thất bại");
            resultTitle.setStyle("-fx-text-fill: #dc2626;");
            resultMessage.setText(resp.getMessage());
        }
    }

    // ═══════════════════════════════════════════════════════════════
    //  RESET
    // ═══════════════════════════════════════════════════════════════
    @FXML
    private void onReset() {
        currentTicket = null;
        ticketIdField.clear();
        cccdField.clear();
        searchStatusLabel.setText("");
        confirmStatusLabel.setText("");
        timeRemainingLabel.setStyle("");
        hideAll();
    }

    // ═══════════════════════════════════════════════════════════════
    //  HELPERS
    // ═══════════════════════════════════════════════════════════════
    private void hideAll() {
        ticketInfoBox.setVisible(false); ticketInfoBox.setManaged(false);
        policyBox.setVisible(false);     policyBox.setManaged(false);
        confirmBox.setVisible(false);    confirmBox.setManaged(false);
        resultBox.setVisible(false);     resultBox.setManaged(false);
        searchStatusLabel.setVisible(false); searchStatusLabel.setManaged(false);
        confirmStatusLabel.setVisible(false); confirmStatusLabel.setManaged(false);
    }

    private void showSearchError(String msg) {
        searchStatusLabel.setText(msg);
        searchStatusLabel.setStyle("-fx-text-fill: #dc2626;");
        searchStatusLabel.setVisible(true);
        searchStatusLabel.setManaged(true);
    }

    private void showConfirmError(String msg) {
        confirmStatusLabel.setText(msg);
        confirmStatusLabel.setStyle("-fx-text-fill: #dc2626;");
        confirmStatusLabel.setVisible(true);
        confirmStatusLabel.setManaged(true);
    }

    private String money(double v) { return CURRENCY.format((long) v); }
}