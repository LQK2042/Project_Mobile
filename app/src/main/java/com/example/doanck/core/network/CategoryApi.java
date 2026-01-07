//code thu thach 5 - hoatd
//start
package com.example.doanck.core.network;

import com.example.doanck.data.model.CategoryResponse;

import java.util.List;

import retrofit2.Call;
import retrofit2.http.GET;

public interface CategoryApi {

    @GET("api.js?action=products")
    Call<CategoryResponse> getCategories();
}
//end