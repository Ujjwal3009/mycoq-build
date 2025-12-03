package com.example.auth;

public class AuthService {
    public boolean authenticate(String user) {
        return user != null && !user.isEmpty();
    }
}
