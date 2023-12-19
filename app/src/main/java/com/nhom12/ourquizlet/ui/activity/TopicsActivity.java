package com.nhom12.ourquizlet.ui.activity;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.databinding.ActivityTopicsBinding;
import com.nhom12.ourquizlet.ui.adapter.TopicsAdapter;
import com.nhom12.ourquizlet.data.model.Topic;
import com.nhom12.ourquizlet.ui.viewModel.Topic2ViewModel;

import java.util.ArrayList;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TopicsActivity extends AppCompatActivity implements TopicsAdapter.setOnItemClickListener {

    private ActivityTopicsBinding binding;
    private TopicsAdapter adapter;
    private ArrayList<Topic> topics;
    private String idCategory;
    private FirebaseFirestore db;
    private Topic2ViewModel topicVM;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityTopicsBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        setSupportActionBar (binding.toolbar);
        Objects.requireNonNull (getSupportActionBar ()).setTitle (getIntent ().getStringExtra ("CATEGORY_TOPIC_NAME"));
        getSupportActionBar ().setDisplayShowTitleEnabled (true);

        init();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void init() {
        topicVM = new ViewModelProvider (this).get (Topic2ViewModel.class);
        ExecutorService executorService = Executors.newFixedThreadPool (1);
        db = FirebaseFirestore.getInstance ();
        topics = new ArrayList<> ();

        idCategory = getIntent ().getStringExtra ("CATEGORY_TOPIC_ID");

        if (idCategory != null) {
            MainActivity.topicVM.getTopicAll ().observe (this, topics -> {
                for (Topic t : topics) {
                    if (t.getIdCategory () != null && t.getIdCategory ().equals (idCategory) && t.isPublic ()) {
                        this.topics.add (t);
                    }
                }

                runOnUiThread (() -> {
                    adapter = new TopicsAdapter (this, this, this.topics, true);
                    adapter.setListener (this);

                    binding.recyclerview.setHasFixedSize (true);
                    binding.recyclerview.setLayoutManager (new LinearLayoutManager (this, LinearLayoutManager.VERTICAL, false));
                    binding.recyclerview.setAdapter (adapter);
                });
            });
        }
    }

    @Override
    public void onClickListener(Topic topic, int position, int numberWord) {
        Intent intent = new Intent (this, TopicActivity.class);
        intent.putExtra ("TOPIC_NAME", topic.getTitle ());
        intent.putExtra ("TOPIC_ID", topic.getId ());
        intent.putExtra ("COUNT", topic.getCount ());
        intent.putExtra ("NUMBER_WORD", numberWord);
        intent.putExtra ("CATEGORY_ID", idCategory);
        startActivity (intent);
    }
}
