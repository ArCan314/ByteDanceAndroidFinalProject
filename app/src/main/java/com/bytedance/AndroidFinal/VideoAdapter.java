package com.bytedance.AndroidFinal;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.provider.BaseColumns;
import android.util.Log;
import android.view.GestureDetector;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.SurfaceHolder;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.view.animation.LinearInterpolator;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.airbnb.lottie.LottieAnimationView;
import com.bytedance.AndroidFinal.Comment.Comment;
import com.bytedance.AndroidFinal.Comment.CommentAdapter;
import com.bytedance.AndroidFinal.Comment.CommentContract;
import com.bytedance.AndroidFinal.Comment.CommentDbHelper;
import com.bytedance.AndroidFinal.Comment.CommentPopup;
import com.danikula.videocache.HttpProxyCacheServer;
import com.google.android.material.bottomnavigation.BottomNavigationView;
import com.lxj.xpopup.XPopup;
import com.lxj.xpopup.interfaces.SimpleCallback;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import static com.bytedance.AndroidFinal.Utils.Proxy.getProxy;

public class VideoAdapter extends RecyclerView.Adapter<VideoAdapter.VideoViewHolder> {
    private List<ApiResponse> dataSet;
    public List<VideoViewHolder> viewHolderList;
    public List<Boolean> attachedHolders;
    public Boolean isClickAllowed = true;
    private CommentClickListener listener;
    private ViewPager2 viewPager;

    public VideoAdapter(CommentClickListener listener, ViewPager2 viewPager) {
        this.listener = listener;
        viewHolderList = new ArrayList<>();
        attachedHolders = new ArrayList<>();
        this.viewPager = viewPager;
    }

    @NonNull
    @Override
    public VideoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_item, parent, false);
        VideoViewHolder viewHolder = new VideoViewHolder(itemView);
        viewHolder.context = parent.getContext();
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(@NonNull VideoViewHolder holder, int position) {
        holder.bind(dataSet.get(position));
        holder.position = position;
        viewHolderList.set(position, holder);
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull VideoViewHolder holder) {
            super.onViewAttachedToWindow(holder);
            Log.d("Focus", "Attached " + holder.position);
            attachedHolders.set(holder.position, true);
            if (holder.progress != -1)
                holder.videoView.seekTo(holder.progress);
            holder.playIcon.setVisibility(View.INVISIBLE);
            holder.videoView.post(new Runnable() {
                @Override
                public void run() {
                    holder.videoView.start();
                }
            });
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull VideoViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        attachedHolders.set(holder.position, false);
        holder.progress = holder.videoView.getCurrentPosition();
        Log.d("Focus", "Detached " + holder.position);
        if (holder.comment.getVisibility() == View.VISIBLE)
            holder.comment.setVisibility(View.INVISIBLE);
    }


    public void setDataSet(List<ApiResponse> data) {
        dataSet = data;
        viewHolderList.clear();
        attachedHolders.clear();
        for (int i = 0; i < data.size(); i++) {
            viewHolderList.add(null);
            attachedHolders.add(false);
        }
    }

    public int getCurrentPos() {
        int res = -1;
        for (int i = 0; i < attachedHolders.size(); i++)
            if (attachedHolders.get(i)) {
                res = i;
                break;
            }
        return res;
    }

    private Boolean isInComment = false;
    private Boolean isVideoStarted = false;
    public void save() {
        int currentPos = getCurrentPos();
        isInComment = viewHolderList.get(currentPos).isInComment;
        isVideoStarted = viewHolderList.get(currentPos).videoView.isPlaying();
        if (!isInComment && isVideoStarted) {
            viewHolderList.get(currentPos).videoView.pause();
            viewHolderList.get(currentPos).playIcon.setVisibility(View.VISIBLE);
        }
    }

    public void restore() {
        if (!isInComment && isVideoStarted) {
            viewHolderList.get(getCurrentPos()).videoView.start();
            viewHolderList.get(getCurrentPos()).playIcon.setVisibility(View.INVISIBLE);
        }

    }

    public interface CommentClickListener {
        void onCommentClick();
    }

    public class VideoViewHolder extends RecyclerView.ViewHolder {
        // Data
        public ApiResponse apiResponse;
        public Context context;
        public int position;

        // Video
        public VideoView videoView;
        private ImageView playIcon;
        private int progress = -1;
        private HttpProxyCacheServer proxy;

        // VideoInfo
        private TextView description;
        private TextView author;

        // Like
        private ImageView beforeLike, afterLike;
        private TextView likeCount;
        private boolean like = false;
        private int count;

        // Comment
        private ImageView iv_comment;
        private TextView tv_send, comment_count, total_comment;
        public LinearLayout close_comment;
        private RecyclerView recyclerView;
        private CommentAdapter commentAdapter;
        private View comment;
        private EditText comment_content;
        private CommentDbHelper dbHelper;
        private final SimpleDateFormat format =
                new SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH);

        // Animation and ClickListener
        private LottieAnimationView animationView;
        private Handler clickHandler, handler;
        private GestureDetector gestureDetector;

        public Boolean isVideoPausedBeforeEnterComment = false;
        public Boolean isInComment = false;

        private BottomNavigationView bnvMenu;

        public VideoViewHolder(@NonNull View itemView) {
            super(itemView);
            dbHelper = new CommentDbHelper(itemView.getContext());
            proxy = getProxy(itemView.getContext());
            // context = itemView.getContext();
            getViewsById(itemView);
            setAnimation(itemView);
            setAllClickHandler(itemView);

            // 播放完毕时自动重播
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.start();
                    mp.setLooping(true);
                }
            });

            videoView.getHolder().addCallback(new SurfaceHolder.Callback() {
                @Override
                public void surfaceCreated(SurfaceHolder holder) {
                    Log.d("VideoView", "surfaceCreate " + position);
                }

                @Override
                public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
                    Log.d("VideoView", "surfaceChanged " + position);
                }

                @Override
                public void surfaceDestroyed(SurfaceHolder holder) {
                    Log.d("VideoView", "surfaceDestroyed " + position);
                }
            });

            comment_content.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
