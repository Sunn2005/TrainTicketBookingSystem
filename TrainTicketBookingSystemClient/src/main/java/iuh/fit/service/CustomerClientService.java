package iuh.fit.service;

import controller.CustomerController;
import model.entity.Customer;

import java.util.List;

public class CustomerClientService {
    private final CustomerController delegate;

    public CustomerClientService() {
        this.delegate = new CustomerController();
    }

    public CustomerClientService(CustomerController delegate) {
        this.delegate = delegate;
    }

    public Customer getCustomerById(String customerId) {
        return delegate.getCustomerById(customerId);
    }

    public List<Customer> getAllCustomers() {
        return delegate.getAllCustomers();
    }
}