package com.example.payment;

import com.example.logging.Logger;
import com.example.auth.AuthService;

public class PaymentApp {

    public static void main(String[] args) {
        Logger.log("Starting payment service...");

        AuthService auth = new AuthService();
        boolean ok = auth.authenticate("user123");

        Logger.log("Auth result: " + ok);

        System.out.println("Payment service running successfully!");
    }
}
