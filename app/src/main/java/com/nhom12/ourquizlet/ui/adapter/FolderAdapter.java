package com.nhom12.ourquizlet.ui.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.Folder;
import com.nhom12.ourquizlet.data.repository.FolderRepository;
import com.nhom12.ourquizlet.databinding.ItemFolderBinding;
import com.nhom12.ourquizlet.ui.activity.InFolderActivity;
import com.squareup.picasso.Picasso;

import java.util.List;

public class FolderAdapter extends RecyclerView.Adapter<FolderAdapter.FolderViewHolder> {
    private final List<Folder> folders;
    private final Context mContext;
    public interface OnFolderListener {
        void onFolderEdit(Folder folder);
    }

    private static OnFolderListener listener;


    public FolderAdapter(List<Folder> folders, Context mContext, OnFolderListener listener) {
        this.folders = folders;
        this.mContext = mContext;
        this.listener = listener;
    }

    @NonNull
    @Override
    public FolderViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemFolderBinding itemBinding = ItemFolderBinding.inflate(layoutInflater, parent, false);
        return new FolderViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull FolderViewHolder holder, int position) {
        Folder folder = folders.get(position);
        holder.bind(folder);
        holder.itemView.setOnClickListener(v -> {
            Animation animation = AnimationUtils.loadAnimation(mContext, R.anim.scale_animation);
            holder.itemView.startAnimation(animation);
            Intent intent = new Intent(mContext, InFolderActivity.class);
            intent.putExtra("idFolder", folder.getId());
            mContext.startActivity(intent);
        });
    }

    @Override
    public int getItemCount() {
        return folders.size();
    }

    static class FolderViewHolder extends RecyclerView.ViewHolder {
        private final ItemFolderBinding binding;

        FolderViewHolder(ItemFolderBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Folder folder) {
            binding.tvName.setText(folder.getName());
            binding.tvUserName.setText(folder.getUsername());
            Picasso.get()
                    .load(folder.getAvatar())
                    .resize(50, 50)
                    .into(binding.ivAvatarUser);

            //Delete and Edit
            binding.ivMenu.setOnClickListener(v -> {
                PopupMenu popupMenu = new PopupMenu(v.getContext(), binding.ivMenu);
                popupMenu.getMenuInflater().inflate(R.menu.crud_menu, popupMenu.getMenu());

                popupMenu.setOnMenuItemClickListener(item -> {
                    int itemId = item.getItemId();
                    if (itemId == R.id.menuEdit) {
                        listener.onFolderEdit(folder);
                        return true;
                    } else if (itemId == R.id.menuDelete) {
                        FolderRepository studentRepository = new FolderRepository();
                        studentRepository.deleteFolder(folder, task -> {
                            if (task.isSuccessful()) {
                                MainActivity.folderViewModel.loadFolders();
                            }
                        });
                        return true;
                    } else {
                        return false;
                    }
                });
                popupMenu.show();
            });
        }
    }
}
