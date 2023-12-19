package com.nhom12.ourquizlet.ui.adapter;

import static androidx.core.content.ContextCompat.startActivity;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.imageview.ShapeableImageView;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.Topic;
import com.nhom12.ourquizlet.ui.activity.CreateEditTopicActivity;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class TopicsAdapter extends RecyclerView.Adapter<TopicsAdapter.TopicViewHolder> {

    public interface setOnItemClickListener {
        void onClickListener(Topic topic, int position, int numberWord);
    }

    private Activity activity;
    private Context context;
    private setOnItemClickListener listener;
    private List<Topic> topics;
    private ExecutorService executorService;
    private boolean isStudy;

    public TopicsAdapter(Activity activity, Context context, List<Topic> topics, boolean isStudy) {
        this.activity = activity;
        this.context = context;
        this.topics = new ArrayList<> ();
        this.topics.addAll (topics);
        this.isStudy = isStudy;
        this.executorService = Executors.newFixedThreadPool (2);
    }

    @NonNull
    @Override
    public TopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (activity).inflate (R.layout.item_topic, parent, false);
        return new TopicViewHolder (view);
    }

    @SuppressLint({"SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull TopicViewHolder holder, int position) {
        Topic topic = topics.get (position);

        holder.tvNumberTerm.setText (topic.getNumberWord () +" terms");
        holder.tvPercentScore.setText (topic.getPercent () + " %");
        holder.progressBar.setProgress (topic.getPercent ());

        holder.tvTopicName.setText (topic.getTitle ());
        holder.tvUserName.setText (topic.getUsername ());

        if (isStudy) {
            holder.ivMenu.setVisibility (View.GONE);
        } else
            holder.ivMenu.setVisibility (View.VISIBLE);

        holder.ivMenu.setOnClickListener (v -> {
            Intent intent = new Intent (activity, CreateEditTopicActivity.class);
            intent.putExtra ("TOPIC_ID", topic.getId ());
            startActivity (this.context, intent, new Bundle ());
        });

        holder.itemView.setOnClickListener (v -> {
            if (listener != null)
                listener.onClickListener (topic, position, topic.getNumberWord ());
        });

    }

    @Override
    public int getItemCount() {
        return topics.size ();
    }


    @SuppressLint("NotifyDataSetChanged")
    public void setTopics (List<Topic> topics) {
        this.topics.clear ();
        this.topics.addAll (topics);
        notifyDataSetChanged ();
    }
    public void setListener(setOnItemClickListener listener) {this.listener = listener;}

    public static class TopicViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvTopicName;
        private final TextView tvNumberTerm;

        private final TextView tvPercentScore;
        private final TextView tvUserName;
        private final ShapeableImageView ivAvatarUser;
        private final ImageView ivMenu;
        private final ProgressBar progressBar;

        public TopicViewHolder(@NonNull View itemView) {
            super (itemView);
            tvTopicName = itemView.findViewById (R.id.tvTopicName);
            tvNumberTerm = itemView.findViewById (R.id.tvNumberTerm);
            tvUserName = itemView.findViewById (R.id.tvUserName);
            tvPercentScore = itemView.findViewById (R.id.tvPercentScore);
            ivAvatarUser = itemView.findViewById (R.id.ivAvatarUser);
            progressBar = itemView.findViewById (R.id.progressBar);
            ivMenu = itemView.findViewById (R.id.ivMenu);
        }
    }
}
