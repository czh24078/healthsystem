package com.healthsys.common.controller;

import javax.swing.JOptionPane;

public class MockPaymentService implements PaymentService {

    @Override
    public boolean pay(Long appointmentId, double amount) {
        // 模拟网络请求或第三方支付 SDK 调用
        System.out.println("正在处理支付: 预约ID=" + appointmentId + ", 金额=¥" + amount);

        // 模拟支付成功
        return true;
    }
}
