package model.entity;

import com.fasterxml.jackson.annotation.JsonManagedReference;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.List;
import java.util.Set;

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

    @OneToMany(mappedBy = "train", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference // Cho phép serialize danh sách toa
    private List<Carriage> carriages;

}
