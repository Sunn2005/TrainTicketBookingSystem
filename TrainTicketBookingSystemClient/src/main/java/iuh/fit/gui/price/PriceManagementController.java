package iuh.fit.gui.price;

import controller.PriceController;
import model.entity.BasePrice;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;

import java.time.LocalDateTime;

public class PriceManagementController {

    private final PriceController controller = new PriceController();
    private BasePrice current;

    @FXML private Label pricePerKmLabel;
    @FXML private Label softSeatFeeLabel;
    @FXML private Label softSleeperFeeLabel;
    @FXML private Label studentDiscountLabel;
    @FXML private Label childDiscountLabel;
    @FXML private Label elderlyDiscountLabel;
    @FXML private Label updatedInfoLabel;
    @FXML private Label statusLabel;

    @FXML private TextField pricePerKmField;
    @FXML private TextField softSeatFeeField;
    @FXML private TextField softSleeperFeeField;
    @FXML private TextField studentDiscountField;
    @FXML private TextField childDiscountField;
    @FXML private TextField elderlyDiscountField;

    @FXML private javafx.scene.control.ComboBox<String> routeCombo;
    @FXML private javafx.scene.control.ComboBox<String> seatTypeCombo;
    @FXML private javafx.scene.control.ComboBox<String> customerTypeCombo;

    @FXML private Label basePriceLabel;
    @FXML private Label feeLabel;
    @FXML private Label discountLabel;
    @FXML private Label finalPriceLabel;

    @FXML
    public void initialize() {

        loadData();

        routeCombo.getItems().addAll(
                "Sài Gòn (STA-001) → Đà Nẵng (STA-003)",
                "Hà Nội (STA-002) → Sài Gòn (STA-001)"
        );

        seatTypeCombo.getItems().addAll("Ghế mềm", "Giường nằm");
        customerTypeCombo.getItems().addAll("Thường", "Sinh viên", "Trẻ em", "Người cao tuổi");

        routeCombo.getSelectionModel().selectFirst();
        seatTypeCombo.getSelectionModel().selectFirst();
        customerTypeCombo.getSelectionModel().selectFirst();

        routeCombo.setOnAction(e -> safeSimulate());
        seatTypeCombo.setOnAction(e -> safeSimulate());
        customerTypeCombo.setOnAction(e -> safeSimulate());

        Platform.runLater(this::safeSimulate);
    }

    private void loadData() {
        current = controller.getBasePrice();

        if (current == null) {
            statusLabel.setText("Không tải được dữ liệu giá!");
            return;
        }

        pricePerKmLabel.setText("Đơn giá / km: " + format(current.getPricePerDistance()));
        softSeatFeeLabel.setText("Phụ thu ghế mềm: " + format(current.getSoftSeatFee()));
        softSleeperFeeLabel.setText("Phụ thu giường nằm: " + format(current.getSoftSleeperFee()));
        studentDiscountLabel.setText("Giảm sinh viên: " + (int)(current.getStudentDiscount() * 100) + " %");
        childDiscountLabel.setText("Giảm trẻ em: " + (int)(current.getChildDiscount() * 100) + " %");
        elderlyDiscountLabel.setText("Giảm người cao tuổi: " + (int)(current.getElderlyDiscount() * 100) + " %");

        updatedInfoLabel.setText("Cập nhật gần nhất: " + LocalDateTime.now());
        statusLabel.setText("Đang áp dụng");

        pricePerKmField.setText(String.valueOf((int) current.getPricePerDistance()));
        softSeatFeeField.setText(String.valueOf((int) current.getSoftSeatFee()));
        softSleeperFeeField.setText(String.valueOf((int) current.getSoftSleeperFee()));
        studentDiscountField.setText(String.valueOf((int)(current.getStudentDiscount() * 100)));
        childDiscountField.setText(String.valueOf((int)(current.getChildDiscount() * 100)));
        elderlyDiscountField.setText(String.valueOf((int)(current.getElderlyDiscount() * 100)));
    }

    @FXML
    private void onSave() {
        try {
            double pricePerKm = Double.parseDouble(pricePerKmField.getText());
            double softSeatFee = Double.parseDouble(softSeatFeeField.getText());
            double softSleeperFee = Double.parseDouble(softSleeperFeeField.getText());
            double studentDiscount = Double.parseDouble(studentDiscountField.getText());
            double childDiscount = Double.parseDouble(childDiscountField.getText());
            double elderlyDiscount = Double.parseDouble(elderlyDiscountField.getText());

            if (pricePerKm < 0 || softSeatFee < 0 || softSleeperFee < 0 ||
                    studentDiscount < 0 || childDiscount < 0 || elderlyDiscount < 0 ||
                    studentDiscount > 100 || childDiscount > 100 || elderlyDiscount > 100) {
                statusLabel.setText("Giá trị nhập không hợp lệ!");
                return;
            }

            BasePrice p = new BasePrice(
                    pricePerKm,
                    softSeatFee,
                    softSleeperFee,
                    studentDiscount / 100,
                    childDiscount / 100,
                    elderlyDiscount / 100
            );

            boolean ok = controller.updateBasePrice(p);

            if (ok) {
                statusLabel.setText("Cập nhật thành công!");
                loadData();
                safeSimulate();
            } else {
                statusLabel.setText("Cập nhật thất bại!");
            }

        } catch (Exception e) {
            statusLabel.setText("Vui lòng nhập đúng định dạng số!");
        }
    }

    @FXML
    private void onReset() {
        loadData();
        safeSimulate();
    }

    private void safeSimulate() {
        if (current == null) return;
        simulatePrice();
    }

    private void simulatePrice() {
        int distance = 935;

        if (routeCombo.getValue() != null &&
                routeCombo.getValue().contains("Hà Nội")) {
            distance = 1726;
        }

        double base = current.getPricePerDistance() * distance;

        double fee = 0;
        if (seatTypeCombo.getValue() != null) {
            if (seatTypeCombo.getValue().contains("Ghế mềm"))
                fee = current.getSoftSeatFee();
            else if (seatTypeCombo.getValue().contains("Giường nằm"))
                fee = current.getSoftSleeperFee();
        }

        double total = base + fee;

        double discount = 0;
        if (customerTypeCombo.getValue() != null) {
            if (customerTypeCombo.getValue().contains("Sinh viên"))
                discount = current.getStudentDiscount();
            else if (customerTypeCombo.getValue().contains("Trẻ em"))
                discount = current.getChildDiscount();
            else if (customerTypeCombo.getValue().contains("Người cao tuổi"))
                discount = current.getElderlyDiscount();
        }

        double discountAmount = total * discount;
        double finalPrice = total - discountAmount;

        basePriceLabel.setText("Giá theo km " + distance + " x " +
                format(current.getPricePerDistance()));

        feeLabel.setText("Phụ thu ghế: " + format(fee));
        discountLabel.setText("Giảm giá (" + (int)(discount * 100) + "%): -" +
                format(discountAmount));
        finalPriceLabel.setText(format(finalPrice));
    }

    private String format(double v) {
        return String.format("%,.0f VND", v);
    }
}