package com.infosys.ims.enums;

public enum StockIssueStatus {
    DRAFT,      // Staff is building the issue (not visible to manager yet)
    PENDING,    // Staff submitted for manager review
    APPROVED,   // Manager approved — staff can execute stock out
    ISSUED,     // Staff executed stock out — inventory deducted
    REJECTED,   // Manager rejected
    CANCELLED   // Staff or manager cancelled
}