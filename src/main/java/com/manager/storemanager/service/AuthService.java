package com.manager.storemanager.service;

import com.manager.storemanager.dao.UserDao;
import com.manager.storemanager.model.User;
import com.manager.storemanager.util.PasswordUtils;
import com.manager.storemanager.util.ValidationUtils;
import java.sql.SQLException;

public class AuthService {

    private final UserDao userDao = new UserDao();

    public User login(String username, char[] password) throws SQLException {
        ValidationUtils.requireNotBlank(username, "usuario");
        ValidationUtils.requireNotBlank(password == null ? null : new String(password), "contraseña");

        User user = userDao.authenticate(username.trim(), PasswordUtils.sha256(new String(password))).orElse(null);
        if (user == null) {
            throw new IllegalArgumentException("Usuario o contraseña inválidos.");
        }
        return user;
    }
}
