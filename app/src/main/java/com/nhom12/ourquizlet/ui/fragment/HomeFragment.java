package com.nhom12.ourquizlet.ui.fragment;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.StaggeredGridLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.databinding.FragmentHomeBinding;
import com.nhom12.ourquizlet.ui.activity.TopicsActivity;
import com.nhom12.ourquizlet.ui.adapter.CategoryTopicAdapter;
import com.nhom12.ourquizlet.data.model.CategoryTopic;
import com.nhom12.ourquizlet.ui.viewModel.CategoryViewModel;

import java.util.ArrayList;
import java.util.List;

public class HomeFragment extends Fragment {

    private FragmentHomeBinding binding;
    private List<CategoryTopic> categoryTopics;
    public static CategoryViewModel categoryVM;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        // Inflate the layout for this fragment
        binding = FragmentHomeBinding.inflate (inflater, container, false);
        return binding.getRoot ();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated (view, savedInstanceState);
        initView ();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void initView() {
        categoryTopics = new ArrayList<> ();
        categoryVM = new ViewModelProvider (this).get (CategoryViewModel.class);
        CategoryTopicAdapter adapterCategoryTopic = new CategoryTopicAdapter (getContext (), categoryTopics);

        categoryVM.getCategoryAll ().observe (getViewLifecycleOwner (), categoryTopics -> {
            this.categoryTopics.clear ();
            this.categoryTopics.addAll (categoryTopics);
            adapterCategoryTopic.setListener ((categoryTopic, position) -> {
                Intent intent = new Intent (requireActivity (), TopicsActivity.class);
                intent.putExtra ("CATEGORY_TOPIC_NAME", categoryTopic.getTitle ());
                intent.putExtra ("CATEGORY_TOPIC_ID", categoryTopic.getId ());
                startActivity (intent);
            });

            binding.recyclerview.setHasFixedSize(true);
            binding.recyclerview.setNestedScrollingEnabled (false);
            binding.recyclerview.setLayoutManager (new StaggeredGridLayoutManager (2, StaggeredGridLayoutManager.VERTICAL));
            binding.recyclerview.setAdapter(adapterCategoryTopic);
        });
    }
}