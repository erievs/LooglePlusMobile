package com.example.looglem;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import org.jetbrains.annotations.NotNull;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;

    private SwipeRefreshLayout swipeRefreshLayout;

    @SuppressLint("MissingInflatedId")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        recyclerView = findViewById(R.id.recyclerView);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        postAdapter = new PostAdapter();
        recyclerView.setAdapter(postAdapter);

        PostHandler postFetcher = new PostHandler(this);
        postFetcher.fetchPostsData(new PostHandler.PostFetchListener() {
            @Override
            public void onSuccess(List<Post> postList) {
                postAdapter.setPosts(postList);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        swipeRefreshLayout.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                fetchPosts(); // Refresh the posts
            }
        });

        Button btnRefresh = findViewById(R.id.btnRefresh);
        btnRefresh.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                refreshPostList();
            }
        });

        Button btnSModal = findViewById(R.id.btnSModal);
        btnSModal.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubmitPostModal();
            }
        });

    }

    private void showSubmitPostModal() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.submit_post_modal);

        EditText editTextUsername = dialog.findViewById(R.id.editTextUsername);
        EditText editTextPassword = dialog.findViewById(R.id.editTextPassword);
        EditText editTextContent = dialog.findViewById(R.id.editTextContent);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                String content = editTextContent.getText().toString();

                Log.d(username, password);
                Log.d(username, content);

                submitPost(username, password, content);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void submitPost(String username, String password, String content) {
        OkHttpClient client = new OkHttpClient();

        RequestBody formBody = new FormBody.Builder()
                .add("username", username)
                .add("password", password)
                .add("postContent", content)
                .build();

        Request request = new Request.Builder()
                .url("http://plus.loogle.mooo.com/apiv1/submit_mobile_post.php")
                .post(formBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                String errorMessage = "Failed to submit post: " + e.getMessage();
                Log.e("SubmitPost", errorMessage, e);

                runOnUiThread(new Runnable() {
                    @Override
                    public void run() {
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                    }
                });
            }


            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseBody = response.body().string();
                Log.d("SubmitPost", "Response: " + responseBody);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");

                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            if (status.equals("success")) {
                                Toast.makeText(MainActivity.this, "Post submitted successfully", Toast.LENGTH_SHORT).show();
                                refreshPostList();
                            } else {
                                Toast.makeText(MainActivity.this, "Failed to submit post: " + message, Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            Toast.makeText(MainActivity.this, "Failed to submit post: Invalid response", Toast.LENGTH_SHORT).show();
                        }
                    });
                }
            }
        });
    }


    private void fetchPosts() {
        // Fetch posts from server
        PostHandler postFetcher = new PostHandler(this);
        postFetcher.fetchPostsData(new PostHandler.PostFetchListener() {
            @Override
            public void onSuccess(List<Post> postList) {
                postAdapter.setPosts(postList);
                swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false); // Stop the refreshing animation
            }
        });
    }

    private void refreshPostList() {
        PostHandler postFetcher = new PostHandler(MainActivity.this);
        postFetcher.fetchPostsData(new PostHandler.PostFetchListener() {
            @Override
            public void onSuccess(List<Post> postList) {
                postAdapter.setPosts(postList);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
            }
        });
    }
}
