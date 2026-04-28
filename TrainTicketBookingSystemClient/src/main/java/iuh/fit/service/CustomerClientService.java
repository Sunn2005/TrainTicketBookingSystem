package iuh.fit.service;

import model.entity.Customer;
import iuh.fit.socketconfig.SocketClient;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.fasterxml.jackson.databind.SerializationFeature;

import java.time.LocalDate;
import java.util.List;

public class CustomerClientService {
    private final SocketClient socketClient = new SocketClient();
    private final ObjectMapper objectMapper = new ObjectMapper()
            .registerModule(new JavaTimeModule())
            .disable(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS);

    public CustomerClientService() {
    }

    public Customer getCustomerById(String customerId) {
        try {
            String message = "GET_CUSTOMER|" + customerId;
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, message);
            if (response == null || response.startsWith("ERROR") || "No response".equals(response)) {
                return null;
            }
            return objectMapper.readValue(response, Customer.class);
        } catch (Exception e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<Customer> getAllCustomers() {
        try {
            String response = socketClient.sendMessage(SocketClient.HOST, SocketClient.PORT, "GET_ALL_CUSTOMERS");
            if (response == null || response.startsWith("ERROR") || "No response".equals(response)) {
                return List.of();
            }
            return objectMapper.readValue(response, new TypeReference<List<Customer>>(){});
        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }

    public List<Customer> getCustomersBookedBetween(LocalDate from, LocalDate to) {
        try {
            String message = "GET_CUSTOMERS_BOOKED_BETWEEN|" + from + "|" + to;

            String response = socketClient.sendMessage(
                    SocketClient.HOST,
                    SocketClient.PORT,
                    message
            );

            if (response == null || response.startsWith("ERROR")
                    || "No response".equals(response)) {
                return List.of();
            }

            return objectMapper.readValue(
                    response,
                    new TypeReference<List<Customer>>() {}
            );

        } catch (Exception e) {
            e.printStackTrace();
            return List.of();
        }
    }
}