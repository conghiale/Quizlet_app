package com.nhom12.ourquizlet.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.data.model.CategoryTopic;
import com.nhom12.ourquizlet.data.repository.CategoryRepository;

import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class CategoryViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;
    private final MutableLiveData<List<CategoryTopic>> mCategory;

    public CategoryViewModel() {
        db = FirebaseFirestore.getInstance ();
        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);
        mCategory = new MutableLiveData<> ();
        cloneCategoryAll ();
    }

    private void cloneCategoryAll() {
        executorService.execute (() -> {
            db.collection ("categories_topic")
                .get ()
                .addOnCompleteListener (task -> {
                    if (task.isSuccessful () && auth.getCurrentUser () != null) {
                        mCategory.setValue (task.getResult ().toObjects (CategoryTopic.class));
                    }
                });
        });
    }

    public LiveData<List<CategoryTopic>> getCategoryAll () {
        return mCategory;
    }

    public void setCategories (List<CategoryTopic> categoryTopics) {
        mCategory.setValue (categoryTopics);
    }

    public Task<DocumentReference> createCategory (CategoryTopic categoryTopic) {
        return CategoryRepository.getInstance ().createCategory (categoryTopic);
    }

    public Task<Void> updatedIdCategory(String idCategory) {
        return CategoryRepository.getInstance ().updateIdCategory (idCategory);
    }
}
