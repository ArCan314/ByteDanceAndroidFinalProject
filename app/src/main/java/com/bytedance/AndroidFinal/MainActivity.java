package com.bytedance.AndroidFinal;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;
import android.widget.Toast;

import com.bytedance.AndroidFinal.Message.ChatRoom;
import com.bytedance.AndroidFinal.Message.Model.Message;
import com.bytedance.AndroidFinal.Message.Model.MessageAdapter;
import com.bytedance.AndroidFinal.Message.Utils.PullParser;
import com.google.android.material.bottomnavigation.BottomNavigationView;

import java.io.InputStream;
import java.util.List;

import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

public class MainActivity extends AppCompatActivity implements MessageAdapter.ListItemClickListener, VideoAdapter.CommentClickListener {

    private ViewPager2 viewPager2;
    private VideoAdapter videoAdapter;
    private BottomNavigationView bnvMenu;
    private RecyclerView rvMessage;
    private LinearLayout messageLayout;
    private List<Message> messages;
    private static int progress = -1;
    private int currentSelectedItemId = -1;
    private boolean enterChatroom = false;

    @RequiresApi(api = Build.VERSION_CODES.M)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);;
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);

        setContentView(R.layout.activity_main);
        rvMessage = findViewById(R.id.message_list);
        rvMessage.setLayoutManager(new LinearLayoutManager(this));
        messageLayout = findViewById(R.id.message_layout);
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
                    Log.d("progress2", String.valueOf(progress));
                    videoAdapter.restore(progress, enterChatroom);
                    viewPager2.setUserInputEnabled(true);
                    videoAdapter.isClickAllowed = true;
                    messageLayout.setVisibility(View.INVISIBLE);
                    bnvMenu.setBackgroundColor(Color.TRANSPARENT);
                    enterChatroom = false;
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        window.addFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    return true;
                case R.id.message_page:
                    progress = videoAdapter.getProgress();
                    Log.d("progress1", String.valueOf(progress));
                    videoAdapter.save();
                    viewPager2.setUserInputEnabled(false);
                    videoAdapter.isClickAllowed = false;
                    bnvMenu.setBackgroundColor(Color.BLACK);
                    if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
                        window.clearFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN);
                    showMessage();
                    return true;
                default:
                    break;
            }
            return false;
        });

        getData();
    }

    private void showMessage() {
        if (messages == null) {
            InputStream assetInput;
            try {
                assetInput = getAssets().open("data.xml");
                messages = PullParser.pull2xml(assetInput);
            } catch (Exception exception) {
                exception.printStackTrace();
            }
            rvMessage.setAdapter(new MessageAdapter(messages, this));
        }
        messageLayout.setVisibility(View.VISIBLE);
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
                // TODO is context correct?
                Toast.makeText(getApplicationContext(), "连接失败", Toast.LENGTH_SHORT);
                Log.d("retrofit", t.getMessage());
            }
        });
    }

    @Override
    public void onListItemClick(int clickedItemIndex) {
        Intent intent = new Intent(this, ChatRoom.class);
        Bundle bundle = new Bundle();
        bundle.putInt("id", clickedItemIndex);
        bundle.putString("title", messages.get(clickedItemIndex).getTitle());
        intent.putExtra("data", bundle);
        enterChatroom = true;
        startActivity(intent);
    }

    @Override
    public void onCommentClick() {
        if(bnvMenu.getVisibility() == View.VISIBLE)
            bnvMenu.setVisibility(View.GONE);
        else
            bnvMenu.setVisibility(View.VISIBLE);
    }
}
