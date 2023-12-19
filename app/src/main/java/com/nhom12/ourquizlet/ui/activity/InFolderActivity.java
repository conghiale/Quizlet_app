package com.nhom12.ourquizlet.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.widget.PopupMenu;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.databinding.ActivityInFolderBinding;
import com.nhom12.ourquizlet.ui.adapter.TopicAdapter;

public class InFolderActivity extends AppCompatActivity {
    private TopicAdapter adapter;
    private ActivityInFolderBinding binding;
    private FirebaseAuth auth;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityInFolderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String folderId = intent.getStringExtra("idFolder");
        Log.e("folderId",folderId);

        binding.ivBack.setOnClickListener(v -> finish());

        //View topic
        if (MainActivity.folderViewModel.getTopicInFolder(folderId) != null) {
            MainActivity.folderViewModel.getTopicInFolder(folderId).observe(this, topics -> {
                binding.rcvTopic.setLayoutManager(new LinearLayoutManager(this));
                binding.rcvTopic.setAdapter(adapter);
                //topics: list topic, topic: onItemClick Interface
                binding.rcvTopic.setAdapter(new TopicAdapter(topics, topic -> {

                }));
            });
        }

        binding.sortButton.setOnClickListener(v -> {
            PopupMenu sortMenu = new PopupMenu(v.getContext(), v);
            sortMenu.getMenuInflater().inflate(R.menu.sort_topic_menu, sortMenu.getMenu());
            sortMenu.setOnMenuItemClickListener(menuItem -> {
                int itemId = menuItem.getItemId();
                if (itemId == R.id.sortAll) {
                    binding.sortButton.setText("Tất cả");
                    MainActivity.folderViewModel.loadTopicsInFolder(folderId, "all");
                } else if (itemId == R.id.sortCreated) {
                    binding.sortButton.setText("Đã tạo");
                    MainActivity.folderViewModel.loadTopicsInFolder(folderId, "created");
                } else if (itemId == R.id.sortJoined) {
                    binding.sortButton.setText("Đã tham gia");
                    MainActivity.folderViewModel.loadTopicsInFolder(folderId, "joined");
                }
                return true;
            });
            sortMenu.show();
        });

        binding.ivAdd.setOnClickListener(v -> {
            Intent intent1 = new Intent(this, AddTopicToFolderActivity.class);
            intent1.putExtra("folderId", folderId);
            startActivity(intent1);
        });
    }
}
