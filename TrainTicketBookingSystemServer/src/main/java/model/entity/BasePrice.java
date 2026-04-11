package model.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class BasePrice {
    private double pricePerDistance;
    private double softSeatFee;
    private double softSleeperFee;
    private double studentDiscount;
    private double childDiscount;
    private double elderlyDiscount;

}
