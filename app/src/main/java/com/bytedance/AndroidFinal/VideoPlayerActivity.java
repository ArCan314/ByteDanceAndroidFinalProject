package com.bytedance.AndroidFinal;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;

public class VideoPlayerActivity extends AppCompatActivity {
    private VideoView videoView;
    private ImageView playIcon;
    private TextView textView;
    private LottieAnimationView animationView;
    private static int progress = -1;
    private long firstPressTime = 0;
    private long mNow = 0;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }

        setContentView(R.layout.activity_videoplayer);
        Intent intent = getIntent();
        videoView = findViewById(R.id.videoView);
        videoView.setVideoPath(intent.getStringExtra("url"));
        textView = findViewById(R.id.des);
        textView.setText(intent.getStringExtra("description"));
        playIcon = findViewById(R.id.play_icon1);
        animationView = findViewById(R.id.animation_view);

        ObjectAnimator scaleXAnimator = ObjectAnimator.ofFloat(playIcon,
                "scaleX", 0.45f, 0.3f);
        scaleXAnimator.setInterpolator(new LinearInterpolator());
        scaleXAnimator.setDuration(300);

        ObjectAnimator scaleYAnimator = ObjectAnimator.ofFloat(playIcon,
                "scaleY", 0.45f, 0.3f);
        scaleYAnimator.setInterpolator(new LinearInterpolator());
        scaleYAnimator.setDuration(300);

        AnimatorSet animatorSet = new AnimatorSet();
        animatorSet.playTogether(scaleXAnimator, scaleYAnimator);

        Handler handler = new Handler();
        Runnable runnable = () -> {
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(animationView,
                    "alpha",1,0);
            animator1.start();
        };

        Handler clickhandler = new Handler() {
            @Override
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1:
                        ObjectAnimator animator1 = ObjectAnimator.ofFloat(animationView,
                                "alpha",0,1);
                        animator1.start();
                        animationView.playAnimation();
                        handler.postDelayed(runnable, 2000);
                        break;
                    case 2:
                        if(videoView.isPlaying()) {
                            videoView.pause();
                            playIcon.setVisibility(View.VISIBLE);
                            animatorSet.start();
                        }
                        else {
                            videoView.start();
                            playIcon.setVisibility(View.INVISIBLE);
                            Log.d("videolog", "resume");
                        }
                        break;
                }
            }
        };

        videoView.setOnClickListener(v -> {
            firstPressTime = mNow;
            mNow = System.currentTimeMillis();
            if (mNow - firstPressTime < 300){
                clickhandler.removeMessages(2);
                clickhandler.sendEmptyMessage(1);
                mNow = 0;
            }
            else
                clickhandler.sendEmptyMessageDelayed(2,310);
        });

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