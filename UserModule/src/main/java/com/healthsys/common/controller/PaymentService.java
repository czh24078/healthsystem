package com.healthsys.common.controller;

public interface PaymentService {
    boolean pay(Long appointmentId, double amount);
}
