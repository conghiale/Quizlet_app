package com.nhom12.ourquizlet.ui.activity;

import androidx.appcompat.app.AppCompatActivity;

import android.os.Bundle;

import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.databinding.ActivityAddTopicBinding;
import com.nhom12.ourquizlet.databinding.ActivitySettingsBinding;
import com.nhom12.ourquizlet.databinding.ActivityTopicBinding;

public class AddTopicActivity extends AppCompatActivity {
    private ActivityAddTopicBinding binding;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityAddTopicBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> finish());


    }
}