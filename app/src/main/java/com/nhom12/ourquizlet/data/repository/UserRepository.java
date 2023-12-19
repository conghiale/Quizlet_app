package com.nhom12.ourquizlet.data.repository;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.gms.tasks.Tasks;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.EmailAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.firestore.FirebaseFirestore;
import com.nhom12.ourquizlet.data.model.User;

import java.util.Map;
import java.util.Objects;

public class UserRepository {
    private FirebaseAuth firebaseAuth;
    private FirebaseFirestore firebaseFirestore;


    public UserRepository() {
        firebaseAuth = FirebaseAuth.getInstance();
        firebaseFirestore = FirebaseFirestore.getInstance();
    }

    // Đăng ký
    public Task<AuthResult> register(String email, String password) {
        return firebaseAuth.createUserWithEmailAndPassword(email, password);
    }

    // Đăng nhập
    public Task<AuthResult> login(String email, String password) {
        return firebaseAuth.signInWithEmailAndPassword(email, password);
    }

    // Gửi link đặt lại mật khẩu
    public Task<Void> sendPasswordResetEmail(String email) {
        return firebaseAuth.sendPasswordResetEmail(email);
    }

    // Đổi mật khẩu
    public Task<Void> updatePassword(String currentPassword, String newPassword) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            return Tasks.forException(new Exception("No authenticated user"));
        }
        // Reauthentication
        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        return user.reauthenticate(credential)
                .continueWithTask(task -> {
                    if (task.isSuccessful()) {
                        return user.updatePassword(newPassword);
                    } else {
                        throw Objects.requireNonNull(task.getException());
                    }
                });
    }

    // Update email with verification and reauthentication
    public Task<Void> updateEmailWithVerification(String currentPassword, String newEmail) {
        FirebaseUser user = firebaseAuth.getCurrentUser();

        if (user == null || user.getEmail() == null) {
            return Tasks.forException(new Exception("No authenticated user"));
        }

        AuthCredential credential = EmailAuthProvider.getCredential(user.getEmail(), currentPassword);
        return user.reauthenticate(credential).continueWithTask(task -> {
            if (task.isSuccessful()) {
                return user.verifyBeforeUpdateEmail(newEmail);
            } else {
                throw Objects.requireNonNull(task.getException());
            }
        });
    }

    // Thêm user vào Firestore
    public Task<Void> addUserToFirestore(User user) {
        FirebaseUser firebaseUser = firebaseAuth.getCurrentUser();
        if (firebaseUser != null) {
            //  UID từ Firebase Authentication
            return firebaseFirestore.collection("users").document(firebaseUser.getUid()).set(user);
        } else {
            return Tasks.forException(new Exception("No authenticated user"));
        }
    }

    public void editUser(String userId, Map<String, Object> updatedUser, OnCompleteListener<Void> onCompleteListener) {
        firebaseFirestore.collection("users").document(userId)
                .update(updatedUser)
                .addOnCompleteListener(onCompleteListener);
    }
}
