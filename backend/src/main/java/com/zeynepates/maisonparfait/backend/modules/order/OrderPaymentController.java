package com.zeynepates.maisonparfait.backend.modules.order;

import com.zeynepates.maisonparfait.backend.modules.payment.PaymentResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;

@RestController
@RequiredArgsConstructor
@RequestMapping("/orders")
public class OrderPaymentController {

    private final OrderPaymentService orderPaymentService;

    @PostMapping("/{id}/payments")
    @ResponseStatus(HttpStatus.CREATED)
    public PaymentResponse initiate(@PathVariable UUID id) {
        return orderPaymentService.initiatePayment(id);
    }
}
