package com.example.looglem;

import android.os.Handler;
import android.os.Looper;
import android.util.Log;

import androidx.appcompat.app.AppCompatActivity;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class PostHandler {
    private static final String TAG = "PostFetcher";
    private final AppCompatActivity activity;


    public PostHandler(AppCompatActivity activity) {
        this.activity = activity;
    }

    public void fetchPostsData(PostFetchListener listener) {
        OkHttpClient client = new OkHttpClient();
        Request request = new Request.Builder()
                .url("http://plus.loogle.mooo.com/apiv1/mobile_posts.php")
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(Call call, IOException e) {
                e.printStackTrace();
                new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Failed to fetch data"));
            }

            @Override
            public void onResponse(Call call, Response response) throws IOException {
                if (response.isSuccessful()) {
                    String jsonData = response.body().string();
                    try {
                        List<Post> postList = new ArrayList<>();
                        JSONArray jsonArray = new JSONArray(jsonData);
                        for (int i = 0; i < jsonArray.length(); i++) {
                            JSONObject jsonObject = jsonArray.getJSONObject(i);
                            JSONObject postObject = jsonObject.getJSONObject("post");

                            // Parse post data
                            String id = postObject.getString("id");
                            String username = postObject.getString("username");
                            String content = postObject.getString("content");
                            String imageUrl = postObject.getString("image_url");
                            String createdAt = postObject.getString("created_at");
                            String postLinkUrl = postObject.getString("post_link_url");

                            Log.d("postLinkUrl", "Post Link Url: " + postLinkUrl);

                            // Parse comments
                            JSONArray commentsArray = jsonObject.getJSONArray("comments");
                            List<Comment> comments = new ArrayList<>();
                            for (int j = 0; j < commentsArray.length(); j++) {
                                JSONObject commentObject = commentsArray.getJSONObject(j);
                                String commentUsername = commentObject.getString("username");
                                String commentContent = commentObject.getString("comment_content");
                                Comment comment = new Comment(commentUsername, commentContent);
                                comments.add(comment);
                            }

                            Post post = new Post(id, username, content, imageUrl, createdAt, postLinkUrl, comments);
                            postList.add(post);
                        }
                        new Handler(Looper.getMainLooper()).post(() -> listener.onSuccess(postList));
                    } catch (JSONException e) {
                        e.printStackTrace();
                        new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Error parsing JSON"));
                    }
                } else {
                    new Handler(Looper.getMainLooper()).post(() -> listener.onFailure("Unexpected response code: " + response.code()));
                }
            }
        });
    }

    public interface PostFetchListener {
        void onSuccess(List<Post> postList);

        void onFailure(String errorMessage);
    }
}