package com.bakare_dev.logistics.service;

import com.bakare_dev.logistics.dto.request.ProcessPaymentRequest;
import com.bakare_dev.logistics.dto.response.PaymentResponse;
import com.bakare_dev.logistics.entity.PaymentStatus;

import java.util.List;

public interface PaymentService {
    PaymentResponse processPayment(ProcessPaymentRequest request);
    PaymentResponse getPaymentById(Long paymentId);
    PaymentResponse getPaymentByReference(String paymentReference);
    List<PaymentResponse> getPaymentsByInvoice(Long invoiceId);
    List<PaymentResponse> getPaymentsByStatus(PaymentStatus status);
}
