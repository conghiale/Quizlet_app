package com.nhom12.ourquizlet.ui.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom12.ourquizlet.data.model.Folder;
import com.nhom12.ourquizlet.data.model.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class FolderViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private MutableLiveData<List<Folder>> foldersLiveData;

    private MutableLiveData<List<Topic>> topicInFolderLiveData;

    public LiveData<List<Folder>> getFolders() {
        if (foldersLiveData == null) {
            foldersLiveData = new MutableLiveData<>();
            loadFolders();
        }
        return foldersLiveData;
    }

    public LiveData<List<Topic>> getTopicInFolder(String folderId) {
        if (topicInFolderLiveData == null) {
            topicInFolderLiveData = new MutableLiveData<>();
        }
        loadTopicsInFolder(folderId, "all");
        return topicInFolderLiveData;
    }

    public void loadFolders() {
        db.collection("folders").whereEqualTo("idCreator", Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Folder> listFolders = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Folder folder = documentSnapshot.toObject(Folder.class);
                        folder.setId(documentSnapshot.getId());
                        listFolders.add(folder);
                    }
                    foldersLiveData.setValue(listFolders);
                });
    }

    public void loadTopicsInFolder(String folderId, String options) {
        if (topicInFolderLiveData == null) {
            topicInFolderLiveData = new MutableLiveData<>();
        }

        db.collection("folders").document(folderId)
                .get()
                .addOnSuccessListener(documentSnapshot -> {
                    Folder folder = documentSnapshot.toObject(Folder.class);
                    if (folder != null && folder.getIdTopics() != null && !folder.getIdTopics().isEmpty()) {
                        if (options.equals("all")){
                            fetchAllTopicsByIds(folder.getIdTopics());
                        } else if (options.equals("created")){
                            fetchCreatedTopicsByIds(folder.getIdTopics());
                        } else {
                            fetchJoinedTopicsByIds(folder.getIdTopics());
                        }

                    } else {
                        topicInFolderLiveData.setValue(new ArrayList<>());
                    }
                });
    }

    public void fetchAllTopicsByIds(List<String> topicIds) {
        db.collection("topics").whereIn(FieldPath.documentId(), topicIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Topic> topics = queryDocumentSnapshots.toObjects(Topic.class);
                    topicInFolderLiveData.setValue(topics);
                })
                .addOnFailureListener(e -> {
                    Log.e("FolderViewModel", "Error fetching topics", e);
                    topicInFolderLiveData.setValue(new ArrayList<>());
                });
    }

    public void fetchCreatedTopicsByIds(List<String> topicIds) {
        db.collection("topics").whereIn(FieldPath.documentId(), topicIds)
                .whereEqualTo("idUser", Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Topic> topics = queryDocumentSnapshots.toObjects(Topic.class);
                    topicInFolderLiveData.setValue(topics);
                })
                .addOnFailureListener(e -> {
                    Log.e("FolderViewModel", "Error fetching topics", e);
                    topicInFolderLiveData.setValue(new ArrayList<>());
                });
    }

    public void fetchJoinedTopicsByIds(List<String> topicIds) {
        db.collection("topics").whereIn(FieldPath.documentId(), topicIds)
                .whereNotEqualTo("idUser", Objects.requireNonNull(auth.getCurrentUser()).getUid())
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Topic> topics = queryDocumentSnapshots.toObjects(Topic.class);
                    topicInFolderLiveData.setValue(topics);
                })
                .addOnFailureListener(e -> {
                    Log.e("FolderViewModel", "Error fetching topics", e);
                    topicInFolderLiveData.setValue(new ArrayList<>());
                });
    }
}
