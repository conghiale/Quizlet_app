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

import com.google.firebase.auth.FirebaseAuth;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.databinding.ActivityRecoveryPasswordBinding;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class RecoveryPasswordActivity extends AppCompatActivity {
    private ActivityRecoveryPasswordBinding binding;
    private FirebaseAuth mAuth;
    private ExecutorService executorService;
    private final View.OnClickListener onClickListener = v -> {
        if (v == binding.btnBack)
            startActivity (new Intent (this, LoginActivity.class));

        if (v == binding.ivForgotPassword) {
            if (checkErrorInput ()) {
                binding.progressBar.setVisibility (View.VISIBLE);
                String email = binding.etEmail.getText ().toString ().trim ();
                recoveryPasswordInBackground (email);
            }
        }
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityRecoveryPasswordBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        mAuth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);

        binding.btnBack.setOnClickListener (onClickListener);
        binding.ivForgotPassword.setOnClickListener (onClickListener);
    }

    @Override
    protected void onDestroy() {
        super.onDestroy ();
        if (executorService != null)
            executorService.shutdown ();
    }

    private boolean checkErrorInput() {
        String email = binding.etEmail.getText ().toString ().trim ();

        if (email.isEmpty ())
            binding.etEmail.setError ("Please enter your email");
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches())
            binding.etEmail.setError ("Invalid email");
        else
            return true;
        return false;
    }

    private void recoveryPasswordInBackground(String email) {
        executorService.execute (() -> {
            mAuth.sendPasswordResetEmail (email)
                    .addOnCompleteListener (task -> {
                        binding.progressBar.setVisibility (View.INVISIBLE);
                        if (task.isSuccessful ()) {
                            showSuccessAlertDialog("Reset password link has been  sent to your registered email");
                        } else {
                            String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                            showErrorAlertDialog(errorMessage);
                        }
                    })
                    .addOnCanceledListener (() ->
                            showErrorAlertDialog("The system is maintenance. The Forgot Password task has been cancelled. Please Forgot Password again later")
                    );
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

    private void showSuccessAlertDialog(String message) {
        AlertDialog.Builder builder = new AlertDialog.Builder (this, R.style.AlertDialogTheme);
        View view = LayoutInflater.from (this).inflate (
                R.layout.layout_success_dialog, (ConstraintLayout)findViewById (R.id.layoutDialogContainer));
        builder.setView (view);

        ((TextView) view.findViewById (R.id.tvTitle)).setText ("Success");
        ((TextView) view.findViewById (R.id.tvMessage)).setText (message);
        ((AppCompatButton) view.findViewById (R.id.btnAction)).setText ("Okay");
        ((ImageView) view.findViewById (R.id.ivImageIcon)).setImageResource (R.drawable.baseline_done);

        final AlertDialog alertDialog = builder.create ();
        view.findViewById (R.id.btnAction).setOnClickListener (v -> {
            alertDialog.dismiss ();
            startActivity (new Intent (this, LoginActivity.class));
            finish ();
        });

        if (alertDialog.getWindow () != null)
            alertDialog.getWindow ().setBackgroundDrawable (new ColorDrawable (0));
        alertDialog.show ();
        alertDialog.setCancelable(false);
    }
}
