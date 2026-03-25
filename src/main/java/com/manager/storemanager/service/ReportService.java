package com.manager.storemanager.service;

import com.manager.storemanager.dao.ReportDao;
import com.manager.storemanager.model.DailySalesTotal;
import com.manager.storemanager.model.DashboardSummary;
import java.sql.SQLException;
import java.time.LocalDate;
import java.util.List;

public class ReportService {

    private final ReportDao reportDao = new ReportDao();

    public DashboardSummary loadDashboardSummary() throws SQLException {
        return reportDao.loadDashboardSummary();
    }

    public List<DailySalesTotal> findDailyTotals(LocalDate from, LocalDate to) throws SQLException {
        return reportDao.findDailyTotals(from, to);
    }
}
