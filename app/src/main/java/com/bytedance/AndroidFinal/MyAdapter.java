package com.bytedance.AndroidFinal;

import android.animation.AnimatorSet;
import android.animation.ObjectAnimator;
import android.content.Context;
import android.content.Intent;
import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.LinearInterpolator;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.airbnb.lottie.LottieAnimationView;
import com.bumptech.glide.Glide;
import com.bumptech.glide.request.RequestOptions;

import java.util.List;

public class MyAdapter extends RecyclerView.Adapter<MyAdapter.MyViewHolder> {
    private List<ApiResponse> dataSet;
    private Context context;

    public MyAdapter(Context context) {
        this.context = context;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.my_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
//        Glide.with(holder.thumbNail.getContext())
//                .setDefaultRequestOptions(
//                        new RequestOptions()
//                                .frame(0))
//                .load(dataSet.get(position).url)
//                .into(holder.thumbNail);
        holder.bind(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    @Override
    public void onViewAttachedToWindow(@NonNull MyViewHolder holder) {
        super.onViewAttachedToWindow(holder);
        if(holder.progress != -1)
            holder.videoView.seekTo(holder.progress);
        holder.playIcon.setVisibility(View.INVISIBLE);
        // holder.thumbNail.setVisibility(View.INVISIBLE);
        holder.videoView.start();
    }

    @Override
    public void onViewDetachedFromWindow(@NonNull MyViewHolder holder) {
        super.onViewDetachedFromWindow(holder);
        holder.progress = holder.videoView.getCurrentPosition();
//        holder.thumbNail.setVisibility(View.VISIBLE);
    }

    public void setDataSet(List<ApiResponse> data) { dataSet = data; }

    public class MyViewHolder extends RecyclerView.ViewHolder {

        private VideoView videoView;
        private ImageView playIcon;
        private TextView description;
        private TextView author;
        private LottieAnimationView animationView;
        private ImageView beforeLike;
        private ImageView thumbNail;
        private ImageView afterLike;
        private TextView likeCount;
        private int progress = -1;
        private long firstPressTime = 0;
        private long mNow = 0;
        private boolean like = false;
        private int count;
        public ApiResponse apiResponse;

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            videoView = itemView.findViewById(R.id.videoView);
            description = itemView.findViewById(R.id.des);
            author = itemView.findViewById(R.id.author);
            playIcon = itemView.findViewById(R.id.play_icon1);
            beforeLike = itemView.findViewById(R.id.beforelike);
            afterLike = itemView.findViewById(R.id.afterlike);
            likeCount = itemView.findViewById(R.id.like_count);
            animationView = itemView.findViewById(R.id.animation_view);
//            thumbNail = itemView.findViewById(R.id.videoViewThumbNail);

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

            videoView.setOnClickListener(v -> {
                Log.d("videoView", "Cliked!");
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

            // 播放完毕时自动重播
            videoView.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
                @Override
                public void onCompletion(MediaPlayer mp) {
                    mp.start();
                    mp.setLooping(true);
                }
            });

        }

        public void bind(ApiResponse apiResponse) {
            this.apiResponse = apiResponse;
            updateData();
        }

        public void updateData() {
            videoView.setVideoPath(apiResponse.url);
            description.setText(apiResponse.description);
            author.setText('@' + apiResponse.nickname);
            count = apiResponse.likeCount;
            setLikeCount(count);
            if(progress != -1)
                videoView.seekTo(progress);
            // videoView.start();
        }

        public void setLikeCount(int count) {
            if(count <= 9999)
                likeCount.setText(Integer.toString(count));
            else {
                int a = count / 10000;
                int b = (count % 10000) / 1000;
                likeCount.setText(a + "." + b + "w");
            }
        }

    }
}
