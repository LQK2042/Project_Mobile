package com.example.doanck.feature.category;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;

import com.example.doanck.R;
import com.example.doanck.core.adapter.ProductGridAdapter;
import com.example.doanck.core.network.ApiClient;
import com.example.doanck.core.network.ApiService;
import com.example.doanck.data.model.Product;
import com.example.doanck.data.model.ProductItem;
import com.example.doanck.data.model.ProductResponse;

import androidx.annotation.NonNull;
import java.util.ArrayList;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class ProductGridActivity extends AppCompatActivity {

    private RecyclerView rvProducts;
    private ProductGridAdapter adapter;

    // List để hiển thị cho RecyclerView
    private List<Product> productList = new ArrayList<>();

    // List dữ liệu gốc từ API
    private List<Product> productsFull = new ArrayList<>();

    // Lazy load config
    private int currentIndex = 0;
    private final int PAGE_SIZE = 10;
    private boolean isLoading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_product_grid);

        rvProducts = findViewById(R.id.rvProducts);
        rvProducts.setLayoutManager(new GridLayoutManager(this, 2));

        adapter = new ProductGridAdapter(productList);
        rvProducts.setAdapter(adapter);

        loadProductsFromApi();
        setupLazyLoad();
    }

    private void loadProductsFromApi() {
        ApiService api = ApiClient.getApiService();

        api.getProducts().enqueue(new Callback<ProductResponse>() {

            @Override
            public void onResponse(Call<ProductResponse> call, Response<ProductResponse> response) {
                if (response.isSuccessful() && response.body() != null) {

                    List<ProductItem> items = response.body().getData();

                    for (ProductItem p : items) {
                        productsFull.add(convert(p));
                    }

                    loadMore();
                }
            }

            @Override
            public void onFailure(Call<ProductResponse> call, Throwable t) {
                Log.e("API_ERR", "Failed: " + t.getMessage());
            }
        });
    }


    private void setupLazyLoad() {
        rvProducts.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(@NonNull RecyclerView recyclerView, int dx, int dy) {
                super.onScrolled(recyclerView, dx, dy);

                GridLayoutManager layoutManager = (GridLayoutManager) recyclerView.getLayoutManager();

                if (!isLoading && layoutManager != null &&
                        layoutManager.findLastVisibleItemPosition() >= productList.size() - 1) {

                    loadMore();
                }
            }
        });
    }

    private void loadMore() {
        if (isLoading) return;
        isLoading = true;

        int nextIndex = Math.min(currentIndex + PAGE_SIZE, productsFull.size());

        for (int i = currentIndex; i < nextIndex; i++) {
            productList.add(productsFull.get(i));
        }

        currentIndex = nextIndex;
        adapter.notifyDataSetChanged();

        isLoading = false;
    }

    private Product convert(ProductItem p) {
        return new Product(
                p.getID(),
                p.getProductName(),
                0,
                p.getImageURL(),
                p.getCategory()
        );
    }

}

