package com.nhom12.ourquizlet.ui.adapter;

import android.app.Activity;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.textfield.TextInputEditText;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.Word;

import java.util.ArrayList;

public class CreateTopicAdapter extends RecyclerView.Adapter<CreateTopicAdapter.CreateTopicViewHolder> {

    public interface OnClickItemListener {
        void onIVDeleteWordClickListener(int position);
    }

    private final Activity activity;
    private final ArrayList<Word> words;
    private OnClickItemListener onClickItemListener;

    public CreateTopicAdapter(Activity activity, ArrayList<Word> words) {
        this.activity = activity;
        this.words = words;
    }

    @NonNull
    @Override
    public CreateTopicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (activity).inflate (R.layout.item_create_topic, parent, false);
        return new CreateTopicViewHolder (view);
    }

    @Override
    public void onBindViewHolder(@NonNull CreateTopicViewHolder holder, int position) {
        Word word = words.get (position);

        holder.etTerm.setText (word.getTerm () == null ? "" : word.getTerm ());
        holder.etDefine.setText (word.getDefine () == null ? "" : word.getDefine ());

        holder.ivDeleteWord.setOnClickListener (v -> {
            if (onClickItemListener != null)
                onClickItemListener.onIVDeleteWordClickListener (position);
        });

        holder.etTerm.addTextChangedListener (new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                words.get (holder.getAdapterPosition ()).setTerm (s.toString ());
            }

            @Override
            public void afterTextChanged(Editable s) {
            }
        });

        holder.etDefine.addTextChangedListener (new TextWatcher () {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {
                words.get (holder.getAdapterPosition ()).setDefine (s.toString ());
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });
    }

    @Override
    public int getItemCount() {
        return words.size ();
    }

    public void setOnClickItemListener(OnClickItemListener onClickItemListener) {
        this.onClickItemListener = onClickItemListener;
    }

    public static class CreateTopicViewHolder extends RecyclerView.ViewHolder {
        private final TextInputEditText etTerm, etDefine;
        private final ImageView ivDeleteWord;

        public CreateTopicViewHolder(@NonNull View itemView) {
            super (itemView);
            etTerm = itemView.findViewById (R.id.etTerm);
            etDefine = itemView.findViewById (R.id.etDefine);
            ivDeleteWord = itemView.findViewById (R.id.ivDeleteWord);
        }
    }
}
