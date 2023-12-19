package com.nhom12.ourquizlet.ui.activity;

import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.data.model.Topic;
import com.nhom12.ourquizlet.data.repository.FolderRepository;
import com.nhom12.ourquizlet.databinding.ActivityAddTopicToFolderBinding;
import com.nhom12.ourquizlet.ui.adapter.ChooseTopicAdapter;

import java.util.HashSet;
import java.util.Set;

public class AddTopicToFolderActivity extends AppCompatActivity {

    private ActivityAddTopicToFolderBinding binding;
    private FirebaseAuth auth = FirebaseAuth.getInstance();
    private final Set<String> selectedTopicIds = new HashSet<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTopicToFolderBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        Intent intent = getIntent();
        String folderId = intent.getStringExtra("folderId");

        binding.ivBack.setOnClickListener(v -> finish());

        MainActivity.folderViewModel.getTopicInFolder(folderId).observe(this, topics -> {
            for (Topic topic: topics){
                selectedTopicIds.add(topic.getId());
            }
        });

        if (MainActivity.topicViewModel.getTopics() != null) {
            MainActivity.topicViewModel.getTopics().observe(this, topics -> {
                binding.rcvTopic.setLayoutManager(new LinearLayoutManager(this));
                binding.rcvTopic.setAdapter(new ChooseTopicAdapter(topics, selectedTopicIds, (topic, viewHolderBinding) -> {
                    boolean isSelected = selectedTopicIds.contains(topic.getId());
                    if (!isSelected) {
                        viewHolderBinding.cardTopic.setCardBackgroundColor(Color.parseColor("#BB86FC"));
                        selectedTopicIds.add(topic.getId());
                    } else {
                        viewHolderBinding.cardTopic.setCardBackgroundColor(Color.parseColor("#2E3856"));
                        selectedTopicIds.remove(topic.getId());
                    }
                }));
            });
        }

        binding.ivSave.setOnClickListener(v -> {
            FolderRepository folderRepository = new FolderRepository();
            folderRepository.addTopicsToFolder(folderId, selectedTopicIds, task -> {
                if (task.isSuccessful()) {
                    MainActivity.folderViewModel.loadTopicsInFolder(folderId, "all");
                    finish();
                }
            });
        });
    }
}


