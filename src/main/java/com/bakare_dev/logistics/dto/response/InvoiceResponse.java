package com.bakare_dev.logistics.dto.response;

import com.bakare_dev.logistics.entity.InvoiceStatus;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class InvoiceResponse {
    private Long id;
    private String invoiceNumber;
    private Long shipmentId;
    private String trackingNumber;
    private String billedToName;
    private String billedToEmail;
    private String billedToAddress;
    private BigDecimal subtotal;
    private BigDecimal tax;
    private BigDecimal totalAmount;
    private InvoiceStatus status;
    private LocalDate dueDate;
    private List<InvoiceItemResponse> items;
    private LocalDateTime createdAt;
}