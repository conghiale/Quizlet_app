package com.nhom12.ourquizlet;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;
import androidx.navigation.ui.NavigationUI;

import android.app.AlertDialog;
import android.app.Dialog;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;
import android.widget.LinearLayout;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.data.model.Folder;
import com.nhom12.ourquizlet.data.model.User;
import com.nhom12.ourquizlet.data.repository.FolderRepository;
import com.nhom12.ourquizlet.data.repository.UserRepository;
import com.nhom12.ourquizlet.databinding.ActivityMainBinding;

import com.nhom12.ourquizlet.databinding.DialogAddFolderBinding;
import com.nhom12.ourquizlet.ui.activity.CreateEditTopicActivity;
import com.nhom12.ourquizlet.ui.viewModel.CategoryViewModel;
import com.nhom12.ourquizlet.ui.viewModel.FolderViewModel;
import com.nhom12.ourquizlet.ui.viewModel.HomeViewModel;
import com.nhom12.ourquizlet.ui.viewModel.ProfileViewModel;
import com.nhom12.ourquizlet.ui.viewModel.Topic2ViewModel;
import com.nhom12.ourquizlet.ui.viewModel.TopicViewModel;
import com.nhom12.ourquizlet.ui.viewModel.WordViewModel;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

public class MainActivity extends AppCompatActivity {
    private ActivityMainBinding binding;
    public static HomeViewModel homeVM;
    public static WordViewModel wordVM;
    public static Topic2ViewModel topicVM;
    public static TopicViewModel topicViewModel;
    public static CategoryViewModel categoryVM;
    public static FolderViewModel folderViewModel;
    public static ProfileViewModel profileViewModel;
    private DialogAddFolderBinding dialogAddFolderBinding;
    private FirebaseFirestore db;
    private FirebaseAuth auth;
    public static User currentUser;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        NavController navController = Navigation.findNavController (this, R.id.frame_layout);
        NavigationUI.setupWithNavController (binding.bottomNavigationView, navController);

        homeVM = new ViewModelProvider (this).get (HomeViewModel.class);
        wordVM = new ViewModelProvider (this).get (WordViewModel.class);
        topicVM = new ViewModelProvider (this).get (Topic2ViewModel.class);
        categoryVM = new ViewModelProvider (this).get (CategoryViewModel.class);
        folderViewModel = new ViewModelProvider (this).get (FolderViewModel.class);
        topicViewModel = new ViewModelProvider (this).get (TopicViewModel.class);
        profileViewModel = new ViewModelProvider (this).get (ProfileViewModel.class);

        binding.fabCreate.setOnClickListener (v -> {
            showBottomDialog ();
        });

        //Update email to db after change email and get info current user
        FirebaseAuth auth = FirebaseAuth.getInstance();
        String currentEmail = Objects.requireNonNull(auth.getCurrentUser()).getEmail();
        FirebaseFirestore db = FirebaseFirestore.getInstance();
        db.collection("users").document(auth.getCurrentUser().getUid())
                .get()
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful() && task.getResult() != null && task.getResult().exists()) {
                        DocumentSnapshot document = task.getResult();
                        currentUser = document.toObject(User.class);
                        String email = document.getString("email");
                        assert currentEmail != null;
                        if (!currentEmail.equals(email)){
                            UserRepository userRepository = new UserRepository();

                            Map<String, Object> updateEmail = new HashMap<>();
                            updateEmail.put("email", currentEmail);
                            userRepository.editUser(auth.getCurrentUser().getUid(), updateEmail, task2 -> {});
                        }
                    }
                });
