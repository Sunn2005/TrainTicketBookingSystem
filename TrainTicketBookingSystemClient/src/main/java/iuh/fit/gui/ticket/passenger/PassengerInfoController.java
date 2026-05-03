package iuh.fit.gui.ticket.passenger;

import iuh.fit.App;
import iuh.fit.context.TicketContext;
import iuh.fit.context.TicketContext.PassengerInfo;
import iuh.fit.constance.AppTheme;
import javafx.collections.FXCollections;
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
import java.util.*;

import iuh.fit.service.CustomerClientService;
import model.entity.Customer;

public class PassengerInfoController {

    @FXML private VBox        passengerContainer;
    @FXML private RadioButton cashRadio;
    @FXML private RadioButton qrRadio;
    @FXML private Label       statusLabel;
    @FXML private VBox        cartBox;
    @FXML private VBox        cartTotalBox;
    @FXML private Label       cartTotalLabel;

    private final TicketContext ctx = TicketContext.getInstance();
    private final CustomerClientService customerClientService = new CustomerClientService();
    private ToggleGroup payGroup;
    private static final NumberFormat CURRENCY =
            NumberFormat.getNumberInstance(new Locale("vi", "VN"));

    private static final String FULLNAME_REGEX = "^[\\p{L} ]+$";
    private static final String CCCD_REGEX = "^\\d{12}$";

    @FXML
    private void initialize() {
        payGroup = new ToggleGroup();
        cashRadio.setToggleGroup(payGroup);
        qrRadio.setToggleGroup(payGroup);
        cashRadio.setSelected(!ctx.isQrPayment());
        qrRadio.setSelected(ctx.isQrPayment());

        syncPassengers();
        buildForms();
        refreshCart();
    }

    private void syncPassengers() {
        List<Seat> outSeats = new ArrayList<>(ctx.getOutboundSeats());
        outSeats.sort(Comparator.comparingInt(Seat::getSeatNumber));
        List<Seat> retSeats = new ArrayList<>(ctx.getReturnSeats());
        retSeats.sort(Comparator.comparingInt(Seat::getSeatNumber));

        // Sync số lượng
        while (ctx.getPassengers().size() < outSeats.size()) {
            int i = ctx.getPassengers().size();
            PassengerInfo p = new PassengerInfo(outSeats.get(i));
            if (i < retSeats.size()) p.setReturnSeat(retSeats.get(i));
            ctx.getPassengers().add(p);
        }
        while (ctx.getPassengers().size() > outSeats.size()) {
            ctx.getPassengers().remove(ctx.getPassengers().size() - 1);
        }
        // Cập nhật ghế về nếu có thêm
        for (int i = 0; i < ctx.getPassengers().size(); i++) {
            ctx.getPassengers().get(i).setOutboundSeat(outSeats.get(i));
            if (i < retSeats.size())
                ctx.getPassengers().get(i).setReturnSeat(retSeats.get(i));
        }
    }

    private void buildForms() {
        passengerContainer.getChildren().clear();
        for (int i = 0; i < ctx.getPassengers().size(); i++) {
            passengerContainer.getChildren()
                    .add(buildCard(i + 1, ctx.getPassengers().get(i)));
        }
    }

