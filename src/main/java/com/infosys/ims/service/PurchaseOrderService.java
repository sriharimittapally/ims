package com.infosys.ims.service;

import com.infosys.ims.dtos.request.PurchaseOrderRejectionRequest;
import com.infosys.ims.dtos.request.PurchaseOrderRequest;
import com.infosys.ims.dtos.response.PurchaseOrderResponse;
import com.infosys.ims.entity.Product;
import com.infosys.ims.entity.PurchaseOrder;
import com.infosys.ims.entity.Warehouse;
import com.infosys.ims.enums.PurchaseOrderStatus;

import java.util.List;

public interface PurchaseOrderService {

    PurchaseOrderResponse createPO(String managerEmail, PurchaseOrderRequest request);

    /** System auto-drafts PO when stock falls below reorder level */
    void autoDraftPOIfLowStock(Product product, Warehouse warehouse);

    /** Manager approves and sends draft PO to supplier */
    PurchaseOrderResponse sendPO(Long poId, String managerEmail);

    /** Staff marks PO as received and stock is added */
    PurchaseOrderResponse receivePO(Long poId, String staffEmail);

    PurchaseOrderResponse cancelPO(Long poId, String userEmail);

    List<PurchaseOrderResponse> getAllPOs();

    List<PurchaseOrderResponse> getPOsByStatus(PurchaseOrderStatus status);

    List<PurchaseOrderResponse> getMyWarehousePOsByStatus(
            String email,
            PurchaseOrderStatus status
    );

    List<PurchaseOrderResponse> getMyWarehousePOs(String managerEmail);

    PurchaseOrderResponse getPOById(Long poId);

    List<PurchaseOrderResponse> getMyPOs(String supplierEmail);

    List<PurchaseOrderResponse> getMyPOsByStatus(String supplierEmail, PurchaseOrderStatus status);

    PurchaseOrderResponse acceptPO(Long poId, String supplierEmail);

    PurchaseOrderResponse rejectPO(Long poId, String supplierEmail, PurchaseOrderRejectionRequest request);

    PurchaseOrderResponse shipPO(Long poId, String supplierEmail);

    PurchaseOrder getPOEntity(Long poId);
}