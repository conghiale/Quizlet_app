package com.nhom12.ourquizlet.ui.activity;

import android.animation.AnimatorInflater;
import android.animation.AnimatorSet;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.Editable;
import android.text.TextWatcher;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.github.ybq.android.spinkit.style.ChasingDots;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.databinding.ActivityFillInWordBinding;
import com.nhom12.ourquizlet.ui.adapter.AdapterResultOverview;
import com.nhom12.ourquizlet.data.model.UserAnswer;
import com.nhom12.ourquizlet.data.model.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class FillInWordActivity extends AppCompatActivity {

    private ActivityFillInWordBinding binding;
    private Handler handler;
    private ExecutorService executorService;
    private ArrayList<Word> words;
    private ArrayList<Word> originalWords;
    private ArrayList<Word> wordsList;
    private ArrayList<UserAnswer> userAnswers;
    private String languageDefine;
    private String idTopic;
    private String idCategory;
    private boolean isFront;
    private boolean isRedo;
    private boolean isFinish = false;
    private boolean isShuffle = false;
    private boolean isStar = false;
    private int numberWords;
    private int count;
    private int wordIndex;
    private final View.OnClickListener onClickListener = v -> {
        if (v == binding.btnFinish) {
            isFinish = true;
//            binding.fillInWord.setBackgroundColor (ContextCompat.getColor (this, R.color.background));
            binding.container.setVisibility (View.GONE);
            binding.ivTextToSpeechBack.setVisibility (View.GONE);
            binding.progressBar2.setIndeterminateDrawable (new ChasingDots ());
            binding.progressBar2.setVisibility (View.VISIBLE);

            long knownWord = originalWords.stream().filter (w -> w.getStatus ().equals ("Known")).count ();
            long studyingWord = originalWords.stream().filter (w -> w.getStatus ().equals ("Studying")).count ();
            int percent = (int)((knownWord * 2 + studyingWord) * 100) / (originalWords.size () * 2);

            Log.d ("TAG", "knownWord: " + knownWord);
            Log.d ("TAG", "studyingWord: " + studyingWord);
            Log.d ("TAG", "percent: " + percent);

            count += 1;
            MainActivity.topicVM.updatePercentCountTopic (idTopic, percent, count,
                    () -> runOnUiThread (() -> Toast.makeText (this, "Can not update PERCENT of this topic", Toast.LENGTH_LONG).show ()),
                    () -> {
                        for (Word w : words) {
                            MainActivity.wordVM.updateStatusWord (w,
                                    () -> runOnUiThread (() -> Toast.makeText (this, "Can not update STATUS of WORD_ID: " + w.getId (), Toast.LENGTH_LONG).show ()),
                                    () -> {});
                        }
                        runOnUiThread (() -> {
                            binding.progressBar2.setVisibility (View.GONE);
                            binding.recyclerview.setVisibility (View.VISIBLE);});
                    });
        } else {
            if (v == binding.btnNext) {
                wordIndex++;
                Word nextWord = words.get (wordIndex);

                if (isFront)
                    binding.tvBack.setText (nextWord.getTerm ().trim ().split (", ")[0]);
                else
                    binding.tvFront.setText (nextWord.getTerm ().trim ().split (", ")[0]);
                createAnimatorFlip (binding.cvFront, binding.cvBack);

                UserAnswer userAnswer = userAnswers.stream().filter (
                        userAnswer1 -> userAnswer1.getIdWord ().equals (nextWord.getId ())).findFirst ().orElse (null);
                if (userAnswer == null)
                    binding.etEnterDefine.setText ("");
                else
                    binding.etEnterDefine.setText (userAnswer.getAnswer ().trim ());
            }

            if (v == binding.btnPrevious) {
                wordIndex--;
                Word previousWord = words.get (wordIndex);

                if (isFront)
                    binding.tvBack.setText (previousWord.getTerm ().trim ().split (", ")[0]);
                else
                    binding.tvFront.setText (previousWord.getTerm ().trim ().split (", ")[0]);
                createAnimatorFlip (binding.cvFront, binding.cvBack);

                UserAnswer userAnswer = userAnswers.stream().filter (
                        userAnswer1 -> userAnswer1.getIdWord ().equals (previousWord.getId ())).findFirst ().orElse (null);
                if (userAnswer == null)
                    binding.etEnterDefine.setText ("");
                else
                    binding.etEnterDefine.setText (userAnswer.getAnswer ().trim ());
            }

            binding.tvCountTerm.setText (String.valueOf (wordIndex + 1));
            binding.progressBar.setProgress (wordIndex + 1);

            if (wordIndex == (words.size () - 1)) {
                binding.btnNext.setVisibility (View.GONE);
                binding.btnFinish.setVisibility (View.VISIBLE);
            } else {
                binding.btnNext.setVisibility (View.VISIBLE);
                binding.btnFinish.setVisibility (View.GONE);
            }

            if (wordIndex == 0)
                binding.btnPrevious.setEnabled (false);
            else
                binding.btnPrevious.setEnabled (true);
        }
    };

    private final TextWatcher onChange = new TextWatcher () {
        @Override
        public void beforeTextChanged(CharSequence s, int start, int count, int after) {}

        @Override
        public void onTextChanged(CharSequence s, int start, int before, int count) {
            String yourAnswer = binding.etEnterDefine.getText ().toString ().trim ();
            Word wordCurrent = words.get (wordIndex);
            UserAnswer userAnswer = userAnswers.stream ().filter (userAnswer1 ->
                            userAnswer1.getIdWord ().equals (wordCurrent.getId ()))
                    .findFirst ().orElse (null);

            if (userAnswer == null) {
                userAnswer = new UserAnswer (wordCurrent.getId (), yourAnswer);
                userAnswers.add (userAnswer);
            } else
                userAnswer.setAnswer (yourAnswer);


            String answer = wordCurrent.getDefine ().trim ().toLowerCase ();
            String userEnter = userAnswer.getAnswer ().trim ().toLowerCase ();

            userAnswer.setCorrect (answer.contains (userEnter));
            if (answer.contains (userEnter)) {
                userAnswer.setCorrect(true);
                wordCurrent.setStatus ("Known");
            } else {
                userAnswer.setCorrect(false);
                wordCurrent.setStatus ("Studying");
            }

        }

        @Override
        public void afterTextChanged(Editable s) {
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityFillInWordBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        init();
    }

    private void init() {
//        binding.fillInWord.setBackgroundColor (ContextCompat.getColor (this, R.color.white));
        handler = new Handler (Objects.requireNonNull (Looper.getMainLooper ()));
        userAnswers = new ArrayList<> ();
        words = new ArrayList<> ();
        wordsList = new ArrayList<> ();
        wordIndex = 0;
        binding.btnPrevious.setEnabled (false);
        isFront = true;

        count = getIntent ().getIntExtra ("COUNT", 0);

        isShuffle = SetUpStudyActivity.setUpStudyVM.isShuffle ();
        isStar = SetUpStudyActivity.setUpStudyVM.isStar ();
        languageDefine = SetUpStudyActivity.setUpStudyVM.getLanguageDefine ();
        numberWords = SetUpStudyActivity.setUpStudyVM.getNumberWords ();
        idTopic = SetUpStudyActivity.setUpStudyVM.getIdTopic ();
        idCategory = SetUpStudyActivity.setUpStudyVM.getIdCategory ();

        Intent intent = getIntent ();
        if (intent != null) {
            isRedo = intent.getBooleanExtra ("IS_REDO", false);
        }

        executorService = Executors.newFixedThreadPool (5);
        CompletableFuture<Void> completableFuture  = CompletableFuture.runAsync (() -> {
            words = initData ();
        }, executorService);

        completableFuture.thenRun (() -> {
            if (isRedo) {
                words.removeIf (w -> !w.getStatus ().equals ("Studying"));
            }

            numberWords = words.size ();

            binding.tvCountTerm.setText (String.valueOf (wordIndex + 1));
            binding.tvTotalTerm.setText (String.valueOf (words.size ()));

            binding.progressBar.setMax (numberWords);

            binding.tvBack.setText (words.get (wordIndex).getTerm ().trim ().split (", ")[0]);
            binding.tvFront.setText (words.get (wordIndex).getTerm ().trim ().split (", ")[0]);

            if (languageDefine.equals ("vietnamese")) {
                binding.etEnterDefine.setHint ("Định nghĩa");
                binding.ivTextToSpeechBack.setVisibility (View.VISIBLE);
                binding.ivTextToSpeechFront.setVisibility (View.VISIBLE);
            } else {
                binding.etEnterDefine.setHint ("Define");
                binding.ivTextToSpeechBack.setVisibility (View.GONE);
                binding.ivTextToSpeechFront.setVisibility (View.GONE);
            }

            ArrayList<Object> data2 = new ArrayList<> ();
            data2.add (0);
            data2.add (1);
            data2.add (2);
            data2.addAll (words);
            AdapterResultOverview adapterResult = new AdapterResultOverview (this, data2, userAnswers, "FillInWordActivity", idCategory, idTopic);
            adapterResult.setOnButtonLearnNewTopicClickListener (this::adapterResultOnClickListener);

            binding.recyclerview.setHasFixedSize (true);
            binding.recyclerview.setLayoutManager (new LinearLayoutManager (this, LinearLayoutManager.VERTICAL, false));
            binding.recyclerview.addItemDecoration (new DividerItemDecoration (this, LinearLayoutManager.VERTICAL));
            binding.recyclerview.setAdapter (adapterResult);

            if (words.size () == (wordIndex + 1)) {
                binding.btnNext.setVisibility (View.GONE);
                binding.btnFinish.setVisibility (View.VISIBLE);
            }

            executorService.shutdown();
        });

        binding.ivCloseToolbar.setOnClickListener (v -> {
            if (isFinish) {
                getOnBackPressedDispatcher ().onBackPressed ();
                finish ();
            } else {
                showWarningAlertDialog ("Your study progress will be canceled if you cancel.", () -> {
                    getOnBackPressedDispatcher ().onBackPressed ();
                    finish ();
                });
            }
        });

        binding.btnPrevious.setOnClickListener (onClickListener);
        binding.btnNext.setOnClickListener (onClickListener);
        binding.btnFinish.setOnClickListener (onClickListener);
        binding.etEnterDefine.addTextChangedListener (onChange);
    }

    private ArrayList<Word> initData() {
        List<Word> words = MainActivity.wordVM.getWordsOfTopic (idTopic);
        ArrayList<Word> data;
        originalWords = new ArrayList<> ();

        for (Word w : words) {
//            Log.d ("TAG", "initData- originalWords: " + w.getTerm () + " - " + w.getStatus () + " - " + w.isStar ());
            originalWords.add (w.clone ());
        }

        if (isStar)
            data = originalWords.stream ().filter (Word::isStar).collect (Collectors.toCollection (ArrayList::new));
        else
            data = new ArrayList<> (originalWords);

        if (languageDefine.equals ("english")) {
            for (Word word : data) {
                String term = word.getTerm ();
                String define = word.getDefine ();

                word.setTerm (define);
                word.setDefine (term);
            }
        }

//        Log.d ("TAG", "initData: isShuffle: " + isShuffle);
//        Log.d ("TAG", "initData: isStar: " + isStar);
//        Log.d ("TAG", "initData: data: " + data.size ());

        if (isShuffle)
            Collections.shuffle (data);

        if (isStar) {
            return data;
        } else
            return new ArrayList<> (data.subList (0, numberWords));
    }

    private void createAnimatorFlip(View cvFlashCardFront, View cvFlashCardBack) {
        float scale = getApplicationContext ().getResources ().getDisplayMetrics ().density;
        cvFlashCardFront.setCameraDistance (8000 * scale);
        cvFlashCardBack.setCameraDistance (8000 * scale);

        AnimatorSet frontAnim = (AnimatorSet) AnimatorInflater.loadAnimator (getApplicationContext (),
                R.animator.front_card_animator);
        AnimatorSet backAnim = (AnimatorSet) AnimatorInflater.loadAnimator (getApplicationContext (),
                R.animator.back_card_animator);

        if (isFront) {
            frontAnim.setTarget (cvFlashCardFront);
            backAnim.setTarget (cvFlashCardBack);
            frontAnim.start ();
            backAnim.start ();
            isFront = false;
        } else {
            frontAnim.setTarget (cvFlashCardBack);
            backAnim.setTarget (cvFlashCardFront);
            frontAnim.start ();
            backAnim.start ();
            isFront = true;
        }
    }

    private void adapterResultOnClickListener (View v) {
        Intent intent = new Intent (this, TopicsActivity.class);
        intent.putExtra ("CATEGORY_TOPIC_ID", idCategory);
        startActivity (intent);
        finish ();
    }

    private void showWarningAlertDialog(String message, Runnable runnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder (this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (this).inflate (
                R.layout.layout_warning_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Warning");
        ((TextView) view.findViewById (R.id.tvMessage)).setText (message);
        ((AppCompatButton) view.findViewById (R.id.btnActionNo)).setText ("No");
        ((AppCompatButton) view.findViewById (R.id.btnActionYes)).setText ("Yes");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_warning);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnActionNo).setOnClickListener (v -> {
            alertDialog.dismiss ();
        });

        view.findViewById (R.id.btnActionYes).setOnClickListener (v -> {
            runnable.run ();
            alertDialog.dismiss ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }
}
