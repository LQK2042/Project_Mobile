package com.example.doanck.data.model;


import com.google.gson.annotations.SerializedName;

public class Category {

    @SerializedName("ID")
    private final int id;

    @SerializedName("ProductName")
    private final String name;

    @SerializedName("Category")
    private final String category;

    @SerializedName("ImageURL")
    private final String imageUrl;

    public Category(int id, String name, String category, String imageUrl) {
        this.id = id;
        this.name = name;
        this.category = category;
        this.imageUrl = imageUrl;
    }

    public int getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getCategory() {
        return category;
    }

    public String getImageUrl() {
        return imageUrl;
    }
}
//end