    private VBox buildCard(int idx, PassengerInfo info) {
        VBox card = new VBox(12);
        card.getStyleClass().add("passenger-card");
        card.setPadding(new Insets(14));

        // Header
        String outType = typeName(info.getOutboundSeat().getSeatType());
        String headerTxt = "Hành khách " + idx
                + "  —  Ghế " + info.getOutboundSeat().getSeatNumber()
                + " • Toa " + info.getOutboundSeat().getCarriage().getCarriageNumber()
                + " • " + outType;
        if (info.getReturnSeat() != null) {
            headerTxt += "   |   Về: Ghế "
                    + info.getReturnSeat().getSeatNumber()
                    + " • Toa " + info.getReturnSeat().getCarriage().getCarriageNumber()
                    + " • " + typeName(info.getReturnSeat().getSeatType());
        }
        Label header = new Label(headerTxt);
        header.getStyleClass().add("passenger-card-header");
        header.setWrapText(true);

        // Row 1: Tên + CCCD
        HBox row1 = new HBox(12);

        VBox nameBox = new VBox(5);
        HBox.setHgrow(nameBox, Priority.ALWAYS);
        Label nameLabel = new Label("Họ và tên *");
        nameLabel.getStyleClass().add("field-label");
        TextField nameFld = new TextField(info.getName());
        nameFld.setPromptText("Nguyễn Văn A");
        nameFld.getStyleClass().add("form-field");
        nameFld.textProperty().addListener((obs, o, n) -> {
            // Chỉ chữ cái và khoảng trắng
            if (!n.matches("[\\p{L} ]*")) {
                nameFld.setText(o);
            } else {
                info.setName(n);
            }
        });
        nameBox.getChildren().addAll(nameLabel, nameFld);

        VBox cccdBox = new VBox(5);
        HBox.setHgrow(cccdBox, Priority.ALWAYS);
        Label cccdLabel = new Label("CCCD / CMND *");
        cccdLabel.getStyleClass().add("field-label");
        TextField cccdFld = new TextField(info.getCccd());
        cccdFld.setPromptText("012345678901");
        cccdFld.getStyleClass().add("form-field");
        cccdBox.getChildren().addAll(cccdLabel, cccdFld);
        row1.getChildren().addAll(nameBox, cccdBox);

        // Row 2: Loại KH + Giá
        HBox row2 = new HBox(12);
        row2.setAlignment(Pos.CENTER_LEFT);

        VBox typeBox = new VBox(5);
        Label typeLabel = new Label("Loại khách hàng");
        typeLabel.getStyleClass().add("field-label");
        ComboBox<CustomerType> typeCombo = new ComboBox<>();
        typeCombo.setItems(FXCollections.observableArrayList(CustomerType.values()));
        typeCombo.setValue(info.getType());
        typeCombo.getStyleClass().add("form-combo");
        typeCombo.setConverter(new javafx.util.StringConverter<>() {
            @Override public String toString(CustomerType t) {
                if (t == null) return "";
                if (TicketContext.getInstance() == null) {
                    return t.name(); // Fallback nếu chưa load được
                }
                double discountPercent = (TicketContext.getInstance().getDiscountRate(t)) * 100;
                return switch (t) {
                    case ADULT   -> "Người lớn";
                    case CHILD   -> String.format("Trẻ em  (-%.0f%%)", discountPercent);
                    case STUDENT -> String.format("Sinh viên  (-%.0f%%)", discountPercent);
                    case ELDERLY -> String.format("Người cao tuổi  (-%.0f%%)", discountPercent);
                };
            }
            @Override public CustomerType fromString(String s) { return null; }
        });
        typeCombo.valueProperty().addListener((obs, o, n) -> {
            info.setType(n);
            refreshCart();
            // Cập nhật giá label
        });
        typeBox.getChildren().addAll(typeLabel, typeCombo);

        cccdFld.textProperty().addListener((obs, o, n) -> {
            if (!n.matches("[0-9]*") || n.length() > 12) {
                cccdFld.setText(o);
            } else {
                info.setCccd(n);
                boolean isDup = checkDupCccd(cccdFld, n, info);
                if (n.length() == 12 && !isDup) {
                    java.util.concurrent.CompletableFuture.runAsync(() -> {
                        try {
                            Customer c = customerClientService.getCustomerById(n);
                            if (c != null) {
                                javafx.application.Platform.runLater(() -> {
                                    if (c.getFullName() != null) {
                                        nameFld.setText(c.getFullName());
                                        info.setName(c.getFullName());
                                    }
                                    if (c.getCustomerType() != null) {
                                        typeCombo.setValue(c.getCustomerType());
                                        info.setType(c.getCustomerType());
                                        refreshCart();
                                    }
                                });
                            }
                        } catch (Exception e) {
                            e.printStackTrace();
                        }
                    });
                }
            }
        });

        // Giá real-time
        Label priceLabel = new Label();
        priceLabel.getStyleClass().add("passenger-price-label");
        Runnable updateP = () -> priceLabel.setText(
                money(info.calcTotalPrice(ctx.getDistance())) + " đ");
        updateP.run();
        typeCombo.valueProperty().addListener((obs, o, n) -> updateP.run());

        row2.getChildren().addAll(typeBox, priceLabel);
        card.getChildren().addAll(header, row1, row2);
        return card;
    }

