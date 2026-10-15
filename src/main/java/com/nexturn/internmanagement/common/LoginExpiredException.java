package com.nexturn.internmanagement.common;

public class LoginExpiredException extends RuntimeException {
    public LoginExpiredException() {
        super("Login expired. Please check with HR.");
    }
}
