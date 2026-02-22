package com.bakare_dev.logistics.repository;

import com.bakare_dev.logistics.entity.Invoice;
import com.bakare_dev.logistics.entity.InvoiceItem;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface InvoiceItemRepository extends JpaRepository<InvoiceItem, Long> {

    List<InvoiceItem> findByInvoice(Invoice invoice);

    List<InvoiceItem> findByInvoiceId(Long invoiceId);
}