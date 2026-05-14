package com.infosys.ims.mapper;

import com.infosys.ims.dtos.response.PurchaseOrderItemResponse;
import com.infosys.ims.dtos.response.PurchaseOrderResponse;
import com.infosys.ims.entity.PurchaseOrder;
import com.infosys.ims.entity.PurchaseOrderItem;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
public class PurchaseOrderMapper {

    public PurchaseOrderResponse mapToResponse(PurchaseOrder po) {
        PurchaseOrderResponse response = new PurchaseOrderResponse();
        response.setId(po.getId());
        response.setPoNumber(po.getPoNumber());
        response.setSupplierId(po.getSupplier().getId());
        response.setSupplierName(po.getSupplier().getUser().getName());
        response.setSupplierUserCode(po.getSupplier().getUser().getUserCode());
        response.setCompanyName(po.getSupplier().getCompanyName());
        response.setWarehouseId(po.getWarehouse().getId());
        response.setWarehouseName(po.getWarehouse().getName());
        response.setWarehouseCity(po.getWarehouse().getCity());
        // FIX: enum → String
        response.setStatus(po.getStatus().name());
        response.setTotalAmount(po.getTotalAmount());
        response.setNote(po.getNote());
        response.setRejectionReason(po.getRejectionReason());
        response.setCreatedByName(po.getCreatedBy() != null ? po.getCreatedBy().getName() : "SYSTEM");
        response.setCreatedAt(po.getCreatedAt());
        response.setSentAt(po.getSentAt());
        response.setAcceptedAt(po.getAcceptedAt());
        response.setShippedAt(po.getShippedAt());
        response.setReceivedAt(po.getReceivedAt());

        List itemResponses = new ArrayList<>();
        if (po.getItems() != null) {
            for (PurchaseOrderItem item : po.getItems()) {
                itemResponses.add(mapToItemResponse(item));
            }
        }
        response.setItems(itemResponses);
        return response;
    }

    public PurchaseOrderItemResponse mapToItemResponse(PurchaseOrderItem item) {
        PurchaseOrderItemResponse response = new PurchaseOrderItemResponse();
        response.setId(item.getId());
        response.setProductId(item.getProduct().getId());
        response.setProductName(item.getProduct().getProductName());
        response.setSku(item.getProduct().getSku());
        response.setCategoryName(item.getProduct().getCategory().getName());
        response.setQuantity(item.getQuantity());
        response.setPurchasePrice(item.getPurchasePrice());
        response.setLineTotal(item.getLineTotal());
        return response;
    }
}