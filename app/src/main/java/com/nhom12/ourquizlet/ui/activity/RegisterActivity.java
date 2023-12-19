package com.nhom12.ourquizlet.ui.activity;

import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.ybq.android.spinkit.style.ChasingDots;
import com.google.firebase.auth.FirebaseAuth;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.databinding.ActivityRegisterBinding;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RegisterActivity extends AppCompatActivity {

    private ActivityRegisterBinding binding;
    private FirebaseAuth auth;
    private ExecutorService executorService;

    private final View.OnClickListener onClickListener = v -> {
        if (v == binding.ivRegister) {
            if (checkErrorInput ()) {
                binding.progressBar.setVisibility (View.VISIBLE);
                registerAccountInBackground (binding.etEmail.getText ().toString ().trim (),
                                binding.etPassword.getText ().toString ().trim ());
            } else
                return;
        }

        if (v == binding.tvOpenLoginPage) {
            startActivity (new Intent (this, LoginActivity.class));
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityRegisterBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);

        binding.progressBar.setIndeterminateDrawable (new ChasingDots ());
        binding.ivRegister.setOnClickListener (onClickListener);
        binding.tvOpenLoginPage.setOnClickListener (onClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown();
    }

    private void registerAccountInBackground(String email, String password) {
        binding.progressBar.setVisibility (View.INVISIBLE);
        executorService.execute (() -> {
            auth.createUserWithEmailAndPassword (email, password)
                    .addOnCompleteListener (this, task -> {
                        if (task.isSuccessful ()) {
                            runOnUiThread (() -> {
                                binding.etEmail.setText ("");
                                binding.etPassword.setText ("");
                                binding.etRePassword.setText ("");

                                Intent intent = new Intent (this, ProvideInfoUserActivity.class);
                                intent.putExtra ("EMAIL", email);
                                intent.putExtra ("PASSWORD", password);
                                startActivity (intent);
                            });
                        } else {
                            runOnUiThread (() -> {
                                String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                                showErrorAlertDialog (errorMessage);
                            });
                        }
                    })
                    .addOnCanceledListener (() -> {
                        runOnUiThread (() -> {
                            String errorMessage = "The Register task has been cancelled. Please Register again later";
                            showErrorAlertDialog (errorMessage);
                        });
                    });
        });

    }

    private boolean checkErrorInput() {
        String email = binding.etEmail.getText ().toString ().trim ();
        String username = binding.etUsername.getText ().toString ().trim ();
        String password = binding.etPassword.getText ().toString ().trim ();
        String rePassword = binding.etRePassword.getText ().toString ().trim ();

        if (email.isEmpty ())
            binding.etEmail.setError ("Please enter your email");
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches())
            binding.etEmail.setError ("Invalid email");
        else if (username.isEmpty ())
            binding.etUsername.setError ("Please enter your username");
        else if (password.isEmpty ())
            binding.etPassword.setError ("Please enter your password");
        else if (password.length () < 6)
            binding.etPassword.setError ("Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(password))
            binding.etPassword.setError ("Password must contain at least one letter or number");
        else if (rePassword.isEmpty ())
            binding.etRePassword.setError ("Please re-enter your password");
        else if (rePassword.length () < 6)
            binding.etRePassword.setError ("Password must contain at least 6 characters");
        else if (!containsLetterAndDigit(password))
            binding.etRePassword.setError ("Password must contain at least one letter or number");
        else if (!rePassword.equals (password))
            binding.etRePassword.setError ("Invalid password");
        else
            return true;
        return false;
    }

    private boolean containsLetterAndDigit(String str) {
        boolean hasLetter = false;
        boolean hasDigit = false;

        for (int i = 0; i < str.length(); i++) {
            if (Character.isLetter(str.charAt(i))) {
                hasLetter = true;
            } else if (Character.isDigit(str.charAt(i))) {
                hasDigit = true;
            }
        }

        return hasLetter && hasDigit;
    }

    private void showErrorAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder (RegisterActivity.this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (RegisterActivity.this).inflate (
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
