package com.bakare_dev.logistics.service;

import com.bakare_dev.logistics.dto.request.CreateInvoiceRequest;
import com.bakare_dev.logistics.dto.response.InvoiceResponse;
import com.bakare_dev.logistics.entity.InvoiceStatus;

import java.util.List;

public interface InvoiceService {
    InvoiceResponse createInvoice(CreateInvoiceRequest request);
    InvoiceResponse getInvoiceById(Long invoiceId);
    InvoiceResponse getInvoiceByNumber(String invoiceNumber);
    InvoiceResponse getInvoiceByShipmentId(Long shipmentId);
    List<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status);
    List<InvoiceResponse> getAllInvoices();
    void markAsPaid(Long invoiceId);
    void markAsOverdue(Long invoiceId);
}
