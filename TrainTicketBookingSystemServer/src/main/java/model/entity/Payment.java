package model.entity;

import model.entity.enums.PaymentStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@AllArgsConstructor
@NoArgsConstructor

@Entity
@Table(name = "payments")
public class Payment {
    @Id
    @Column(name = "payment_id", nullable = false)
    private String paymentID;

    @OneToOne
    @JoinColumn(name = "ticket_id", referencedColumnName = "ticket_id", unique = true, nullable = false)
    private Ticket ticket;

    @Column(name = "payment_method", nullable = false)
    private String paymentMethod;

    @Column(name = "amount", precision = 15, scale = 2, nullable = false)
    private BigDecimal amount;

    @Column(name = "payment_time", nullable = false)
    private LocalDateTime paymentTime;

    @Enumerated(EnumType.STRING)
    @Column(name = "payment_status", nullable = false)
    private PaymentStatus paymentStatus;
}
