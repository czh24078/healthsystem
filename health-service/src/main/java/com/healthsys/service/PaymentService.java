package com.healthsys.service;

public interface PaymentService {
    boolean pay(Long appointmentId, double amount);
}
