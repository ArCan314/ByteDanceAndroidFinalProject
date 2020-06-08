package com.bytedance.AndroidFinal.Message;

import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import com.bytedance.AndroidFinal.R;
import android.view.View;
import android.widget.ImageButton;

public class ChatRoom extends AppCompatActivity implements View.OnClickListener{
    private TextView tvWithName;
    private TextView tvInfo;
    private TextView tvContentInfo;
    private ImageButton button;

    @Override
    public void onCreate(Bundle savedInstanceState){
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_chatroom);

        tvWithName = findViewById(R.id.tv_with_name);
        tvContentInfo = findViewById(R.id.tv_content_info);
        button = findViewById(R.id.btn_back);

        Bundle bundle = getIntent().getBundleExtra("data");
        assert bundle != null;
        int id = bundle.getInt("id");
        String title = bundle.getString("title");

        tvWithName.setText(title);
        tvContentInfo.setText("这是第 " + (id+1) + "个消息");
        button.setOnClickListener(this);
    }

    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.btn_back)
            finish();
    }
}