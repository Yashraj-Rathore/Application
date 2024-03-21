package com.example.myapplication;

public class User {
    private String username;
    private String email;


    // Default constructor is required for Firebase Realtime Database
    public User() {
    }

    // Constructor with parameters
    public User(String username, String email) {
        this.username = username;
        this.email = email;
    }

    public User(String username) {
        this.username = username;

    }

    // Getters and Setters
    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // You can add more fields and methods as needed
}
