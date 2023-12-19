package com.nhom12.ourquizlet.ui.adapter;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.LinearLayout;
import android.widget.ProgressBar;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;


import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.ui.activity.FillInWordActivity;
import com.nhom12.ourquizlet.ui.activity.MultipleChoiceTestActivity;
import com.nhom12.ourquizlet.data.model.UserAnswer;
import com.nhom12.ourquizlet.data.model.Word;

import java.util.ArrayList;

public class AdapterResultOverview extends RecyclerView.Adapter<RecyclerView.ViewHolder> {
    public interface SetOnButtonLearnNewTopicClickListener {
        void onClickListener(View v);
    }

    private SetOnButtonLearnNewTopicClickListener onButtonLearnNewTopicClickListener;
    private final String activityName;
    private final String idCategory;
    private final String idTopic;
    private final Activity activity;
    private final ArrayList<Object> data;
    private final ArrayList<UserAnswer> userAnswers;
    private final int TYPE_FEEDBACK = 1;
    private final int TYPE_YOUR_ANSWER = 2;
    private final int TYPE_NEXT_STEP = 3;
    private final int TYPE_TERM_REVIEW = 4;

    public AdapterResultOverview(Activity activity, ArrayList<Object> data, ArrayList<UserAnswer> userAnswers, String activityName, String idCategory, String idTopic) {
        this.activity = activity;
        this.data = data;
        this.userAnswers = userAnswers;
        this.activityName = activityName;
        this.idCategory = idCategory;
        this.idTopic = idTopic;
    }

