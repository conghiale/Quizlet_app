package com.nhom12.ourquizlet.ui.fragment;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.nhom12.ourquizlet.data.repository.UserRepository;
import com.nhom12.ourquizlet.databinding.FragmentChangeEmailBinding;

public class ChangeEmailFragment extends Fragment {
    private FragmentChangeEmailBinding binding;
    private UserRepository userRepository;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangeEmailBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userRepository = new UserRepository();

        binding.btnSave.setOnClickListener(v -> {
            String currentPassword = binding.etPassword.getText().toString().trim();
            String newEmail = binding.etNewEmail.getText().toString().trim();

            if (currentPassword.isEmpty() || newEmail.isEmpty()) {
                Toast.makeText(getContext(), "Please enter all fields", Toast.LENGTH_SHORT).show();
                return;
            }

            userRepository.updateEmailWithVerification(currentPassword, newEmail)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Verification email sent. Please check your email to confirm the change.", Toast.LENGTH_LONG).show();
                            if (isAdded()) {
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        } else {
                            Toast.makeText(getContext(), "Failed to update email: " + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}