package com.bytedance.AndroidFinal.Message;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.bytedance.AndroidFinal.R;

public class ChatRoom extends AppCompatActivity {
    private TextView contentText;
    public static final String CLICK_POS_KEY = "clickPos";

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);
        contentText = findViewById(R.id.tv_content_info);
        Intent intent = getIntent();
        if (intent != null) {
            int clickPos = intent.getIntExtra(CLICK_POS_KEY, 0);
            contentText.setText("Index: " + String.valueOf(clickPos));
        }
    }
}
