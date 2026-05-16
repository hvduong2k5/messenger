package com.team12345.messenger.service;

public interface EmailService {

    public void sendPasswordResetEmail(String to, String otp);

}
