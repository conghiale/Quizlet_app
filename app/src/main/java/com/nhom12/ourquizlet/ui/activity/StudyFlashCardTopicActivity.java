package com.nhom12.ourquizlet.ui.activity;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.speech.tts.TextToSpeech;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.utils.widget.ImageFilterView;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.github.ybq.android.spinkit.style.ChasingDots;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.databinding.ActivityStudyTopicFlashCardBinding;
import com.nhom12.ourquizlet.ui.adapter.StudyFCAdapter;
import com.nhom12.ourquizlet.data.model.Word;
import com.nhom12.ourquizlet.ui.viewModel.StudyFCViewModel;
import com.nhom12.ourquizlet.ui.viewModel.Topic2ViewModel;
import com.nhom12.ourquizlet.ui.viewModel.WordViewModel;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Objects;
import java.util.UUID;

public class StudyFlashCardTopicActivity extends AppCompatActivity {

    private ActivityStudyTopicFlashCardBinding binding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    private ViewPager2 viewPager2;
    private StudyFCAdapter adapter;
    private List<Word> words;
    private List<Word> wordList;
    private Handler handler;
    private EasyFlipView flipView;
    private StudyFCViewModel studyFCVM;
    private WordViewModel wordVM;
    private Topic2ViewModel topicVM;
    private TextToSpeech textToSpeech ;
    private boolean isReady;
    private boolean isPlay = false;
    private boolean isRunnable = false;
    private boolean isShuffle = false;
    private boolean isMakeSound = false;
    private boolean isUpdate = false;
    private boolean isFinish = false;
    private boolean isStar = false;
    private int numberWord;
    private int count;
    private String idTopic;
    private String idCategory;
    private String frontSide = "TERM";

    private final Runnable runnable = new Runnable () {
        @Override
        public void run() {
            isRunnable = true;
            if (isPlay && (viewPager2.getCurrentItem () != Objects.requireNonNull (viewPager2.getAdapter ()).getItemCount () - 1)) {
                    viewPager2.setCurrentItem (viewPager2.getCurrentItem () + 1);
            }
        }
    };

    private final Runnable runnable2 = new Runnable () {
        @Override
        public void run() {
            if (flipView != null) {
                Word word = wordList.get (viewPager2.getCurrentItem ());
                word.setFlipped (!word.isFlipped ()); // thuc hien chuc nang auto flip nen ham đuoc goi thi value luon la false
                flipView.setFlipDuration (400);
                flipView.flipTheView ();

                checkDisplayIconTTS ();

                handler.removeCallbacks (runnable);
                handler.postDelayed (runnable, 3000);
            }
            if (isPlay) {
                handler.postDelayed (runnable3, 3000);
            }
        }
    };

    private final Runnable runnable3 = new Runnable () {
        @Override
        public void run() {
            if (isPlay) {
                Word word = wordList.get (viewPager2.getCurrentItem ());
                if (word.isFlipped ()) {
                    handler.removeCallbacks (runnable);
                    handler.postDelayed (runnable, 3000);
                } else {
                    handler.removeCallbacks (runnable2);
                    handler.postDelayed (runnable2, 3000);
                }
            }
        }
    };

