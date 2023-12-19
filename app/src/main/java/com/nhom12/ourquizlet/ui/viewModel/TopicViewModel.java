package com.nhom12.ourquizlet.ui.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FieldPath;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QueryDocumentSnapshot;
import com.nhom12.ourquizlet.data.model.Topic;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

public class TopicViewModel extends ViewModel {
    private final FirebaseFirestore db = FirebaseFirestore.getInstance();
    private final FirebaseAuth auth = FirebaseAuth.getInstance();
    private MutableLiveData<List<Topic>> topicsLiveData;

    public LiveData<List<Topic>> getTopics() {
        if (topicsLiveData == null) {
            topicsLiveData = new MutableLiveData<>();
            loadAllTopics(Objects.requireNonNull(auth.getCurrentUser()).getUid());
        }
        return topicsLiveData;
    }

    public void loadAllTopics(String idUser) {
        db.collection("user_topic").whereEqualTo("idUser", idUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> topicIds = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        String idTopic = documentSnapshot.getString("idTopic");
                        topicIds.add(idTopic);
                    }
                    fetchAllTopics(topicIds);
                });
    }
    public void fetchAllTopics(List<String> topicIds) {
        if (topicIds.isEmpty()) {
            topicsLiveData.setValue(new ArrayList<>());
            return;
        }

        db.collection("topics").whereIn(FieldPath.documentId(), topicIds)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Topic> topics = new ArrayList<>();
                    for (QueryDocumentSnapshot documentSnapshot : queryDocumentSnapshots) {
                        Topic topic = documentSnapshot.toObject(Topic.class);
                        topics.add(topic);
                    }
                    topicsLiveData.setValue(topics);
                })
                .addOnFailureListener(e -> Log.e("TopicViewModel", "Error fetching topics", e));
    }

    public void loadCreatedTopics(String idUser) {
        db.collection("topics").whereEqualTo("idUser", idUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<Topic> topics = queryDocumentSnapshots.toObjects(Topic.class);
                    topicsLiveData.setValue(topics);
                });
    }

    public void loadJoinedTopics(String idUser) {
        db.collection("user_topic").whereEqualTo("idUser", idUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    List<String> topicIds = queryDocumentSnapshots.toObjects(Topic.class).stream()
                            .map(Topic::getId)
                            .collect(Collectors.toList());
                    fetchJoinedTopics(topicIds, idUser);
                });
    }

    private void fetchJoinedTopics(List<String> topicIds, String idUser) {
        if (topicIds.isEmpty()) {
            topicsLiveData.setValue(new ArrayList<>());
            return;
        }
        db.collection("topics").whereNotEqualTo("idUser", idUser)
                .get()
                .addOnSuccessListener(queryDocumentSnapshots -> {
                    // Filter out the topics that the user has joined
                    List<Topic> topics = queryDocumentSnapshots.toObjects(Topic.class).stream()
                            .filter(topic -> topicIds.contains(topic.getId()))
                            .collect(Collectors.toList());
                    topicsLiveData.setValue(topics);
                });
    }
}
