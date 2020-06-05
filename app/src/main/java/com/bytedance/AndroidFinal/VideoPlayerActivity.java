package com.bytedance.AndroidFinal;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Intent;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.graphics.Color;
import android.os.Build;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.KeyEvent;
import android.view.View;
import android.view.Window;
import android.view.WindowManager;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.os.Bundle;

import com.airbnb.lottie.LottieAnimationView;
import com.danikula.videocache.HttpProxyCacheServer;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.bytedance.AndroidFinal.Proxy.getProxy;

public class VideoPlayerActivity extends AppCompatActivity {
    private VideoView videoView;
    private ImageView playIcon;
    private LottieAnimationView animationView;
    private ImageView beforeLike, afterLike, iv_comment;
    private TextView tv_send, comment_count, total_comment;
    private LinearLayout close_comment;
    private TextView likeCount;
    private RecyclerView recyclerView;
    private CommentAdapter commentAdapter;
    private View comment;
    private Handler clickhandler, handler;
    private String videoId;
    private EditText comment_content;
    private static int progress = -1;
    private long firstPressTime = 0;
    private long mNow = 0;
    private boolean like = false;
    private int count;
    private CommentDbHelper dbHelper;
    private static final SimpleDateFormat format =
            new SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH);

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        dbHelper = new CommentDbHelper(this);
        setContentView(R.layout.activity_videoplayer);
        setFullScreen();
        getView();
        setAnimation();
        setAllClickListener();

        if(progress != -1)
            videoView.seekTo(progress);
        videoView.start();
    }

    public void setFullScreen() {
        Window window = this.getWindow();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP) {
            window.clearFlags(WindowManager.LayoutParams.FLAG_TRANSLUCENT_STATUS);
            window.getDecorView().setSystemUiVisibility(View.SYSTEM_UI_FLAG_LAYOUT_FULLSCREEN | View.SYSTEM_UI_FLAG_LAYOUT_STABLE);
            window.addFlags(WindowManager.LayoutParams.FLAG_DRAWS_SYSTEM_BAR_BACKGROUNDS);
            window.setStatusBarColor(Color.TRANSPARENT);
        }
    }

    public void getView() {
        Intent intent = getIntent();
        videoId = intent.getStringExtra("videoid");
        videoView = findViewById(R.id.videoView);
        HttpProxyCacheServer proxy = getProxy(this);
        String proxyUrl = proxy.getProxyUrl(intent.getStringExtra("url"));
        videoView.setVideoPath(proxyUrl);
//        videoView.setVideoPath(intent.getStringExtra("url"));
        TextView textView = findViewById(R.id.des);
        textView.setText(intent.getStringExtra("description"));
        playIcon = findViewById(R.id.play_icon1);
        beforeLike = findViewById(R.id.beforelike);
        afterLike = findViewById(R.id.afterlike);
        likeCount = findViewById(R.id.like_count);
        count = intent.getIntExtra("likecount", 0);
        animationView = findViewById(R.id.animation_view);
        recyclerView = findViewById(R.id.recycler_comment);
        comment = findViewById(R.id.comment);
        tv_send = findViewById(R.id.tv_send);
        close_comment = findViewById(R.id.close_comment);
        iv_comment = findViewById(R.id.iv_comment);
        comment_content = findViewById(R.id.comment_content);
        comment_count = findViewById(R.id.comment_count);
        total_comment = findViewById(R.id.total_comment);
        setCommentCount();
        setLikeCount(count);
    }

    public void setCommentCount() {
        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                CommentContract.CommentEntry.COLUMN_NAME_VIEDOID,
                CommentContract.CommentEntry.COLUMN_NAME_TIME,
                CommentContract.CommentEntry.COLUMN_NAME_USER,
                CommentContract.CommentEntry.COLUMN_NAME_CONTENT
        };

        String selection = CommentContract.CommentEntry.COLUMN_NAME_VIEDOID + "=?";
        String[] selectionArgs = {videoId};

        Cursor cursor = db.query(CommentContract.CommentEntry.TABLE_NAME, projection, selection,
                selectionArgs, null, null, null);
        comment_count.setText(String.valueOf(cursor.getCount()));
        cursor.close();
    }

    public void setAnimation() {
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

        handler = new Handler();
        Runnable runnable = () -> {
            ObjectAnimator animator1 = ObjectAnimator.ofFloat(animationView,
                    "alpha",1,0);
            animator1.start();
        };

        clickhandler = new Handler() {
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
                        if(!like) {
                            beforeLike.setVisibility(View.INVISIBLE);
                            afterLike.setVisibility(View.VISIBLE);
                            setLikeCount(++count);
                            like = true;
                        }
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
    }

    public void setAllClickListener() {
        beforeLike.setOnClickListener(v -> {
            beforeLike.setVisibility(View.INVISIBLE);
            afterLike.setVisibility(View.VISIBLE);
            setLikeCount(++count);
            like = true;
        });

        afterLike.setOnClickListener(v -> {
            afterLike.setVisibility(View.INVISIBLE);
            beforeLike.setVisibility(View.VISIBLE);
            setLikeCount(--count);
            like = false;
        });

        iv_comment.setOnClickListener(v -> {
            try {
                showComment();
            } catch (ParseException e) {
                e.printStackTrace();
            }
        });

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

        close_comment.setOnClickListener(view -> {
            comment.setVisibility(View.GONE);
        });

        tv_send.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                try {
                    addComment();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
                SoftKeyHideShow.hideShowSoftKey(VideoPlayerActivity.this);
            }
        });
    }

    @Override
    public void onPause() {
        super.onPause();
        progress = videoView.getCurrentPosition();
    }

    @Override
    public boolean onKeyDown(int keyCode, KeyEvent event) {
        if(keyCode == KeyEvent.KEYCODE_BACK) {
            if(comment.getVisibility() == View.VISIBLE) {
                comment.setVisibility(View.INVISIBLE);
                return true;
            }
        }
        return super.onKeyDown(keyCode, event);
    }

    public void setLikeCount(int count) {
        if(count <= 9999) {
            String result = Integer.toString(count);
            likeCount.setText(result);
        }
        else {
            int a = count / 10000;
            int b = (count % 10000) / 1000;
            String result = a + "." + b + "w";
            likeCount.setText(result);
        }
    }

    public void showComment() throws ParseException {
        if (commentAdapter == null) {
            commentAdapter = new CommentAdapter();
            recyclerView.setLayoutManager(new LinearLayoutManager(this));
            recyclerView.setAdapter(commentAdapter);
            Log.d("I'm new", "f");
        }

        commentAdapter.refresh(loadCommentsFromDatabase(videoId));

        Animation showAction = AnimationUtils.loadAnimation(this, R.anim.actionsheet_dialog_in);
        comment.startAnimation(showAction);

        comment.setVisibility(View.VISIBLE);

    }

    public void addComment() throws ParseException {
        String content = comment_content.getText().toString();
        if(!content.equals("")) {
            SQLiteDatabase db = dbHelper.getWritableDatabase();
            ContentValues values = new ContentValues();
            values.put(CommentContract.CommentEntry.COLUMN_NAME_CONTENT, content);
            values.put(CommentContract.CommentEntry.COLUMN_NAME_TIME, format.format(new Date(System.currentTimeMillis())));
            values.put(CommentContract.CommentEntry.COLUMN_NAME_USER, "User");
            values.put(CommentContract.CommentEntry.COLUMN_NAME_VIEDOID, videoId);
            db.insert(CommentContract.CommentEntry.TABLE_NAME, null, values);
            commentAdapter.refresh(loadCommentsFromDatabase(videoId));
            comment_content.setText("");
            Toast.makeText(VideoPlayerActivity.this, "评论成功", Toast.LENGTH_SHORT).show();
        }
        else
            Toast.makeText(VideoPlayerActivity.this, "评论为空!", Toast.LENGTH_SHORT).show();
    }

    public List<Comment> loadCommentsFromDatabase(String videoId) throws ParseException {

        SQLiteDatabase db = dbHelper.getReadableDatabase();

        String[] projection = {
                BaseColumns._ID,
                CommentContract.CommentEntry.COLUMN_NAME_VIEDOID,
                CommentContract.CommentEntry.COLUMN_NAME_TIME,
                CommentContract.CommentEntry.COLUMN_NAME_USER,
                CommentContract.CommentEntry.COLUMN_NAME_CONTENT
        };

        String sortOrder = CommentContract.CommentEntry._ID + " DESC";

        String selection = CommentContract.CommentEntry.COLUMN_NAME_VIEDOID + "=?";
        String[] selectionArgs = {videoId};

        Cursor cursor = db.query(
                CommentContract.CommentEntry.TABLE_NAME,
                projection,
                selection,
                selectionArgs,
                null,
                null,
                sortOrder
        );

        List<Comment> result = new ArrayList<>();
        while (cursor.moveToNext()) {
            long itemId = cursor.getLong(cursor.getColumnIndexOrThrow(CommentContract.CommentEntry._ID));
            String tempDate = cursor.getString(cursor.getColumnIndex(CommentContract.CommentEntry.COLUMN_NAME_TIME));
            Date time = format.parse(tempDate);
            String content = cursor.getString(cursor.getColumnIndex(CommentContract.CommentEntry.COLUMN_NAME_CONTENT));
            String user = cursor.getString(cursor.getColumnIndex(CommentContract.CommentEntry.COLUMN_NAME_USER));
            Comment comment_item = new Comment(itemId);
            comment_item.setContent(content);
            comment_item.setTime(time);
            comment_item.setUser(user);
            result.add(comment_item);
        }

        String temp = "全部评论(" + cursor.getCount() + ")";
        total_comment.setText(temp);
        comment_count.setText(String.valueOf(cursor.getCount()));
        cursor.close();
        return result;
    }
}