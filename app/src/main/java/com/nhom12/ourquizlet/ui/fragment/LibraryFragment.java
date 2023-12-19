package com.nhom12.ourquizlet.ui.fragment;


import android.app.AlertDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.material.tabs.TabLayoutMediator;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.data.model.Folder;
import com.nhom12.ourquizlet.data.repository.FolderRepository;
import com.nhom12.ourquizlet.databinding.DialogAddFolderBinding;
import com.nhom12.ourquizlet.databinding.FragmentLibraryBinding;
import com.nhom12.ourquizlet.ui.activity.CreateEditTopicActivity;
import com.nhom12.ourquizlet.ui.adapter.ViewPagerAdapter;

public class LibraryFragment extends Fragment {
    private FragmentLibraryBinding binding;
    private DialogAddFolderBinding dialogAddFolderBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        binding = FragmentLibraryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        //Tablayout, viewpager
        ViewPagerAdapter viewPagerAdapter = new ViewPagerAdapter(requireActivity());
        binding.viewPager.setAdapter(viewPagerAdapter);

        // Setup TabLayout with ViewPager
        new TabLayoutMediator(binding.tabLayout, binding.viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("TOPIC");
                    break;
                case 1:
                    tab.setText("FOLDER");
                    break;
            }
        }).attach();

        binding.ivAdd.setOnClickListener(v -> {
            int currentTabPosition = binding.tabLayout.getSelectedTabPosition();
            Intent intent;
            if (currentTabPosition == 0) {
                intent = new Intent(getContext(), CreateEditTopicActivity.class);
                startActivity(intent);
            } else if (currentTabPosition == 1) {
                showAddFolderDialog();
            } else {
                intent = new Intent(getContext(), CreateEditTopicActivity.class);
                startActivity(intent);
            }
        });
    }

    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        dialogAddFolderBinding = DialogAddFolderBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogAddFolderBinding.getRoot());
        AlertDialog dialog = builder.create();

        dialogAddFolderBinding.btnCancel.setOnClickListener(view -> dialog.dismiss());
        dialogAddFolderBinding.btnOk.setOnClickListener(view -> {
            Folder folder = new Folder();
            String folderName = dialogAddFolderBinding.etFolderName.getText().toString();
            folder.setName(folderName);
            FolderRepository folderRepository = new FolderRepository();
            folderRepository.addFolder(folder, task -> {
                if(task.isSuccessful()) {
                    MainActivity.folderViewModel.loadFolders();
                    dialog.dismiss();
                }
            });

        });

        dialog.show();
    }

}

