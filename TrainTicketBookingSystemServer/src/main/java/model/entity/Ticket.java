package model.entity;

import model.entity.enums.TicketStatus;
import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
@Entity
@Table(
        name = "tickets",
        uniqueConstraints = {
                @UniqueConstraint(name = "uk_ticket_schedule_seat", columnNames = {"schedule_id", "seat_id"})
        }
)
public class Ticket {
    @Id
    @Column(name = "ticket_id", nullable = false)
    private String ticketID;

    @ManyToOne
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @ManyToOne
    @JoinColumn(name = "customer_id", nullable = false)
    private Customer customer;

    @ManyToOne
    @JoinColumn(name = "schedule_id", nullable = false)
    private Schedule schedule;

    @ManyToOne
    @JoinColumn(name = "seat_id", nullable = false)
    private Seat seat;

    @Column(name = "discount")
    private String discount;

    @Column(name = "price", nullable = false)
    private double price;

    @Column(name = "final_price", nullable = false)
    private double finalPrice;

    @Enumerated(EnumType.STRING)
    @Column(name = "ticket_status", nullable = false)
    private TicketStatus ticketStatus;
}
