package com.example.myapplication.ui.notifications;

public class NotificationItem implements Comparable<NotificationItem> {
    private String title;
    private String message;
    private String timestamp; // Timestamp field

    public NotificationItem(String title, String message, String timestamp) {
        this.title = title;
        this.message = message;
        this.timestamp = timestamp;
    }

@Override
    public int compareTo(NotificationItem other) {
        return //this.timestamp.compareTo(other.timestamp); // Sort in ascending order
                    other.timestamp.compareTo(this.timestamp); //descending order
    }

    // Getters
    public String getTitle() {
        return title;
    }

    public String getMessage() {
        return message;
    }

    public String getTimestamp() {
        return timestamp;
    }
}

