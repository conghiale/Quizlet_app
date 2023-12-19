package com.nhom12.ourquizlet.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom12.ourquizlet.databinding.ActivityIntroBinding;
import com.nhom12.ourquizlet.ui.adapter.IntroSliderAdapter;

public class IntroActivity extends AppCompatActivity {
    private ActivityIntroBinding binding;
    private FirebaseAuth auth;
    IntroSliderAdapter introSliderAdapter;
    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityIntroBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        auth = FirebaseAuth.getInstance ();

        introSliderAdapter = new IntroSliderAdapter (this);
        binding.viewPager.setAdapter (introSliderAdapter);

        binding.indicator.setViewPager (binding.viewPager);

        introSliderAdapter.registerDataSetObserver (binding.indicator.getDataSetObserver ());
        introSliderAdapter.unregisterDataSetObserver (binding.indicator.getDataSetObserver ());

        binding.tvGetStarted.setOnClickListener (v -> {
            startActivity (new Intent (this, LoginActivity.class));
        });
    }

    @Override
    protected void onResume() {
        super.onResume ();
        if (auth.getCurrentUser () != null) {
            startActivity (new Intent (this, LoginActivity.class));
        }
    }
}
