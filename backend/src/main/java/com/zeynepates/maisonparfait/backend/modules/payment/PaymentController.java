package com.zeynepates.maisonparfait.backend.modules.payment;

import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/payments")
public class PaymentController {

    private final PaymentService paymentService;

    @PostMapping("/{id}/confirm")
    public PaymentResponse confirm(@PathVariable Long id,
                                   @RequestParam PaymentResult result) {
        return paymentService.confirmPayment(id, result);
    }

    @GetMapping("/{id}")
    public PaymentResponse getPayment(@PathVariable Long id) {
        return paymentService.getPaymentById(id);
    }
}
