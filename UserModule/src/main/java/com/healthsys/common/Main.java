package com.healthsys.common;

import com.healthsys.common.view.LoginView;

import javax.swing.*;

public class Main {
    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            LoginView loginView = new LoginView();
            loginView.showLogin();
        });
    }
}