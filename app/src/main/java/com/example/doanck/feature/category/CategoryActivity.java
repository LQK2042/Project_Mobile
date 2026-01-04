//code thu thach 5 - hoatd
//start
package com.example.doanck.feature.category;


import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import com.example.doanck.R;
import com.example.doanck.core.network.ApiClient;
import com.example.doanck.core.network.CategoryApi;
import com.example.doanck.core.utils.UserHeader;
import com.example.doanck.data.model.Category;
import com.example.doanck.data.model.CategoryResponse;
import com.example.doanck.feature.profile.ProfileActivity;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class CategoryActivity extends AppCompatActivity {

    private CategoryAdapter categoryAdapter;
    private final List<Category> categories = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        EdgeToEdge.enable(this);
        setContentView(R.layout.activity_category);
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.main), (v, insets) -> {
            Insets systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars());
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom);
            return insets;
        });

        // Tìm các view cho user header
        CardView cardAvatar = findViewById(R.id.cardAvatar);
        ImageView ivUserAvatar = findViewById(R.id.ivUserAvatar);
        TextView tvGreeting = findViewById(R.id.tvGreeting);
        TextView tvUserName = findViewById(R.id.tvUserName);
        TextView tvUserEmail = findViewById(R.id.tvUserEmail);

        // Click vào avatar để mở Profile
        cardAvatar.setOnClickListener(v -> {
            Intent intent = new Intent(CategoryActivity.this, ProfileActivity.class);
            startActivity(intent);
        });

        // Gọi UserHeader.displayUserInfo để hiển thị thông tin user (bao gồm avatar từ API)
        UserHeader.displayUserInfo(this, ivUserAvatar, tvUserName, tvUserEmail, tvGreeting);

        RecyclerView rvCategories = findViewById(R.id.rvCategories);
        rvCategories.setLayoutManager(
                new LinearLayoutManager(this, LinearLayoutManager.HORIZONTAL, false)
        );

        categoryAdapter = new CategoryAdapter(categories, category -> {
            Intent intent = new Intent(CategoryActivity.this, ProductGridActivity.class);
            intent.putExtra("category_name", category.getCategory());
            // nếu Category có id, bạn có thể gửi thêm: intent.putExtra("category_id", category.getId());
            startActivity(intent);
        });
        rvCategories.setAdapter(categoryAdapter);


        loadCategoriesFromApi();
    }

    private void loadCategoriesFromApi() {
        CategoryApi api = ApiClient.getCategoryApi();
        Call<CategoryResponse> call = api.getCategories();

        call.enqueue(new Callback<CategoryResponse>() {
            @Override
            public void onResponse(Call<CategoryResponse> call, Response<CategoryResponse> response) {
                if (response.isSuccessful() && response.body() != null) {
                    CategoryResponse body = response.body();

                    if (body.isSuccess() && body.getData() != null) {
                        // Lọc trùng theo tên category
                        List<Category> uniqueCategories = new ArrayList<>();
                        Set<String> seenNames = new HashSet<>();
                        for (Category c : body.getData()) {
                            if (c == null) continue;
                            String catName = c.getCategory();
                            if (catName == null) continue;
                            if (!seenNames.contains(catName)) {
                                seenNames.add(catName);
                                uniqueCategories.add(c);
                            }
                        }

                        categories.clear();
                        categories.addAll(uniqueCategories);
                        categoryAdapter.notifyDataSetChanged();
                    } else {
                        Log.e("MainActivity", "API success=false hoặc data null");
                        Toast.makeText(CategoryActivity.this, "Không có dữ liệu", Toast.LENGTH_SHORT).show();
                    }
                } else {
                    Log.e("MainActivity", "API error: " + response.code());
                    Toast.makeText(CategoryActivity.this, "API error: " + response.code(), Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onFailure(Call<CategoryResponse> call, Throwable t) {
                Log.e("MainActivity", "API failure", t);
                Toast.makeText(CategoryActivity.this, "API failure: " + t.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }
}
//end