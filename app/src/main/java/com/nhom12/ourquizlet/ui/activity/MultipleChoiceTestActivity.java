package com.nhom12.ourquizlet.ui.activity;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.github.ybq.android.spinkit.style.ChasingDots;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.databinding.ActivityMultipleChoiceTestBinding;
import com.nhom12.ourquizlet.ui.adapter.AdapterActivityMultipleChoiceTest;
import com.nhom12.ourquizlet.ui.adapter.AdapterResultOverview;
import com.nhom12.ourquizlet.data.model.UserAnswer;
import com.nhom12.ourquizlet.data.model.Word;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.Random;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;

public class MultipleChoiceTestActivity extends AppCompatActivity {

    private ActivityMultipleChoiceTestBinding binding;
    private Handler handler;
    private ExecutorService executorService;
    private TextToSpeech textToSpeech ;
    private AdapterActivityMultipleChoiceTest adapter;
    private AdapterResultOverview adapterResult;
    private ArrayList<Word> words;

    private ArrayList<Word> originalWords;
    private ArrayList<UserAnswer> userAnswers;
    private String idTopic;
    private String idCategory;
    private String languageDefine;
    private int numberWords;
    private int count;
    private boolean isFinish = false;
    private boolean isShuffle = false;
    private boolean isStar = false;
    private boolean isReady;
    private boolean isRedo;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityMultipleChoiceTestBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        init();


        setUpTransformer ();

