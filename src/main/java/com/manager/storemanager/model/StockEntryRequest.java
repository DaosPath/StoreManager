package com.manager.storemanager.model;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.List;

public class StockEntryRequest {

    private User user;
    private String entryMode;
    private Supplier supplier;
    private String documentType;
    private String series;
    private String correlative;
    private String destination;
    private String adjustmentType;
    private String generalReason;
    private String note;
    private final List<StockEntryItem> items = new ArrayList<>();

    public User getUser() {
        return user;
    }

    public void setUser(User user) {
        this.user = user;
    }

    public String getEntryMode() {
        return entryMode;
    }

    public void setEntryMode(String entryMode) {
        this.entryMode = entryMode;
    }

    public Supplier getSupplier() {
        return supplier;
    }

    public void setSupplier(Supplier supplier) {
        this.supplier = supplier;
    }

    public String getDocumentType() {
        return documentType;
    }

    public void setDocumentType(String documentType) {
        this.documentType = documentType;
    }

    public String getSeries() {
        return series;
    }

    public void setSeries(String series) {
        this.series = series;
    }

    public String getCorrelative() {
        return correlative;
    }

    public void setCorrelative(String correlative) {
        this.correlative = correlative;
    }

    public String getDestination() {
        return destination;
    }

    public void setDestination(String destination) {
        this.destination = destination;
    }

    public String getAdjustmentType() {
        return adjustmentType;
    }

    public void setAdjustmentType(String adjustmentType) {
        this.adjustmentType = adjustmentType;
    }

    public String getGeneralReason() {
        return generalReason;
    }

    public void setGeneralReason(String generalReason) {
        this.generalReason = generalReason;
    }

    public String getNote() {
        return note;
    }

    public void setNote(String note) {
        this.note = note;
    }

    public List<StockEntryItem> getItems() {
        return items;
    }

    public String getDocumentReference() {
        String left = series == null ? "" : series.trim();
        String right = correlative == null ? "" : correlative.trim();
        if (left.isBlank() && right.isBlank()) {
            return "";
        }
        if (left.isBlank()) {
            return right;
        }
        if (right.isBlank()) {
            return left;
        }
        return left + "-" + right;
    }

    public int getTotalLines() {
        return items.size();
    }

    public int getTotalUnits() {
        return items.stream()
                .map(StockEntryItem::getQuantity)
                .filter(quantity -> quantity != null)
                .mapToInt(Integer::intValue)
                .sum();
    }

    public BigDecimal getTotalAmount() {
        return items.stream()
                .map(StockEntryItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }
}
