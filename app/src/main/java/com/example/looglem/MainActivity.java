package com.example.looglem;

import android.app.Dialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
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
import org.jetbrains.annotations.Nullable;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.MultipartBody;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class MainActivity extends AppCompatActivity {

    private RecyclerView recyclerView;
    private PostAdapter postAdapter;


    private static final int PICK_IMAGE_REQUEST = 1;
    private File imageFile;

    private SwipeRefreshLayout swipeRefreshLayout;

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

        Button btnModalComment = findViewById(R.id.btnModalComment);
        btnModalComment.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showSubmitCommentModal();
            }
        });

    }

    private void openImageChooser() {
        Intent intent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
        startActivityForResult(intent, PICK_IMAGE_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == PICK_IMAGE_REQUEST && resultCode == RESULT_OK && data != null && data.getData() != null) {
            Uri imageUri = data.getData();
            try {
                InputStream inputStream = getContentResolver().openInputStream(imageUri);
                File tempFile = new File(getCacheDir(), "temp_image.png");
                FileOutputStream outputStream = new FileOutputStream(tempFile);
                byte[] buffer = new byte[1024];
                int length;
                while ((length = inputStream.read(buffer)) > 0) {
                    outputStream.write(buffer, 0, length);
                }
                outputStream.flush();
                outputStream.close();
                inputStream.close();

                imageFile = tempFile;
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }

    private void showSubmitPostModal() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.submit_post_modal);

        EditText editTextUsername = dialog.findViewById(R.id.editTextUsername);
        EditText editTextPassword = dialog.findViewById(R.id.editTextPassword);
        EditText editTextContent = dialog.findViewById(R.id.editTextContent);
        EditText editTextUrl = dialog.findViewById(R.id.editTextUrl);
        Button btnUploadImage = dialog.findViewById(R.id.btnUploadImage);
        Button btnSubmit = dialog.findViewById(R.id.btnSubmit);

        btnUploadImage.setOnClickListener(v -> openImageChooser());

        btnSubmit.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String username = editTextUsername.getText().toString();
                String password = editTextPassword.getText().toString();
                String content = editTextContent.getText().toString();
                String posturl = editTextUrl.getText().toString();

                Log.d(username, password);
                Log.d(username, content);

                submitPost(username, password, content, posturl, imageFile);

                dialog.dismiss();
            }
        });

        dialog.show();
    }

    private void showSubmitCommentModal() {
        Dialog dialog = new Dialog(this);
        dialog.setContentView(R.layout.submit_comment_modal);

        EditText editTextPostId = dialog.findViewById(R.id.editTextPostId);
        EditText editTextCommentContent = dialog.findViewById(R.id.editTextCommentContent);
        EditText editTextUsernameComment = dialog.findViewById(R.id.editTextUsernameComment);
        EditText editTextPasswordComment = dialog.findViewById(R.id.editTextPasswordComment);


        Button btnSubmitComment = dialog.findViewById(R.id.btnSubmitComment);

        btnSubmitComment.setOnClickListener(v -> {

            String postId = editTextPostId.getText().toString();
            String commentContent = editTextCommentContent.getText().toString();
            String usernameComment = editTextUsernameComment.getText().toString();
            String passwordComment = editTextPasswordComment.getText().toString();

            Log.d("postId", "Post ID: " + postId);
            Log.d("commentContent", "Comment: " + commentContent);
            Log.d("usernameComment", "Comment Username: " + usernameComment);
            Log.d("passwordComment", "Comment Password: " + passwordComment);

            submitComment(postId, usernameComment, passwordComment, commentContent);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void submitPost(String username, String password, String content, String posturl, File imageFile) {
        OkHttpClient client = new OkHttpClient();

        MultipartBody.Builder builder = new MultipartBody.Builder()
                .setType(MultipartBody.FORM)
                .addFormDataPart("username", username)
                .addFormDataPart("password", password)
                .addFormDataPart("postContent", content)
                .addFormDataPart("postLinkUrl", posturl);

        if (imageFile != null) {
            builder.addFormDataPart("postImage", imageFile.getName(),
                    RequestBody.create(imageFile, MediaType.parse("image/jpeg")));
        }


        RequestBody requestBody = builder.build();

        Request request = new Request.Builder()
                .url("http://plus.loogle.mooo.com/apiv1/submit_mobile_post.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                String errorMessage = "Failed to submit post: " + e.getMessage();
                Log.e("SubmitPost", errorMessage, e);

                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseBody = response.body().string();
                Log.d("SubmitPost", "Response: " + responseBody);

                try {
                    JSONObject jsonResponse = new JSONObject(responseBody);
                    String status = jsonResponse.getString("status");
                    String message = jsonResponse.getString("message");

                    runOnUiThread(() -> {
                        if (status.equals("success")) {
                            Toast.makeText(MainActivity.this, "Post submitted successfully", Toast.LENGTH_SHORT).show();

                            if (imageFile != null) {
                                if (imageFile.exists()) {
                                    boolean deleted = imageFile.delete();
                                    if (deleted) {
                                        Log.d("DeleteImage", "Image file deleted successfully.");
                                    } else {
                                        Log.d("DeleteImage", "Failed to delete image file.");
                                    }
                                } else {
                                    Log.d("DeleteImage", "Image file does not exist.");
                                }
                            }

                            refreshPostList();
                        } else {
                            Toast.makeText(MainActivity.this, "Failed to submit post: " + message, Toast.LENGTH_SHORT).show();
                        }
                    });
                } catch (JSONException e) {
                    e.printStackTrace();
                    runOnUiThread(() ->
                            Toast.makeText(MainActivity.this, "Failed to submit post: Invalid response", Toast.LENGTH_SHORT).show()
                    );
                }
            }
        });
    }

    private void submitComment(String postID, String username, String password, String commentContent) {
        OkHttpClient client = new OkHttpClient();

        RequestBody requestBody = new FormBody.Builder()
                .add("postID", postID)
                .add("username", username)
                .add("password", password)
                .add("commentContent", commentContent)
                .build();

        Request request = new Request.Builder()
                .url("http://plus.loogle.mooo.com/apiv1/submit_comment.php")
                .post(requestBody)
                .build();

        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onFailure(@NotNull Call call, @NotNull IOException e) {
                String errorMessage = "Failed to submit comment: " + e.getMessage();
                Log.e("SubmitComment", errorMessage, e);

                runOnUiThread(() ->
                        Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show()
                );
            }

            @Override
            public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {
                final String responseBody = response.body().string();
                Log.d("SubmitComment", "Response: " + responseBody);

                runOnUiThread(() -> {

                    Toast.makeText(MainActivity.this, "Response: " + responseBody, Toast.LENGTH_LONG).show();
                    Log.d("SubmitComment", "Response: " + responseBody);

                    fetchPosts();
                });
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
                swipeRefreshLayout.setRefreshing(false);
            }

            @Override
            public void onFailure(String errorMessage) {
                Toast.makeText(MainActivity.this, errorMessage, Toast.LENGTH_SHORT).show();
                swipeRefreshLayout.setRefreshing(false);
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