    private boolean checkDupCccd(TextField fld, String cccd, PassengerInfo cur) {
        if (cccd.length() != 12) { fld.setStyle(""); statusLabel.setText(""); return false; }
        boolean dup = ctx.getPassengers().stream()
                .filter(p -> p != cur)
                .anyMatch(p -> cccd.equals(p.getCccd()));
        if (dup) {
            fld.setStyle("-fx-border-color:#dc2626;-fx-border-width:1.5;");
            statusLabel.setText("⚠ CCCD đã được nhập cho hành khách khác!");
            statusLabel.setStyle("-fx-text-fill:#dc2626;");
        } else {
            fld.setStyle("");
            statusLabel.setText("");
        }
        return dup;
    }

    private void refreshCart() {
        cartBox.getChildren().clear();
        double total = 0;
        double dist = ctx.getDistance();

        for (int i = 0; i < ctx.getPassengers().size(); i++) {
            PassengerInfo p = ctx.getPassengers().get(i);
            double outP = p.calcOutboundPrice(dist);
            double retP = p.calcReturnPrice(dist);
            total += outP + retP;

            String outT = typeName(p.getOutboundSeat().getSeatType());
            Label lo = new Label("HK" + (i + 1) + " → Ghế "
                    + p.getOutboundSeat().getSeatNumber()
                    + " Toa " + p.getOutboundSeat().getCarriage().getCarriageNumber()
                    + " (" + outT + ")  " + money(outP) + "đ");
            lo.getStyleClass().add("cart-seat"); lo.setWrapText(true);
            cartBox.getChildren().add(lo);

            if (p.getReturnSeat() != null) {
                String retT = typeName(p.getReturnSeat().getSeatType());
                Label lr = new Label("HK" + (i + 1) + " ← Ghế "
                        + p.getReturnSeat().getSeatNumber()
                        + " Toa " + p.getReturnSeat().getCarriage().getCarriageNumber()
                        + " (" + retT + ")  " + money(retP) + "đ");
                lr.getStyleClass().add("cart-seat"); lr.setWrapText(true);
                cartBox.getChildren().add(lr);
            }
        }
        cartTotalLabel.setText(money(total) + " đ");
    }

    @FXML
    private void onNext() {
        // Validate
        for (int i = 0; i < ctx.getPassengers().size(); i++) {
            PassengerInfo p = ctx.getPassengers().get(i);
            if (p.getName() == null || p.getName().trim().isEmpty() || !p.getName().matches(FULLNAME_REGEX)) {
                showError("Hành khách " + (i+1) + ": Họ tên chỉ được chứa chữ cái và khoảng trắng."); return;
            }
            if (!Character.isUpperCase(p.getName().trim().charAt(0))) {
                showError("Hành khách " + (i+1) + ": Họ tên phải bắt đầu bằng chữ in hoa."); return;
            }
            if (p.getCccd() == null || !p.getCccd().matches(CCCD_REGEX)) {
                showError("Hành khách " + (i+1) + ": CCCD phải là 12 chữ số."); return;
            }
        }
        // Kiểm tra CCCD trùng
        long distinct = ctx.getPassengers().stream()
                .map(PassengerInfo::getCccd).distinct().count();
        if (distinct < ctx.getPassengers().size()) {
            showError("Có CCCD bị trùng giữa các hành khách!"); return;
        }

         boolean isQr = qrRadio.isSelected();
         ctx.setQrPayment(isQr);
         ctx.setCurrentStep(TicketContext.BookingStep.CONFIRM);
         navigateTo("/iuh/fit/gui/ticket/confirm/confirm-view.fxml");
    }

    @FXML
    private void onBack() {
        navigateTo("/iuh/fit/gui/ticket/seat/select-seat-view.fxml");
    }

    private void navigateTo(String fxml) {
        try {
            Parent root = FXMLLoader.load(App.class.getResource(fxml));
            StackPane content = (StackPane) passengerContainer.getScene()
                    .lookup("#contentContainer");
            if (content != null) {
                AppTheme.applyTo(content.getScene());
                content.getChildren().setAll(root);
            }
        } catch (IOException e) {
            showError("Lỗi: " + e.getMessage());
        }
    }

    private String typeName(SeatType t) {
        return t == SeatType.SOFT_SLEEPER ? "Giường mềm" : "Ghế mềm";
    }

    private String money(double v) { return CURRENCY.format((long) v); }

    private void showError(String msg) {
        statusLabel.setText(msg);
        statusLabel.setStyle("-fx-text-fill:#dc2626;");
    }
}
