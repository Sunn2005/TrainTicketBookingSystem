package iuh.fit.gui.home;

import javafx.fxml.FXML;
import javafx.scene.control.Accordion;
import javafx.scene.control.Button;

import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

public class HomeSidebarController {
    @FXML
    private Accordion menuAccordion;

    @FXML
    private Button sellTicketButton;

    @FXML
    private Button exchangeTicketButton;

    @FXML
    private Button cancelTicketButton;

    @FXML
    private Button scheduleButton;

    @FXML
    private Button coachButton;

    @FXML
    private Button customerListButton;

    @FXML
    private Button customerUpdateButton;

    private List<Button> menuButtons;
    private Map<String, Button> routeButtonMap;
    private Consumer<String> routeHandler;
    private Runnable logoutHandler;

    @FXML
    private void initialize() {
        if (menuAccordion != null && !menuAccordion.getPanes().isEmpty()) {
            menuAccordion.setExpandedPane(menuAccordion.getPanes().getFirst());
        }

        menuButtons = List.of(
                sellTicketButton,
                exchangeTicketButton,
                cancelTicketButton,
                scheduleButton,
                coachButton,
                customerListButton,
                customerUpdateButton
        );

        routeButtonMap = Map.of(
                HomeContentFactory.ROUTE_SELL_TICKET, sellTicketButton,
                HomeContentFactory.ROUTE_EXCHANGE_TICKET, exchangeTicketButton,
                HomeContentFactory.ROUTE_CANCEL_TICKET, cancelTicketButton,
                HomeContentFactory.ROUTE_SCHEDULE, scheduleButton,
                HomeContentFactory.ROUTE_COACH, coachButton,
                HomeContentFactory.ROUTE_CUSTOMER_LIST, customerListButton,
                HomeContentFactory.ROUTE_CUSTOMER_UPDATE, customerUpdateButton
        );
    }

    void setRouteHandler(Consumer<String> routeHandler) {
        this.routeHandler = routeHandler;
    }

    void setLogoutHandler(Runnable logoutHandler) {
        this.logoutHandler = logoutHandler;
    }

    void selectMenu(String route) {
        Button activeButton = routeButtonMap.getOrDefault(route, sellTicketButton);
        setActiveButton(activeButton);
    }

    @FXML
    private void onSellTicketClick() {
        handleRoute(HomeContentFactory.ROUTE_SELL_TICKET);
    }

    @FXML
    private void onExchangeTicketClick() {
        handleRoute(HomeContentFactory.ROUTE_EXCHANGE_TICKET);
    }

    @FXML
    private void onCancelTicketClick() {
        handleRoute(HomeContentFactory.ROUTE_CANCEL_TICKET);
    }

    @FXML
    private void onScheduleClick() {
        handleRoute(HomeContentFactory.ROUTE_SCHEDULE);
    }

    @FXML
    private void onCoachClick() {
        handleRoute(HomeContentFactory.ROUTE_COACH);
    }

    @FXML
    private void onCustomerListClick() {
        handleRoute(HomeContentFactory.ROUTE_CUSTOMER_LIST);
    }

    @FXML
    private void onCustomerUpdateClick() {
        handleRoute(HomeContentFactory.ROUTE_CUSTOMER_UPDATE);
    }

    @FXML
    private void onLogoutClick() {
        if (logoutHandler != null) {
            logoutHandler.run();
        }
    }

    private void handleRoute(String route) {
        selectMenu(route);
        if (routeHandler != null) {
            routeHandler.accept(route);
        }
    }

    private void setActiveButton(Button activeButton) {
        for (Button button : menuButtons) {
            button.getStyleClass().remove("menu-button-active");
        }
        activeButton.getStyleClass().add("menu-button-active");
    }
}

