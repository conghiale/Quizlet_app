package com.nhom12.ourquizlet.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;


import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nhom12.ourquizlet.data.model.User;

public class HomeViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private MutableLiveData<User> mCurrentUser;

    public HomeViewModel() {
        db = FirebaseFirestore.getInstance ();
        auth = FirebaseAuth.getInstance ();
        mCurrentUser = new MutableLiveData<> ();
        setCurrentUser();
    }

    private void setCurrentUser() {
        if (auth.getCurrentUser () != null) {
            String idUser = auth.getCurrentUser ().getUid ();
            db.collection ("users")
                .document (idUser)
                .get ()
                .addOnSuccessListener (documentSnapshot -> {
                    mCurrentUser.setValue (documentSnapshot.toObject (User.class));
                });
        }
    }

    public LiveData<User> getCurrentUser() {
        return mCurrentUser;
    }
}
