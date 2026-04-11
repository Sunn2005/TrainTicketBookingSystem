package controller;
import model.entity.Customer;
import service.CustomerService;
import java.util.List;
public class CustomerController {
    private final CustomerService customerService;
    public CustomerController() {
        this.customerService = new CustomerService();
    }
    public CustomerController(CustomerService customerService) {
        this.customerService = customerService;
    }
    /**
     * API Tra cứu thông tin khách hàng bằng ID
     */
    public Customer getCustomerById(String customerId) {
        return customerService.getCustomerById(customerId);
    }

    /**
     * API Tra cứu thông tin khách hàng bằng Tên hoặc CCCD (ID)
     * Trả về danh sách khách hàng vì Tên có thể trùng
     *  public List<Customer> searchCustomers(String keyword) {
     *         return customerService.searchCustomers(keyword);
     *     }
     */


    /**
     * API Lấy toàn bộ danh sách khách hàng
     */
    public List<Customer> getAllCustomers() {
        return customerService.getAllCustomers();
    }
}
