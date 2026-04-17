package iuh.fit.gui.home;

import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.Separator;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Priority;
import javafx.scene.layout.VBox;

final class HomeContentFactory {
    static final String ROUTE_SELL_TICKET = "sell-ticket";
    static final String ROUTE_EXCHANGE_TICKET = "exchange-ticket";
    static final String ROUTE_CANCEL_TICKET = "cancel-ticket";
    static final String ROUTE_SCHEDULE = "schedule";
    static final String ROUTE_COACH = "coach";
    static final String ROUTE_CUSTOMER_LIST = "customer-list";
    static final String ROUTE_CUSTOMER_UPDATE = "customer-update";

    private HomeContentFactory() {
    }

    static Node createContent(String route) {
        return switch (route) {
            case ROUTE_EXCHANGE_TICKET -> createSampleScreen(
                    "Doi ve",
                    "Man hinh mau cho doi ve theo ma dat cho.",
                    "Yeu cau moi", "9",
                    "Dang xu ly", "5",
                    "Da hoan tat", "42"
            );
            case ROUTE_CANCEL_TICKET -> createSampleScreen(
                    "Huy ve",
                    "Man hinh mau cho huy ve va tinh phi hoan.",
                    "Yeu cau huy", "7",
                    "Da duyet", "31",
                    "Hoan tien", "14,700,000 VND"
            );
            case ROUTE_SCHEDULE -> createSampleScreen(
                    "Lich trinh",
                    "Man hinh mau quan ly lich trinh tau chay.",
                    "Tau dang chay", "18",
                    "Cho khoi hanh", "11",
                    "Tre gio", "2"
            );
            case ROUTE_COACH -> createSampleScreen(
                    "Toa tau",
                    "Man hinh mau quan ly toa, ghe va trang thai.",
                    "Tong toa", "126",
                    "Con trong", "34",
                    "Bao tri", "6"
            );
            case ROUTE_CUSTOMER_LIST -> createSampleScreen(
                    "Danh sach khach hang",
                    "Man hinh mau tra cuu va loc khach hang.",
                    "Tong KH", "5,280",
                    "Moi tuan nay", "126",
                    "VIP", "420"
            );
            case ROUTE_CUSTOMER_UPDATE -> createSampleScreen(
                    "Cap nhat khach hang",
                    "Man hinh mau cap nhat thong tin ho so khach hang.",
                    "Yeu cau cap nhat", "16",
                    "Da xu ly", "13",
                    "Con lai", "3"
            );
            default -> createSampleScreen(
                    "Ban ve",
                    "Man hinh mau cho nghiep vu ban ve tai quay.",
                    "Ve da ban", "248",
                    "Cho thanh toan", "12",
                    "Doanh thu", "58,200,000 VND"
            );
        };
    }

    private static Node createSampleScreen(
            String title,
            String subtitle,
            String metric1Label,
            String metric1Value,
            String metric2Label,
            String metric2Value,
            String metric3Label,
            String metric3Value
    ) {
        Label titleLabel = new Label(title);
        titleLabel.getStyleClass().add("screen-title");

        Label subtitleLabel = new Label(subtitle);
        subtitleLabel.getStyleClass().add("screen-subtitle");

        HBox cardRow = new HBox(12);
        cardRow.getChildren().addAll(
                createMetricCard(metric1Label, metric1Value),
                createMetricCard(metric2Label, metric2Value),
                createMetricCard(metric3Label, metric3Value)
        );

        VBox panel = new VBox(10);
        panel.getStyleClass().add("panel");
        VBox.setVgrow(panel, Priority.ALWAYS);

        Label panelTitle = new Label("Noi dung mau");
        panelTitle.getStyleClass().add("panel-title");

        VBox placeholder = new VBox(6);
        placeholder.getStyleClass().add("placeholder");
        VBox.setVgrow(placeholder, Priority.ALWAYS);

        Label info1 = new Label("- Day la khu vuc content se thay doi theo menu sidebar.");
        info1.getStyleClass().add("placeholder-text");
        Label info2 = new Label("- Ban co the thay bang TableView/Form that trong buoc tiep theo.");
        info2.getStyleClass().add("placeholder-text");
        Label info3 = new Label("- Mau nay giup team test dieu huong truoc khi gan du lieu that.");
        info3.getStyleClass().add("placeholder-text");
        placeholder.getChildren().addAll(info1, info2, info3);

        panel.getChildren().addAll(panelTitle, new Separator(), placeholder);

        VBox root = new VBox(16);
        root.getStyleClass().add("screen-root");
        root.getChildren().addAll(titleLabel, subtitleLabel, cardRow, panel);
        VBox.setVgrow(panel, Priority.ALWAYS);
        return root;
    }

    private static VBox createMetricCard(String label, String value) {
        Label metricLabel = new Label(label);
        metricLabel.getStyleClass().add("card-label");

        Label metricValue = new Label(value);
        metricValue.getStyleClass().add("card-value");

        VBox card = new VBox(6, metricLabel, metricValue);
        card.getStyleClass().add("card");
        HBox.setHgrow(card, Priority.ALWAYS);
        card.setMaxWidth(Double.MAX_VALUE);
        return card;
    }
}