//        createData();
    }

    private void showBottomDialog() {
        final Dialog dialog = new Dialog (this);
        dialog.requestWindowFeature (Window.FEATURE_NO_TITLE);
        dialog.setContentView (R.layout.bottom_sheet_dialog_01);

        Window window = dialog.getWindow ();
        if (window == null)
            return;

        LinearLayout containerCreateTopic = dialog.findViewById (R.id.containerCreateTopic);
        LinearLayout containerCreateFolder = dialog.findViewById (R.id.containerCreateFolder);

        containerCreateTopic.setOnClickListener (v -> {
//            startActivity (new Intent (this, CreateTopicActivity.class));
            startActivity (new Intent (this, CreateEditTopicActivity.class));
            dialog.dismiss ();
        });

        containerCreateFolder.setOnClickListener (v -> {
            showAddFolderDialog();
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

    private void showAddFolderDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        dialogAddFolderBinding = DialogAddFolderBinding.inflate(LayoutInflater.from(this));
        builder.setView(dialogAddFolderBinding.getRoot());
        AlertDialog dialog = builder.create();

        dialogAddFolderBinding.btnCancel.setOnClickListener(view -> dialog.dismiss());
        dialogAddFolderBinding.btnOk.setOnClickListener(view -> {
            Folder folder = new Folder();
            String folderName = dialogAddFolderBinding.etFolderName.getText().toString();
            folder.setName(folderName);
            FolderRepository folderRepository = new FolderRepository();
            folderRepository.addFolder(folder, task -> {
                if(task.isSuccessful()) {
                    MainActivity.folderViewModel.loadFolders();
                    dialog.dismiss();
                }
            });

        });

        dialog.show();
    }

    private void createData() {
        db = FirebaseFirestore.getInstance ();
        auth = FirebaseAuth.getInstance ();
        ArrayList<String> idTopics = new ArrayList<> ();
//        String[][] initData = {
//                {"E-Commerce", "Thương mại điện tử"},
//                {"Mobile Commerce", "Thương mại di động"},
//                {"Online Marketplace", "Thị trường trực tuyến"},
//                {"Digital Payments", "Thanh toán số"},
//                {"E-Commerce Platform", "Nền tảng thương mại điện tử"},
//                {"Chatbots", "Trò chuyện tự động"},
//                {"Augmented Reality", "Thực tế ảo"},
//                {"Cryptocurrency", "Tiền điện tử"},
//                {"Social Commerce", "Thương mại xã hội"},
//                {"Funding", "Nguồn vốn, Vốn"},
//                {"Fluctuate", "Biến động"},
//                {"Franchise", "Nhượng quyền"},
//                {"Global", "Toàn cầu, Quốc tế"},
//                {"Guarantee", "Bảo hành, Bảo đảm"},
//                {"Goal", "Mục tiêu"},
//        };
//
//        for (String[] o : initData) {
//            Map<String, Object> data = new HashMap<> ();
//            data.put ("term", o[0]);
//            data.put ("define", o[1]);
//            data.put ("idTopic", "KNvc5ck1HYoGeN4IsVqz");
//
//            db = FirebaseFirestore.getInstance ();
//            db.collection ("words")
//                    .add (data)
//                    .addOnSuccessListener (documentReference -> {
//                        Log.d ("TAG", "createData_word: " + documentReference.getId ());
//                        db.collection ("words")
//                            .document (documentReference.getId ())
//                            .update ("id", documentReference.getId ())
//                            .addOnSuccessListener (unused -> {
//                                Log.d ("TAG", documentReference.getId () + " - SUCCESS");
//                            });
//                    });
//        }

        db = FirebaseFirestore.getInstance ();
        db.collection ("topics")
            .get ()
            .addOnSuccessListener (queryDocumentSnapshots -> {
                Log.d ("TAG", "createData: topics: " + queryDocumentSnapshots.size ());
                for (DocumentSnapshot d : queryDocumentSnapshots) {
                    idTopics.add (d.getId ());
                }

                if (auth.getCurrentUser () != null && !idTopics.isEmpty ()) {
                    for (String idTopic : idTopics) {
                        Map<String, Object> userTopic = new HashMap<> ();
                        userTopic.put ("idTopic", idTopic);
                        userTopic.put ("idUser", auth.getCurrentUser ().getUid ());
                        userTopic.put ("count", 0);

                        db.collection ("user_topic")
                            .add (userTopic)
                            .addOnSuccessListener (unused -> {
                                Log.d ("TAG", "createData: " + idTopic + " - added");
                            })
                            .addOnFailureListener (e -> {
                                Log.e ("TAG", "createData: " + e.getMessage ());
                            });
                    }
                }
            });

//        db = FirebaseFirestore.getInstance ();
//        db.collection ("user_topic")
//                .get ()
//                .addOnSuccessListener (queryDocumentSnapshots -> {
//                    for (DocumentSnapshot d : queryDocumentSnapshots) {
//                        db.collection ("user_topic")
//                                .document (d.getId ())
//                                .delete ()
//                                .addOnSuccessListener (unused -> {
//                                    Log.d ("TAG", "createData: " + d.getId () + " - deleted");
//                                })
//                                .addOnFailureListener (e -> {
//                                    Log.e ("TAG", "createData: " + e.getMessage ());
//                                });
//
//                    }
//                });

    }

    @Override
    protected void onStart() {
        super.onStart();
    }
}