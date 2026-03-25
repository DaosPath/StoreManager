package com.manager.storemanager.util;

import javax.swing.JTable;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.JTableHeader;

public final class TableUtils {

    private TableUtils() {
    }

    public static DefaultTableModel nonEditableModel(String... columns) {
        return new DefaultTableModel(columns, 0) {
            @Override
            public boolean isCellEditable(int row, int column) {
                return false;
            }
        };
    }

    public static void configureTable(JTable table) {
        table.setRowHeight(44);
        table.setFillsViewportHeight(true);
        table.setAutoCreateRowSorter(true);
        table.setShowHorizontalLines(true);
        table.setShowVerticalLines(false);
        table.setGridColor(new java.awt.Color(235, 240, 248));
        table.setIntercellSpacing(new java.awt.Dimension(0, 1));
        table.setSelectionBackground(new java.awt.Color(235, 241, 255));
        table.setSelectionForeground(new java.awt.Color(17, 24, 39));
        table.setFont(new java.awt.Font("Segoe UI", java.awt.Font.PLAIN, 13));

        JTableHeader header = table.getTableHeader();
        header.setReorderingAllowed(false);
        header.setBackground(new java.awt.Color(248, 250, 253));
        header.setForeground(new java.awt.Color(90, 100, 115));
        header.setFont(new java.awt.Font("Segoe UI", java.awt.Font.BOLD, 12));
        header.setPreferredSize(new java.awt.Dimension(0, 40));
    }
}
