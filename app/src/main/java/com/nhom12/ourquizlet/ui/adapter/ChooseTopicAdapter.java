package com.nhom12.ourquizlet.ui.adapter;

import android.graphics.Color;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom12.ourquizlet.data.model.Topic;
import com.nhom12.ourquizlet.databinding.ItemTopicBinding;
import com.squareup.picasso.Picasso;

import java.util.List;
import java.util.Set;

public class ChooseTopicAdapter extends RecyclerView.Adapter<ChooseTopicAdapter.TopicViewHolder> {
    public interface OnItemClickListener {
        void onItemClick(Topic topic, ItemTopicBinding binding);
    }

    private final List<Topic> topics;
    private final Set<String> selectedTopicIds;
    private final OnItemClickListener listener;

    public ChooseTopicAdapter(List<Topic> topics, Set<String> selectedTopicIds, OnItemClickListener listener) {
        this.topics = topics;
        this.selectedTopicIds = selectedTopicIds;
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
        Topic topic = topics.get(position);
        holder.bind(topic, listener);

        // Set the background color based on whether the topic is selected
        if (selectedTopicIds.contains(topic.getId())) {
            holder.binding.cardTopic.setCardBackgroundColor(Color.parseColor("#BB86FC"));
        } else {
            holder.binding.cardTopic.setCardBackgroundColor(Color.parseColor("#2E3856"));
        }
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

        void bind(Topic topic, OnItemClickListener listener) {
            binding.tvTopicName.setText(topic.getTitle());
            binding.tvNumberTerm.setText(String.valueOf(topic.getNumberWord()));
            binding.tvUserName.setText(topic.getUsername());
            Picasso.get()
                    .load(topic.getAvatar())
                    .resize(50, 50)
                    .into(binding.ivAvatarUser);
            itemView.setOnClickListener(v -> listener.onItemClick(topic, binding));
        }
    }
}