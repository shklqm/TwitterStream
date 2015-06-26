package com.bledi.android.ListAdapter;

public class ExampleViewModel {
    private String text;
    private String imageUrl;

    public ExampleViewModel(String text, String imageUrl) { 
        this.text = text;
        this.imageUrl = imageUrl;
    }

    public String getText() {
        return text;
    }

    public void setText(String text) {
        this.text = text;
    }

    public String getImageUrl() {
        return imageUrl;
    }

    public void setImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}