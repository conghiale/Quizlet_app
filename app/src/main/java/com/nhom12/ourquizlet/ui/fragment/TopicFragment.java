package com.nhom12.ourquizlet.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;

import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.Topic;
import com.nhom12.ourquizlet.databinding.FragmentTopicBinding;
import com.nhom12.ourquizlet.ui.activity.TopicActivity;
import com.nhom12.ourquizlet.ui.adapter.TopicsAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TopicFragment extends Fragment {

    private FragmentTopicBinding binding;
    private TopicsAdapter topicsAdapter;
    private List<Topic> topicsAll;
    private List<Topic> topicList;
    private List<Topic> topicsCreated;
    private List<Topic> topicsLearned;
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
//        return inflater.inflate(R.layout.fragment_topic, container, false);
        binding = FragmentTopicBinding.inflate (inflater, container, false);
        return binding.getRoot ();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated (view, savedInstanceState);
        topicsAll = new ArrayList<> ();
        topicsCreated = new ArrayList<> ();
        topicsLearned = new ArrayList<> ();
        topicList = new ArrayList<> ();

        String[] filterTopic = getResources ().getStringArray (R.array.filter_topics);
        ArrayAdapter<String> adapterFilter = new ArrayAdapter<> (requireContext (), R.layout.item_auto_complete, filterTopic);
        binding.filterTopic.setAdapter (adapterFilter);
        binding.filterTopic.setText (filterTopic[0], false);
        Log.d ("TAG", "64 - onViewCreated: filterTopic: " + binding.filterTopic.getText ());

        MainActivity.topicVM.getTopicsOfCurrentUser ().observe (getViewLifecycleOwner (), topics -> {
            topicsAll.clear ();
            topicList.clear ();
            topicsLearned.clear ();

            for (Topic t : topics) {
                if (t.getCount () > 0) {
                    topicsLearned.add (t.clone ());
                }
                topicsAll.add (t.clone ());
            }


            if (binding.filterTopic.getText ().toString ().trim ().equals ("All")) {
                topicList.addAll (topicsAll);
            } else if (binding.filterTopic.getText ().toString ().trim ().equals ("Created")) {
                topicList.addAll (topicsCreated);
            } else {
                topicList.addAll (topicsLearned);
            }
            topicsAdapter = new TopicsAdapter (requireActivity (), requireContext (), topicList, false);
            topicsAdapter.setListener (this::onClickListener);

            binding.recTopics.setHasFixedSize (true);
            binding.recTopics.setNestedScrollingEnabled (false);
            binding.recTopics.setLayoutManager (new LinearLayoutManager (requireContext (), LinearLayoutManager.VERTICAL, false));
            binding.recTopics.setAdapter (topicsAdapter);

            MainActivity.topicVM.getTopicOfCurrentUserCreated (this.topicsCreated);
        });

        binding.filterTopic.setOnItemClickListener ((parent, view1, position, id) -> {
            String infoTopic = Objects.requireNonNull (binding.etFilter.getText ()).toString ().trim ().toLowerCase ();
            this.topicList.clear ();

            if (position == 0) {
                this.topicList.addAll (this.topicsAll);
            } else if (position == 1) {
                this.topicList.addAll (this.topicsCreated);
            } else {
                this.topicList.addAll (this.topicsLearned);
            }
            if (infoTopic.equals ("")) {
                topicsAdapter.setTopics (this.topicList);
            } else {
                topicsAdapter.setTopics (this.topicList.stream().filter (topic ->
                        topic.getTitle ().trim ().toLowerCase ().contains (infoTopic) ||
                        topic.getDescription ().trim ().toLowerCase ().contains (infoTopic)).collect(Collectors.toList()));
            }
        });

        binding.etFilter.addTextChangedListener (new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                String infoTopic = Objects.requireNonNull (binding.etFilter.getText ()).toString ().trim ().toLowerCase ();
                if (infoTopic.equals ("")) {
                    topicsAdapter.setTopics (topicList);
                } else {
                    topicsAdapter.setTopics (new ArrayList<> (topicList.stream().filter (topic ->
                            topic.getTitle ().toLowerCase ().contains (infoTopic) ||
                            topic.getDescription ().toLowerCase ().contains (infoTopic)).collect(Collectors.toList())));
                }
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    private void onClickListener(Topic topic, int i, int i1) {

        Intent intent = new Intent (requireActivity (), TopicActivity.class);
        intent.putExtra ("TOPIC_NAME", topic.getTitle ());
        intent.putExtra ("TOPIC_ID", topic.getId ());
        intent.putExtra ("COUNT", topic.getCount ());
        intent.putExtra ("NUMBER_WORD", topic.getNumberWord ());
        intent.putExtra ("CATEGORY_ID", topic.getIdCategory ());
        startActivity (intent);
    }
}