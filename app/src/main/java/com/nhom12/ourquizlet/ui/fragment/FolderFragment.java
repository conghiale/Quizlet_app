package com.nhom12.ourquizlet.ui.fragment;

import android.app.AlertDialog;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.Folder;
import com.nhom12.ourquizlet.data.repository.FolderRepository;
import com.nhom12.ourquizlet.databinding.DialogEditFolderBinding;
import com.nhom12.ourquizlet.databinding.FragmentFolderBinding;
import com.nhom12.ourquizlet.ui.adapter.FolderAdapter;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

public class FolderFragment extends Fragment implements FolderAdapter.OnFolderListener{
    private FolderAdapter adapter;
    private FragmentFolderBinding fragmentFolderBinding;
    private DialogEditFolderBinding dialogEditFolderBinding;

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container, Bundle savedInstanceState) {
        fragmentFolderBinding = FragmentFolderBinding.inflate(inflater, container, false);
        return fragmentFolderBinding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        if(MainActivity.folderViewModel.getFolders() != null) {
            MainActivity.folderViewModel.getFolders().observe(getViewLifecycleOwner(), folders -> {
                adapter = new FolderAdapter(folders, this.getContext(), this);
                fragmentFolderBinding.rcvFolder.setLayoutManager(new LinearLayoutManager(getContext()));
                fragmentFolderBinding.rcvFolder.setAdapter(adapter);
            });
        }
    }
    private void showEditFolderDialog(Folder folder) {
        AlertDialog.Builder builder = new AlertDialog.Builder(getContext());
        dialogEditFolderBinding = DialogEditFolderBinding.inflate(LayoutInflater.from(getContext()));
        builder.setView(dialogEditFolderBinding.getRoot());
        AlertDialog dialog = builder.create();

        dialogEditFolderBinding.etFolderName.setText(folder.getName());
        dialogEditFolderBinding.btnCancel.setOnClickListener(view -> dialog.dismiss());
        dialogEditFolderBinding.btnOk.setOnClickListener(view -> {
            String folderName = dialogEditFolderBinding.etFolderName.getText().toString();
            Map<String, Object> updatedFolder = new HashMap<>();
            updatedFolder.put("name", folderName);

            FolderRepository folderRepository = new FolderRepository();
            folderRepository.editFolder(folder.getId(), Collections.unmodifiableMap(updatedFolder), task -> {
                if(task.isSuccessful()) {
                    MainActivity.folderViewModel.loadFolders();
                    dialog.dismiss();
                }
            });

        });

        dialog.show();
    }

    @Override
    public void onFolderEdit(Folder folder) {
        showEditFolderDialog(folder);
    }
}