    @SuppressLint("SetTextI18n")
    private final View.OnClickListener onClickListener = v -> {
        if (v == binding.ivUndoTerm) {
              if (viewPager2.getCurrentItem () > 0 ){
                  viewPager2.setCurrentItem (viewPager2.getCurrentItem () - 1);

                  if (isPlay) {
                      adapter.notifyItemChanged (viewPager2.getCurrentItem ());
                  }
              }
          }

        if (v == binding.ivPlayTerm) {
              Word word = wordList.get (viewPager2.getCurrentItem ());
              binding.ivPlayTerm.setImageResource (!isPlay ? R.drawable.baseline_pause : R.drawable.baseline_play_arrow);
              if (!isPlay) {
                  isPlay = true;
                  handler.removeCallbacks (runnable);
                  handler.removeCallbacks (runnable2);
                  handler.removeCallbacks (runnable3);

                  if (word.isFlipped ()) {
                      handler.postDelayed (runnable, 3000);
                  } else {
                      handler.postDelayed (runnable2, 3000);
                  }
              } else {
                  isPlay = false;
                  handler.removeCallbacks (runnable);
                  handler.removeCallbacks (runnable2);
                  handler.removeCallbacks (runnable3);
              }
          }

        if (v == binding.ivCloseToolbar) {
            if (isFinish) {
                // error
                getOnBackPressedDispatcher ().onBackPressed ();
                finish ();
            } else {
                showWarningAlertDialog ("Your study progress will be canceled if you cancel.", () -> {
                    getOnBackPressedDispatcher ().onBackPressed ();
                    finish ();
                });
            }
          }

        if (v == binding.ivSettingToolbar) {
              isPlay = false;
              binding.ivPlayTerm.setImageResource (R.drawable.baseline_play_arrow);
              showBottomDialogSetupFlashCard();
          }

        if (v == binding.tvStudying) {
            Word word = wordList.get (viewPager2.getCurrentItem ());
            word.setStatus ("Studying");
            if (viewPager2.getCurrentItem () != Objects.requireNonNull (viewPager2.getAdapter ()).getItemCount () - 1) {
                viewPager2.setCurrentItem (viewPager2.getCurrentItem () + 1);
            }
            binding.tvStudying.setText (String.valueOf (Integer.parseInt (binding.tvStudying.getText ().toString ().trim ()) + 1));
        }

        if (v == binding.tvKnown) {
            Word word = wordList.get (viewPager2.getCurrentItem ());
            word.setStatus ("Known");
            if (viewPager2.getCurrentItem () != Objects.requireNonNull (viewPager2.getAdapter ()).getItemCount () - 1) {
                viewPager2.setCurrentItem (viewPager2.getCurrentItem () + 1);
            }
            binding.tvKnown.setText (String.valueOf (Integer.parseInt (binding.tvKnown.getText ().toString ().trim ()) + 1));
        }

        if (v == binding.btnFinishLesson) {
            isFinish = true;
            binding.containerStudyFlashCard.setVisibility (View.GONE);
            binding.countNumberTerm.setVisibility (View.GONE);
            binding.ivTextToSpeech.setVisibility (View.GONE);

            long knownWord = wordList.stream().filter (word -> word.getStatus ().equals ("Known")).count ();
            long studyingWord = wordList.stream().filter (word -> word.getStatus ().equals ("Studying")).count ();
            long notKnownWord = numberWord - (knownWord + studyingWord);

            int percent = (int)((knownWord * 2 + studyingWord) * 100) / (numberWord*2);
//                feedback
                if (percent < 50) {
                    binding.titleFeedback.setText ("Oh no, that's so sad");
                    binding.subTitleFeedback.setText ("Too bad. The amount of vocabulary you have " +
                              "learned is too small. Try to study better.");
                } else if (percent > 50 && percent < 100) {
                    binding.titleFeedback.setText ("Congratulation on your completion");
                    binding.subTitleFeedback.setText ("You still have a few words you haven't learned " +
                              "yet. Try to study better to complete all the words");
                } else {
                    binding.titleFeedback.setText ("Congratulation on your completion");
                    binding.subTitleFeedback.setText ("You have completed all the words");
                }

//            Your results
            binding.tvPercentScore.setText (percent + "%");
            binding.percentYourResult.setProgress (percent);
            binding.tvCountWordKnown.setText (String.valueOf (knownWord));
            binding.tvCountWordStudying.setText (String.valueOf (studyingWord));
            binding.tvCountWordNotKnown.setText (String.valueOf (notKnownWord));

            binding.progressBar.setIndeterminateDrawable (new ChasingDots ());
            binding.progressBar.setVisibility (View.VISIBLE);

            count += 1;
            Log.d ("TAG", "StudyFlashCardTopicActivity: line241 - count: " + count);
            MainActivity.topicVM.updatePercentCountTopic (idTopic, percent, count,
                    () -> runOnUiThread (() -> Toast.makeText (this, "Can not update PERCENT of this topic", Toast.LENGTH_LONG).show ()),
                    () -> {
                        for (Word w : wordList) {
                            MainActivity.wordVM.updateStatusWord (w,
                                () -> runOnUiThread (() -> Toast.makeText (this, "Can not update STATUS of WORD_ID: " + w.getId (), Toast.LENGTH_LONG).show ()),
                                () -> {});
                        }
                        runOnUiThread (() -> {
                            binding.progressBar.setVisibility (View.GONE);
                            binding.containerTotalResult.setVisibility (View.VISIBLE);});
                        });
        }

        if (v == binding.btnBackLesson) {
            getOnBackPressedDispatcher ().onBackPressed ();
            finish ();
        }

        if (v == binding.btnLearnNewTopic) {
            Intent intent = new Intent (this, TopicsActivity.class);
            intent.putExtra ("CATEGORY_TOPIC_ID", idCategory);
            startActivity (intent);
            finish ();
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityStudyTopicFlashCardBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        setSupportActionBar (binding.toolbar);
        Objects.requireNonNull (getSupportActionBar ()).setTitle ("");

        viewPager2 = binding.viewPager2;

        binding.containerTotalResult.setVisibility (View.GONE);
        binding.containerStudyFlashCard.setVisibility (View.VISIBLE);
        binding.countNumberTerm.setVisibility (View.VISIBLE);
        binding.ivTextToSpeech.setVisibility (View.VISIBLE);

        studyFCVM = new ViewModelProvider (this).get (StudyFCViewModel.class);
        wordVM = new ViewModelProvider (this).get (WordViewModel.class);
        topicVM = new ViewModelProvider (this).get (Topic2ViewModel.class);

        isShuffle = studyFCVM.isShuffle ();
        isMakeSound = studyFCVM.isMakeSound ();
        frontSide = studyFCVM.getFrontSide ();
        isUpdate = studyFCVM.isUpdated ();

        init ();

        binding.ivUndoTerm.setOnClickListener (onClickListener);
        binding.ivPlayTerm.setOnClickListener (onClickListener);
        binding.ivCloseToolbar.setOnClickListener (onClickListener);
        binding.ivSettingToolbar.setOnClickListener (onClickListener);
        binding.btnFinishLesson.setOnClickListener (onClickListener);
        binding.ivCloseToolbar.setOnClickListener (onClickListener);
        binding.tvStudying.setOnClickListener (onClickListener);
        binding.tvKnown.setOnClickListener (onClickListener);
        binding.btnBackLesson.setOnClickListener (onClickListener);
        binding.btnLearnNewTopic.setOnClickListener (onClickListener);

        setUpTransformer();

        viewPager2.registerOnPageChangeCallback (new ViewPager2.OnPageChangeCallback () {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected (position);
                binding.tvCountTerm.setText (String.valueOf (position + 1));
                if (position == 0)
                    binding.ivUndoTerm.setColorFilter(Color.parseColor ("#808e9b")); //(getContext().getResources().getColor(R.color.blue));
                else
                    binding.ivUndoTerm.setColorFilter(getApplicationContext ().getResources ().getColor (R.color.white));
                binding.ivUndoTerm.setEnabled (position != 0);

                if (textToSpeech == null) {
                    textToSpeech = new TextToSpeech (getApplicationContext (), status -> {
                        if (status == TextToSpeech.SUCCESS) {
                            setTextToSpeechLanguage ();
                        }
                    });
                } else
                    checkDisplayIconTTS(); // luon luon visible

                if (isPlay) {
                    handler.removeCallbacks (runnable);
                    handler.removeCallbacks (runnable2);
                    handler.removeCallbacks (runnable3);

                    handler.postDelayed (runnable2, 3000);
                }

                if (position > 0)
                    isRunnable = true;

                if (isPlay) {
                    if (position != 0) {
                        adapter.notifyItemChanged(position);
                    }
                } else {
                    if (isRunnable)
                        adapter.notifyItemChanged(position);
                }

                if (position == wordList.size () - 1)
                    binding.btnFinishLesson.setVisibility (View.VISIBLE);
            }

            @Override
            public void onPageScrollStateChanged(int state) {
                super.onPageScrollStateChanged (state);

            }
        });

    }

