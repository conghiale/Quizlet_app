package com.nhom12.ourquizlet.ui.viewModel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.data.model.Word;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordHomeViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Word>> mWords;

    public WordHomeViewModel() {
        db = FirebaseFirestore.getInstance ();
        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);
        mWords = new MutableLiveData<> ();
        cloneWordAll ();
    }

    private void cloneWordAll() {
        executorService.execute (() -> {
            db.collection ("words")
                .get ()
                .addOnCompleteListener (task -> {
                   if (task.isSuccessful () && auth.getCurrentUser () != null) {
                       mWords.setValue (task.getResult ().toObjects (Word.class));
                       db.collection ("user_word")
                           .whereEqualTo ("idUser", auth.getCurrentUser ().getUid ())
                           .get ()
                           .addOnCompleteListener (task1 -> {
                               for (DocumentSnapshot d : task1.getResult ()) {
                                   String idWord = d.getString ("idWord");
                                   String status = d.getString ("status");
                                   boolean star = Boolean.TRUE.equals (d.getBoolean ("star"));

                                   if (mWords.getValue () != null) {
                                       Optional<Word> word = mWords.getValue ().stream()
                                               .filter (word1 -> word1.getId ().equals (idWord)).findFirst ();
                                       word.ifPresent (value -> {
                                           if (status != null)
                                                value.setStatus (status);
                                           value.setStar (star);
                                       });
                                   }
                               }
                           });
                   }
                });
        });
    }

    public LiveData<List<Word>> getWordAll () {
        return mWords;
    }

    public List<Word> getWordsOfTopic (String idTopic) {
        List<Word> words = new ArrayList<> ();
        if (mWords.getValue () != null && !mWords.getValue ().isEmpty ()) {
            for (Word w : mWords.getValue ()) {
                if (w.getIdTopic ().equals (idTopic))
                    words.add (w);
            }
        }
        return words;
    }

    public void updateStarWord(Word word, Runnable runnableError, Runnable runnableSuccess) {
        if (auth.getCurrentUser () != null) {
            db.collection ("user_word")
                    .whereEqualTo ("idUser", auth.getCurrentUser ().getUid ())
                    .whereEqualTo ("idWord", word.getId ())
                    .get ()
                    .addOnSuccessListener (queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty ()) {
                            Map<String, Object> newUserWord = new HashMap<> ();
                            newUserWord.put ("idUser", auth.getCurrentUser ().getUid ());
                            newUserWord.put ("idWord", word.getId ());
                            newUserWord.put ("star", word.isStar ());

                            db.collection ("user_word")
                                .add (newUserWord)
                                .addOnSuccessListener (documentReference -> {
                                    runnableSuccess.run ();
                                });

                            updateStarOfWordLocal (word.getId (), word.isStar ());
                        } else {
                            String id = queryDocumentSnapshots.getDocuments ().get (0).getId ();
                            db.collection ("user_word")
                                .document (id)
                                .update ("star", word.isStar ())
                                .addOnSuccessListener (unused -> {
                                    runnableSuccess.run ();
                                });

                            updateStarOfWordLocal (word.getId (), word.isStar ());
                        }
                    })
                    .addOnFailureListener (e -> runnableError.run ())
                    .addOnCanceledListener (runnableError::run);
        }
    }

    public void updateStatusWord(Word word, Runnable runnableError, Runnable runnableSuccess) {
        if (auth.getCurrentUser () != null) {
            db.collection ("user_word")
                    .whereEqualTo ("idUser", auth.getCurrentUser ().getUid ())
                    .whereEqualTo ("idWord", word.getId ())
                    .get ()
                    .addOnSuccessListener (queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty ()) {
                            Map<String, Object> newUserWord = new HashMap<> ();
                            newUserWord.put ("idUser", auth.getCurrentUser ().getUid ());
                            newUserWord.put ("idWord", word.getId ());
                            newUserWord.put ("status", word.getStatus ());

                            db.collection ("user_word")
                                    .add (newUserWord)
                                    .addOnSuccessListener (documentReference -> {
                                        runnableSuccess.run ();
                                    });

                            updateStatusOfWordLocal (word.getId (), word.getStatus ());
                        } else {
                            String id = queryDocumentSnapshots.getDocuments ().get (0).getId ();
                            db.collection ("user_word")
                                    .document (id)
                                    .update ("status", word.getStatus ())
                                    .addOnSuccessListener (unused -> {
                                        runnableSuccess.run ();
                                    });

                            updateStatusOfWordLocal (word.getId (), word.getStatus ());
                        }
                    })
                    .addOnFailureListener (e -> runnableError.run ())
                    .addOnCanceledListener (runnableError::run);
        }
    }

    private void updateStatusOfWordLocal (String idWord, String status) {
        if (mWords.getValue () != null) {
            for (Word w : mWords.getValue ()) {
                if (w.getId ().equals (idWord)) {
                    w.setStatus (status);
//                    mWords.setValue (mWords.getValue ());
                    break;
                }
            }
        }
    }

    private void updateStarOfWordLocal (String idWord, boolean star) {
        if (mWords.getValue () != null) {
            for (Word w : mWords.getValue ()) {
                if (w.getId ().equals (idWord)) {
                    w.setStar (star);
//                    mWords.setValue (mWords.getValue ());
                    break;
                }
            }
        }
    }
}
