package com.manager.storemanager.dao;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.Role;
import com.manager.storemanager.model.User;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Optional;

public class UserDao {

    public Optional<User> authenticate(String username, String passwordHash) {
        String sql = """
                SELECT u.id, u.username, u.password_hash, u.nombre_completo, u.activo, u.ultimo_acceso,
                       r.id AS role_id, r.nombre AS role_name, r.descripcion, r.activo AS role_active
                FROM usuarios u
                INNER JOIN roles r ON r.id = u.rol_id
                WHERE u.username = ? AND u.password_hash = ? AND u.activo = 1
                """;
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setString(1, username);
            statement.setString(2, passwordHash);
            try (ResultSet resultSet = statement.executeQuery()) {
                if (!resultSet.next()) {
                    return Optional.empty();
                }
                User user = mapUser(resultSet);
                updateLastAccess(user.getId());
                return Optional.of(user);
            }
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible validar el usuario.", exception);
        }
    }

    public void updateLastAccess(Long userId) {
        String sql = "UPDATE usuarios SET ultimo_acceso = CURRENT_TIMESTAMP WHERE id = ?";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setLong(1, userId);
            statement.executeUpdate();
        } catch (SQLException exception) {
            throw new IllegalStateException("No fue posible actualizar el ultimo acceso.", exception);
        }
    }

    private User mapUser(ResultSet resultSet) throws SQLException {
        Role role = new Role();
        role.setId(resultSet.getLong("role_id"));
        role.setName(resultSet.getString("role_name"));
        role.setDescription(resultSet.getString("descripcion"));
        role.setActive(resultSet.getBoolean("role_active"));

        User user = new User();
        user.setId(resultSet.getLong("id"));
        user.setUsername(resultSet.getString("username"));
        user.setPasswordHash(resultSet.getString("password_hash"));
        user.setFullName(resultSet.getString("nombre_completo"));
        user.setActive(resultSet.getBoolean("activo"));
        Timestamp lastAccess = resultSet.getTimestamp("ultimo_acceso");
        user.setLastAccess(lastAccess == null ? null : lastAccess.toLocalDateTime());
        user.setRole(role);
        return user;
    }
}
