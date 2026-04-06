package dao;

import model.entity.Customer;

public class CustomerDAO extends BaseDAO<Customer, String> {
    public CustomerDAO() {
        super(Customer.class);
    }
}
