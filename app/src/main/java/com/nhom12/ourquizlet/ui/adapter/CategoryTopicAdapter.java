package com.nhom12.ourquizlet.ui.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.CategoryTopic;

import java.util.List;

public class CategoryTopicAdapter extends RecyclerView.Adapter<CategoryTopicAdapter.TopPicViewHolder> {

    public interface setOnItemClickListener {
        void onClickListener(CategoryTopic categoryTopic, int position);
    }

    private final Context context;
    private setOnItemClickListener listener;
    private final List<CategoryTopic> categoryTopics;
    private FirebaseFirestore db;

    public CategoryTopicAdapter(Context context, List<CategoryTopic> categoryTopics) {
        this.context = context;
        this.categoryTopics = categoryTopics;
        this.db = FirebaseFirestore.getInstance ();
    }

    @NonNull
    @Override
    public TopPicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.item_category_topic, parent, false);
        return new TopPicViewHolder (view);
    }

    @SuppressLint({"SetTextI18n"})
    @Override
    public void onBindViewHolder(@NonNull TopPicViewHolder holder, int position) {
        CategoryTopic categoryTopic = categoryTopics.get (position);

        holder.tvCategoryTopic.setText (categoryTopic.getTitle ());

        db.collection ("topics_2")
                .whereEqualTo ("idCategory", categoryTopic.getId ())
                .get ()
                .addOnSuccessListener (queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty ())
                        holder.tvNumberTopic.setText (0 + " topics");
                    else
                        holder.tvNumberTopic.setText (queryDocumentSnapshots.size () + " topics");
                });

        holder.itemView.setOnClickListener (v -> {
            if (listener != null)
                listener.onClickListener (categoryTopic, position);
        });
    }

    @Override
    public int getItemCount() {
        return categoryTopics.size ();
    }

    public void setListener(setOnItemClickListener listener) {this.listener = listener;}

    public static class TopPicViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvCategoryTopic;
        private final TextView tvNumberTopic;

        public TopPicViewHolder(@NonNull View itemView) {
            super (itemView);
            tvCategoryTopic = itemView.findViewById (R.id.tvCategoryTopic);
            tvNumberTopic = itemView.findViewById (R.id.tvNumberTopic);
        }
    }
}
