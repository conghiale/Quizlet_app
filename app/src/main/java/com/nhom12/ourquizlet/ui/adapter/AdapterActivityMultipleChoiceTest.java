package com.nhom12.ourquizlet.ui.adapter;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.Word;

import java.util.ArrayList;

public class AdapterActivityMultipleChoiceTest extends RecyclerView.Adapter<AdapterActivityMultipleChoiceTest.TopPicViewHolder> {
    public interface setOnItemClickListener {
        void onClickListener(AppCompatButton buttonPressed, ArrayList<AppCompatButton> buttons, String idTerm);
    }
    private setOnItemClickListener listener;
    private final Context context;
    private final ArrayList<Word> data;

    public AdapterActivityMultipleChoiceTest(Context context, ArrayList<Word> data) {
        this.context = context;
        this.data = data;
    }

    @NonNull
    @Override
    public TopPicViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from (context).inflate (R.layout.item_multiple_choice_test, parent, false);
        return new TopPicViewHolder (view);
    }

    @Override
    public void onBindViewHolder(@NonNull TopPicViewHolder holder, int position) {
        Word word = data.get (position);

        holder.tvQuestion.setText (word.getTerm ());
        holder.btnAnswer1.setText (word.getAnswer1 ().split (", ")[0]);
        holder.btnAnswer2.setText (word.getAnswer2 ().split (", ")[0]);
        holder.btnAnswer3.setText (word.getAnswer3 ().split (", ")[0]);
        holder.btnAnswer4.setText (word.getAnswer4 ().split (", ")[0]);

        ArrayList<AppCompatButton> buttons = new ArrayList<> ();
        buttons.add (holder.btnAnswer1);
        buttons.add (holder.btnAnswer2);
        buttons.add (holder.btnAnswer3);
        buttons.add (holder.btnAnswer4);

        for (AppCompatButton button : buttons) {
            button.setOnClickListener (v -> {
                if (listener != null)
                    listener.onClickListener (button, buttons, word.getId ());

            });
        }
    }

    @Override
    public int getItemCount() {
        return data.size ();
    }

    public void setListener(setOnItemClickListener listener) {this.listener = listener;}

    public static class TopPicViewHolder extends RecyclerView.ViewHolder {
        private final TextView tvQuestion;
        private final AppCompatButton btnAnswer1;
        private final AppCompatButton btnAnswer2;
        private final AppCompatButton btnAnswer3;
        private final AppCompatButton btnAnswer4;

        public TopPicViewHolder(@NonNull View itemView) {
            super (itemView);
            tvQuestion = itemView.findViewById (R.id.tvTerm);
            btnAnswer1 = itemView.findViewById (R.id.btnAnswer1);
            btnAnswer2 = itemView.findViewById (R.id.btnAnswer2);
            btnAnswer3 = itemView.findViewById (R.id.btnAnswer3);
            btnAnswer4 = itemView.findViewById (R.id.btnAnswer4);
        }
    }
}
