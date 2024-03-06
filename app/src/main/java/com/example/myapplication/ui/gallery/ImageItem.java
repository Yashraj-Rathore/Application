package com.example.myapplication.ui.gallery;

public class ImageItem {
    private String imageUrl;
    private String uploadTime;

    public ImageItem(String imageUrl, String uploadTime) {
        this.imageUrl = imageUrl;
        this.uploadTime = uploadTime;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public String getUploadTime() {
        return uploadTime;
    }
}
