package com.infosys.ims.service;

import com.infosys.ims.dtos.response.*;

public interface DashboardService {
    AdminDashboardResponse getAdminDashboard();
    ManagerDashboardResponse getManagerDashboard(String managerEmail);
    StaffDashboardResponse getStaffDashboard(String staffEmail);
    SupplierDashboardResponse getSupplierDashboard(String supplierEmail);
}