    @SuppressLint("NotifyDataSetChanged")
    private void init() {
        db = FirebaseFirestore.getInstance ();
        auth = FirebaseAuth.getInstance ();
        words = new ArrayList<> ();
        wordList = new ArrayList<> ();
        idTopic = getIntent ().getStringExtra ("TOPIC_ID");
        numberWord = getIntent ().getIntExtra ("NUMBER_WORD", 0);
        count = getIntent ().getIntExtra ("COUNT", 0);
        idCategory = getIntent ().getStringExtra ("CATEGORY_ID");
        isStar = getIntent ().getBooleanExtra ("IS_STAR", false);

        binding.tvTotalTerm.setText (String.valueOf (numberWord));

        this.words.clear ();
        for (Word w : MainActivity.wordVM.getWordsOfTopic (idTopic)) {
            if (isStar) {
                if (w.isStar ())
                    this.words.add (w.clone ());
            } else
                this.words.add (w.clone ());
        }

        if (isUpdate) {
            updateTermsFlashCard ();
        } else {
            wordList = new ArrayList<> (this.words);
        }

        runOnUiThread (() -> {
            adapter = new StudyFCAdapter (this, wordList);
            handler = new Handler (Objects.requireNonNull (Looper.getMainLooper ()));
            adapter.setListener ((flipView, word) -> {
                this.flipView = flipView;
                checkDisplayIconTTS ();
                if (isPlay) {
                    handler.removeCallbacks (runnable);
                    handler.removeCallbacks (runnable2);

                    if (word.isFlipped ()) {
                        handler.removeCallbacks (runnable);
                        handler.postDelayed (runnable, 3000);
                    } else {
//                            word.setFlipped (true);
                        handler.removeCallbacks (runnable2);
                        handler.postDelayed (runnable2, 3000);
                    }
                }
            });

            viewPager2.setLayoutDirection (View.LAYOUT_DIRECTION_LTR);
            viewPager2.setAdapter (adapter);
            viewPager2.setOffscreenPageLimit (3);
            viewPager2.setClipToPadding (false);
            viewPager2.setClipChildren (false);
            viewPager2.getChildAt (0).setOverScrollMode (RecyclerView.OVER_SCROLL_NEVER);
        });

//        MainActivity.wordVM.getWordAll ().observe (this, words -> {
//            this.words.clear ();
//            for (Word w : words) {
//                if (w.getIdTopic ().equals (idTopic)) {
//                    this.words.add (w.clone ());
//                }
//            }
//
//            if (isUpdate) {
//                updateTermsFlashCard ();
//            } else {
//                wordList = new ArrayList<> (this.words);
//            }
//
//            runOnUiThread (() -> {
//                adapter = new StudyFCAdapter (this, wordList);
//                handler = new Handler (Objects.requireNonNull (Looper.getMainLooper ()));
//                adapter.setListener ((flipView, word) -> {
//                    this.flipView = flipView;
//                    checkDisplayIconTTS ();
//                    if (isPlay) {
//                        handler.removeCallbacks (runnable);
//                        handler.removeCallbacks (runnable2);
//
//                        if (word.isFlipped ()) {
//                            handler.removeCallbacks (runnable);
//                            handler.postDelayed (runnable, 3000);
//                        } else {
////                            word.setFlipped (true);
//                            handler.removeCallbacks (runnable2);
//                            handler.postDelayed (runnable2, 3000);
//                        }
//                    }
//                });
//
//                viewPager2.setLayoutDirection (View.LAYOUT_DIRECTION_LTR);
//                viewPager2.setAdapter (adapter);
//                viewPager2.setOffscreenPageLimit (3);
//                viewPager2.setClipToPadding (false);
//                viewPager2.setClipChildren (false);
//                viewPager2.getChildAt (0).setOverScrollMode (RecyclerView.OVER_SCROLL_NEVER);
//            });
//        });

        binding.ivTextToSpeech.setOnClickListener (v -> {
            speakOut();
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

        checkDisplayIconTTS ();
    }

    private void speakOut() {
        if (!isReady) {
            Toast.makeText(this, "Text to Speech not ready", Toast.LENGTH_LONG).show();
            return;
        }

        // Văn bản cần đọc.
        Word word = wordList.get (viewPager2.getCurrentItem ());
        String toSpeak = word.getTerm ();
        String utteranceId = UUID.randomUUID().toString();
        textToSpeech.speak(toSpeak, TextToSpeech.QUEUE_FLUSH, null, utteranceId);
    }

    @Override
    protected void onPause() {
        super.onPause ();
        if (isRunnable)
            handler.removeCallbacks (runnable);
    }

    @Override
    protected void onResume() {
        super.onResume ();
        if (isRunnable)
            handler.postDelayed (runnable, 2000);
    }

    private void setUpTransformer() {
        CompositePageTransformer transformer = new CompositePageTransformer ();
        transformer.addTransformer (new MarginPageTransformer(40));
        transformer.addTransformer ((page, position) -> {
            float r = 1 - abs(position);
            page.setScaleY (0.85f + r * 0.14f);
        });

        viewPager2.setPageTransformer (transformer);
    }

    private void showBottomDialogSetupFlashCard() {
        final Dialog dialog = new Dialog (this);
        dialog.requestWindowFeature (Window.FEATURE_NO_TITLE);
        dialog.setContentView (R.layout.bottom_sheet_flash_card);

        Window window = dialog.getWindow ();
        if (window == null)
            return;

        ImageFilterView ivShuffle = dialog.findViewById (R.id.ivShuffle);
        ImageFilterView ivMakeSound = dialog.findViewById (R.id.ivMakeSound);
        AppCompatButton btnDefine = dialog.findViewById (R.id.btnDefine);
        AppCompatButton btnTerm = dialog.findViewById (R.id.btnTerm);
        TextView tvReplaceFlashCard = dialog.findViewById (R.id.tvReplaceFlashCard);

        ivShuffle.setColorFilter (isShuffle ? Color.WHITE : Color.parseColor ("#808e9b"));
        ivMakeSound.setColorFilter (isMakeSound ? Color.WHITE : Color.parseColor ("#808e9b"));
        btnTerm.setBackgroundResource (frontSide.equals ("TERM") ? R.drawable.background_btn_03 : R.drawable.background_btn_04);
        btnDefine.setBackgroundResource (frontSide.equals ("DEFINE") ? R.drawable.background_btn_03 : R.drawable.background_btn_04);

        ivShuffle.setOnClickListener (v -> {
            studyFCVM.setShuffle (!isShuffle);
            isShuffle = !isShuffle;
            ivShuffle.setColorFilter (isShuffle ? Color.WHITE : Color.parseColor ("#808e9b"));
        });

        ivMakeSound.setOnClickListener (v -> {
            studyFCVM.setMakeSound (!isMakeSound);
            isMakeSound = !isMakeSound;
            ivMakeSound.setColorFilter (isMakeSound ? Color.WHITE : Color.parseColor ("#808e9b"));
        });

        btnTerm.setOnClickListener (v -> {
            studyFCVM.setFrontSide ("TERM");
            frontSide = "TERM";
            btnTerm.setBackgroundResource (R.drawable.background_btn_03);
            btnDefine.setBackgroundResource (R.drawable.background_btn_04);
        });

        btnDefine.setOnClickListener (v -> {
            studyFCVM.setFrontSide ("DEFINE");
            frontSide = "DEFINE";
            btnDefine.setBackgroundResource (R.drawable.background_btn_03);
            btnTerm.setBackgroundResource (R.drawable.background_btn_04);
        });

        tvReplaceFlashCard.setOnClickListener (v -> {
            studyFCVM.setUpdated (true);
            recreate();
            dialog.dismiss ();
        });

        dialog.show ();
        dialog.setCancelable (true);

        window.setLayout (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable (new ColorDrawable (Color.TRANSPARENT));

        WindowManager.LayoutParams windowAttributes = window.getAttributes ();
        windowAttributes.windowAnimations = R.style.DialogAnimation;
        windowAttributes.gravity = Gravity.BOTTOM;
        window.setAttributes (windowAttributes);
    }

    private void updateTermsFlashCard() {
        if (frontSide.equals ("TERM")) {
            wordList = new ArrayList<> (words);
        } else {
            wordList.clear ();
            List<Word> originalList = new ArrayList<> (words);

            for (Word word : originalList) {

                // Lấy title của item hiện tại
                String title = word.getTerm ();
                String answer = word.getDefine ();

                // Tao term moi
                Word t = new Word ();
                t.setId (word.getId ());
                t.setTerm (answer);
                t.setDefine (title);

                wordList.add (t);
            }
        }

        if (isShuffle)
            Collections.shuffle (wordList);

        isUpdate = false;
    }

    public int getCurrentPositionViewPager() {
        return viewPager2.getCurrentItem ();
    }

    public void checkDisplayIconTTS() {

        Word word = wordList.get (viewPager2.getCurrentItem ());
        if (frontSide.equals ("TERM") && !word.isFlipped ()) {
            binding.ivTextToSpeech.setVisibility (View.VISIBLE);
            if (isMakeSound)
                speakOut ();
        }
        else if (frontSide.equals ("DEFINE") && word.isFlipped ()) {
            binding.ivTextToSpeech.setVisibility (View.VISIBLE);
            if (isMakeSound)
                speakOut ();
        } else
            binding.ivTextToSpeech.setVisibility (View.GONE);
    }
    public void setFlipView(EasyFlipView flipView) {
        this.flipView = flipView;
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
