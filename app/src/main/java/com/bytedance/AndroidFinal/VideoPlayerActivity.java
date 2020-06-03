package com.bytedance.AndroidFinal;

import android.content.Intent;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

public class VideoPlayerActivity extends AppCompatActivity {
    private VideoView videoView;
    private static int progress = -1;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_videoplayer);
        Intent intent = getIntent();
        videoView = findViewById(R.id.videoView);
        videoView.setVideoPath(intent.getStringExtra("url"));
//        if(this.getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE) {
//            WindowManager.LayoutParams lp = getWindow().getAttributes();
//            lp.flags |= WindowManager.LayoutParams.FLAG_FULLSCREEN;
//            getWindow().setAttributes(lp);
//            getWindow().addFlags(WindowManager.LayoutParams.FLAG_LAYOUT_NO_LIMITS);
//        }
//        videoView.setMediaController(new MediaController(this));
        if(progress != -1)
            videoView.seekTo(progress);
        videoView.start();
    }

    @Override
    public void onPause() {
        super.onPause();
        progress = videoView.getCurrentPosition();
    }
}