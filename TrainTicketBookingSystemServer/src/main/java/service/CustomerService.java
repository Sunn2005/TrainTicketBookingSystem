package service;

import dao.CustomerDAO;
import model.entity.Customer;

import java.time.LocalDate;
import java.util.List;

public class CustomerService {
    private final CustomerDAO customerDAO;

    public CustomerService() {
        this.customerDAO = new CustomerDAO();
    }

    public CustomerService(CustomerDAO customerDAO) {
        this.customerDAO = customerDAO;
    }

    /**
     * Tra cứu thông tin khách hàng theo ID (cccd)
     */
    public Customer getCustomerById(String customerId) {
        return customerDAO.findByID(customerId).orElse(null);
    }

    /**
     * Tra cứu thông tin khách hàng theo ID hoặc Tên
     *   public List<Customer> searchCustomers(String keyword) {
     *         return customerDAO.searchCustomers(keyword);
     *     }
     */


    /**
     * Lấy danh sách tất cả khách hàng
     */
    public List<Customer> getAllCustomers() {
        return customerDAO.findAll();
    }

    public List<Customer> getCustomersBookedBetween(LocalDate from, LocalDate to) {
        return customerDAO.findCustomersBookedBetween(from, to);
    }
}
