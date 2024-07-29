package com.example.looglem;

import android.content.Intent;
import android.net.Uri;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.squareup.picasso.Callback;
import com.squareup.picasso.Picasso;

import java.util.ArrayList;
import java.util.List;

public class PostAdapter extends RecyclerView.Adapter<PostAdapter.PostViewHolder> {
    private final List<Post> postList;

    public PostAdapter() {
        this.postList = new ArrayList<>();
    }

    public void setPosts(List<Post> postList) {
        this.postList.clear();
        this.postList.addAll(postList);
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PostViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.post_item, parent, false);
        return new PostViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PostViewHolder holder, int position) {
        Post post = postList.get(position);
        holder.bind(post);
    }

    @Override
    public int getItemCount() {
        return postList.size();
    }

    static class PostViewHolder extends RecyclerView.ViewHolder {
        TextView idTextView, usernameTextView, contentTextView, createdAtTextView, postUrlTextView;
        ImageView imageView;
        TextView commentsTextView;

        public PostViewHolder(View itemView) {
            super(itemView);
            idTextView = itemView.findViewById(R.id.idTextView);
            usernameTextView = itemView.findViewById(R.id.usernameTextView);
            contentTextView = itemView.findViewById(R.id.contentTextView);
            createdAtTextView = itemView.findViewById(R.id.createdAtTextView);
            imageView = itemView.findViewById(R.id.imageView);
            postUrlTextView = itemView.findViewById(R.id.postUrlView);
            commentsTextView = itemView.findViewById(R.id.commentsTextView);
        }

        public void bind(Post post) {
            idTextView.setText(post.getId());
            usernameTextView.setText(post.getUsername());
            contentTextView.setText(post.getContent());
            createdAtTextView.setText(post.getCreatedAt());

            String postLinkUrl = post.getPostLinkUrl();
            if (postLinkUrl != null && !postLinkUrl.trim().isEmpty()) {
                postUrlTextView.setText(postLinkUrl);
                postUrlTextView.setVisibility(View.VISIBLE);

                postUrlTextView.setOnClickListener(v -> {
                    Intent intent = new Intent(Intent.ACTION_VIEW);
                    intent.setData(Uri.parse(postLinkUrl));
                    itemView.getContext().startActivity(intent);
                });
            } else {
                postUrlTextView.setVisibility(View.GONE);
            }

            imageView.setVisibility(View.VISIBLE);

            imageView.setImageDrawable(null);

            Picasso.get()
                    .load(post.getImageUrl())
                    .into(imageView, new Callback() {
                        @Override
                        public void onSuccess() {
                            Log.d("Picasso", "Image loaded successfully: " + post.getImageUrl());
                        }

                        @Override
                        public void onError(Exception e) {
                            Log.e("Picasso", "Error loading image: " + post.getImageUrl(), e);
                        }
                    });

            List<Comment> comments = post.getComments();

            if (comments != null && !comments.isEmpty()) {
                commentsTextView.setText("");

                for (Comment comment : comments) {

                    commentsTextView.append(comment.getUsername() + "\n");
                    commentsTextView.append(comment.getContent() + "\n\n");
                }

                commentsTextView.setVisibility(View.VISIBLE);
            } else {
                commentsTextView.setVisibility(View.GONE);
            }
        }
    }
}