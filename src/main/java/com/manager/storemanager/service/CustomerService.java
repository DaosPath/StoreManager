package com.manager.storemanager.service;

import com.manager.storemanager.dao.CustomerDao;
import com.manager.storemanager.model.Customer;
import com.manager.storemanager.util.ValidationUtils;
import java.sql.SQLException;
import java.util.List;

public class CustomerService {

    private final CustomerDao customerDao = new CustomerDao();

    public List<Customer> findCustomers(String search) throws SQLException {
        return customerDao.findAll(search);
    }

    public Customer save(Customer customer) throws SQLException {
        validate(customer);
        return customerDao.save(customer);
    }

    public void update(Customer customer) throws SQLException {
        validate(customer);
        customerDao.update(customer);
    }

    private void validate(Customer customer) {
        ValidationUtils.requireNotBlank(customer.getName(), "nombre");
    }
}
