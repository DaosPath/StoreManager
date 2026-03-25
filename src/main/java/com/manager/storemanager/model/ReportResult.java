package com.manager.storemanager.model;

import java.util.ArrayList;
import java.util.List;

public class ReportResult {

    private final List<String> columns = new ArrayList<>();
    private final List<Object[]> rows = new ArrayList<>();

    public List<String> getColumns() {
        return columns;
    }

    public List<Object[]> getRows() {
        return rows;
    }
}
