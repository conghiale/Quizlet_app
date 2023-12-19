package com.nhom12.ourquizlet.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.data.model.Topic;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicInteger;

public class TopicHomeViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Topic>> mTopics;

    public TopicHomeViewModel() {
        db = FirebaseFirestore.getInstance();
        auth = FirebaseAuth.getInstance();
        executorService = Executors.newFixedThreadPool(1);
        mTopics = new MutableLiveData<>();
        cloneTopicAll();
    }

    private void cloneTopicAll() {
        AtomicInteger percent = new AtomicInteger();
        executorService.execute(() -> {
            db.collection("topics")
                    .get()
                    .addOnCompleteListener(task -> {
                        if (task.isSuccessful() && auth.getCurrentUser() != null) {
                            mTopics.setValue(task.getResult().toObjects(Topic.class));
                            db.collection("user_topic")
                                    .whereEqualTo("idUser", auth.getCurrentUser().getUid())
                                    .get()
                                    .addOnCompleteListener(task1 -> {
                                        for (DocumentSnapshot d : task1.getResult()) {
                                            String idTopic = d.getString("idTopic");
                                            if (d.getLong("percent") != null) {
                                                percent.set(Objects.requireNonNull(d.getLong("percent")).intValue());
                                            } else {
                                                percent.set(-1);
                                            }

                                            if (mTopics.getValue() != null) {
                                                Optional<Topic> topic = mTopics.getValue().stream()
                                                        .filter(topic1 -> Objects.equals(topic1.getId(), idTopic))
                                                        .findFirst();
                                                topic.ifPresent(topic1 -> topic1.setPercent(percent.get()));
                                            }
                                        }

                                        if (mTopics.getValue() != null) {
                                            for (Topic t : mTopics.getValue()) {
                                                if (t.getNumberWord() == -1) {
                                                    if (MainActivity.wordVM.getWordAll().getValue() != null) {
                                                        t.setNumberWord((int) MainActivity.wordVM.getWordAll().getValue().stream()
                                                                .filter(word -> Objects.equals(word.getIdTopic(), t.getId()))
                                                                .count());
                                                    } else {
                                                        t.setNumberWord(-1);
                                                    }
                                                }

                                                if (t.getPercent() == -1) {
                                                    t.setPercent(0);
                                                }
                                            }
                                        }
                                    });
                        }
                    });
        });
    }

    public LiveData<List<Topic>> getTopicAll() {
        return mTopics;
    }

    public void updatePercentTopic(String idTopic, int percent, Runnable runnableError, Runnable runnableSuccess) {
        if (auth.getCurrentUser() != null) {
            db.collection("user_topic")
                    .whereEqualTo("idUser", auth.getCurrentUser().getUid())
                    .whereEqualTo("idTopic", idTopic)
                    .get()
                    .addOnSuccessListener(queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty()) {
                            Map<String, Object> newUserTopic = new HashMap<>();
                            newUserTopic.put("idUser", auth.getCurrentUser().getUid());
                            newUserTopic.put("idTopic", idTopic);
                            newUserTopic.put("percent", percent);

                            db.collection("user_topic")
                                    .add(newUserTopic)
                                    .addOnSuccessListener(documentReference -> runnableSuccess.run())
                                    .addOnFailureListener(e -> runnableError.run())
                                    .addOnCanceledListener(runnableError::run);

                            updatePercentOfWordLocal(idTopic, percent);
                        } else {
                            String idDocument = queryDocumentSnapshots.getDocuments().get(0).getId();
                            db.collection("user_topic")
                                    .document(idDocument)
                                    .update("percent", percent)
                                    .addOnSuccessListener(unused -> runnableSuccess.run())
                                    .addOnFailureListener(e -> runnableError.run())
                                    .addOnCanceledListener(runnableError::run);

                            updatePercentOfWordLocal(idTopic, percent);
                        }
                    });
        }
    }

    private void updatePercentOfWordLocal(String idTopic, int percent) {
        if (mTopics.getValue() != null) {
            for (Topic t : mTopics.getValue()) {
                if (Objects.equals(t.getId(), idTopic)) {
                    t.setPercent(percent);
                    // If you want to notify observers, uncomment the next line:
                    // mTopics.setValue(mTopics.getValue());
                    break;
                }
            }
        }
    }
}