//                    v.postOnAnimationDelayed(new Runnable() {
//                        @Override
//                        public void run() {
//                            v.requestFocus();
//                        }
//                    },10);
                    Log.d("WhereDoesFocusGo?", "comment_content " + position + " " + String.valueOf(hasFocus));
                }
            });

            videoView.setOnFocusChangeListener(new View.OnFocusChangeListener() {
                @Override
                public void onFocusChange(View v, boolean hasFocus) {
                    Log.d("WhereDoesFocusGo?", "videoView " + position + " " + String.valueOf(hasFocus));
                }
            });
        }

        public void getViewsById(@NonNull View itemView) {
            videoView = itemView.findViewById(R.id.videoView);
            description = itemView.findViewById(R.id.des);
            author = itemView.findViewById(R.id.author);
            playIcon = itemView.findViewById(R.id.play_icon1);
            beforeLike = itemView.findViewById(R.id.beforelike);
            afterLike = itemView.findViewById(R.id.afterlike);
            likeCount = itemView.findViewById(R.id.like_count);
            animationView = itemView.findViewById(R.id.animation_view);
            recyclerView = itemView.findViewById(R.id.recycler_comment);
            comment = itemView.findViewById(R.id.comment);
            tv_send = itemView.findViewById(R.id.tv_send);
            close_comment = itemView.findViewById(R.id.close_comment);
            iv_comment = itemView.findViewById(R.id.iv_comment);
            comment_content = itemView.findViewById(R.id.comment_content);
            comment_count = itemView.findViewById(R.id.comment_count);
            total_comment = itemView.findViewById(R.id.total_comment);
        }

        public void setAnimation(@NonNull View itemView) {
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
                        "alpha", 1, 0);
                animator1.start();
            };

            clickHandler = new Handler() {
                @Override
                public void handleMessage(Message msg) {
                    super.handleMessage(msg);
                    if (isInComment)
                        return;
                    switch (msg.what) {
                        case 1:
                            ObjectAnimator animator1 = ObjectAnimator.ofFloat(animationView,
                                    "alpha", 0, 1);
                            animator1.start();
                            animationView.playAnimation();
                            handler.postDelayed(runnable, 2000);
                            if (!like) {
                                beforeLike.setVisibility(View.INVISIBLE);
                                afterLike.setVisibility(View.VISIBLE);
                                setLikeCount(++count);
                                like = true;
                            }
                            break;
                        case 2:
                            if (videoView.isPlaying()) {
                                videoView.pause();
                                playIcon.setVisibility(View.VISIBLE);
                                animatorSet.start();
                            } else {
                                videoView.start();
                                playIcon.setVisibility(View.INVISIBLE);
                                Log.d("videolog", "resume");
                            }
                            break;
                    }
                }
            };
        }

        public void setAllClickHandler(@NonNull View itemView) {
            beforeLike.setOnClickListener(v -> {
                if (!isClickAllowed)
                    return;
                beforeLike.setVisibility(View.INVISIBLE);
                afterLike.setVisibility(View.VISIBLE);
                setLikeCount(++count);
                like = true;
            });

            afterLike.setOnClickListener(v -> {
                if (!isClickAllowed)
                    return;
                afterLike.setVisibility(View.INVISIBLE);
                beforeLike.setVisibility(View.VISIBLE);
                setLikeCount(--count);
                like = false;
            });

            iv_comment.setOnClickListener(v -> {
                if (!isClickAllowed)
                    return;
                try {
                    showComment();
                    if(listener != null)
                        listener.onCommentClick();
                    viewPager.setUserInputEnabled(false);
                    isVideoPausedBeforeEnterComment = !videoView.isPlaying();
                    isInComment = true;
                    if (!isVideoPausedBeforeEnterComment)
                        videoView.pause();
                } catch (ParseException e) {
                    e.printStackTrace();
                }
            });

            close_comment.setOnClickListener(view -> {
                if (!isClickAllowed)
                    return;
                comment.setVisibility(View.GONE);
                listener.onCommentClick();
                viewPager.setUserInputEnabled(true);
                isInComment = false;
                if (!isVideoPausedBeforeEnterComment)
                    videoView.start();
            });

            tv_send.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {
                    if (!isClickAllowed)
                        return;
                    try {
                        addComment();
                    } catch (ParseException e) {
                        e.printStackTrace();
                    }


                    // SoftKeyHideShow.hideShowSoftKey(context);
                }
            });

            comment_content.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (!isClickAllowed)
                        return;
                    CommentPopup commentPopup = new CommentPopup(context);

                    new XPopup.Builder(context)
                            .autoOpenSoftInput(true)
                            .setPopupCallback(new SimpleCallback() {
                                @Override
                                public void onCreated() {
                                    commentPopup.setContent(comment_content.getText().toString());
                                }

                                @Override
                                public void onDismiss() {
                                    comment_content.setText(commentPopup.getContent());
                                    if (commentPopup.confirmClicked) {
                                        tv_send.performClick();
                                    }
                                }
                            })
                            .asCustom(commentPopup)
                            .show();
                }
            });

            gestureDetector = new GestureDetector(itemView.getContext(), new GestureDetector.SimpleOnGestureListener() {
                @Override
                public boolean onSingleTapConfirmed(MotionEvent e) {
                    clickHandler.sendEmptyMessage(2);
                    Log.d("WhereDoesTheClick?", "single tap");
                    return true;
                }

                @Override
                public boolean onDoubleTap(MotionEvent e) {
                    clickHandler.removeMessages(2);
                    clickHandler.sendEmptyMessage(1);
                    Log.d("WhereDoesTheClick?", "double tap.");
                    return true;
                }

                @Override
                public boolean onDown(MotionEvent e) {
                    return true;
                }
            });

            videoView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (!isClickAllowed)
                        return true;
                    return gestureDetector.onTouchEvent(event);
                }
            });

            itemView.setOnTouchListener(new View.OnTouchListener() {
                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (!isClickAllowed)
                        return true;
                    return gestureDetector.onTouchEvent(event);
                }
            });
        }

        public void bind(ApiResponse apiResponse) {
            this.apiResponse = apiResponse;
            if (!apiResponse.url.startsWith("https")) {
                StringBuilder stringBuilder = new StringBuilder(apiResponse.url);
                stringBuilder.insert(4, 's');
                this.apiResponse.url = stringBuilder.toString();
            }
            updateData();
        }

        public void updateData() {
            String proxyUrl = proxy.getProxyUrl(apiResponse.url);
            videoView.setVideoPath(proxyUrl);
            description.setText(apiResponse.description);
            author.setText('@' + apiResponse.nickname);
            count = apiResponse.likeCount;
            setCommentCount();
            setLikeCount(count);
            if (progress != -1)
                videoView.seekTo(progress);
            // videoView.start();
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
            String[] selectionArgs = {apiResponse.id};

            Cursor cursor = db.query(CommentContract.CommentEntry.TABLE_NAME, projection, selection,
                    selectionArgs, null, null, null);
            comment_count.setText(String.valueOf(cursor.getCount()));
            cursor.close();
        }

        public void setLikeCount(int count) {
            if (count <= 9999)
                likeCount.setText(String.valueOf(count));
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
                recyclerView.setLayoutManager(new LinearLayoutManager(context));
                recyclerView.setAdapter(commentAdapter);
                Log.d("I'm new", "f");
            }

            commentAdapter.refresh(loadCommentsFromDatabase(apiResponse.id));

            Animation showAction = AnimationUtils.loadAnimation(context, R.anim.actionsheet_dialog_in);
            comment.startAnimation(showAction);
            comment.setVisibility(View.VISIBLE);
        }

        public void addComment() throws ParseException {
            String content = comment_content.getText().toString();
            if (!content.equals("")) {
                SQLiteDatabase db = dbHelper.getWritableDatabase();
                ContentValues values = new ContentValues();
                values.put(CommentContract.CommentEntry.COLUMN_NAME_CONTENT, content);
                values.put(CommentContract.CommentEntry.COLUMN_NAME_TIME, format.format(new Date(System.currentTimeMillis())));
                values.put(CommentContract.CommentEntry.COLUMN_NAME_USER, "User");
                values.put(CommentContract.CommentEntry.COLUMN_NAME_VIEDOID, apiResponse.id);
                db.insert(CommentContract.CommentEntry.TABLE_NAME, null, values);
                commentAdapter.refresh(loadCommentsFromDatabase(apiResponse.id));
                comment_content.setText("");
                Toast.makeText(context, "评论成功", Toast.LENGTH_SHORT).show();
            }
            else
                Toast.makeText(context, "评论为空!", Toast.LENGTH_SHORT).show();
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

            String sortOrder =
                    CommentContract.CommentEntry._ID + " DESC";

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

        public View getComment() { return comment; }
    }
}
