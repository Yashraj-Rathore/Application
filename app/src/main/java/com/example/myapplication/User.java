package com.example.myapplication;

public class User {
    private String Firstname;
    private String email;


    // Default constructor is required for Firebase Realtime Database
    public User() {
    }

    // Constructor with parameters
    public User(String username, String email) {
        this.Firstname = username;
        this.email = email;
    }

    public User(String username) {
        this.Firstname = username;

    }

    // Getters and Setters
    public String getUsername() {
        return Firstname;
    }

    public void setUsername(String username) {
        this.Firstname = username;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    // You can add more fields and methods as needed
}
