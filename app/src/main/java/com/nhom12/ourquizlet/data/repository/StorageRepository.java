package com.nhom12.ourquizlet.data.repository;

import android.net.Uri;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;
import java.util.UUID;

public class StorageRepository {
    public void uploadImageToFirebase(Uri imageUri, OnSuccessListener<Uri> onSuccessListener, OnFailureListener onFailureListener) {
        FirebaseStorage storage = FirebaseStorage.getInstance();
        if (imageUri == null) {
            onFailureListener.onFailure(new Exception("Image URI is null"));
            return;
        }

        String imageFileName = UUID.randomUUID().toString() + ".jpg";
        StorageReference imageRef = storage.getReference().child(imageFileName);

        imageRef.putFile(imageUri)
                .continueWithTask(task -> {
                    if (!task.isSuccessful()) {
                        throw task.getException();
                    }
                    return imageRef.getDownloadUrl();
                })
                .addOnSuccessListener(onSuccessListener)
                .addOnFailureListener(onFailureListener);
    }
}
