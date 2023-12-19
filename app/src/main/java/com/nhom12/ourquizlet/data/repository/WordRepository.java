package com.nhom12.ourquizlet.data.repository;

import com.google.android.gms.tasks.Task;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.QuerySnapshot;
import com.nhom12.ourquizlet.data.model.Word;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WordRepository {
    private final FirebaseFirestore db;
    private static WordRepository instance;

    private WordRepository() {
        db = FirebaseFirestore.getInstance();
    }

    public static WordRepository getInstance () {
        if (instance == null) {
            synchronized (WordRepository.class) {
                if (instance == null) {
                    instance = new WordRepository ();
                }
            }
        }
        return instance;
    }

    public Task<DocumentReference> createWordsInTopic(Word word) {
        Map<String, Object> mWord = new HashMap<> ();
        mWord.put ("define", word.getDefine ());
        mWord.put ("idTopic", word.getIdTopic ());
        mWord.put ("term", word.getTerm ());

        return db.collection ("words_2")
            .add (mWord);
    }

    public Task<Void> updateIdWord (String id) {
        return db.collection ("words_2")
                .document (id)
                .update ("id", id);
    }

    public Task<DocumentReference> updateWordInUserWord (Word word, String idUser) {
        Map<String, Object> mUserWord = new HashMap<> ();
        mUserWord.put ("idWord", word.getId ());
        mUserWord.put ("idUser", idUser);
        mUserWord.put ("status", "Don't Known");
        mUserWord.put ("star", false);
        return db.collection ("user_word_2")
                .add (mUserWord);
    }

    public Task<Void> updateFieldsWord (Word word) {
        Map<String, Object> mWord = new HashMap<> ();
        mWord.put ("define", word.getDefine ());
        mWord.put ("idTopic", word.getIdTopic ());
        mWord.put ("term", word.getTerm ());

        return db.collection ("words_2")
                .document (word.getId ())
                .update (mWord);
    }

    public Task<QuerySnapshot> getUserWordByIdWord(String idWord) {
        return db.collection ("user_word_2")
            .whereEqualTo ("idWord", idWord)
            .get ();
    }

    public Task<Void> deleteUserWordById(String id) {
        return db.collection ("user_word_2")
                .document (id)
                .delete ();
    }

    public Task<Void> deleteWordById(String id) {
        return db.collection ("words_2")
                .document (id)
                .delete ();
    }
}

