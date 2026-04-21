module iuh.fit.trainticketbookingsystemclient {
    requires javafx.controls;
    requires javafx.fxml;
    requires TrainTicketBookingSystemServer;
    requires com.fasterxml.jackson.databind;
    requires org.hibernate.orm.core;
    requires jakarta.persistence;
    requires java.naming;

    opens iuh.fit to javafx.fxml;
    opens iuh.fit.service to javafx.fxml;
    opens iuh.fit.gui.login to javafx.fxml;
    opens iuh.fit.socketconfig to javafx.fxml;
    opens iuh.fit.gui.home to javafx.fxml;
    opens iuh.fit.gui.ticket to javafx.fxml;
    opens iuh.fit.gui.ticket.search to javafx.fxml;
    opens iuh.fit.gui.ticket.seat to javafx.fxml;
    opens iuh.fit.gui.ticket.passenger to javafx.fxml;
    opens iuh.fit.gui.ticket.confirm to javafx.fxml;

    exports iuh.fit;
    exports iuh.fit.service;
    exports iuh.fit.gui.login;
    exports iuh.fit.socketconfig;
    exports iuh.fit.gui.home;
    exports iuh.fit.gui.ticket;
}