package com.nhom12.ourquizlet.ui.activity;

import android.app.Activity;
import android.content.Intent;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.constraintlayout.widget.ConstraintLayout;

import com.github.ybq.android.spinkit.style.ChasingDots;
import com.google.android.gms.auth.api.signin.GoogleSignIn;
import com.google.android.gms.auth.api.signin.GoogleSignInAccount;
import com.google.android.gms.auth.api.signin.GoogleSignInClient;
import com.google.android.gms.auth.api.signin.GoogleSignInOptions;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.GoogleAuthProvider;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.R;
import com.nhom12.ourquizlet.data.model.User;
import com.nhom12.ourquizlet.databinding.ActivityLoginBinding;

import java.util.Objects;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class LoginActivity extends AppCompatActivity {

    private ActivityLoginBinding binding;
    private FirebaseAuth auth;
    private FirebaseFirestore db;
    private ExecutorService executorService;
    private GoogleSignInClient mGoogleSignInClient;
    private static final int REQ_ONE_TAP = 2;  // Can be any integer unique to the Activity.
//    private boolean showOneTapUI = true;

    private final ActivityResultLauncher<Intent> mActivityResultLauncher = registerForActivityResult (
            new ActivityResultContracts.StartActivityForResult (), result -> {
                if (result.getResultCode () == Activity.RESULT_OK) {
                    Task<GoogleSignInAccount> task = GoogleSignIn.getSignedInAccountFromIntent (result.getData ());
                    handleSignInResult(task);
                }
            }
    );

    private final View.OnClickListener onClickListener = v -> {
        if (v == binding.ivLogin) {
            if (checkErrorInput()) {
                binding.progressBar.setVisibility (View.VISIBLE);
                loginAccount (binding.etEmail.getText ().toString ().trim (),
                        binding.etPassword.getText ().toString ().trim ());
            } else
                return;
        }

        if (v == binding.btnSignUpWithGoogle) {
            binding.progressBar.setVisibility (View.VISIBLE);
            mActivityResultLauncher.launch (mGoogleSignInClient.getSignInIntent ());
        }


        if (v == binding.btnSignUpWithFacebook) {

        }

        if (v == binding.tvOpenRegisterPage) {
            startActivity (new Intent (this, RegisterActivity.class));
        }

        if (v == binding.tvForgotPassword)
            startActivity (new Intent (this, RecoveryPasswordActivity.class));
    };

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate (savedInstanceState);
        binding = ActivityLoginBinding.inflate (getLayoutInflater ());
        setContentView (binding.getRoot ());

        auth = FirebaseAuth.getInstance ();
        db = FirebaseFirestore.getInstance ();
        executorService = Executors.newFixedThreadPool (1);

//        Login Account Google
        GoogleSignInOptions gso = new GoogleSignInOptions.Builder (GoogleSignInOptions.DEFAULT_SIGN_IN)
                .requestIdToken (getString (R.string.default_web_client_id))
                .requestEmail ()
                .build ();

        mGoogleSignInClient = GoogleSignIn.getClient (this, gso);


        binding.progressBar.setIndeterminateDrawable (new ChasingDots ());

        binding.ivLogin.setOnClickListener (onClickListener);
        binding.btnSignUpWithGoogle.setOnClickListener (onClickListener);
        binding.btnSignUpWithFacebook.setOnClickListener (onClickListener);
        binding.tvOpenRegisterPage.setOnClickListener (onClickListener);
        binding.tvForgotPassword.setOnClickListener (onClickListener);
    }

    @Override
    public void onStart() {
        super.onStart();
        // Check if user is signed in (non-null) and update UI accordingly.
        FirebaseUser currentUser = auth.getCurrentUser();
        if(currentUser != null) {
            String email = currentUser.getEmail ();
            String username = currentUser.getDisplayName ();
            Intent intent = new Intent (this, MainActivity.class);
            intent.putExtra ("Email", email);
            intent.putExtra ("username", username);
            startActivity (intent);
            finish ();
        }

    }

    private boolean checkErrorInput() {
        String email = binding.etEmail.getText ().toString ().trim ();
        String password = binding.etPassword.getText ().toString ().trim ();
        String rePassword = binding.etRePassword.getText ().toString ().trim ();

        if (email.isEmpty ())
            binding.etEmail.setError ("Please enter your email");
        else if (!Patterns.EMAIL_ADDRESS.matcher (email).matches())
            binding.etEmail.setError ("Invalid email");
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

    private void loginAccount(String email, String password) {
        AuthCredential credential = EmailAuthProvider.getCredential(email, password);
        String signInMethod = credential.getSignInMethod();
        if (signInMethod.isEmpty()) {
            Toast.makeText(this, "User not found", Toast.LENGTH_SHORT).show();
        } else {
            loginAccountInBackground(email,password);
        }

    }

    private void loginAccountInBackground(String email, String password) {
        executorService.execute (() -> {
            auth.signInWithEmailAndPassword (email, password)
                    .addOnCompleteListener(this, task -> {
                        if (task.isSuccessful()) {
                            runOnUiThread (() -> {
                                FirebaseUser user = auth.getCurrentUser();
                                if (user != null) {
                                    Intent intent = new Intent (LoginActivity.this, MainActivity.class);
                                    startActivity (intent);
                                    finish ();
                                }
                            });
                        } else {
                            runOnUiThread (() -> {
                                String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                                showErrorAlertDialog(errorMessage);
                            });
                        }
                    }).addOnCanceledListener (() ->
                            runOnUiThread (() -> {
                                showErrorAlertDialog("The system is maintenance. The Login task has been cancelled. Please Login again later");
                            })

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

    private void handleSignInResult(Task<GoogleSignInAccount> completedTask) {
        try {
            GoogleSignInAccount acc = completedTask.getResult (ApiException.class);
            firebaseGoogleAuth(acc);
        } catch (ApiException e) {
            firebaseGoogleAuth(null);
        }
    }

    //    login by account google
    private void firebaseGoogleAuth(GoogleSignInAccount acct) {
        AuthCredential authCredential = GoogleAuthProvider.getCredential (acct.getIdToken (), null);
        auth.signInWithCredential (authCredential)
                .addOnCompleteListener(this, task -> {
                    binding.progressBar.setVisibility (View.INVISIBLE);
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        Toast.makeText (this, "Sign In Successfully", Toast.LENGTH_LONG).show ();
                        FirebaseUser user = auth.getCurrentUser();
                        if (user != null) {
                            String email = user.getEmail ();
                            Intent intent = new Intent (LoginActivity.this, MainActivity.class);
                            intent.putExtra ("Email", email);
                            startActivity (intent);
                            finish ();
                        }
                    } else {
                        // If sign in fails, display a message to the user.
                        String errorMessage = Objects.requireNonNull (task.getException ()).getMessage ();
                        Toast.makeText (this, errorMessage, Toast.LENGTH_LONG).show ();
                    }
                }).addOnCanceledListener (() ->
                        showErrorAlertDialog("The system is maintenance. The Google SignIn Account task has been cancelled. Please Google SignIn Account again later")
                );
    }
}
