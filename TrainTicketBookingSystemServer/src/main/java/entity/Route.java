package entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "routes")
public class Route {
    @Id
    @Column(name = "route_id")
    private String routeID;

    @ManyToOne
    @JoinColumn(name = "departure_station_id", referencedColumnName = "station_id")
    private Station departureStation;

    @ManyToOne
    @JoinColumn(name = "arrival_station_id", referencedColumnName = "station_id")
    private Station arrivalStation;

    @Column(name = "distance")
    private double distance;
}
