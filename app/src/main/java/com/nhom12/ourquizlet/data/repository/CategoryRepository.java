package com.nhom12.ourquizlet.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.data.model.CategoryTopic;

import java.util.HashMap;
import java.util.Map;

public class CategoryRepository {
    private FirebaseFirestore db;
    private static CategoryRepository instance;

    private CategoryRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static CategoryRepository getInstance () {
        if (instance == null) {
            synchronized (TopicRepository.class) {
                if (instance == null) {
                    instance = new CategoryRepository ();
                }
            }
        }
        return instance;
    }

    public Task<DocumentReference> createCategory(CategoryTopic categoryTopic) {
        Map<String, Object> mCategory = new HashMap<> ();
        mCategory.put ("title", categoryTopic.getTitle ());

        return db.collection ("categories_topic")
                .add (mCategory);
    }

    public Task<Void> updateIdCategory (String id) {
        return db.collection ("categories_topic")
                .document (id)
                .update ("id", id);
    }
}
