module iuh.fit.trainticketbookingsystemclient {
    requires javafx.controls;
    requires javafx.fxml;


    opens iuh.fit to javafx.fxml;
    exports iuh.fit;
}