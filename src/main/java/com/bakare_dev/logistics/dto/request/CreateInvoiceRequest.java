package com.bakare_dev.logistics.dto.request;

import jakarta.validation.Valid;
import jakarta.validation.constraints.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreateInvoiceRequest {
    @NotNull(message = "Shipment ID is required")
    private Long shipmentId;

    @NotBlank(message = "Billed to name is required")
    private String billedToName;

    @NotBlank(message = "Billed to email is required")
    @Email(message = "Billed to email must be valid")
    private String billedToEmail;

    @NotBlank(message = "Billed to address is required")
    private String billedToAddress;

    @NotNull(message = "Tax rate is required")
    @PositiveOrZero(message = "Tax rate must be zero or positive")
    private BigDecimal taxRate;

    @NotEmpty(message = "At least one invoice item is required")
    @Valid
    private List<InvoiceItemRequest> items;
}