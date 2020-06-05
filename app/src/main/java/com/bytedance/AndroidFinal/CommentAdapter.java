package com.bytedance.AndroidFinal;

import android.content.Context;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

public class CommentAdapter extends RecyclerView.Adapter<CommentAdapter.MyViewHolder> {

    private final List<Comment> comments = new ArrayList<>();

    public void refresh(List<Comment> newComments) {
        comments.clear();
        if (newComments != null) {
            comments.addAll(newComments);
        }
        notifyDataSetChanged();
        for(int i = 0; i < comments.size(); i++)
            Log.d("dataiii", comments.get(i).getContent());
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.comment_item, parent, false);
        return new MyViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        holder.bind(comments.get(position));
        Log.d("dataiiii", comments.get(position).getContent());
    }

    @Override
    public int getItemCount() {
        return comments.size();
    }

    public class MyViewHolder extends  RecyclerView.ViewHolder {
        TextView user, content, time;
        private final SimpleDateFormat format =
                new SimpleDateFormat("MM-dd HH:mm", Locale.ENGLISH);

        public MyViewHolder(@NonNull View itemView) {
            super(itemView);
            user = itemView.findViewById(R.id.tv_user);
            content = itemView.findViewById(R.id.tv_content);
            time = itemView.findViewById(R.id.tv_time);
        }

        public void bind(Comment comment) {
            user.setText(comment.getUser());
            content.setText(comment.getContent());
            time.setText(format.format(comment.getTime()));
            Log.d("dataiiii", comment.getContent());
        }
    }
}