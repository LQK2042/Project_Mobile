package com.example.doanck.data.model;

import java.util.List;

public class ProductResponse {
    private boolean success;
    private int count;
    private List<ProductItem> data;

    public boolean isSuccess() { return success; }
    public int getCount() { return count; }
    public List<ProductItem> getData() { return data; }
}

