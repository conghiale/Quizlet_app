package com.nhom12.ourquizlet.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.ViewPager2;

import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.ui.activity.TopicActivity;
import com.nhom12.ourquizlet.data.model.Word;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.List;
import java.util.Objects;

public class TopActivityTopicAdapter extends RecyclerView.Adapter<TopActivityTopicAdapter.TopPicViewHolder> {

    public interface setOnItemClickListener {
        void onClickListener(EasyFlipView flipView, Word word);
    }

    private final ViewPager2 viewPager2;
    private final List<Word> words;
    private Context context;
    private setOnItemClickListener listener;
    private final Handler handler;
    @SuppressLint("NotifyDataSetChanged")
    private final Runnable runnable = new Runnable () {
        @Override
        public void run() {
            words.addAll (words);
            notifyDataSetChanged ();
        }
    };

    public TopActivityTopicAdapter(Context context, ViewPager2 viewPager2, List<Word> words) {
        this.context = context;
        this.viewPager2 = viewPager2;
        this.words = words;
        this.handler = new Handler (Objects.requireNonNull (Looper.getMainLooper ()));
    }

    @NonNull
    @Override
    public TopPicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (parent.getContext ()).inflate (R.layout.item_word, parent, false);
        return new TopPicViewHolder (view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopPicViewHolder holder, int position) {
        if (context instanceof TopicActivity) {
            TopicActivity topicActivity = (TopicActivity)(context);

            int currentPosition = topicActivity.getCurrentPositionViewPager ();

            if (position == currentPosition) {
                topicActivity.setFlipView (holder.cardFlipView);
            }
        }
        Word word = words.get (position);

        holder.tvContentTerm.setText (word.getTerm ());
        holder.tvContentDefine.setText (word.getDefine ());
        holder.tvWordTermStatus.setText (word.getStatus ());
        holder.tvWordDefineStatus.setText (word.getStatus ());

        if (word.getStatus ().equals ("Known")) {
            holder.tvWordTermStatus.setTextColor (ContextCompat.getColor (context, R.color.sweet_garden));
            holder.tvWordDefineStatus.setTextColor (ContextCompat.getColor (context, R.color.sweet_garden));
        } else if (word.getStatus ().equals ("Studying")) {
            holder.tvWordTermStatus.setTextColor (ContextCompat.getColor (context, R.color.yellow));
            holder.tvWordDefineStatus.setTextColor (ContextCompat.getColor (context, R.color.yellow));
        } else {
            holder.tvWordTermStatus.setTextColor (ContextCompat.getColor (context, R.color.synthetic_pumpkin));
            holder.tvWordDefineStatus.setTextColor (ContextCompat.getColor (context, R.color.synthetic_pumpkin));
        }

        if (holder.cardFlipView.getCurrentFlipState () == EasyFlipView.FlipState.FRONT_SIDE && word.isFlipped ()) {
            word.setFlipped (false);
        } else if (holder.cardFlipView.getCurrentFlipState () == EasyFlipView.FlipState.BACK_SIDE && !word.isFlipped ()) {
            holder.cardFlipView.setFlipDuration (0);
            holder.cardFlipView.flipTheView ();
        } else if (holder.cardFlipView.getCurrentFlipState () == EasyFlipView.FlipState.BACK_SIDE && word.isFlipped ()) {
            word.setFlipped (false);
            holder.cardFlipView.setFlipDuration (0);
            holder.cardFlipView.flipTheView ();
        }

        holder.cardFlipView.setOnClickListener (v -> {
            word.setFlipped (!word.isFlipped ());

            holder.cardFlipView.setFlipDuration(400);
            holder.cardFlipView.flipTheView();

            if (listener != null)
                listener.onClickListener (holder.cardFlipView, word);
        });

        if (position == words.size () - 1)
            viewPager2.post (runnable);
    }

    @Override
    public int getItemCount() {
        return words.size ();
    }

    public void setListener(setOnItemClickListener listener) {
        this.listener = listener;
    }

    public static class TopPicViewHolder extends RecyclerView.ViewHolder {

        private final TextView tvContentTerm, tvContentDefine, tvWordTermStatus, tvWordDefineStatus;
        private final EasyFlipView cardFlipView;
        public TopPicViewHolder(@NonNull View itemView) {
            super (itemView);
            tvContentTerm = itemView.findViewById (R.id.tvContentTerm);
            tvContentDefine = itemView.findViewById (R.id.tvContentDefine);
            tvWordTermStatus = itemView.findViewById (R.id.tvWordTermStatus);
            tvWordDefineStatus = itemView.findViewById (R.id.tvWordDefineStatus);
            cardFlipView = itemView.findViewById (R.id.cardFlipView);
        }
    }
}
