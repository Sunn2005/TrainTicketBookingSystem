package iuh.fit.gui.ticket.confirm;

import dto.ActionResponse;
import dto.SellTicketRequest;
import iuh.fit.App;
import iuh.fit.constance.AppTheme;
import iuh.fit.context.UserContext;
import iuh.fit.gui.ticket.TicketContext;
import iuh.fit.gui.ticket.TicketContext.PassengerInfo;
import iuh.fit.service.TicketClientService;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Parent;
import javafx.scene.control.*;
import javafx.scene.layout.*;
import model.entity.Seat;
import model.entity.enums.CustomerType;
import model.entity.enums.SeatType;

import java.io.IOException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class ConfirmController {

    @FXML private VBox  detailsBox;
    @FXML private Label paymentLabel;
    @FXML private Label totalLabel;
    @FXML private Label statusLabel;
    @FXML private HBox  actionBox;
    @FXML private Button payBtn;
    @FXML private VBox  resultBox;
    @FXML private Label resultIcon;
    @FXML private Label resultTitle;
    @FXML private Label resultMessage;
    @FXML private Label resultQr;
    @FXML private VBox  summaryBox;

    private final TicketContext ctx = TicketContext.getInstance();
    private final TicketClientService ticketService = new TicketClientService();
    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));
    private static final DateTimeFormatter SHORT =
            DateTimeFormatter.ofPattern("dd/MM HH:mm");

    @FXML
    private void initialize() {
        paymentLabel.setText("Phương thức: "
                + (ctx.isQrPayment() ? "QR Code (VietQR)" : "Tiền mặt"));
        buildDetails();
        buildSummary();
    }

    private void buildDetails() {
        detailsBox.getChildren().clear();
        double total = 0;
        double dist  = ctx.getDistance();

        if (!ctx.getOutboundSeats().isEmpty() && ctx.getOutboundSchedule() != null) {
            detailsBox.getChildren().add(
                    buildSegment("→ CHIỀU ĐI", ctx.getOutboundSchedule(),
                            ctx.getOutboundSeats(), true));
            for (int i = 0; i < ctx.getOutboundSeats().size(); i++) {
                PassengerInfo p = i < ctx.getPassengers().size()
                        ? ctx.getPassengers().get(i) : null;
                CustomerType t = p != null ? p.getType() : CustomerType.ADULT;
                total += TicketContext.calcPrice(
                        dist, ctx.getOutboundSeats().get(i).getSeatType(), t);
            }
        }

        if (!ctx.getReturnSeats().isEmpty() && ctx.getReturnSchedule() != null) {
            detailsBox.getChildren().add(
                    buildSegment("← CHIỀU VỀ", ctx.getReturnSchedule(),
                            ctx.getReturnSeats(), false));
            for (int i = 0; i < ctx.getReturnSeats().size(); i++) {
                PassengerInfo p = i < ctx.getPassengers().size()
                        ? ctx.getPassengers().get(i) : null;
                CustomerType t = p != null ? p.getType() : CustomerType.ADULT;
                total += TicketContext.calcPrice(
                        dist, ctx.getReturnSeats().get(i).getSeatType(), t);
            }
        }

        totalLabel.setText(money(total) + " đ*");
    }

    private VBox buildSegment(String title, dto.ScheduleInfoResponse sc,
                              List<Seat> seats, boolean isOut) {
        VBox card = new VBox(8);
        card.getStyleClass().add("confirm-segment");
        card.setPadding(new Insets(12));

        Label segTitle = new Label(title);
        segTitle.getStyleClass().add(isOut
                ? "confirm-segment-title" : "confirm-segment-title-return");

        Label train = new Label(sc.getTrainName() + "  (" + sc.getTrainId() + ")");
        train.getStyleClass().add("confirm-train");

        Label route = new Label(sc.getDepartureStationName()
                + "  →  " + sc.getArrivalStationName());
        route.getStyleClass().add("confirm-route");

        Label time = new Label(
                (sc.getDepartureTime() != null ? sc.getDepartureTime().format(SHORT) : "--")
                        + "  →  "
                        + (sc.getArrivalTime()   != null ? sc.getArrivalTime().format(SHORT)   : "--"));
        time.getStyleClass().add("confirm-time");

        card.getChildren().addAll(segTitle, train, route, time);

        double dist = ctx.getDistance();
        for (int i = 0; i < seats.size(); i++) {
            Seat seat = seats.get(i);
            PassengerInfo p = i < ctx.getPassengers().size()
                    ? ctx.getPassengers().get(i) : null;
            CustomerType type = p != null ? p.getType() : CustomerType.ADULT;
            double price = TicketContext.calcPrice(dist, seat.getSeatType(), type);
            String tName = seat.getSeatType() == SeatType.SOFT_SLEEPER
                    ? "Giường mềm" : "Ghế mềm";
            String typeStr = typeStr(type);

            HBox row = new HBox(8);
            row.setAlignment(Pos.CENTER_LEFT);
            row.setPadding(new Insets(4, 0, 0, 8));

            VBox paxBox = new VBox(2);
            HBox.setHgrow(paxBox, Priority.ALWAYS);
            Label paxName = new Label("👤 " + (p != null ? p.getName() : "")
                    + "  (" + typeStr + ")");
            paxName.getStyleClass().add("confirm-pax");
            Label paxCccd = new Label("CCCD: " + (p != null ? p.getCccd() : ""));
            paxCccd.getStyleClass().add("confirm-route");
            Label seatInfo = new Label("Ghế " + seat.getSeatNumber()
                    + " • Toa " + seat.getCarriage().getCarriageNumber()
                    + " • " + tName);
            seatInfo.getStyleClass().add("confirm-seat");
            paxBox.getChildren().addAll(paxName, paxCccd, seatInfo);

            Label priceL = new Label(money(price) + " đ");
            priceL.getStyleClass().add("confirm-price");

            row.getChildren().addAll(paxBox, priceL);
            card.getChildren().add(row);
        }
        return card;
    }

    private void buildSummary() {
        summaryBox.getChildren().clear();
        double dist = ctx.getDistance();
        for (int i = 0; i < ctx.getPassengers().size(); i++) {
            PassengerInfo p = ctx.getPassengers().get(i);
            Label name = new Label("HK" + (i+1) + ": " + p.getName());
            name.getStyleClass().add("cart-train");
            summaryBox.getChildren().add(name);

            double outP = p.calcOutboundPrice(dist);
            Label lo = new Label("  → Ghế " + p.getOutboundSeat().getSeatNumber()
                    + "  " + money(outP) + "đ");
            lo.getStyleClass().add("cart-seat");
            summaryBox.getChildren().add(lo);

            if (p.getReturnSeat() != null) {
                double retP = p.calcReturnPrice(dist);
                Label lr = new Label("  ← Ghế " + p.getReturnSeat().getSeatNumber()
                        + "  " + money(retP) + "đ");
                lr.getStyleClass().add("cart-seat");
                summaryBox.getChildren().add(lr);
            }
        }
    }

    @FXML
    private void onPay() {
        String sellerId = UserContext.getInstance().getUserID();
        List<SellTicketRequest.TicketDetail> details = new ArrayList<>();

        for (int i = 0; i < ctx.getOutboundSeats().size(); i++) {
            PassengerInfo p = i < ctx.getPassengers().size()
                    ? ctx.getPassengers().get(i) : null;
            details.add(new SellTicketRequest.TicketDetail(
                    ctx.getOutboundSchedule().getScheduleId(),
                    ctx.getOutboundSeats().get(i).getSeatID(),
                    p != null ? p.getName().trim() : "",
                    p != null ? p.getCccd().trim() : "",
                    p != null ? p.getType() : CustomerType.ADULT));
        }

        for (int i = 0; i < ctx.getReturnSeats().size(); i++) {
            if (ctx.getReturnSchedule() == null) break;
            PassengerInfo p = i < ctx.getPassengers().size()
                    ? ctx.getPassengers().get(i) : null;
            details.add(new SellTicketRequest.TicketDetail(
                    ctx.getReturnSchedule().getScheduleId(),
                    ctx.getReturnSeats().get(i).getSeatID(),
                    p != null ? p.getName().trim() : "",
                    p != null ? p.getCccd().trim() : "",
                    p != null ? p.getType() : CustomerType.ADULT));
        }

        SellTicketRequest req = new SellTicketRequest(sellerId, ctx.isQrPayment(), details);
        payBtn.setDisable(true);
        payBtn.setText("Đang xử lý...");

        new Thread(() -> {
            try {
                ActionResponse resp = ticketService.sellTicket(req);
                Platform.runLater(() -> {
                    payBtn.setDisable(false);
                    payBtn.setText("✔  Xác nhận thanh toán");
                    showResult(resp);
                });
            } catch (Exception e) {
                Platform.runLater(() -> {
                    payBtn.setDisable(false);
                    payBtn.setText("✔  Xác nhận thanh toán");
                    showError("Lỗi: " + e.getMessage());
                });
            }
        }).start();
    }

    private void showResult(ActionResponse resp) {
        detailsBox.setVisible(false); detailsBox.setManaged(false);
        actionBox.setVisible(false);  actionBox.setManaged(false);
        resultBox.setVisible(true);   resultBox.setManaged(true);

        if (resp.isSuccess()) {
            resultIcon.setText("✔");
            resultIcon.setStyle("-fx-text-fill:#16a34a;");
            resultTitle.setText("Bán vé thành công!");
            resultTitle.setStyle("-fx-text-fill:#16a34a;");
            String msg = resp.getMessage();
            if (ctx.isQrPayment() && msg.contains("URL_QR:")) {
                String[] parts = msg.split("URL_QR:");
                resultMessage.setText("Mã vé: " + parts[0].trim());
                resultQr.setText("🔗 " + parts[1].trim());
                resultQr.setVisible(true); resultQr.setManaged(true);
            } else {
                resultMessage.setText("Mã vé: " + msg);
            }
        } else {
            resultIcon.setText("✗");
            resultIcon.setStyle("-fx-text-fill:#dc2626;");
            resultTitle.setText("Bán vé thất bại");
            resultTitle.setStyle("-fx-text-fill:#dc2626;");
            resultMessage.setText(resp.getMessage());
        }
    }

    @FXML
    private void onNewTicket() {
        ctx.reset();
        navigateTo("/iuh/fit/gui/ticket/search/search-schedule-view.fxml");
    }

    @FXML
    private void onBack() {
        navigateTo("/iuh/fit/gui/ticket/passenger/passenger-info-view.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource(fxml));
            StackPane content = (StackPane) detailsBox.getScene()
                    .lookup("#contentContainer");
            if (content != null) {
                AppTheme.applyTo(content.getScene());
                content.getChildren().setAll(root);
            }
        } catch (IOException e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private String typeStr(CustomerType t) {
        if (t == null) return "Người lớn";
        return switch (t) {
            case CHILD   -> "Trẻ em";
            case STUDENT -> "Sinh viên";
            case ELDERLY -> "Người cao tuổi";
            default      -> "Người lớn";
        };
    }

    private String money(double v) { return CURRENCY.format((long) v); }
    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill:#dc2626;");
    }
}