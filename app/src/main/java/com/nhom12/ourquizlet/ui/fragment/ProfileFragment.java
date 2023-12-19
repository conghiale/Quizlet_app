package com.nhom12.ourquizlet.ui.fragment;

import static android.app.Activity.RESULT_OK;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.firebase.auth.FirebaseAuth;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.data.model.User;
import com.nhom12.ourquizlet.data.repository.StorageRepository;
import com.nhom12.ourquizlet.data.repository.UserRepository;
import com.nhom12.ourquizlet.databinding.FragmentProfileBinding;
import com.nhom12.ourquizlet.ui.activity.SettingsActivity;
import com.squareup.picasso.Picasso;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;


public class ProfileFragment extends Fragment {
    private FragmentProfileBinding fragmentProfileBinding;

    private UserRepository userRepository;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentProfileBinding = FragmentProfileBinding.inflate(inflater, container, false);
        return fragmentProfileBinding.getRoot();
    }
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        MainActivity.profileViewModel.getUserLiveData().observe(getViewLifecycleOwner(), this::updateUI);
        fragmentProfileBinding.ivSetting.setOnClickListener(v -> {
            Intent intent = new Intent(getContext(), SettingsActivity.class);
            startActivity(intent);
        });

        fragmentProfileBinding.editInfo.setOnClickListener(v -> {
            if (fragmentProfileBinding.btnSave.getVisibility() == View.INVISIBLE) {
                openEdit();
            } else {
                closeEdit();
            }
        });

        fragmentProfileBinding.btnSave.setOnClickListener(v -> {
            Map<String, Object> updatedUser = new HashMap<>();
            updatedUser.put("username", fragmentProfileBinding.etUsername.getText().toString());
            updatedUser.put("age", Integer.parseInt(fragmentProfileBinding.etAge.getText().toString()));
            updatedUser.put("phoneNumber", fragmentProfileBinding.etPhoneNumber.getText().toString());

            FirebaseAuth auth = FirebaseAuth.getInstance();

            userRepository = new UserRepository();
            userRepository.editUser(Objects.requireNonNull(auth.getCurrentUser()).getUid(), updatedUser, task -> {
                if (task.isSuccessful()) {
                    Toast.makeText(this.getContext(), "Successfully", Toast.LENGTH_SHORT).show();
                } else {
                    Toast.makeText(this.getContext(), "Failed", Toast.LENGTH_SHORT).show();
                }
            });

            closeEdit();
        });

        fragmentProfileBinding.editAvatar.setOnClickListener(v -> {
            String updateUID = Objects.requireNonNull(FirebaseAuth.getInstance().getCurrentUser()).getUid();
            Intent intentImage = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.EXTERNAL_CONTENT_URI);
            startActivityForResult(intentImage, 1);
        });
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        FirebaseAuth auth = FirebaseAuth.getInstance();

        if (requestCode == 1 && resultCode == RESULT_OK) {
            if (data != null && data.getData() != null) {
                Uri selectedImageUri = data.getData();

                StorageRepository storageRepository = new StorageRepository();
                storageRepository.uploadImageToFirebase(selectedImageUri, uri -> {
                    String downloadUri = uri.toString();

                    Map<String, Object> updatedUser = new HashMap<>();
                    updatedUser.put("avatar", downloadUri);

                    UserRepository userRepository = new UserRepository();
                    userRepository.editUser(Objects.requireNonNull(auth.getCurrentUser()).getUid(), updatedUser, task -> {
                        if (!task.isSuccessful()){
                            Log.e("ImageError", "update image failed");
                        }
                    });

                    Picasso.get()
                            .load(downloadUri)
                            .resize(400, 400)
                            .into(fragmentProfileBinding.profileAvatar);
                }, e -> {
                    Log.e("ImageError", "update image failed");
                });
            }
        }
    }

    private void updateUI(User user) {
        fragmentProfileBinding.etUsername.setText(user.getUsername());
        fragmentProfileBinding.tvEmail.setText(user.getEmail());
        fragmentProfileBinding.etAge.setText(String.valueOf(user.getAge()));
        fragmentProfileBinding.etPhoneNumber.setText(user.getPhoneNumber());
        Picasso.get()
                .load(user.getAvatar())
                .resize(400, 400)
                .into(fragmentProfileBinding.profileAvatar);
    }

    private void closeEdit() {
        fragmentProfileBinding.etUsername.setEnabled(false);
        fragmentProfileBinding.etAge.setEnabled(false);
        fragmentProfileBinding.etPhoneNumber.setEnabled(false);
        fragmentProfileBinding.btnSave.setVisibility(View.INVISIBLE);
    }

    private void openEdit() {
        fragmentProfileBinding.etUsername.setEnabled(true);
        fragmentProfileBinding.etAge.setEnabled(true);
        fragmentProfileBinding.etPhoneNumber.setEnabled(true);
        fragmentProfileBinding.btnSave.setVisibility(View.VISIBLE);
    }
}