package com.nhom12.ourquizlet.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nhom12.ourquizlet.data.model.Topic;

import java.util.HashMap;
import java.util.Map;

public class TopicRepository {
    private final FirebaseFirestore db;
    private static TopicRepository instance;

    private TopicRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static TopicRepository getInstance () {
        if (instance == null) {
            synchronized (TopicRepository.class) {
                if (instance == null) {
                    instance = new TopicRepository ();
                }
            }
        }
        return instance;
    }

    public Task<QuerySnapshot> getTopicOfCurrentUser(String idUser) {
        return db.collection ("user_topic_2")
                .whereEqualTo ("idUser", idUser)
                .get ();
    }

    public Task<QuerySnapshot> getTopicOfCurrentUserCreated(String idUser) {
        return db.collection ("topics_2")
                .whereEqualTo ("idUser", idUser)
                .get ();
    }

    public Task<DocumentReference> createTopic(Topic topic) {
        Map<String, Object> mTopic = new HashMap<> ();
        mTopic.put ("description", topic.getDescription ());
        mTopic.put ("idCategory", topic.getIdCategory ());
        mTopic.put ("idUser", topic.getIdUser ());
        mTopic.put ("isPublic", topic.isPublic ());
        mTopic.put ("title", topic.getTitle ());
        mTopic.put ("username", topic.getUsername ());

        return db.collection ("topics_2")
            .add (mTopic);
    }

    public Task<Void> updateIdTopic (String id) {
        return db.collection ("topics_2")
                .document (id)
                .update ("id", id);
    }

    public Task<Void> updateFieldsTopic (Topic topic) {
        Map<String, Object> mTopic = new HashMap<> ();
        mTopic.put ("description", topic.getDescription ());
        mTopic.put ("idCategory", topic.getIdCategory ());
        mTopic.put ("idUser", topic.getIdUser ());
        mTopic.put ("isPublic", topic.isPublic ());
        mTopic.put ("title", topic.getTitle ());
        mTopic.put ("username", topic.getUsername ());

        return db.collection ("topics_2")
                .document (topic.getId ())
                .update (mTopic);
    }

    public Task<QuerySnapshot> getUserTopicByIdTopic(String idTopic) {
        return db.collection ("user_topic_2")
                .whereEqualTo ("idTopic", idTopic)
                .get ();
    }

    public Task<Void> deleteUserTopicById(String id) {
        return db.collection ("user_topic_2")
                .document (id)
                .delete ();
    }

    public Task<Void> deleteTopicById(String id) {
        return db.collection ("topics_2")
                .document (id)
                .delete ();
    }
}

