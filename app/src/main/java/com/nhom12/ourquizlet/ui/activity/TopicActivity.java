package com.nhom12.ourquizlet.ui.activity;

import static java.lang.Math.abs;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.core.content.ContextCompat;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.viewpager2.widget.CompositePageTransformer;
import androidx.viewpager2.widget.MarginPageTransformer;
import androidx.viewpager2.widget.ViewPager2;

import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.ui.adapter.BottomActivityTopicAdapter;
import com.nhom12.ourquizlet.ui.adapter.TopActivityTopicAdapter;
import com.nhom12.ourquizlet.databinding.ActivityTopicBinding;
import com.nhom12.ourquizlet.data.model.Topic;
import com.nhom12.ourquizlet.data.model.Word;
import com.nhom12.ourquizlet.ui.viewModel.WordViewModel;
import com.nhom12.ourquizlet.ui.viewModel.Topic2ViewModel;
import com.wajahatkarim3.easyflipview.EasyFlipView;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;

public class TopicActivity extends AppCompatActivity {

    private ActivityTopicBinding binding;
    private TopActivityTopicAdapter topActivityTopicAdapter;
    private BottomActivityTopicAdapter bottomActivityTopicAdapter;
    private List<Word> words;
    private List<Word> wordList;
    private List<Word> wordsStar;
    private WordViewModel wordVM;
    private FirebaseFirestore db;
    private String idTopic;
    private String idCategory;
    private int numberWord;
    private int count;
    private ViewPager2 viewPager2;
    private Handler handler;
    private Topic2ViewModel topicVM;
    private boolean isRunnable = false;
    private boolean isStar = false;
    private EasyFlipView flipView;
    private final Runnable runnable = new Runnable () {
        @Override
        public void run() {
            isRunnable = true;
            if (viewPager2.getCurrentItem () != Objects.requireNonNull (viewPager2.getAdapter ()).getItemCount () - 1) {
                viewPager2.setCurrentItem (viewPager2.getCurrentItem () + 1);
            }
        }
    };

    private final Runnable runnable2 = new Runnable () {
        @Override
        public void run() {
            if (flipView != null) {

                Word word = words.get (viewPager2.getCurrentItem ());
                word.setFlipped (!word.isFlipped ());
                flipView.setFlipDuration (400);
                flipView.flipTheView ();

                handler.removeCallbacks (runnable);
                handler.postDelayed (runnable, 3000);
            }
        }
    };

