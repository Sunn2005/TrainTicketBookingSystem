    package iuh.fit.gui.ticket.confirm;

    import dto.ActionResponse;
    import iuh.fit.App;
    import iuh.fit.context.TicketContext;
    import iuh.fit.context.TicketContext.PassengerInfo;
    import iuh.fit.constance.AppTheme;
    import iuh.fit.service.TicketClientService;
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
    import model.entity.enums.PaymentStatus;
    import model.entity.enums.SeatType;

    import java.io.File;
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
        @FXML private VBox  summaryBox;
        @FXML private VBox  cashPaymentBox;
        @FXML private VBox  qrPaymentBox;
        @FXML private Label cashTotalLabel;
        @FXML private TextField cashReceivedField;
        @FXML private Label changeLabel;
        @FXML private Button confirmCashBtn;
        @FXML private Label qrCodeLabel;
        @FXML private ImageView qrImage;
        @FXML private Button confirmQrBtn;
        @FXML private VBox  resultBox;
        @FXML private Label resultIcon;
        @FXML private Label resultTitle;
        @FXML private Label resultMessage;
        @FXML private Label resultQr;
        @FXML private Button printBtn;
        private List<String> lastTicketIds = new ArrayList<>();

        private final TicketClientService ticketService = new TicketClientService();
        private final TicketContext ctx = TicketContext.getInstance();
        private final List<String> currentPaymentIds = new ArrayList<>();
        private final List<String> currentTicketIds = new ArrayList<>();
        private String currentQrUrl;
        private double currentTotal = 0;
        private static final NumberFormat CURRENCY =
                NumberFormat.getNumberInstance(Locale.of("vi", "VN"));
        private static final DateTimeFormatter SHORT =
                DateTimeFormatter.ofPattern("dd/MM HH:mm");

        @FXML
        private void initialize() {
            boolean isQr = ctx.isQrPayment();
            paymentLabel.setText("Phương thức: " + (isQr ? "QR Code (VietQR)" : "Tiền mặt"));
            buildDetails();
            buildSummary();

            if (isQr) {
                qrPaymentBox.setVisible(true);
                qrPaymentBox.setManaged(true);
                cashPaymentBox.setVisible(false);
                cashPaymentBox.setManaged(false);
                prepareQrUi();
            } else {
                cashPaymentBox.setVisible(true);
                cashPaymentBox.setManaged(true);
                qrPaymentBox.setVisible(false);
                qrPaymentBox.setManaged(false);
                setupCashPayment();
            }
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
                            + (sc.getArrivalTime() != null ? sc.getArrivalTime().format(SHORT) : "--"));
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

        private void setupCashPayment() {
            double total = calculateTotal();
            cashTotalLabel.setText(money(total) + " đ");

            cashReceivedField.textProperty().addListener((obs, oldVal, newVal) -> {
                try {
                    if (newVal == null || newVal.isEmpty()) {
                        changeLabel.setText("0 đ");
                        return;
                    }
                    if (!newVal.matches("[0-9]*")) {
                        cashReceivedField.setText(oldVal);
                        return;
                    }
                    long received = Long.parseLong(newVal);
                    long change = received - (long) total;
                    if (change < 0) {
                        changeLabel.setStyle("-fx-text-fill:#dc2626;");
                        changeLabel.setText(money(change) + " đ (Thiếu)");
                    } else {
                        changeLabel.setStyle("-fx-text-fill:#16a34a;");
                        changeLabel.setText(money(change) + " đ");
                    }
                } catch (NumberFormatException e) {
                    changeLabel.setText("0 đ");
                }
            });
        }

        private void prepareQrUi() {
            currentPaymentIds.clear();
            currentTicketIds.clear();
            currentQrUrl = null;
            currentTotal = 0;
            resultQr.setVisible(false);
            qrCodeLabel.setVisible(false);
            qrCodeLabel.setManaged(false);
            qrImage.setVisible(false);
            qrImage.setManaged(false);
            confirmQrBtn.setText("Tạo mã QR");
            statusLabel.setText("Nhấn nút để tạo mã QR và thanh toán khi khách xác nhận.");
            statusLabel.setStyle("-fx-text-fill:#000000;");
        }

        private void createQrPayment() {
            currentPaymentIds.clear();
            currentTicketIds.clear();
            currentQrUrl = null;
            currentTotal = 0;
            resultQr.setVisible(false);
            qrCodeLabel.setVisible(false);
            qrCodeLabel.setManaged(false);
            qrImage.setVisible(false);
            qrImage.setManaged(false);

            try {
                ActionResponse response;
                if (ctx.isRoundTrip()) {
                    response = ticketService.sellRoundTrip(
                            PaymentFlowSupport.buildRoundTripRequest(ctx, true));
                } else {
                    response = ticketService.sellTicket(
                            PaymentFlowSupport.buildRequest(ctx, true));
                }
                if (!response.isSuccess()) {
                    showError(response.getMessage());
                    return;
                }
                if (response.getQrUrl() == null || response.getQrUrl().isBlank()) {
                    showError("Không nhận được dữ liệu QR từ server.");
                    return;
                }
                currentQrUrl = response.getQrUrl();
                currentTotal = response.getTotal();
                if (response.getPaymentIds() != null && !response.getPaymentIds().isBlank()) {
                    String[] ids = response.getPaymentIds().split(",\\s*");
                    currentPaymentIds.addAll(List.of(ids));
                }
                if (response.getTicketIds() != null && !response.getTicketIds().isBlank()) {
                    String[] ids = response.getTicketIds().split(",\\s*");
                    currentTicketIds.addAll(List.of(ids));
                }
                totalLabel.setText(money(currentTotal) + " đ");
                qrImage.setImage(new Image(currentQrUrl, true));
                qrImage.setVisible(true);
                qrImage.setManaged(true);
                confirmQrBtn.setText("✓ Xác nhận thanh toán");
                statusLabel.setText("Quét mã QR và bấm xác nhận sau khi khách thanh toán xong.");
                statusLabel.setStyle("-fx-text-fill:#16a34a;");
            } catch (Exception e) {
                showError("Lỗi khi tạo mã QR: " + e.getMessage());
            }
        }

        private double calculateTotal() {
            double total = 0;
            double dist = ctx.getDistance();

            for (int i = 0; i < ctx.getOutboundSeats().size(); i++) {
                PassengerInfo p = i < ctx.getPassengers().size()
                        ? ctx.getPassengers().get(i) : null;
                CustomerType t = p != null ? p.getType() : CustomerType.ADULT;
                total += TicketContext.calcPrice(
                        dist, ctx.getOutboundSeats().get(i).getSeatType(), t);
            }

            for (int i = 0; i < ctx.getReturnSeats().size(); i++) {
                PassengerInfo p = i < ctx.getPassengers().size()
                        ? ctx.getPassengers().get(i) : null;
                CustomerType t = p != null ? p.getType() : CustomerType.ADULT;
                total += TicketContext.calcPrice(
                        dist, ctx.getReturnSeats().get(i).getSeatType(), t);
            }
            return total;
        }

        @FXML
        private void onConfirmCashPayment() {
            ctx.setCurrentStep(TicketContext.BookingStep.PAYMENT);
            try {
                String received = cashReceivedField.getText();
                if (received == null || received.isEmpty()) {
                    showError("Vui lòng nhập số tiền khách đưa");
                    return;
                }
                long receivedAmount = Long.parseLong(received);
                double total = calculateTotal();
                if (receivedAmount < total) {
                    showError("Tiền nhân viên nhập chưa đủ!");
                    return;
                }
                ActionResponse response;
                if (ctx.isRoundTrip()) {
                    response = ticketService.sellRoundTrip(
                            PaymentFlowSupport.buildRoundTripRequest(ctx, false));
                } else {
                    response = ticketService.sellTicket(
                            PaymentFlowSupport.buildRequest(ctx, false));
                }
                if (!response.isSuccess()) {
                    showError(response.getMessage());
                    return;
                }

                // ── FIX: lưu ticketIds vào currentTicketIds trước khi showPaymentSuccess ──
                currentTicketIds.clear();
                if (response.getTicketIds() != null && !response.getTicketIds().isBlank()) {
                    String[] ticketIds = response.getTicketIds().split(",\\s*");
                    for (String ticketId : ticketIds) {
                        currentTicketIds.add(ticketId.trim());
                        ActionResponse updateRes = ticketService.updateTicketStatus(
                                ticketId.trim(), model.entity.enums.TicketStatus.PAID);
                        if (!updateRes.isSuccess()) {
                            showError("Lỗi cập nhật trạng thái vé: " + updateRes.getMessage());
                            return;
                        }
                    }
                }

                showPaymentSuccess();
            } catch (NumberFormatException e) {
                showError("Số tiền không hợp lệ");
            }
        }

        @FXML
        private void onConfirmQrPayment() {
            ctx.setCurrentStep(TicketContext.BookingStep.PAYMENT);
            if (currentPaymentIds.isEmpty()) {
                createQrPayment();
                return;
            }

            for (String paymentId : currentPaymentIds) {
                ActionResponse result = ticketService.updatePaymentStatus(
                        paymentId, PaymentStatus.SUCCESS);
                if (!result.isSuccess()) {
                    showError("Không thể xác nhận thanh toán: " + result.getMessage());
                    return;
                }
            }
            for (String ticketId : currentTicketIds) {
                ActionResponse updateRes = ticketService.updateTicketStatus(
                        ticketId.trim(), model.entity.enums.TicketStatus.PAID);
                if (!updateRes.isSuccess()) {
                    showError("Lỗi cập nhật trạng thái vé: " + updateRes.getMessage());
                    return;
                }
            }
            confirmQrBtn.setDisable(true);
            showPaymentSuccess();
        }

        private void showPaymentSuccess() {
            lastTicketIds.clear();
            lastTicketIds.addAll(currentTicketIds);

            resultBox.setVisible(true);
            resultBox.setManaged(true);
            resultIcon.setText("✓");
            resultIcon.setStyle("-fx-text-fill:#16a34a;-fx-font-size:48;");
            resultTitle.setText("Thanh toán thành công!");
            resultMessage.setText("Mã vé: "
                    + (lastTicketIds.isEmpty() ? "--" : String.join(", ", lastTicketIds)));

            printBtn.setVisible(true);
            printBtn.setManaged(true);
        }

        @FXML
        private void onPrintTicket() {
            try {
                String fileName = "Ve_Tau_" + System.currentTimeMillis() + ".pdf";
                File invoiceDir = new File("invoice");
                invoiceDir.mkdirs();
                String outputPath = new File(invoiceDir, fileName).getAbsolutePath();

                iuh.fit.util.TicketPdfExporter.export(ctx, lastTicketIds, outputPath);
                java.awt.Desktop.getDesktop().open(new java.io.File(outputPath));

                statusLabel.setText("✔ Đã xuất vé PDF: ");
                statusLabel.setStyle("-fx-text-fill:#16a34a;");
            } catch (Exception e) {
                e.printStackTrace();
                showError("Lỗi xuất PDF: " + e.getMessage());
            }
        }

        @FXML
        private void onPay() {}

        @FXML
        private void onNewTicket() {
            ctx.reset();
            ctx.setCurrentStep(TicketContext.BookingStep.OUTBOUND_SEARCH);
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