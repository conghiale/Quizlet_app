package com.nhom12.ourquizlet.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom12.ourquizlet.databinding.ItemInTopicBinding;
import com.nhom12.ourquizlet.data.model.Word;

import java.util.List;

public class WordAdapter extends RecyclerView.Adapter<WordAdapter.WordViewHolder> {
    private Context context;
    private List<Word> listWord;

    public WordAdapter(Context context) {
        this.context = context;
    }

    @SuppressLint("NotifyDataSetChanged")
    public void setData(List<Word> listWord) {
        this.listWord = listWord;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public WordViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        ItemInTopicBinding itemInTopicBinding = ItemInTopicBinding.inflate(LayoutInflater.from(parent.getContext()),
                parent,
                false);
        return new WordAdapter.WordViewHolder(itemInTopicBinding);
    }

    @Override
    public void onBindViewHolder(@NonNull WordViewHolder holder, int position) {
        Word word = listWord.get(position);
        holder.bind(word);
    }

    @Override
    public int getItemCount() {
        return listWord.size();
    }

    public static class WordViewHolder extends RecyclerView.ViewHolder {
        private ItemInTopicBinding itemInTopicBinding;
        public WordViewHolder(ItemInTopicBinding itemInTopicBinding) {
            super(itemInTopicBinding.getRoot());
            this.itemInTopicBinding = itemInTopicBinding;
        }

        public void bind (Word word) {
            itemInTopicBinding.tvEng.setText(word.getTerm ());
            itemInTopicBinding.tvViet.setText(word.getDefine ());
        }
    }
}