package com.bakare_dev.logistics.service.impl;

import com.bakare_dev.logistics.dto.request.ProcessPaymentRequest;
import com.bakare_dev.logistics.dto.response.PaymentResponse;
import com.bakare_dev.logistics.entity.Invoice;
import com.bakare_dev.logistics.entity.InvoiceStatus;
import com.bakare_dev.logistics.entity.Payment;
import com.bakare_dev.logistics.entity.PaymentStatus;
import com.bakare_dev.logistics.exception.InsufficientPaymentException;
import com.bakare_dev.logistics.exception.InvalidOperationException;
import com.bakare_dev.logistics.exception.ResourceNotFoundException;
import com.bakare_dev.logistics.repository.InvoiceRepository;
import com.bakare_dev.logistics.repository.PaymentRepository;
import com.bakare_dev.logistics.service.InvoiceService;
import com.bakare_dev.logistics.service.PaymentService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class PaymentServiceImpl implements PaymentService {

    private final PaymentRepository paymentRepository;
    private final InvoiceRepository invoiceRepository;
    private final InvoiceService invoiceService;

    @Override
    @Transactional
    public PaymentResponse processPayment(ProcessPaymentRequest request) {
        Invoice invoice = invoiceRepository.findById(request.getInvoiceId())
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", request.getInvoiceId()));

        if (invoice.getStatus() == InvoiceStatus.PAID) {
            throw new InvalidOperationException("Invoice is already fully paid");
        }

        BigDecimal totalPaid = paymentRepository.findByInvoice(invoice).stream()
                .filter(p -> p.getStatus() == PaymentStatus.SUCCESS)
                .map(Payment::getAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);

        BigDecimal remaining = invoice.getTotalAmount().subtract(totalPaid);

        if (request.getAmount().compareTo(remaining) > 0) {
            throw new InsufficientPaymentException("Payment amount exceeds remaining balance of " + remaining);
        }

        Payment payment = new Payment();
        payment.setInvoice(invoice);
        payment.setAmount(request.getAmount());
        payment.setPaymentReference(generatePaymentReference());
        payment.setStatus(PaymentStatus.SUCCESS);
        payment.setPaidAt(LocalDateTime.now());

        paymentRepository.save(payment);


        BigDecimal newTotalPaid = totalPaid.add(request.getAmount());
        if (newTotalPaid.compareTo(invoice.getTotalAmount()) >= 0) {
            invoiceService.markAsPaid(invoice.getId());
        }

        return mapToPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentById(Long paymentId) {
        Payment payment = paymentRepository.findById(paymentId)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "id", paymentId));
        return mapToPaymentResponse(payment);
    }

    @Override
    public PaymentResponse getPaymentByReference(String paymentReference) {
        Payment payment = paymentRepository.findByPaymentReference(paymentReference)
                .orElseThrow(() -> new ResourceNotFoundException("Payment", "paymentReference", paymentReference));
        return mapToPaymentResponse(payment);
    }

    @Override
    public List<PaymentResponse> getPaymentsByInvoice(Long invoiceId) {
        Invoice invoice = invoiceRepository.findById(invoiceId)
                .orElseThrow(() -> new ResourceNotFoundException("Invoice", "id", invoiceId));
        return paymentRepository.findByInvoice(invoice).stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    @Override
    public List<PaymentResponse> getPaymentsByStatus(PaymentStatus status) {
        return paymentRepository.findByStatus(status).stream()
                .map(this::mapToPaymentResponse)
                .toList();
    }

    private String generatePaymentReference() {
        return "PAY-" + UUID.randomUUID().toString().substring(0, 12).toUpperCase();
    }

    private PaymentResponse mapToPaymentResponse(Payment payment) {
        return PaymentResponse.builder()
                .id(payment.getId())
                .invoiceId(payment.getInvoice().getId())
                .invoiceNumber(payment.getInvoice().getInvoiceNumber())
                .amount(payment.getAmount())
                .paymentReference(payment.getPaymentReference())
                .status(payment.getStatus())
                .paidAt(payment.getPaidAt())
                .createdAt(payment.getCreatedAt())
                .build();
    }
}