    @NonNull
    @Override
    public RecyclerView.ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        if (viewType == TYPE_FEEDBACK) {
            View view = LayoutInflater.from (activity).inflate (R.layout.item_multiple_choice_test_feedback, parent, false);
            return new FeedbackViewHolder (view);
        } else if (viewType == TYPE_YOUR_ANSWER) {
            View view = LayoutInflater.from (activity).inflate (R.layout.item_multiple_choice_your_answer_overview, parent, false);
            return new YourAnswerOverviewViewHolder (view);
        } else if (viewType == TYPE_NEXT_STEP){
            View view = LayoutInflater.from (activity).inflate (R.layout.item_multiple_choice_test_next_step, parent, false);
            return new NextStepViewHolder (view);
        } else {
            View view = LayoutInflater.from (activity).inflate (R.layout.item_multiple_choice_test_your_term_review, parent, false);
            return new TermReviewViewHolder (view);
        }
    }

    @SuppressLint("SetTextI18n")
    @Override
    public void onBindViewHolder(@NonNull RecyclerView.ViewHolder holder, int position) {
        Object o = data.get (position);

        if (o == null)
            return;

        long countCorrect = userAnswers.stream ().filter (UserAnswer::isCorrect)
                .count ();

        if (holder.getItemViewType () == TYPE_FEEDBACK) {
            FeedbackViewHolder feedbackViewHolder = (FeedbackViewHolder) holder;
            int percent =  (int) countCorrect * 100 / userAnswers.size ();

            if (percent == 100) {
                feedbackViewHolder.titleFeedback.setText ("Congratulation");
                feedbackViewHolder.subTitleFeedback.setText ("You have done very well. Try to maintain good vocabulary in the next times you study the topic!!!");
            } else if (percent > 75) {
                feedbackViewHolder.titleFeedback.setText ("You've done quite well");
                feedbackViewHolder.subTitleFeedback.setText ("You are about to pass this test. Try to study a little more. Try your best!!!");
            } else if (percent > 50) {
                feedbackViewHolder.titleFeedback.setText ("You have tried very hard");
                feedbackViewHolder.subTitleFeedback.setText ("Your level is average. More studying is required to pass this test!!!");
            } else if (percent > 25) {
                feedbackViewHolder.titleFeedback.setText ("Oh no. It's so sad!!!");
                feedbackViewHolder.subTitleFeedback.setText ("It seems your vocabulary is not very good. Please study harder and try again!!!");
            } else {
                feedbackViewHolder.titleFeedback.setText ("Oh, so sad!!!");
                feedbackViewHolder.subTitleFeedback.setText ("Your vocabulary is too poor. Try to study hard to get better!!!");
            }

        } else if (holder.getItemViewType () == TYPE_YOUR_ANSWER) {
            YourAnswerOverviewViewHolder yourAnswerOverviewViewHolder = (YourAnswerOverviewViewHolder) holder;
            int percent = (int)((countCorrect * 100) / (userAnswers.size ()));
            yourAnswerOverviewViewHolder.percentYourResult.setProgress (percent);
            yourAnswerOverviewViewHolder.tvPercentScore.setText (percent + "%");
            yourAnswerOverviewViewHolder.tvCountAnswerFalse.setText (String.valueOf (userAnswers.size () - countCorrect));
            yourAnswerOverviewViewHolder.tvCountAnswerTrue.setText (String.valueOf (countCorrect));

        } else if (holder.getItemViewType () == TYPE_NEXT_STEP) {
            NextStepViewHolder nextStepViewHolder = (NextStepViewHolder) holder;
            if (countCorrect == userAnswers.size ())
                nextStepViewHolder.btnRedoIncorrectly.setVisibility (View.GONE);
            else {
                nextStepViewHolder.btnRedoIncorrectly.setText ("Redo " + (userAnswers.size () - countCorrect) + " incorrectly answered terms");
                nextStepViewHolder.btnRedoIncorrectly.setOnClickListener (v -> {
                    ArrayList<Word> originWords = new ArrayList<> ();
                    for (Object object : data) {
                        if (object instanceof Word)
                            originWords.add ((Word) object);
                    }

                    Intent intent;
                    intent = this.activityName.equals ("FillInWordActivity") ?
                            new Intent (activity, FillInWordActivity.class) :
                            new Intent (activity, MultipleChoiceTestActivity.class);
//                    intent.putParcelableArrayListExtra ("DATA", originWords);
                    intent.putExtra ("CATEGORY_ID", idCategory);
                    intent.putExtra ("TOPIC_ID", idTopic);
                    intent.putExtra ("IS_REDO", true);
                    activity.startActivity (intent);
                    activity.finish ();
                });
            }

            nextStepViewHolder.btnLearnNewTopic.setOnClickListener (v -> {
                if (onButtonLearnNewTopicClickListener != null) {
                    onButtonLearnNewTopicClickListener.onClickListener (v);
                }
            });

        } else {
            TermReviewViewHolder termReviewViewHolder = (TermReviewViewHolder) holder;
            Word word = (Word) data.get (position);

            UserAnswer userAnswer = userAnswers.stream().filter (userAnswer1 ->
                            userAnswer1.getIdWord ().equals (word.getId ()))
                    .findFirst ().orElse (null);

            if (userAnswer != null && !userAnswer.isCorrect ()) {
                termReviewViewHolder.containerYourAnswerFalse.setVisibility (View.VISIBLE);
                termReviewViewHolder.tvTerm.setText (word.getTerm ());
                termReviewViewHolder.tvAnswerTrue.setText (word.getDefine ());
                termReviewViewHolder.tvAnswerFalse.setText (userAnswer.getAnswer ());
                termReviewViewHolder.tvTitleYourAnswer.setBackgroundColor (activity.getResources ().getColor (R.color.colorError));
                termReviewViewHolder.tvTitleYourAnswer.setText ("False");
                termReviewViewHolder.tvTitleYourAnswer.setCompoundDrawablesRelativeWithIntrinsicBounds (R.drawable.baseline_close, 0, 0, 0);
                termReviewViewHolder.containerTermReview.setBackgroundResource (R.drawable.background_card_false);
            } else {
                termReviewViewHolder.containerYourAnswerFalse.setVisibility (View.GONE);
                termReviewViewHolder.tvTerm.setText (word.getTerm ());
                termReviewViewHolder.tvAnswerTrue.setText (word.getDefine ());
                termReviewViewHolder.tvTitleYourAnswer.setBackgroundColor (activity.getResources ().getColor (R.color.thumb_on));
                termReviewViewHolder.tvTitleYourAnswer.setText ("True");
                termReviewViewHolder.tvTitleYourAnswer.setCompoundDrawablesRelativeWithIntrinsicBounds (R.drawable.baseline_done, 0, 0, 0);
                termReviewViewHolder.containerTermReview.setBackgroundResource (R.drawable.background_card_true);
            }
        }
    }

    @Override
    public int getItemCount() {
        if (data != null)
            return data.size ();
        return 0;
    }

    @Override
    public int getItemViewType(int position) {
        Object o = data.get (position);
        if (o instanceof Integer) {
            int type = (Integer) o;
            if (type == 0)
                return TYPE_FEEDBACK;
            else if (type == 1)
                return TYPE_YOUR_ANSWER;
            else
                return TYPE_NEXT_STEP;
        } else
            return TYPE_TERM_REVIEW;
    }

    public void setOnButtonLearnNewTopicClickListener(SetOnButtonLearnNewTopicClickListener onButtonLearnNewTopicClickListener) {this.onButtonLearnNewTopicClickListener = onButtonLearnNewTopicClickListener;}

    public static class FeedbackViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleFeedback, subTitleFeedback;

        public FeedbackViewHolder(@NonNull View itemView) {
            super (itemView);
            titleFeedback = itemView.findViewById (R.id.titleFeedback);
            subTitleFeedback = itemView.findViewById (R.id.subTitleFeedback);
        }
    }

    public static class YourAnswerOverviewViewHolder extends RecyclerView.ViewHolder {
        private final ProgressBar percentYourResult;
        private final TextView tvPercentScore, tvCountAnswerTrue, tvCountAnswerFalse;

        public YourAnswerOverviewViewHolder(@NonNull View itemView) {
            super (itemView);
            percentYourResult = itemView.findViewById (R.id.percentYourResult);
            tvPercentScore = itemView.findViewById (R.id.tvPercentScore);
            tvCountAnswerTrue = itemView.findViewById (R.id.tvCountWordComplete);
            tvCountAnswerFalse = itemView.findViewById (R.id.tvCountAnswerFalse);
        }
    }

    public static class NextStepViewHolder extends RecyclerView.ViewHolder {
        private final AppCompatButton btnRedoIncorrectly, btnLearnNewTopic;

        public NextStepViewHolder(@NonNull View itemView) {
            super (itemView);
            btnRedoIncorrectly = itemView.findViewById (R.id.btnRedoIncorrectly);
            btnLearnNewTopic = itemView.findViewById (R.id.btnLearnNewTopic);
        }
    }

    public static class TermReviewViewHolder extends RecyclerView.ViewHolder {
        private final LinearLayout containerTermReview, containerYourAnswerTrue, containerYourAnswerFalse;
        private final TextView tvAnswerTrue, tvAnswerFalse, tvTitleYourAnswer, tvTerm;

        public TermReviewViewHolder(@NonNull View itemView) {
            super (itemView);
            containerTermReview = itemView.findViewById (R.id.containerTermReview);
            containerYourAnswerTrue = itemView.findViewById (R.id.containerYourAnswerTrue);
            containerYourAnswerFalse = itemView.findViewById (R.id.containerYourAnswerFalse);
            tvAnswerTrue = itemView.findViewById (R.id.tvAnswerTrue);
            tvAnswerFalse = itemView.findViewById (R.id.tvAnswerFalse);
            tvTerm = itemView.findViewById (R.id.tvTerm);
            tvTitleYourAnswer = itemView.findViewById (R.id.tvTitleYourAnswer);
        }
    }
}
