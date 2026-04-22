package model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "routes",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_route_stations", columnNames = {"departure_station_id", "arrival_station_id"})
    }
)
public class Route {
    @Id
    @Column(name = "route_id", nullable = false)
    private String routeID;

    @ManyToOne
    @JoinColumn(name = "departure_station_id", referencedColumnName = "station_id", nullable = false)
    private Station departureStation;

    @ManyToOne
    @JoinColumn(name = "arrival_station_id", referencedColumnName = "station_id", nullable = false)
    private Station arrivalStation;

    @Column(name = "distance", nullable = false)
    private double distance;
}
