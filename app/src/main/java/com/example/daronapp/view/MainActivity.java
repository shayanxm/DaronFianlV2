package com.example.daronapp.view;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;
import android.util.Log;
import android.view.View;

import com.airbnb.lottie.LottieAnimationView;
import com.example.daronapp.R;
import com.example.daronapp.adapter.CustomAdapter;
import com.example.daronapp.adapter.ListPaddingDecoration;
import com.example.daronapp.model.Data;
import com.example.daronapp.model.Product;

import com.example.daronapp.model.Repository;
import com.example.daronapp.network.GetDateService;
import com.example.daronapp.network.RetrofitClientInstance;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;

public class MainActivity extends AppCompatActivity {

    RecyclerView recyclerView;
    CustomAdapter adapter;
    RecyclerView.LayoutManager layoutManager;
    boolean firstTime = true;
    int visibleItemCount;
    int totalItemCount;
    int pastVisibleItems;
    boolean doload = true;
    int lastPage = 0;
    int loadPageNumber = 2;
LottieAnimationView lottieAnimationView;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        recyclerView = findViewById(R.id.product_rv);
lottieAnimationView=findViewById(R.id.lottie_loading);
lottieAnimationView.setVisibility(View.INVISIBLE);

        layoutManager = new LinearLayoutManager(MainActivity.this);
        recyclerView.setLayoutManager(layoutManager);
        ListPaddingDecoration listPaddingDecoration = new ListPaddingDecoration(MainActivity.this);


        networkRequester(1);
        recyclerView.addOnScrollListener(new RecyclerView.OnScrollListener() {
            @Override
            public void onScrolled(RecyclerView recyclerView, int dx, int dy) {
                if (dy > 0) {
                    visibleItemCount = layoutManager.getChildCount();
                    totalItemCount = layoutManager.getItemCount();
                    pastVisibleItems = ((LinearLayoutManager) recyclerView.getLayoutManager()).findFirstVisibleItemPosition();
                    if (pastVisibleItems + visibleItemCount >= adapter.getItemCount()) {

                        if (doload) {
                            ///load data
                            Log.e("page", "---------------------load page " + loadPageNumber);

                            networkRequester(loadPageNumber);
                            loadPageNumber++;

                            if (loadPageNumber >= lastPage) doload = false;
                        }
                    }
                }
            }
        });
    }

    //methods
    private void networkRequester(int pageNo) {
        GetDateService service = RetrofitClientInstance.getRetrofitInstance().create(GetDateService.class);
        Call<Product> call = service.getProduct(pageNo);
        lottieAnimationView.setVisibility(View.VISIBLE);

        call.enqueue(new Callback<Product>() {
            @Override
            public void onResponse(Call<Product> call, Response<Product> response) {
                generateDataList(response.body());
                Product product = response.body();
                product.getMeta().getLastPage();
                product.getMeta().getLastPage();
                lottieAnimationView.setVisibility(View.INVISIBLE);

            }

            @Override
            public void onFailure(Call<Product> call, Throwable t) {

                Log.e("test", t + "e");
            }
        });
    }


    private void generateDataList(Product product) {

        List<Data> data = product.getDataList();
        boolean isEmpty = Repository.getInstance().getDataTillNow() == null;
        if (isEmpty) {
            Repository.getInstance().setDataTillNow(data);
            Log.e("page", "first");
        } else {
            Log.e("page", "second");
            List<Data> old = Repository.getInstance().getDataTillNow();
            Log.e("page", old.size() + "old size");
            old.addAll(data);
            Log.e("page", old.size() + "old 2 size");
            Repository.getInstance().setDataTillNow(old);


        }
        Log.e("page", Repository.getInstance().getDataTillNow().size() + "repo 2 size");
        updateUi();
    }

    private void updateUi() {
        if (adapter == null)
            adapter = new CustomAdapter(this, Repository.getInstance().getDataTillNow());

        adapter.updateData(Repository.getInstance().getDataTillNow());
        adapter.notifyDataSetChanged();
        if (firstTime) {
            firstTime = false;
            recyclerView.setAdapter(adapter);
        }

    }

}