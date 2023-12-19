package com.nhom12.ourquizlet.data.repository;

import android.util.Log;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FieldValue;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.data.model.Folder;

import java.util.Map;
import java.util.Objects;
import java.util.Set;

public class FolderRepository {
    private FirebaseFirestore db;
    private FirebaseAuth auth;

    public FolderRepository() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
    }

    public void addFolder(Folder folder, OnCompleteListener<Void> onCompleteListener) {
        DocumentReference newFolderRef = db.collection("folders").document();
        folder.setId(newFolderRef.getId());
        folder.setIdCreator(Objects.requireNonNull(auth.getCurrentUser()).getUid());
        folder.setUsername(MainActivity.currentUser.getUsername());
        folder.setAvatar(MainActivity.currentUser.getAvatar());
        newFolderRef.set(folder)
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(e -> {
                    Log.w("FolderRepository", "Error adding document", e);
                });
    }

    public void deleteFolder(Folder folder, OnCompleteListener<Void> onCompleteListener) {
        db.collection("folders").document(folder.getId())
                .delete()
                .addOnCompleteListener(onCompleteListener);
    }

    public void editFolder(String folderId, Map<String, Object> updatedFolder, OnCompleteListener<Void> onCompleteListener) {
        db.collection("folders").document(folderId)
                .update(updatedFolder)
                .addOnCompleteListener(onCompleteListener);
    }

    public void addTopicsToFolder(String folderId, Set<String> selectedTopicIds, OnCompleteListener<Void> onCompleteListener) {
        db.collection("folders").document(folderId)
                .update("idTopics", FieldValue.arrayUnion(selectedTopicIds.toArray()))
                .addOnCompleteListener(onCompleteListener)
                .addOnFailureListener(e -> Log.e("FolderRepository", "Error adding topics to folder", e));
    }

}

