package model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "stations")
public class Station {
    @Id
    @Column(name = "station_id", nullable = false)
    private String stationID;

    @Column(name = "station_name", nullable = false)
    private String stationName;

    @Column(name = "location", nullable = false)
    private String location;
}
