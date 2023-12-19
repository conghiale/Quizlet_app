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
import com.nhom12.ourquizlet.data.model.Word;
import com.nhom12.ourquizlet.data.repository.WordRepository;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class WordViewModel extends ViewModel {
    private final FirebaseFirestore db;
    private final FirebaseAuth auth;
    private final ExecutorService executorService;
    private final MutableLiveData<List<Word>> mWords;
    private Word word;

    public WordViewModel() {
        db = FirebaseFirestore.getInstance ();
        auth = FirebaseAuth.getInstance ();
        executorService = Executors.newFixedThreadPool (1);
        mWords = new MutableLiveData<> ();
        cloneWordAll ();
    }

    private void cloneWordAll() {
        executorService.execute (() -> {
            db.collection ("words_2")
                .get ()
                .addOnCompleteListener (task -> {
                   if (task.isSuccessful () && auth.getCurrentUser () != null) {
                       mWords.setValue (task.getResult ().toObjects (Word.class));
                       db.collection ("user_word_2")
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

    public void updateWordsLocal(Word word, boolean isCreate) {
        if (mWords.getValue () != null)  {
            if (!isCreate) {
                for (Word w : mWords.getValue ()) {
                    if (w.getId ().equals (word.getId ())) {
                        w.setDefine (word.getDefine ());
                        w.setTerm (word.getTerm ());
                        Log.d ("TAG", "updateWordsLocal - 83: " + w.getTerm () + " - " + w.getDefine ());
                        break;
                    }
                }
            } else
                mWords.getValue ().add (word);
        }

        this.mWords.setValue (mWords.getValue ());
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
            db.collection ("user_word_2")
                .whereEqualTo ("idUser", auth.getCurrentUser ().getUid ())
                .whereEqualTo ("idWord", word.getId ())
                .get ()
                .addOnSuccessListener (queryDocumentSnapshots -> {
                    if (queryDocumentSnapshots.isEmpty ()) {
                        Map<String, Object> newUserWord = new HashMap<> ();
                        newUserWord.put ("idUser", auth.getCurrentUser ().getUid ());
                        newUserWord.put ("idWord", word.getId ());
                        newUserWord.put ("star", word.isStar ());

                        db.collection ("user_word_2")
                            .add (newUserWord)
                            .addOnSuccessListener (documentReference -> {
                                runnableSuccess.run ();
                            });

                        updateStarOfWordLocal (word.getId (), word.isStar ());
                    } else {
                        String id = queryDocumentSnapshots.getDocuments ().get (0).getId ();
                        db.collection ("user_word_2")
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
            db.collection ("user_word_2")
                    .whereEqualTo ("idUser", auth.getCurrentUser ().getUid ())
                    .whereEqualTo ("idWord", word.getId ())
                    .get ()
                    .addOnSuccessListener (queryDocumentSnapshots -> {
                        if (queryDocumentSnapshots.isEmpty ()) {
                            Map<String, Object> newUserWord = new HashMap<> ();
                            newUserWord.put ("idUser", auth.getCurrentUser ().getUid ());
                            newUserWord.put ("idWord", word.getId ());
                            newUserWord.put ("status", word.getStatus ());

                            db.collection ("user_word_2")
                                    .add (newUserWord)
                                    .addOnSuccessListener (documentReference -> {
                                        runnableSuccess.run ();
                                    });

                            updateStatusOfWordLocal (word.getId (), word.getStatus ());
                        } else {
                            String id = queryDocumentSnapshots.getDocuments ().get (0).getId ();
                            db.collection ("user_word_2")
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
                    break;
                }
            }
        }
    }

    public Task<DocumentReference> createWordsInTopic (Word word) {
        return WordRepository.getInstance ().createWordsInTopic (word);
    }

    public Task<Void> updatedIdWord(String idWord) {
        return WordRepository.getInstance ().updateIdWord (idWord);
    }

    public Task<DocumentReference> updateWordInUserWord (Word word, String idUser) {
        return WordRepository.getInstance ().updateWordInUserWord (word, idUser);
    }

    public Task<Void> updateFieldsWord (Word word) {
        return WordRepository.getInstance ().updateFieldsWord (word);
    }

    public Task<QuerySnapshot> getUserWordByIdWord (String idWord) {
        return WordRepository.getInstance ().getUserWordByIdWord (idWord);
    }

    public Task<Void> deleteUserWordById (String id) {
        return WordRepository.getInstance ().deleteUserWordById (id);
    }

    public Task<Void> deleteWordById (String id) {
        return WordRepository.getInstance ().deleteWordById (id);
    }

    public void deleteWordByIdLocal (String id) {
        if (mWords.getValue () != null) {
            Log.d ("TAG", "deleteWordByIdLocal - 232: mWords.getValue (): " + mWords.getValue ().size ());
            for (Word w : mWords.getValue ()) {
                if (w.getId ().equals (id)) {
                    mWords.getValue ().remove (w);
                    break;
                }
            }
        }
        Log.d ("TAG", "deleteWordByIdLocal - 240: mWords.getValue (): " + mWords.getValue ().size ());
        this.mWords.setValue (this.mWords.getValue ());
    }
}
