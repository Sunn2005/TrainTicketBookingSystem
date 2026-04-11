package iuh.fit.controller;

import controller.CustomerController;
import model.entity.Customer;

import java.util.List;

public class CustomerClientController {
    private final CustomerController delegate;

    public CustomerClientController() {
        this.delegate = new CustomerController();
    }

    public CustomerClientController(CustomerController delegate) {
        this.delegate = delegate;
    }

    public Customer getCustomerById(String customerId) {
        return delegate.getCustomerById(customerId);
    }

    public List<Customer> getAllCustomers() {
        return delegate.getAllCustomers();
    }
}