        binding.viewPager2.registerOnPageChangeCallback (new ViewPager2.OnPageChangeCallback () {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected (position);
            }

            @Override
            public void onPageScrolled(int position, float positionOffset, int positionOffsetPixels) {
                super.onPageScrolled (position, positionOffset, positionOffsetPixels);
            }
        });
    }

    @SuppressLint("NotifyDataSetChanged")
    private void init() {
        userAnswers = new ArrayList<> ();
        words = new ArrayList<> ();
        originalWords = new ArrayList<> ();
        handler = new Handler (Objects.requireNonNull (Looper.myLooper ()));

        count = getIntent ().getIntExtra ("COUNT", 0);

        isShuffle = SetUpStudyActivity.setUpStudyVM.isShuffle ();
        isStar = SetUpStudyActivity.setUpStudyVM.isStar ();
        languageDefine = SetUpStudyActivity.setUpStudyVM.getLanguageDefine ();
        numberWords = SetUpStudyActivity.setUpStudyVM.getNumberWords ();
        idTopic = SetUpStudyActivity.setUpStudyVM.getIdTopic ();
        idCategory = SetUpStudyActivity.setUpStudyVM.getIdCategory ();

        Log.d ("TAG", "init: isShuffle: " + isShuffle);
        Log.d ("TAG", "init: isStar: " + isStar);
        Log.d ("TAG", "init: languageDefine: " + languageDefine);
        Log.d ("TAG", "init: numberWords: " + numberWords);
        Log.d ("TAG", "init: idTopic: " + idTopic);
        Log.d ("TAG", "init: idCategory: " + idCategory);

        Intent intent = getIntent ();
        if (intent != null) {
            isRedo = intent.getBooleanExtra ("IS_REDO", false);
        }

        executorService = Executors.newFixedThreadPool (5);
        CompletableFuture<Void> completableFuture  = CompletableFuture.runAsync (() -> {
            words = initData ();
            Log.d ("TAG", "131 - init: words: " + words.size ());
        }, executorService);

        completableFuture.thenRun (() -> {
            if (isRedo) {
                words.removeIf (w -> !w.getStatus ().equals ("Studying"));
            }

            Log.d ("TAG", "139 - init: words: " + words.size ());
            numberWords = words.size ();

            binding.tvCountTerm.setText (String.valueOf (binding.viewPager2.getCurrentItem () + 1));
            binding.tvTotalTerm.setText (String.valueOf (numberWords));

            binding.progressBar.setMax (numberWords);

            if (languageDefine.equals ("vietnamese")) {
                binding.ivTextToSpeech.setVisibility (View.VISIBLE);
                if (textToSpeech == null) {
                    textToSpeech = new TextToSpeech (getApplicationContext (), status -> {
                        if (status == TextToSpeech.SUCCESS) {
                            setTextToSpeechLanguage ();
                        }
                    });
                }
            } else {
                binding.ivTextToSpeech.setVisibility (View.GONE);
            }

            adapter = new AdapterActivityMultipleChoiceTest (this, words);
            adapter.setListener ((buttonPressed, buttons, idTerm) -> {
                buttonPressed.setBackgroundResource (R.drawable.background_answer_pressed);
                for (AppCompatButton button1 : buttons) {
                    button1.setEnabled (false);
                }
                handler.removeCallbacks (runnable (buttonPressed, buttons, idTerm)); //isAnswer, question.getAnswer ()
                handler.postDelayed (runnable (buttonPressed, buttons, idTerm), 600);
            });

            binding.viewPager2.setLayoutDirection (View.LAYOUT_DIRECTION_LTR);
            binding.viewPager2.setAdapter (adapter);
            binding.viewPager2.setOffscreenPageLimit (3);
            binding.viewPager2.setClipToPadding (false);
            binding.viewPager2.setClipChildren (false);
            binding.viewPager2.getChildAt (0).setOverScrollMode (RecyclerView.OVER_SCROLL_NEVER);

            ArrayList<Object> data2 = new ArrayList<> ();
            data2.add (0);
            data2.add (1);
            data2.add (2);
            data2.addAll (words);
            adapterResult = new AdapterResultOverview (this, data2, userAnswers, "MultipleChoiceTestActivity", idCategory, idTopic);
            adapterResult.setOnButtonLearnNewTopicClickListener (this::adapterResultOnClickListener);

            binding.recyclerview.setHasFixedSize (true);
            binding.recyclerview.setLayoutManager (new LinearLayoutManager (this, LinearLayoutManager.VERTICAL, false));
            binding.recyclerview.addItemDecoration (new DividerItemDecoration (this, LinearLayoutManager.VERTICAL));
            binding.recyclerview.setAdapter (adapterResult);

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

        binding.ivTextToSpeech.setOnClickListener (v -> {
            speakOut ();
        });
    }

    private void setTextToSpeechLanguage() {
        Locale language = Locale.ENGLISH;

        int result = textToSpeech.setLanguage(language);
        if (result == TextToSpeech.LANG_MISSING_DATA) {
            this.isReady = false;
            Toast.makeText(this, "Missing language data", Toast.LENGTH_SHORT).show();
        } else if (result == TextToSpeech.LANG_NOT_SUPPORTED) {
            this.isReady = false;
            Toast.makeText(this, "Language not supported", Toast.LENGTH_SHORT).show();
        } else {
            this.isReady = true;
            Locale currentLanguage = textToSpeech.getVoice().getLocale();
            Toast.makeText(this, "Language " + currentLanguage, Toast.LENGTH_SHORT).show();
        }
    }

    private void speakOut() {
        if (!isReady) {
            Toast.makeText(this, "Text to Speech not ready", Toast.LENGTH_LONG).show();
            return;
        }

        // Văn bản cần đọc.
        Word word = words.get (binding.viewPager2.getCurrentItem ());
        String toSpeak = word.getTerm ();
        String utteranceId = UUID.randomUUID().toString();
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    private void setUpTransformer() {
        CompositePageTransformer transformer = new CompositePageTransformer ();
        transformer.addTransformer (new MarginPageTransformer (50));
        transformer.addTransformer ((page, position) -> {
            float r = 1 - abs(position);
            page.setScaleY (0.85f + r * 0.14f);
        });

        binding.viewPager2.setPageTransformer (transformer);
    }

    private Runnable runnable(AppCompatButton btn, ArrayList<AppCompatButton> buttons, String idWord) { //boolean isAnswer, String answer
        return () -> {
            int currentItem = binding.viewPager2.getCurrentItem ();

            Word word = words.stream().filter (term1 -> term1.getId ().equals (idWord))
                    .findFirst ().orElse (null);

//            Lay dap an nguoi dung chon
            UserAnswer userAnswer = new UserAnswer (idWord, btn.getText ().toString ().trim ());
            if (word != null && word.getDefine ().contains (btn.getText ().toString ().trim ())) {
                btn.setBackgroundResource (R.drawable.background_answer_true);
                word.setStatus ("Known");
            } else {
                for (int i = 0; i < buttons.size (); i++) {
                    if (word != null && buttons.get (i).getText ().toString ().trim ().equals (word.getDefine ())) {
                        userAnswer.setNumberAnswer (i);
                        userAnswer.setCorrect (false);
                        word.setStatus ("Studying");
                        buttons.get (i).setBackgroundResource (R.drawable.background_answer_false);
                    }
                }
            }

            userAnswers.add (userAnswer);
            if (currentItem < numberWords - 1) {
                handler.removeCallbacks (runnable2 (buttons));
                handler.postDelayed (runnable2 (buttons), 600);
            } else {
                handler.removeCallbacks (runnable3);
                handler.postDelayed (runnable3, 600);
            }
        };
    }

    private Runnable runnable2(ArrayList<AppCompatButton> buttons) {
        return () -> {
            for (AppCompatButton button : buttons) {
                button.setEnabled (true);
                button.setBackgroundResource (R.drawable.background_answer);
            }
            binding.viewPager2.setCurrentItem (binding.viewPager2.getCurrentItem () + 1);
            binding.tvCountTerm.setText (String.valueOf (binding.viewPager2.getCurrentItem () + 1));
            binding.progressBar.setProgress (binding.viewPager2.getCurrentItem () + 1);
        };
    }

    private final Runnable runnable3 = () -> {
        isFinish = true;
        binding.viewPager2.setVisibility (View.GONE);
        binding.ivTextToSpeech.setVisibility (View.GONE);
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
    };

    private void adapterResultOnClickListener (View v) {
        Intent intent = new Intent (this, TopicsActivity.class);
        intent.putExtra ("CATEGORY_TOPIC_ID", idCategory);
        startActivity (intent);
        finish ();
    }

    @SuppressLint("NotifyDataSetChanged")
    private ArrayList<Word> initData() {
        List<Word> wordList = MainActivity.wordVM.getWordsOfTopic (idTopic);
        ArrayList<Word> data;
        originalWords = new ArrayList<> ();

        for (Word w : wordList) {
//            Log.d ("TAG", "initData - originalWords: " + w.getTerm () + " - " + w.getDefine () + " - " + w.isStar () + " - " + w.getStatus ());
            originalWords.add (w.clone ());
        }

//        Log.d ("TAG", "345 - initData - isStar: " + isStar);
        if (isStar)
            data = originalWords.stream ().filter (Word::isStar).collect (Collectors.toCollection (ArrayList::new));
        else
            data = new ArrayList<> (originalWords);

        if (languageDefine.equals ("vietnamese")) {
            for (Word word : data) {
                ArrayList<String> originalDefine = new ArrayList<> ();
                for (Word v : originalWords) {
                    if (!v.getDefine ().equals (word.getDefine ())) {
                        originalDefine.add (v.getDefine ());
                    }
                }

                Collections.shuffle (originalDefine);

                ArrayList<String> randomDefines = new ArrayList<> ();
                Random random = new Random ();
                do {
                    int index = random.nextInt (originalDefine.size ());
                    String randomTerm = originalDefine.get (index);

                    if (!randomDefines.contains (randomTerm))
                        randomDefines.add (randomTerm);

                } while (randomDefines.size () < 3);

                createAnswers(word, randomDefines);
            }
        } else {
            for (Word word : data) {
                String term = word.getTerm ();

                ArrayList<String> originalTerms = new ArrayList<> ();
                for (Word v : originalWords) {
                    if (!v.getTerm ().equals (term)) {
                        originalTerms.add (v.getTerm ());
                    }
                }

                Collections.shuffle (originalTerms);

                ArrayList<String> randomTerms = new ArrayList<> ();
                Random random = new Random ();
                do {
                    int index = random.nextInt (originalTerms.size ());
                    String randomTerm = originalTerms.get (index);

                    if (!randomTerms.contains (randomTerm)) {
                        randomTerms.add (randomTerm);
                    }

                } while (randomTerms.size () < 3);

                createAnswers (word, randomTerms);
            }

            for (Word word : data) {
                String term = word.getTerm ();
                String define = word.getDefine ();

                word.setTerm (define);
                word.setDefine (term);
            }
        }

        if (isShuffle)
            Collections.shuffle (data);

//        Log.d ("TAG", "413 - initData: data: " + data.size ());

        if (isStar) {
            return data;
        } else
            return new ArrayList<> (data.subList (0, numberWords));
    }

    private void createAnswers(Word word, ArrayList<String> randomAnswers) {
        String define = (languageDefine.equals ("english") ? word.getTerm () : word.getDefine ());
        Random random = new Random ();
        int randomAnswer = random.nextInt (4);
        switch (randomAnswer) {
            case 0:
                word.setAnswer1 (define);
                break;
            case 1:
                word.setAnswer2 (define);
                break;
            case 2:
                word.setAnswer3 (define);
                break;
            default:
                word.setAnswer4 (define);
        }

        for (int i = 0; i < 3; i++) {
            if (word.getAnswer1 () == null || word.getAnswer1 ().equals (""))
                word.setAnswer1 (randomAnswers.get (i));
            else if (word.getAnswer2 () == null || word.getAnswer2 ().equals (""))
                word.setAnswer2 (randomAnswers.get (i));
            else if (word.getAnswer3 () == null || word.getAnswer3 ().equals (""))
                word.setAnswer3 (randomAnswers.get (i));
            else
                word.setAnswer4 (randomAnswers.get (i));
        }
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
