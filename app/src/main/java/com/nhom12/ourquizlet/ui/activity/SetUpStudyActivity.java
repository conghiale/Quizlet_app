package com.nhom12.ourquizlet.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;
import androidx.lifecycle.ViewModelProvider;

import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.Word;
import com.nhom12.ourquizlet.databinding.ActivitySetupStudyBinding;
import com.nhom12.ourquizlet.ui.viewModel.SetUpStudyViewModel;

import java.util.Arrays;
import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class SetUpStudyActivity extends AppCompatActivity {
    private ActivitySetupStudyBinding binding;
    public static SetUpStudyViewModel setUpStudyVM;
    private final String[] languages ={"english", "vietnamese"};
    private String languageTerm = "";
    private String languageDefine = "";
    private ArrayAdapter<String> adapterSelectLanguage;
    private String idTopic;
    private String idCategory;
    private String study;
    private int numberWord;
    private int count;
    private boolean isStar = false;
    private final View.OnClickListener onClickListener = v -> {
        if (v == binding.ivCloseToolbar) {
            getOnBackPressedDispatcher ().onBackPressed ();
            finish ();
        }

        if (v == binding.btnStartMultipleChoiceTest) {
            if (isValidate ()) {
                saveInfoSetUp ();
                Intent intent = new Intent (getApplicationContext (), MultipleChoiceTestActivity.class);
                intent.putExtra ("COUNT", count);
                startActivity (intent);
            }
        }

        if (v == binding.btnStartFillInWord) {
            if (isValidate ()) {
                saveInfoSetUp ();
                Intent intent = new Intent (getApplicationContext (), FillInWordActivity.class);
                intent.putExtra ("COUNT", count);
                startActivity (intent);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivitySetupStudyBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        setSupportActionBar (binding.toolbar);
        Objects.requireNonNull (getSupportActionBar ()).setTitle ("");

        init();

        setAdapterAutoComplete();

        binding.autoCompleteLanguageTerm.setOnItemClickListener ((parent, view, position, id) -> {
            languageTerm = parent.getItemAtPosition (position).toString ();
            languageDefine = Arrays.stream(languages).filter (s -> !s.equals (languageTerm))
                    .findFirst ().orElse ("");
            if (languageTerm.equals ("english"))
                binding.autoCompleteLanguageDefine.setText (languages[1]);
            else
                binding.autoCompleteLanguageDefine.setText (languages[0]);

            setAdapterAutoComplete();
        });

        binding.autoCompleteLanguageDefine.setOnItemClickListener ((parent, view, position, id) -> {
            languageDefine = parent.getItemAtPosition (position).toString ();
            languageTerm = Arrays.stream(languages).filter (s -> !s.equals (languageDefine))
                    .findFirst ().orElse ("");
            if (languageDefine.equals ("english"))
                binding.autoCompleteLanguageTerm.setText (languages[1]);
            else
                binding.autoCompleteLanguageTerm.setText (languages[0]);
            setAdapterAutoComplete();
        });
    }

    private void init() {
        idTopic = getIntent ().getStringExtra ("TOPIC_ID");
        idCategory = getIntent ().getStringExtra ("CATEGORY_ID");
        numberWord = getIntent ().getIntExtra ("NUMBER_WORD", 0);
        count = getIntent ().getIntExtra ("COUNT", 0);
        study = getIntent ().getStringExtra ("STUDY");
        isStar = getIntent ().getBooleanExtra ("IS_STAR", false);
        setUpStudyVM = new ViewModelProvider (this).get (SetUpStudyViewModel.class);

        if (study != null && study.equals ("MULTIPLE_CHOICE_TEST")) {
            binding.btnStartMultipleChoiceTest.setVisibility (View.VISIBLE);
            binding.btnStartFillInWord.setVisibility (View.GONE);
        } else {
            binding.btnStartMultipleChoiceTest.setVisibility (View.GONE);
            binding.btnStartFillInWord.setVisibility (View.VISIBLE);
        }

        binding.etNumberOfWords.setText (String.valueOf (numberWord));
        binding.ivCloseToolbar.setOnClickListener (onClickListener);
        binding.btnStartMultipleChoiceTest.setOnClickListener (onClickListener);
        binding.btnStartFillInWord.setOnClickListener (onClickListener);
    }

    private void setAdapterAutoComplete() {
        adapterSelectLanguage = new ArrayAdapter<> (this, R.layout.item_auto_complete, languages);
        binding.autoCompleteLanguageTerm.setAdapter (adapterSelectLanguage);
        binding.autoCompleteLanguageDefine.setAdapter (adapterSelectLanguage);
    }

    private boolean isValidate() {
        String strNumOfVoc = binding.etNumberOfWords.getText ().toString ().trim ();
        long countWord = MainActivity.wordVM.getWordsOfTopic (idTopic).size ();

        if (strNumOfVoc.equals ("") && Integer.parseInt (strNumOfVoc) < 1) {
            binding.etNumberOfWords.setError ("The number of words must be greater than 1");
            return false;
        } else if (Integer.parseInt (strNumOfVoc) > numberWord) {
            binding.etNumberOfWords.setError ("The number of words must be less than " + numberWord);
            return false;
        } else if (isStar && Integer.parseInt (strNumOfVoc) < 4) {
            binding.etNumberOfWords.setError ("The number of words must be greater than 3");
            return false;
        } else if (languageTerm.equals ("")) {
            binding.autoCompleteLanguageTerm.setError ("Please select the term display language");
            return false;
        } else if (languageDefine.equals ("")) {
            binding.autoCompleteLanguageDefine.setError ("Please select the define display language");
            return false;
        } if (countWord == 0) {
            showErrorAlertDialog ("This topic has no words");
            return false;
        } else
            return true;
    }

    private void saveInfoSetUp() {
        String strNumOfWords = binding.etNumberOfWords.getText ().toString ().trim ();
        ExecutorService executorService = Executors.newFixedThreadPool (1);
        executorService.execute (() -> {
            setUpStudyVM.setNumberWords (Integer.parseInt (strNumOfWords));
            setUpStudyVM.setLanguageDefine (languageDefine);
            setUpStudyVM.setShuffle (binding.scShuffle.isChecked ());
            setUpStudyVM.setStar (isStar);
            setUpStudyVM.setIdTopic (idTopic);
            setUpStudyVM.setIdCategory (idCategory);
        });
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
