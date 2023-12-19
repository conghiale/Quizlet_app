package com.nhom12.ourquizlet.ui.adapter;


import android.annotation.SuppressLint;
import android.app.Activity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;


import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.Word;
import com.nhom12.ourquizlet.ui.activity.TopicActivity;

import java.util.ArrayList;
import java.util.List;

public class BottomActivityTopicAdapter extends RecyclerView.Adapter<BottomActivityTopicAdapter.TopPicViewHolder> {

    public interface setOnItemStarClickListener {
        void onClickStarListener(Word word, int position);
    }
    private setOnItemStarClickListener clickListener;
    private List<Word> words;
    private Activity activity;

    public BottomActivityTopicAdapter(List<Word> words, Activity activity) {
        this.words = new ArrayList<> ();
        this.words.addAll (words);
        this.activity = activity;
    }

    @NonNull
    @Override
    public TopPicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (activity).inflate (R.layout.item_bottom_activity_topic, parent, false);
        return new TopPicViewHolder (view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopPicViewHolder holder, int position) {
        Word word = words.get (position);

        holder.tvContentTerm.setText (word.getTerm ());
        holder.tvContentDefine.setText (word.getDefine ());

        if (word.isStar ()) {
            holder.ivStar.setImageResource (R.drawable.icon_star);
        } else {
            holder.ivStar.setImageResource (R.drawable.icon_star_border);
        }

        holder.ivStar.setOnClickListener (v -> {
            holder.ivStar.setImageResource (
                (!word.isStar ()) ? R.drawable.icon_star : R.drawable.icon_star_border);
            word.setStar (!word.isStar ());

            if (clickListener != null) {
                clickListener.onClickStarListener (word, position);
            }

            MainActivity.wordVM.updateStarWord (word,
                () -> {
                    activity.runOnUiThread (() -> {
                        Toast.makeText (activity, "Can not handler event choose star on FireStore", Toast.LENGTH_LONG).show ();
                    });
                }, () -> {
                    // handler even success
                });
        });
    }

    @Override
    public int getItemCount() {
        return words.size ();
    }

    public void setListener(setOnItemStarClickListener onClickStarListener) {this.clickListener = onClickStarListener;}
    @SuppressLint("NotifyDataSetChanged")
    public void setWords(List<Word> words) {
        this.words.clear ();
        this.words.addAll (words);
        notifyDataSetChanged ();
    }

    public static class TopPicViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvContentTerm, tvContentDefine;
        private final ImageView ivStar;
        public TopPicViewHolder(@NonNull View itemView) {
            super (itemView);
            tvContentTerm = itemView.findViewById (R.id.tvContentTerm);
            tvContentDefine = itemView.findViewById (R.id.tvContentDefine);
            ivStar = itemView.findViewById (R.id.ivStar);
        }
    }
}
