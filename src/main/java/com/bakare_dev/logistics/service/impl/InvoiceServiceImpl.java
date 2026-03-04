package com.bakare_dev.logistics.service.impl;

import com.bakare_dev.logistics.dto.request.CreateInvoiceRequest;
import com.bakare_dev.logistics.dto.request.InvoiceItemRequest;
import com.bakare_dev.logistics.dto.response.InvoiceItemResponse;
import com.bakare_dev.logistics.dto.response.InvoiceResponse;
import com.bakare_dev.logistics.entity.*;
import com.bakare_dev.logistics.exception.DuplicateResourceException;
import com.bakare_dev.logistics.exception.InvalidOperationException;
import com.bakare_dev.logistics.exception.ResourceNotFoundException;
import com.bakare_dev.logistics.repository.InvoiceRepository;
import com.bakare_dev.logistics.repository.ShipmentRepository;
import com.bakare_dev.logistics.service.InvoiceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class InvoiceServiceImpl implements InvoiceService {

    private final InvoiceRepository invoiceRepository;
    private final ShipmentRepository shipmentRepository;

    @Override
    @Transactional
    public InvoiceResponse createInvoice(CreateInvoiceRequest request) {
        Shipment shipment = shipmentRepository.findById(request.getShipmentId())
                .orElseThrow(() -> new ResourceNotFoundException("Shipment", "id", request.getShipmentId()));

        if (shipment.getStatus() != ShipmentStatus.DELIVERED) {
            throw new InvalidOperationException("Can only create invoices for delivered shipments");
        }

        if (invoiceRepository.findByShipmentId(request.getShipmentId()).isPresent()) {
            throw new DuplicateResourceException("Invoice", "shipmentId", request.getShipmentId());
        }


        BigDecimal subtotal = BigDecimal.ZERO;
        List<InvoiceItem> items = new ArrayList<>();

        for (InvoiceItemRequest itemRequest : request.getItems()) {
            BigDecimal itemTotal = itemRequest.getUnitPrice()
                    .multiply(BigDecimal.valueOf(itemRequest.getQuantity()))
                    .setScale(2, RoundingMode.HALF_UP);
            subtotal = subtotal.add(itemTotal);

            InvoiceItem item = new InvoiceItem();
            item.setDescription(itemRequest.getDescription());
            item.setUnitPrice(itemRequest.getUnitPrice());
            item.setQuantity(itemRequest.getQuantity());
            item.setTotal(itemTotal);
            items.add(item);
        }

        BigDecimal tax = subtotal.multiply(request.getTaxRate())
                .divide(BigDecimal.valueOf(100), 2, RoundingMode.HALF_UP);
        BigDecimal totalAmount = subtotal.add(tax);

        Invoice invoice = new Invoice();
        invoice.setShipment(shipment);
        invoice.setInvoiceNumber(generateInvoiceNumber());
        invoice.setBilledToName(request.getBilledToName());
        invoice.setBilledToEmail(request.getBilledToEmail());
        invoice.setBilledToAddress(request.getBilledToAddress());
        invoice.setSubtotal(subtotal);
        invoice.setTax(tax);
        invoice.setTotalAmount(totalAmount);
        invoice.setStatus(InvoiceStatus.UNPAID);
        invoice.setDueDate(LocalDate.now().plusDays(30));

        items.forEach(item -> item.setInvoice(invoice));
        invoice.setItems(items);

        invoiceRepository.save(invoice);
        return mapToInvoiceResponse(invoice);
    }

    @Override
    public InvoiceResponse getInvoiceById(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        return mapToInvoiceResponse(invoice);
    }

    @Override
    public InvoiceResponse getInvoiceByNumber(String invoiceNumber) {
        Invoice invoice = invoiceRepository.findByInvoiceNumber(invoiceNumber)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "invoiceNumber", invoiceNumber));
        return mapToInvoiceResponse(invoice);
    }

    @Override
    public InvoiceResponse getInvoiceByShipmentId(Long shipmentId) {
        Invoice invoice = invoiceRepository.findByShipmentId(shipmentId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "shipmentId", shipmentId));
        return mapToInvoiceResponse(invoice);
    }

    @Override
    public List<InvoiceResponse> getInvoicesByStatus(InvoiceStatus status) {
        return invoiceRepository.findByStatus(status).stream()
                .map(this::mapToInvoiceResponse)
                .toList();
    }

    @Override
    public List<InvoiceResponse> getAllInvoices() {
        return invoiceRepository.findAll().stream()
                .map(this::mapToInvoiceResponse)
                .toList();
    }

    @Override
    @Transactional
    public void markAsPaid(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        invoice.setStatus(InvoiceStatus.PAID);
        invoiceRepository.save(invoice);
    }

    @Override
    @Transactional
    public void markAsOverdue(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));

        if (invoice.getStatus() != InvoiceStatus.UNPAID) {
            throw new InvalidOperationException("Only unpaid invoices can be marked as overdue");
        }

        invoice.setStatus(InvoiceStatus.OVERDUE);
        invoiceRepository.save(invoice);
    }

    private String generateInvoiceNumber() {
        return "INV-" + System.currentTimeMillis() + "-" + UUID.randomUUID().toString().substring(0, 4).toUpperCase();
    }

    private InvoiceResponse mapToInvoiceResponse(Invoice invoice) {
        List<InvoiceItemResponse> itemResponses = invoice.getItems() != null
                ? invoice.getItems().stream().map(this::mapToItemResponse).toList()
                : List.of();

        return InvoiceResponse.builder()
                .id(invoice.getId())
                .invoiceNumber(invoice.getInvoiceNumber())
                .shipmentId(invoice.getShipment().getId())
                .trackingNumber(invoice.getShipment().getTrackingNumber())
                .billedToName(invoice.getBilledToName())
                .billedToEmail(invoice.getBilledToEmail())
                .billedToAddress(invoice.getBilledToAddress())
                .subtotal(invoice.getSubtotal())
                .tax(invoice.getTax())
                .totalAmount(invoice.getTotalAmount())
                .status(invoice.getStatus())
                .dueDate(invoice.getDueDate())
                .items(itemResponses)
                .createdAt(invoice.getCreatedAt())
                .build();
    }

    private InvoiceItemResponse mapToItemResponse(InvoiceItem item) {
        return InvoiceItemResponse.builder()
                .id(item.getId())
                .description(item.getDescription())
                .unitPrice(item.getUnitPrice())
                .quantity(item.getQuantity())
                .total(item.getTotal())
                .build();
    }
}
