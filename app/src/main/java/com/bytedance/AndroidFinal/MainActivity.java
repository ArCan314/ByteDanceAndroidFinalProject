package com.bytedance.AndroidFinal;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity {

    private ViewPager2 viewPager2;
    private VideoAdapter videoAdapter;
    private BottomNavigationView bnvMenu;
    private LinearLayout messageLayout;
    private int currentSelectedItemId = -1;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);;
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            // window.setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_ADJUST_PAN);
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_main);
        viewPager2 = findViewById(R.id.video_info_viewpager2);
        videoAdapter = new VideoAdapter(this, viewPager2);
        viewPager2.setAdapter(videoAdapter);
        viewPager2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Log.d("test", "Test");
            }
        });
        bnvMenu = findViewById(R.id.bnv_bottom_select);
        bnvMenu.setOnNavigationItemSelectedListener(item -> {
            if (currentSelectedItemId == item.getItemId())
                return false;
            currentSelectedItemId = item.getItemId();
            switch (item.getItemId()) {
                case R.id.main_page:
                    videoAdapter.restore();
                    return true;
                case R.id.message_page:
                    videoAdapter.save();
                    return true;
                default:
                    break;
            }
            return false;
        });

        getData();
    }

    @Override
    public void onBackPressed() {
        VideoAdapter.VideoViewHolder viewHolder = videoAdapter.viewHolderList.get(videoAdapter.getCurrentPos());
        View comment = viewHolder.getComment();
        if (comment.getVisibility() == View.VISIBLE) {
            viewHolder.close_comment.performClick();
        }
        else
            super.onBackPressed();
    }

    private void getData() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://beiyou.bytedance.com/")
                .addConverterFactory(GsonConverterFactory.create())
                .build();

        ApiService apiService = retrofit.create(ApiService.class);
        apiService.getVideoData().enqueue(new Callback<List<ApiResponse>>() {
            @Override
            public void onResponse(Call<List<ApiResponse>> call, Response<List<ApiResponse>> response) {
                if (response.body() != null) {
                    List<ApiResponse> apiResponses = response.body();
                    Log.d("retrofit", apiResponses.toString());
                    videoAdapter.setDataSet(response.body());
                    videoAdapter.notifyDataSetChanged();
                }
            }

            @Override
            public void onFailure(Call<List<ApiResponse>> call, Throwable t) {
                Log.d("retrofit", t.getMessage());
            }
        });
    }
}
