package com.nhom12.ourquizlet.ui.adapter;

import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom12.ourquizlet.data.model.Topic;
import com.nhom12.ourquizlet.databinding.ItemTopicBinding;
import com.squareup.picasso.Picasso;

import java.util.List;

public class TopicAdapter extends RecyclerView.Adapter<TopicAdapter.TopicViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Topic topic);
    }

    private final List<Topic> topics;
    private ItemTopicBinding binding;
    private final OnItemClickListener listener;
    public TopicAdapter(List<Topic> topics, OnItemClickListener listener) {
        this.topics = topics;
        this.listener = listener;
    }
    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        LayoutInflater layoutInflater = LayoutInflater.from(parent.getContext());
        ItemTopicBinding itemBinding = ItemTopicBinding.inflate(layoutInflater, parent, false);
        return new TopicViewHolder(itemBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        holder.bind(topics.get(position), listener);
    }


    @Override
    public int getItemCount() {
        return topics.size();
    }

    static class TopicViewHolder extends RecyclerView.ViewHolder {
        private final ItemTopicBinding binding;

        TopicViewHolder(ItemTopicBinding binding) {
            super(binding.getRoot());
            this.binding = binding;
        }

        void bind(Topic topic, final OnItemClickListener listener) {
            binding.tvTopicName.setText(topic.getTitle());
            binding.tvNumberTerm.setText(String.valueOf(topic.getNumberWord()));
            binding.tvUserName.setText(topic.getUsername());
            Picasso.get()
                    .load(topic.getAvatar())
                    .resize(50, 50)
                    .into(binding.ivAvatarUser);
            itemView.setOnClickListener(v -> listener.onItemClick(topic));
        }
    }
}

