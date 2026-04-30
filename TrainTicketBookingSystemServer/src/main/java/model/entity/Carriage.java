package model.entity;

import com.fasterxml.jackson.annotation.JsonBackReference;
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
    @JsonBackReference // Ngăn không cho serialize ngược lại Train để tránh lặp vô tận
    private Train train;

    @Column(name = "carriage_number", nullable = false)
    private int carriageNumber;

    @OneToMany(mappedBy = "carriage", cascade = CascadeType.ALL, orphanRemoval = true, fetch = FetchType.EAGER)
    @JsonManagedReference // Thêm dòng này để cho phép ghi danh sách ghế vào JSON
    private List<Seat> seats;
}
