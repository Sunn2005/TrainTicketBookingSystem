package dao;

import entity.Payment;

public class PaymentDAO extends BaseDAO<Payment, String> {
    public PaymentDAO() {
        super(Payment.class);
    }
}
