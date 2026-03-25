package com.manager.storemanager.dao;

import com.manager.storemanager.config.DatabaseConnection;
import com.manager.storemanager.model.DailySalesTotal;
import com.manager.storemanager.model.DashboardSummary;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.Date;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

public class ReportDao {

    public DashboardSummary loadDashboardSummary() throws SQLException {
        DashboardSummary summary = new DashboardSummary();
        summary.setProductCount(count("SELECT COUNT(*) FROM productos WHERE estado = 'ACTIVO'"));
        summary.setCustomerCount(count("SELECT COUNT(*) FROM clientes WHERE activo = 1"));
        summary.setSupplierCount(count("SELECT COUNT(*) FROM proveedores WHERE activo = 1"));
        summary.setLowStockCount(count("SELECT COUNT(*) FROM productos WHERE estado = 'ACTIVO' AND stock <= stock_minimo"));
        summary.setTodaySalesTotal(sumTodaySales());
        return summary;
    }

    public List<DailySalesTotal> findDailyTotals(LocalDate from, LocalDate to) throws SQLException {
        String sql = """
                SELECT DATE(fecha_venta) AS fecha, COALESCE(SUM(total), 0) AS total
                FROM ventas
                WHERE DATE(fecha_venta) BETWEEN ? AND ?
                GROUP BY DATE(fecha_venta)
                ORDER BY fecha DESC
                """;
        List<DailySalesTotal> rows = new ArrayList<>();
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql)) {
            statement.setDate(1, Date.valueOf(from));
            statement.setDate(2, Date.valueOf(to));
            try (ResultSet resultSet = statement.executeQuery()) {
                while (resultSet.next()) {
                    rows.add(new DailySalesTotal(
                            resultSet.getDate("fecha").toLocalDate(),
                            resultSet.getBigDecimal("total")
                    ));
                }
            }
        }
        return rows;
    }

    private int count(String sql) throws SQLException {
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getInt(1);
        }
    }

    private BigDecimal sumTodaySales() throws SQLException {
        String sql = "SELECT COALESCE(SUM(total), 0) FROM ventas WHERE DATE(fecha_venta) = CURRENT_DATE";
        try (Connection connection = DatabaseConnection.getConnection();
             PreparedStatement statement = connection.prepareStatement(sql);
             ResultSet resultSet = statement.executeQuery()) {
            resultSet.next();
            return resultSet.getBigDecimal(1);
        }
    }
}
