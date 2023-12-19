package com.nhom12.ourquizlet.ui.activity;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.ArrayAdapter;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.recyclerview.widget.LinearLayoutManager;

import com.google.android.material.textfield.TextInputEditText;
import com.google.firebase.firestore.DocumentSnapshot;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.CategoryTopic;
import com.nhom12.ourquizlet.data.model.Topic;
import com.nhom12.ourquizlet.data.model.Word;
import com.nhom12.ourquizlet.databinding.ActivityCreateEditTopicBinding;
import com.nhom12.ourquizlet.ui.adapter.CreateTopicAdapter;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CreateEditTopicActivity extends AppCompatActivity implements CreateTopicAdapter.OnClickItemListener {
    private ActivityCreateEditTopicBinding binding;
    private ExecutorService executorService;
    private ArrayList<Word> words;
    private CreateTopicAdapter createTopicAdapter;
    private String nameCategory, nameTopic, descriptionTopic, idTopic;
    private ArrayList<String> categories;
    private List<CategoryTopic> categoryTopics;
    private List<Topic> topicsCurrentUser;
    private List<String> idWordsRemove;
    private CategoryTopic categoryTopic;
    private Topic topic;
    private boolean isPublic = false;
    private boolean isEdit = false;
    private boolean isDone = false;
    private final Runnable runnableCreateWords = () -> {
        Log.d ("TAG", "EditTopicActivity - 62: topic.getId () - " + topic.getId ());

        if (topic.getId () != null && !topic.getId ().equals ("")) {

            MainActivity.topicVM.setCurrentTopicsVM (this.topic, isEdit);
            MainActivity.topicVM.setTopicsVM (this.topic, isEdit);

            if (!isEdit) {
//                create
                for (Word word : words) {
                    word.setIdTopic (topic.getId ());
                    MainActivity.wordVM.createWordsInTopic (word)
                        .addOnSuccessListener (documentReference -> {
                            String id = documentReference.getId ();
                            MainActivity.wordVM.updatedIdWord (id)
                                .addOnSuccessListener (unused -> {
                                    word.setId (id);
                                    MainActivity.wordVM.updateWordInUserWord (word, topic.getIdUser ())
                                        .addOnSuccessListener (documentReference1 -> {
                                            MainActivity.wordVM.updateWordsLocal (word, true);
                                            isDone = true;
                                            getOnBackPressedDispatcher ().onBackPressed ();
                                            finish ();
                                            Log.d ("TAG", "CreateTopicActivity - 64 - success - wordID - " + word.getId ());
                                        });
                                });
                        });
                }
            } else {
                if (!idWordsRemove.isEmpty ()) {
                    for (String idWord : idWordsRemove) {
                        MainActivity.wordVM.deleteWordById (idWord)
                            .addOnSuccessListener (unused -> {
                                Log.d ("TAG", "96 - fireStore - words - deleted: wordID: " + idWord);

                                MainActivity.wordVM.getUserWordByIdWord (idWord)
                                    .addOnSuccessListener (queryDocumentSnapshots -> {
                                        for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments ()) {
                                            MainActivity.wordVM.deleteUserWordById (d.getId ())
                                                .addOnSuccessListener (unused1 -> {
                                                    Log.d ("TAG", "103 - fireStore - user_word - deleted: user_wordID: " + d.getId ());
                                                });
                                        }

                                        MainActivity.wordVM.deleteWordByIdLocal (idWord);
                                    });
                            });
                    }
                }

                List<Word> originalWords = MainActivity.wordVM.getWordsOfTopic (idTopic);
                for (Word word : this.words) {
                    word.setIdTopic (topic.getId ());

                    long count = originalWords.stream().filter (word1 -> word1.getId ().equals (word.getId ())).count ();

                    if (count == 0) {
                        MainActivity.wordVM.createWordsInTopic (word)
                            .addOnSuccessListener (documentReference -> {
                                String id = documentReference.getId ();
                                MainActivity.wordVM.updatedIdWord (id)
                                    .addOnSuccessListener (unused -> {
                                        word.setId (id);
                                        MainActivity.wordVM.updateWordInUserWord (word, topic.getIdUser ())
                                            .addOnSuccessListener (documentReference1 -> {
                                                MainActivity.wordVM.updateWordsLocal (word, true);
                                                isDone = true;
                                                getOnBackPressedDispatcher ().onBackPressed ();
                                                finish ();
                                                Log.d ("TAG", "CreateTopicActivity - line64 - success - wordID - " + word.getId ());
                                            });
                                });
                            });
                    } else {
                        MainActivity.wordVM.updateFieldsWord (word)
                            .addOnSuccessListener (documentReference -> {
                                MainActivity.wordVM.updateWordsLocal (word, false);
                                isDone = true;
                                getOnBackPressedDispatcher ().onBackPressed ();
                                finish ();

                                Log.d ("TAG", "UPDATE WORD SUCCESS: ID: " + word.getId ());
                            });
                    }
                }
            }
        }
    };

    private final Runnable runnableCreateTopic = () -> {
        if (checkTopic (topic)) {

            if (!isEdit) {
//                create
                MainActivity.topicVM.createTopic (topic)
                    .addOnSuccessListener (documentReference -> {
                        String id = documentReference.getId ();
                        MainActivity.topicVM.updateIdTopic (id)
                                .addOnSuccessListener (unused -> {
                                    topic.setId (id);
                                    topic.setCount (0);
                                    topic.setPercent (0);
                                    topic.setNumberWord (this.words.size ());
                                    MainActivity.topicVM.updatePercentCountTopic (id, topic.getPercent (), topic.getCount (), () -> {}, runnableCreateWords);
                                });
                    });
            } else {
//                edit
                MainActivity.topicVM.updateFieldsTopic (topic)
                    .addOnSuccessListener (unused -> {
                        runnableCreateWords.run ();
                    });
            }
        }
    };

    private final Runnable runnableDeleteTopic = () -> {
        if (idTopic != null && !idTopic.equals ("")) {
            Log.d ("TAG", "runnableDeleteTopic - 181: idTopic: " + idTopic);
            MainActivity.topicVM.getUserTopicByIdTopic (idTopic)
                .addOnSuccessListener (queryDocumentSnapshots -> {
                    for (DocumentSnapshot d : queryDocumentSnapshots.getDocuments ()) {
                        MainActivity.topicVM.deleteUserTopicById (d.getId ())
                            .addOnSuccessListener (unused -> {
                                Log.d ("TAG", "187 - fireStore - user_topic - deleted: user_topicID: " + d.getId ());
                            });
                    }

                    List<Word> wordList = MainActivity.wordVM.getWordsOfTopic (idTopic);
                    for (Word w : wordList) {
                        MainActivity.wordVM.deleteWordById (w.getId ())
                            .addOnSuccessListener (unused -> {
                                Log.d ("TAG", "195 - fireStore - words - deleted: wordID: " + w.getId ());

                                MainActivity.wordVM.getUserWordByIdWord (w.getId ())
                                    .addOnSuccessListener (queryDocumentSnapshots1 -> {
                                        for (DocumentSnapshot d : queryDocumentSnapshots1.getDocuments ()) {
                                            MainActivity.wordVM.deleteUserWordById (d.getId ())
                                                .addOnSuccessListener (unused1 -> {
                                                    Log.d ("TAG", "202 - fireStore - user_word - deleted: user_wordID: " + d.getId ());
                                                });
                                        }

                                        MainActivity.wordVM.deleteWordByIdLocal (w.getId ());
                                    });
                            });
                    }

                    MainActivity.topicVM.deleteTopicById (this.idTopic)
                        .addOnSuccessListener (unused -> {
                            Log.d ("TAG", "213 - fireStore - topics - deleted: topicID: " + this.idTopic);

                            MainActivity.topicVM.deleteCurrentTopicByIdLocal (this.idTopic);
                            MainActivity.topicVM.deleteTopicAllByIdLocal (this.idTopic);

                            getOnBackPressedDispatcher ().onBackPressed ();
                            finish ();
                        });
                });
        }
    };

    @SuppressLint("NotifyDataSetChanged")
    private final View.OnClickListener onClickListener = v -> {
        if (v == binding.ivBack) {
            if (isDone) {
                getOnBackPressedDispatcher ().onBackPressed ();
                finish ();
            } else {
                showWarningAlertDialog ("Warning", "Your study progress will be canceled if you cancel.", () -> {
                    getOnBackPressedDispatcher ().onBackPressed ();
                    finish ();
                });
            }
        }

        if (v == binding.ivSetting) {
            showBottomDialog ();
        }

        if (v == binding.ivCreate) {
            nameCategory = binding.chooseCategoryTopic.getText ().toString ().trim ();
            nameTopic = Objects.requireNonNull (binding.etNameTopic.getText ()).toString ().trim ();
            descriptionTopic = Objects.requireNonNull (binding.etDescriptionTopic.getText ()).toString ().trim ();

            if (checkContent ()) {
//                this.topic = new Topic ();

                MainActivity.homeVM.getCurrentUser ().observe (this, user -> {
                    if (user != null && !user.getUsername ().equals ("") && !user.getId ().equals ("")) {
                        this.topic.setIdUser (user.getId ());
                        this.topic.setUsername (user.getUsername ());
                    }
                });

                topic.setTitle (nameTopic);
                topic.setDescription (descriptionTopic);
                topic.setPublic (isPublic);

                categoryTopic = new CategoryTopic ();
                categoryTopic.setTitle (nameCategory);

                boolean isExists = false;
                for (String c : categories) {
                    if (c.equals (nameCategory)) {
                        isExists = true;
                        break;
                    }
                }

                if (!isExists) {
                    MainActivity.categoryVM.createCategory (categoryTopic).addOnSuccessListener (documentReference -> {
                        String id = documentReference.getId ();
                        MainActivity.categoryVM.updatedIdCategory (id)
                            .addOnSuccessListener (unused -> {
                                this.categoryTopic.setId (id);
                                this.categoryTopics.add (categoryTopic);

                                MainActivity.categoryVM.setCategories (categoryTopics);

                                if (categoryTopic.getId () != null && !categoryTopic.getId ().equals (""))
                                    this.topic.setIdCategory (categoryTopic.getId ());

                                runnableCreateTopic.run ();
                                Log.d ("TAG", "line 125: topicIDCategory - " + topic.getIdCategory ());
                            });
                    });

                } else {
                    for (CategoryTopic c : categoryTopics) {
                        if (c.getTitle ().equals (nameCategory)) {
                            this.topic.setIdCategory (c.getId ());
                            this.topic.setNumberWord (this.words.size ());
                            runnableCreateTopic.run ();
                            break;
                        }
                    }
                }
            }
        }

        if (v == binding.fabCreate) {
            words.add (new Word ());
            if (createTopicAdapter != null) {
                createTopicAdapter.notifyDataSetChanged ();
            }
        }
    };

    private boolean checkContent() {
        if (nameCategory == null || nameCategory.equals ("")) {
            binding.chooseCategoryTopic.setError ("Invalid category");
        } else if (nameTopic == null || nameTopic.equals ("")) {
            binding.etNameTopic.setError ("Invalid name topic");
        } else if (descriptionTopic == null || descriptionTopic.equals ("")) {
            binding.etDescriptionTopic.setError ("Invalid description topic");
        } else
            return  true;
        return false;
    }

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityCreateEditTopicBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        setSupportActionBar (binding.toolbar);

        init();
    }

    @SuppressLint("SetTextI18n")
    private void init() {
        words = new ArrayList<> ();
        categories = new ArrayList<> ();
        topicsCurrentUser = new ArrayList<> ();
        categoryTopics = new ArrayList<> ();
        idWordsRemove = new ArrayList<> ();
        topic = new Topic ();
        executorService = Executors.newFixedThreadPool (2);

        idTopic = getIntent ().getStringExtra ("TOPIC_ID");
        if (idTopic != null) {
            MainActivity.topicVM.getTopicsOfCurrentUser ().observe (this, topics -> {
                for (Topic t : topics) {
                    this.topicsCurrentUser.add (t.clone ());
                    if (t.getId ().equals (idTopic)) {
                        isEdit = true;
                        isPublic = t.isPublic ();
                        this.topic = t.clone ();
                        binding.tvTitle.setText ("Edit Topic");
                        break;
                    }
                }

                binding.etNameTopic.setText (this.topic.getTitle () != null ? this.topic.getTitle ().trim () : "");
                binding.etDescriptionTopic.setText (this.topic.getDescription () != null ? this.topic.getDescription ().trim () : "");
            });

            for (Word w : MainActivity.wordVM.getWordsOfTopic (idTopic)) {
                this.words.add (w.clone ());
            }
        }

        MainActivity.categoryVM.getCategoryAll ().observe (this, categoryTopics -> {
            this.categoryTopics = new ArrayList<> (categoryTopics);
            this.categories.clear ();

            for (CategoryTopic c : categoryTopics) {
                if (topic.getIdCategory () != null && c.getId ().equals (this.topic.getIdCategory ())) {
                    binding.chooseCategoryTopic.setText (c.getTitle (), false);
                }

                this.categories.add (c.getTitle ());
            }

            this.categories.add (categories.size (), "OTHER");
            ArrayAdapter<String> categoryAdapter = new ArrayAdapter<> (this, R.layout.item_auto_complete, this.categories);
            binding.chooseCategoryTopic.setAdapter (categoryAdapter);
            binding.chooseCategoryTopic.setText (binding.chooseCategoryTopic.getText ().toString ().trim ().equals ("") ? categories.get (0) : binding.chooseCategoryTopic.getText ().toString ().trim (), false);
            binding.chooseCategoryTopic.setOnItemClickListener ((parent, view, position, id) -> {
                if (categories.get (position).equals ("OTHER"))
                    showInputAlertDialog ();
            });

            if (this.words.size () == 0) {
                this.words.add (new Word ());
                this.words.add (new Word ());
            }
        });

        createTopicAdapter = new CreateTopicAdapter (this, words);
        createTopicAdapter.setOnClickItemListener (this);

        binding.recWords.setHasFixedSize (true);
        binding.recWords.setLayoutManager (new LinearLayoutManager (this, LinearLayoutManager.VERTICAL, false));
        binding.recWords.setNestedScrollingEnabled (false);
        binding.recWords.setAdapter (createTopicAdapter);

        binding.ivBack.setOnClickListener (onClickListener);
        binding.fabCreate.setOnClickListener (onClickListener);
        binding.ivCreate.setOnClickListener (onClickListener);
        binding.ivSetting.setOnClickListener (onClickListener);
    }

    private void showWarningAlertDialog(String title, String message, Runnable runnable) {
        AlertDialog.Builder builder = new AlertDialog.Builder (this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (this).inflate (
                R.layout.layout_warning_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText (title);
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

    private void showInputAlertDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder (this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (this).inflate (
                R.layout.layout_input_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Name Category");
        ((AppCompatButton) view.findViewById (R.id.btnActionNo)).setText ("No");
        ((AppCompatButton) view.findViewById (R.id.btnActionYes)).setText ("Yes");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_done);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnActionNo).setOnClickListener (v -> {
            alertDialog.dismiss ();
        });

        view.findViewById (R.id.btnActionYes).setOnClickListener (v -> {
            String category = Objects.requireNonNull (((TextInputEditText) view.findViewById (R.id.etMessage)).getText ()).toString ().trim ();
            binding.chooseCategoryTopic.setText (category, false);
            alertDialog.dismiss ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onIVDeleteWordClickListener(int position) {
        if (words.size () > 2) {
            idWordsRemove.add (words.get (position).getId ());
            words.remove (position);
            if (createTopicAdapter != null) {
                createTopicAdapter.notifyItemRemoved (position);
            }
        } else {
            showWarningAlertDialog ("Topic must have at least two terms","Do you want to add a new term?", () -> {
                words.add (new Word ());
                if (createTopicAdapter != null) {
                    createTopicAdapter.notifyDataSetChanged ();
                }
            });
        }
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog (this);
        dialog.requestWindowFeature (Window.FEATURE_NO_TITLE);
        dialog.setContentView (R.layout.bottom_sheet_dialog_02);

        Window window = dialog.getWindow ();
        if (window == null)
            return;

        CheckBox ckIsPublic = dialog.findViewById (R.id.ckIsPublic);
        LinearLayout containerDeleteTopic = dialog.findViewById (R.id.containerDeleteTopic);

        ckIsPublic.setChecked (isPublic);
        ckIsPublic.setOnCheckedChangeListener ((buttonView, isChecked) -> {
            isPublic = isChecked;
            dialog.dismiss ();
        });

        containerDeleteTopic.setOnClickListener (v -> {
            runnableDeleteTopic.run ();
            dialog.dismiss ();
        });

        dialog.show ();
        dialog.setCancelable (true);

        window.setLayout (ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        window.setBackgroundDrawable (new ColorDrawable (Color.TRANSPARENT));
        window.clearFlags (WindowManager.LayoutParams.FLAG_DIM_BEHIND);

        WindowManager.LayoutParams windowAttributes = window.getAttributes ();
        windowAttributes.windowAnimations = R.style.DialogAnimation;
        windowAttributes.gravity = Gravity.BOTTOM;
        window.setAttributes (windowAttributes);
    }

    private boolean checkTopic (Topic topic) {
        if (topic.getIdCategory () == null || topic.getIdCategory ().equals ("")) {
            Log.d ("TAG", "checkTopic: topic.getIdCategory (): " + topic.getIdCategory ());
        } else if (topic.getIdUser () == null || topic.getIdUser ().equals ("")) {
            Log.d ("TAG", "checkTopic: topic.getIdUser (): " + topic.getIdUser ());
        } else if (topic.getUsername () == null || topic.getUsername ().equals ("")) {
            Log.d ("TAG", "checkTopic: topic.getUsername (): " + topic.getUsername ());
        } else
            return true;
        return false;
    }
}
