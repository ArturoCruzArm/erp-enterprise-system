package com.erp.system.purchase.enums;

public enum PurchaseOrderStatus {
    DRAFT,
    PENDING_APPROVAL,
    APPROVED,
    SENT_TO_SUPPLIER,
    ACKNOWLEDGED_BY_SUPPLIER,
    IN_PRODUCTION,
    SHIPPED,
    PARTIALLY_RECEIVED,
    COMPLETED,
    CANCELLED,
    ON_HOLD,
    RETURNED
}