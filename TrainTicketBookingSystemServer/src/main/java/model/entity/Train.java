package model.entity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(name = "trains")
public class Train {
    @Id
    @Column(name = "train_id", nullable = false)
    private String trainID;

    @Column(name = "train_name", unique = true, nullable = false)
    private String trainName;

    @OneToMany(mappedBy = "train")
    @ToString.Exclude
    private List<Carriage> carriages;

}
