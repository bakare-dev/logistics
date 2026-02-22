package com.bakare_dev.logistics.repository;

import com.bakare_dev.logistics.entity.Invoice;
import com.bakare_dev.logistics.entity.InvoiceStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

interface InvoiceRepository extends JpaRepository<Invoice, Long> {
    Optional<Invoice> findByInvoiceNumber(String invoiceNumber);

    List<Invoice> findByStatus(InvoiceStatus status);

    Optional<Invoice> findByShipmentId(Long shipmentId);
}
