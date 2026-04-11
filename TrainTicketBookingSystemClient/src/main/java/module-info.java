module iuh.fit.trainticketbookingsystemclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires TrainTicketBookingSystemServer;

    opens iuh.fit to javafx.fxml;
    opens iuh.fit.controller to javafx.fxml;
    exports iuh.fit;
    exports iuh.fit.controller;
}