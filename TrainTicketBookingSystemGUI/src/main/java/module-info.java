module iuh.fit.trainticketbookingsystemgui {
    requires javafx.controls;
    requires javafx.fxml;


    opens iuh.fit.trainticketbookingsystemgui to javafx.fxml;
    exports iuh.fit.trainticketbookingsystemgui;
}