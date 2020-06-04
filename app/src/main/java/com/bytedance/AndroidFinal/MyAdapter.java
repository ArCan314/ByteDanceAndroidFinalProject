package com.bytedance.AndroidFinal;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatImageView;
import androidx.core.widget.ImageViewCompat;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;

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
        holder.videoName.setText(dataSet.get(position).description);
        Glide.with(holder.imageView.getContext())
                .load(dataSet.get(position).avatarUrl)
                .into(holder.imageView);
        holder.bind(dataSet.get(position));
    }

    @Override
    public int getItemCount() {
        return dataSet == null ? 0 : dataSet.size();
    }

    public void setDataSet(List<ApiResponse> data) { dataSet = data; }

    public  class MyViewHolder extends  RecyclerView.ViewHolder {

        public TextView videoName;
        public ImageView imageView;
        public ImageView playerIcon;
        public ApiResponse apiResponse;
        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            videoName = itemView.findViewById(R.id.video_name);
            imageView = itemView.findViewById(R.id.avatar_image_view);
            playerIcon = itemView.findViewById(R.id.play_icon);
            playerIcon.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intent = new Intent(context, VideoPlayerActivity.class);
                    intent.putExtra("url", apiResponse.url);
                    intent.putExtra("description", apiResponse.description);
                    intent.putExtra("likecount", apiResponse.likeCount);
                    context.startActivity(intent);
                }
            });
        }

        public void bind(ApiResponse apiResponse) {
            this.apiResponse = apiResponse;
        }
    }
}
