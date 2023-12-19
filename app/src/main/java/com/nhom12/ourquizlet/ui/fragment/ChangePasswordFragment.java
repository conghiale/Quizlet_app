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
import com.nhom12.ourquizlet.databinding.FragmentChangePasswordBinding;

public class ChangePasswordFragment extends Fragment {
    private FragmentChangePasswordBinding binding;
    private UserRepository userRepository;


    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentChangePasswordBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        userRepository = new UserRepository();

        binding.btnSave.setOnClickListener(v -> {
            String currentPassword = binding.etCurrentPassword.getText().toString().trim();
            String newPassword = binding.etNewPassword.getText().toString().trim();
            String reNewPassword = binding.etReNewPassword.getText().toString().trim();

            if (currentPassword.isEmpty() || newPassword.isEmpty() || reNewPassword.isEmpty()) {
                Toast.makeText(getContext(), "Please enter complete information", Toast.LENGTH_SHORT).show();
                return;
            }

            if (!newPassword.equals(reNewPassword)) {
                Toast.makeText(getContext(), "Confirm password does not match", Toast.LENGTH_SHORT).show();
                return;
            }

            userRepository.updatePassword(currentPassword, newPassword)
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful()) {
                            Toast.makeText(getContext(), "Password updated successfully", Toast.LENGTH_SHORT).show();
                            if (isAdded()) {
                                requireActivity().getSupportFragmentManager().popBackStack();
                            }
                        } else {
                            Toast.makeText(getContext(), "Current password not correct" , Toast.LENGTH_SHORT).show();
                        }
                    });
        });
    }
}