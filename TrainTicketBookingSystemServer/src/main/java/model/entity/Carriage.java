package model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import jakarta.persistence.UniqueConstraint;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
    name = "carriages",
    uniqueConstraints = {
        @UniqueConstraint(name = "uk_carriage_train", columnNames = {"train_id", "carriage_number"})
    }
)
public class Carriage {

    @Id
    @Column(name = "carriage_id", nullable = false)
    private String carriageID;

    @ManyToOne
    @JoinColumn(name = "train_id", nullable = false)
    private Train train;

    @Column(name = "carriage_number", nullable = false)
    private int carriageNumber;

    @OneToMany(mappedBy = "carriage")
    @ToString.Exclude
    private List<Seat> seats;
}