    private final View.OnClickListener onClickListener = new View.OnClickListener () {
        @SuppressLint("NotifyDataSetChanged")
        @Override
        public void onClick(View v) {
            if (v == binding.containerFlashCard) {
                Intent intent = new Intent (getApplicationContext (), StudyFlashCardTopicActivity.class);
                intent.putExtra ("TOPIC_ID", idTopic);
                intent.putExtra ("NUMBER_WORD", isStar ? wordsStar.size () : wordList.size ());
                intent.putExtra ("CATEGORY_ID", idCategory);
                intent.putExtra ("COUNT", count);
                intent.putExtra ("IS_STAR", isStar);
                startActivity (intent);
            }
            if (v == binding.containerMultipleChoice) {
                numberWord = isStar ? wordsStar.size () : wordList.size ();
                if (numberWord < 4) {
                    showErrorAlertDialog ("The number of vocabulary must be greater than 4");
                } else {
                    Intent intent = new Intent (getApplicationContext (), SetUpStudyActivity.class);
                    intent.putExtra ("STUDY", "MULTIPLE_CHOICE_TEST");
                    intent.putExtra ("TOPIC_ID", idTopic);
                    intent.putExtra ("NUMBER_WORD", isStar ? wordsStar.size () : wordList.size ());
                    intent.putExtra ("CATEGORY_ID", idCategory);
                    intent.putExtra ("COUNT", count);
                    intent.putExtra ("IS_STAR", isStar);
                    startActivity (intent);
                }
            }
            if (v == binding.containerFillInWord) {
                Intent intent = new Intent (getApplicationContext (), SetUpStudyActivity.class);
                intent.putExtra ("STUDY", "FILL_IN_WORD");
                intent.putExtra ("TOPIC_ID", idTopic);
                intent.putExtra ("NUMBER_WORD", isStar ? wordsStar.size () : wordList.size ());
                intent.putExtra ("CATEGORY_ID", idCategory);
                intent.putExtra ("COUNT", count);
                intent.putExtra ("IS_STAR", isStar);
                startActivity (intent);
            }
            if (v == binding.ivUndoToolbar) {
                getOnBackPressedDispatcher ().onBackPressed ();
            }
            if (v == binding.btnStudyAll) {
                binding.btnStudyAll.setBackgroundResource (R.drawable.background_btn_02);
                binding.btnStudyNumber.setBackgroundResource (R.drawable.background_btn_04_radius);
                isStar = false;
                words.clear ();
                words.addAll (wordList);
                topActivityTopicAdapter.notifyDataSetChanged ();
                bottomActivityTopicAdapter.setWords (wordList);
                handler.removeCallbacks (runnable);
                handler.removeCallbacks (runnable2);
                handler.postDelayed (runnable2, 3000);
            }
            if (v == binding.btnStudyNumber) {
                binding.btnStudyAll.setBackgroundResource (R.drawable.background_btn_04_radius);
                binding.btnStudyNumber.setBackgroundResource (R.drawable.background_btn_02);
                isStar = true;
                words.clear ();
                words.addAll (wordsStar);
                topActivityTopicAdapter.notifyDataSetChanged ();
                bottomActivityTopicAdapter.setWords (wordsStar);
                handler.removeCallbacks (runnable);
                handler.removeCallbacks (runnable2);
                handler.postDelayed (runnable2, 3000);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityTopicBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        setSupportActionBar (binding.toolbar);
//        Objects.requireNonNull (getSupportActionBar ()).setTitle (getIntent ().getStringExtra (""));
//        getSupportActionBar ().setDisplayShowTitleEnabled (true);


        init();
        setUpTransformer();

        viewPager2.registerOnPageChangeCallback (new ViewPager2.OnPageChangeCallback () {
            @Override
            public void onPageSelected(int position) {
                super.onPageSelected (position);

                handler.removeCallbacks (runnable);
                handler.removeCallbacks (runnable2);
                handler.postDelayed (runnable2, 3000);

                if (position > 0)
                    isRunnable = true;

                if (isRunnable)
                    topActivityTopicAdapter.notifyItemChanged(position);

            }
        });

        binding.containerFlashCard.setOnClickListener (onClickListener);
        binding.containerMultipleChoice.setOnClickListener (onClickListener);
        binding.containerFillInWord.setOnClickListener (onClickListener);
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

    @SuppressLint("SetTextI18n")
    private void init() {
        wordVM = new ViewModelProvider (this).get (WordViewModel.class);
        topicVM = new ViewModelProvider (this).get (Topic2ViewModel.class);
        viewPager2 = findViewById (R.id.viewPager2);
        RecyclerView recyclerView = findViewById (R.id.recyclerview);
        handler = new Handler (Objects.requireNonNull (Looper.myLooper ()));
        idTopic = getIntent ().getStringExtra ("TOPIC_ID");
        numberWord = getIntent ().getIntExtra ("NUMBER_WORD", 0);
        count = getIntent ().getIntExtra ("COUNT", 0);
        idCategory = getIntent ().getStringExtra ("CATEGORY_ID");
        words = new ArrayList<> ();
        wordList = new ArrayList<> ();
        db = FirebaseFirestore.getInstance ();

        binding.ivUndoToolbar.setOnClickListener (onClickListener);
        binding.tvNumberWord.setText (numberWord + " WORDS");

        MainActivity.topicVM.getTopicAll ().observe (this, topics -> {
            Optional<Topic> topic = topics.stream().filter (topic1 -> topic1.getId ().equals (idTopic)).findFirst ();
            topic.ifPresent (topic1 -> {
                binding.tvTitleTopic.setText (topic1.getTitle ());
                binding.tvUserName.setText (topic1.getUsername ());
            });
        });

        this.wordList = new ArrayList<> (MainActivity.wordVM.getWordsOfTopic (idTopic));
        this.words.addAll (this.wordList);

        topActivityTopicAdapter = new TopActivityTopicAdapter (this, viewPager2, this.words);
        topActivityTopicAdapter.setListener ((flipView, word) -> {
            handler.removeCallbacks (runnable);

            this.flipView = flipView;
            if (word.isFlipped ()) {
                handler.removeCallbacks (runnable);
                handler.postDelayed (runnable, 2000);
            } else {
                word.setFlipped (true);
                handler.removeCallbacks (runnable2);
                handler.postDelayed (runnable2, 2000);
            }
        });

        viewPager2.setAdapter (topActivityTopicAdapter);
        viewPager2.setOffscreenPageLimit (3);
        viewPager2.setClipToPadding (false);
        viewPager2.setClipChildren (false);
        viewPager2.getChildAt (0).setOverScrollMode (RecyclerView.OVER_SCROLL_NEVER);

        bottomActivityTopicAdapter = new BottomActivityTopicAdapter (wordList, this);
        bottomActivityTopicAdapter.setListener (this::onClickStarListener);

        recyclerView.setHasFixedSize (true);
        recyclerView.setNestedScrollingEnabled (false);
        recyclerView.setLayoutManager (new LinearLayoutManager (this, LinearLayoutManager.VERTICAL, false));
        recyclerView.addItemDecoration (new DividerItemDecoration (this, LinearLayoutManager.VERTICAL));
        recyclerView.setAdapter (bottomActivityTopicAdapter);

        handleContainerButtonStudy ();

        binding.btnStudyAll.setOnClickListener (onClickListener);
        binding.btnStudyNumber.setOnClickListener (onClickListener);
//        MainActivity.wordVM.getWordAll ().observe (this, words -> {
//            for (Word w : words) {
//                if (w.getIdTopic () != null && w.getIdTopic ().equals (idTopic)) {
//                    this.words.add (w);
//                }
//            }
//            wordList.addAll (this.words);
//
//            runOnUiThread (() -> {
//                topActivityTopicAdapter = new TopActivityTopicAdapter (this, viewPager2, this.words);
//                topActivityTopicAdapter.setListener ((flipView, word) -> {
//                    handler.removeCallbacks (runnable);
//
//                    this.flipView = flipView;
//                    if (word.isFlipped ()) {
//                        handler.removeCallbacks (runnable);
//                        handler.postDelayed (runnable, 2000);
//                    } else {
//                        word.setFlipped (true);
//                        handler.removeCallbacks (runnable2);
//                        handler.postDelayed (runnable2, 2000);
//                    }
//                });
//
//                viewPager2.setAdapter (topActivityTopicAdapter);
//                viewPager2.setOffscreenPageLimit (3);
//                viewPager2.setClipToPadding (false);
//                viewPager2.setClipChildren (false);
//                viewPager2.getChildAt (0).setOverScrollMode (RecyclerView.OVER_SCROLL_NEVER);
//
//                BottomActivityTopicAdapter bottomActivityTopicAdapter = new BottomActivityTopicAdapter (wordList, this);
//
//                recyclerView.setHasFixedSize (true);
//                recyclerView.setNestedScrollingEnabled (false);
//                recyclerView.setLayoutManager (new LinearLayoutManager (this, LinearLayoutManager.VERTICAL, false));
//                recyclerView.addItemDecoration (new DividerItemDecoration (this, LinearLayoutManager.VERTICAL));
//                recyclerView.setAdapter (bottomActivityTopicAdapter);
//            });
//        });
    }

    @SuppressLint("SetTextI18n")
    private void handleContainerButtonStudy () {
        if (this.wordList != null) {
            this.wordsStar = this.wordList.stream().filter (Word::isStar).collect(Collectors.toList());
            if (this.wordsStar.size () == 0) {
                Log.d ("TAG", "313 - handleContainerButtonStudy: ");
                binding.containerChooseListWord.setVisibility (View.GONE);
            } else {
                binding.btnStudyNumber.setText ("STUDY " + this.wordsStar.size ());
                binding.containerChooseListWord.setVisibility (View.VISIBLE);
            }
        }
    }

    public void setFlipView(EasyFlipView flipView) {
        this.flipView = flipView;
    }
    public int getCurrentPositionViewPager() {
        return viewPager2.getCurrentItem ();
    }

    @SuppressLint("NotifyDataSetChanged")
    public void onClickStarListener(Word word, int position) {
        handleContainerButtonStudy ();
        if (isStar) {
            if (wordsStar.size () == 0) {
                isStar = false;
                this.words.clear ();
                this.words.addAll (this.wordList);
                bottomActivityTopicAdapter.setWords (wordList);
            } else {
                this.words.clear ();
                this.words.addAll (this.wordsStar);
                bottomActivityTopicAdapter.setWords (wordsStar);
            }

            topActivityTopicAdapter.notifyDataSetChanged ();
            bottomActivityTopicAdapter.notifyDataSetChanged ();
            handler.removeCallbacks (runnable);
            handler.removeCallbacks (runnable2);
            handler.postDelayed (runnable2, 3000);
        }
    }

    private void showErrorAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder (this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (this).inflate (
                R.layout.layout_error_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Error");
        ((TextView) view.findViewById (R.id.tvMessage)).setText (message);
        ((AppCompatButton) view.findViewById (R.id.btnAction)).setText ("Okay");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_error);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnAction).setOnClickListener (v -> {
            alertDialog.dismiss ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }
}
