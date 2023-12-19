package com.nhom12.ourquizlet.ui.activity;

import android.content.Intent;
import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.databinding.ActivitySettingsBinding;
import com.nhom12.ourquizlet.ui.fragment.ChangeEmailFragment;
import com.nhom12.ourquizlet.ui.fragment.ChangePasswordFragment;

public class SettingsActivity extends AppCompatActivity {
    private ActivitySettingsBinding binding;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivitySettingsBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        binding.ivBack.setOnClickListener(v -> {
            finish();
        });

        binding.btnLogout.setOnClickListener(v -> {
            FirebaseAuth.getInstance().signOut();
            finish();
            startActivity(new Intent(this, IntroActivity.class));
        });

        binding.btnChangePassword.setOnClickListener(v -> {
            ChangePasswordFragment changePasswordFragment = new ChangePasswordFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, changePasswordFragment)
                    .addToBackStack(null)
                    .commit();
        });

        binding.btnChangeEmail.setOnClickListener(v -> {
            ChangeEmailFragment changeEmailFragment = new ChangeEmailFragment();
            getSupportFragmentManager().beginTransaction()
                    .replace(R.id.fragmentContainer, changeEmailFragment)
                    .addToBackStack(null)
                    .commit();
        });
    }
}