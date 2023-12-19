package com.nhom12.ourquizlet.ui.viewModel;

import android.util.Log;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nhom12.ourquizlet.MainActivity;
import com.nhom12.ourquizlet.data.model.Topic;
import com.nhom12.ourquizlet.data.repository.TopicRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicInteger;

public class Topic2ViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Topic>> mTopics;
    private final MutableLiveData<List<Topic >> myTopics;

    public Topic2ViewModel() {
        db = FirebaseFirestore.getInstance ();
        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);
        mTopics = new MutableLiveData<> ();
        myTopics = new MutableLiveData<> ();
        cloneTopicAll ();
        getTopicOfCurrentUser ();
    }

    private void cloneTopicAll() {
        AtomicInteger percent = new AtomicInteger ();
        AtomicInteger count = new AtomicInteger ();
        AtomicBoolean isPublic = new AtomicBoolean (false);
        executorService.execute (() -> {
            db.collection ("topics_2")
                .get ()
                .addOnCompleteListener (task -> {
                    if (task.isSuccessful () && auth.getCurrentUser () != null) {
                        mTopics.setValue (task.getResult ().toObjects (Topic.class));

                        db.collection ("user_topic_2")
                            .whereEqualTo ("idUser", auth.getCurrentUser ().getUid ())
                            .get ()
                            .addOnCompleteListener (task1 -> {
                                for (DocumentSnapshot d : task1.getResult ()) {
                                    String idTopic = d.getString ("idTopic");
                                    if (d.getLong ("percent") != null) {
                                        percent.set (Objects.requireNonNull (d.getLong ("percent")).intValue ());
                                    } else
                                        percent.set (0);

                                    if (d.getLong ("count") != null) {
                                        count.set (Objects.requireNonNull (d.getLong ("count")).intValue ());
                                    } else
                                        count.set (0);

                                    if (mTopics.getValue () != null) {
                                        Optional<Topic> topic = mTopics.getValue ().stream().filter (topic1 -> topic1.getId ().equals (idTopic)).findFirst ();
                                        topic.ifPresent (topic1 -> {
//                                            topic1.setPublic (isPublic.get ());
                                            topic1.setCount (count.get ());
                                            topic1.setPercent (percent.get ());
                                        });
                                    }
                                }

                                if (mTopics.getValue () != null) {
                                    for (Topic t: mTopics.getValue ()) {
                                        if (MainActivity.wordVM.getWordAll ().getValue () != null) {
                                            t.setNumberWord ((int) MainActivity.wordVM.getWordAll ().getValue ().stream ().filter (word -> word.getIdTopic ().equals (t.getId ())).count ());
                                        }else {
                                            t.setNumberWord (0);
                                        }
                                    }
                                    getTopicOfCurrentUser ();
                                }
                            });
                    }
                });
        });
    }

    public LiveData<List<Topic>> getTopicAll () {
        return mTopics;
    }

    public LiveData<List<Topic>> getTopicsOfCurrentUser() {
        return myTopics;
    }

    public void setCurrentTopicsVM(Topic topic, boolean isEdit) {
        if (myTopics.getValue () != null) {
            if (isEdit) {
                for (Topic t : myTopics.getValue ()) {
                    if (t.getId ().equals (topic.getId ())) {
//                    this.topic = myTopics.getValue ().get (i);
                        t.setDescription (topic.getDescription ());
                        t.setIdCategory (topic.getIdCategory ());
                        t.setIdUser (topic.getIdUser ());
                        t.setPublic (topic.isPublic ());
                        t.setTitle (topic.getTitle ());
                        t.setNumberWord (topic.getNumberWord ());
                        t.setUsername (topic.getUsername ());
                    }
                }
            } else
                myTopics.getValue ().add (topic);
        }

        this.myTopics.setValue (myTopics.getValue ());
    }

    public void setTopicsVM(Topic topic, boolean isEdit) {
        if (!isEdit)
            Objects.requireNonNull (mTopics.getValue ()).add (topic);

        this.mTopics.setValue (mTopics.getValue ());
    }
    public void updatePercentCountTopic(String idTopic, int percent, int count, Runnable runnableError, Runnable runnableSuccess) {
        if (auth.getCurrentUser () != null) {
            db.collection ("user_topic_2")
                    .whereEqualTo ("idUser", auth.getCurrentUser ().getUid ())
                    .whereEqualTo ("idTopic", idTopic)
                    .get ()
                    .addOnSuccessListener (queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty ()) {
                            Map<String, Object> newUserTopic = new HashMap<> ();
                            newUserTopic.put ("idUser", auth.getCurrentUser ().getUid ());
                            newUserTopic.put ("idTopic", idTopic);
                            newUserTopic.put ("percent", percent);
                            newUserTopic.put ("count", count);

                            db.collection ("user_topic_2")
                                    .add (newUserTopic)
                                    .addOnSuccessListener (documentReference -> runnableSuccess.run ())
                                    .addOnFailureListener (e -> runnableError.run ())
                                    .addOnCanceledListener (runnableError::run);

                            updatePercentOfWordLocal(idTopic, percent);
                        } else {
                            String idDocument = queryDocumentSnapshots.getDocuments ().get (0).getId ();
                            Map<String, Object> newUserTopic = new HashMap<> ();
                            newUserTopic.put ("percent", percent);
                            newUserTopic.put ("count", count);
                            db.collection ("user_topic_2")
                                    .document (idDocument)
                                    .update (newUserTopic)
                                    .addOnSuccessListener (unused -> runnableSuccess.run ())
                                    .addOnFailureListener (e -> runnableError.run ())
                                    .addOnCanceledListener (runnableError::run);

                            updatePercentOfWordLocal(idTopic, percent);
                        }
                    });
        }
    }

    private void updatePercentOfWordLocal (String idTopic, int percent) {
        if (mTopics.getValue () != null) {
            for (Topic t : mTopics.getValue ()) {
                if (t.getId ().equals (idTopic)) {
                    t.setPercent (percent);
                    break;
                }
            }
        }
    }

    private void getTopicOfCurrentUser() {
        ArrayList<Topic> topics = new ArrayList<> ();
        if (auth.getCurrentUser () != null && mTopics.getValue () != null) {
            TopicRepository.getInstance ().getTopicOfCurrentUser (auth.getCurrentUser ().getUid ())
                .addOnSuccessListener (queryDocumentSnapshots -> {
                    for (DocumentSnapshot d : queryDocumentSnapshots) {
                        String idTopic = d.getString ("idTopic");
                        for (Topic t : mTopics.getValue ()) {
                            if (t.getId ().equals (idTopic)) {
                                topics.add (t);
                                break;
                            }
                        }
                    }

                    if (!topics.isEmpty ()) {
                        myTopics.setValue (topics);
                    }

                })
                .addOnFailureListener (e -> {
                    Log.d ("TAG", "getTopicsOfCurrentUser: ERROR: " + e.getMessage ());
                });
        }
    }

    public void getTopicOfCurrentUserCreated(List<Topic> topicsCreated) {
        topicsCreated.clear ();
        if (auth.getCurrentUser () != null && mTopics.getValue () != null) {
            TopicRepository.getInstance ().getTopicOfCurrentUserCreated (auth.getCurrentUser ().getUid ())
                .addOnSuccessListener (queryDocumentSnapshots -> {
                    for (DocumentSnapshot d : queryDocumentSnapshots) {
                        for (Topic t : mTopics.getValue ()) {
                            if (t.getId ().equals (d.getId ())) {
                                topicsCreated.add (t.clone ());
                                break;
                            }
                        }

                    }

                })
                .addOnFailureListener (e -> {
                    Log.d ("TAG", "getTopicsOfCurrentUser: ERROR: " + e.getMessage ());
                });
        }
    }

    public Task<DocumentReference> createTopic(Topic topic) {
        return TopicRepository.getInstance ().createTopic (topic);
    }

    public Task<Void> updateIdTopic (String id) {
        return TopicRepository.getInstance ().updateIdTopic (id);
    }

    public Task<Void> updateFieldsTopic (Topic topic) {
        return TopicRepository.getInstance ().updateFieldsTopic (topic);
    }

    public Task<QuerySnapshot> getUserTopicByIdTopic (String idTopic) {
        return TopicRepository.getInstance ().getUserTopicByIdTopic (idTopic);
    }

    public Task<Void> deleteUserTopicById (String id) {
        return TopicRepository.getInstance ().deleteUserTopicById (id);
    }

    public Task<Void> deleteTopicById (String id) {
        return TopicRepository.getInstance ().deleteTopicById (id);
    }

    public void deleteTopicAllByIdLocal (String id) {
        if (mTopics.getValue () != null) {
            for (Topic t : mTopics.getValue ()) {
                if (t.getId ().equals (id)) {
                    mTopics.getValue ().remove (t);
                    break;
                }
            }
        }
        Log.d ("TAG", "293 - deleteWordByIdLocal: mTopics.getValue (): " + mTopics.getValue ().size ());
        this.mTopics.setValue (this.mTopics.getValue ());
    }

    public void deleteCurrentTopicByIdLocal (String id) {
        if (myTopics.getValue () != null) {
            for (Topic t : myTopics.getValue ()) {
                if (t.getId ().equals (id)) {
                    myTopics.getValue ().remove (t);
                    break;
                }
            }
        }
        Log.d ("TAG", "307 - deleteCurrentTopicByIdLocal: myTopics.getValue (): " + myTopics.getValue ().size ());
        this.myTopics.setValue (this.myTopics.getValue ());
    }
}
