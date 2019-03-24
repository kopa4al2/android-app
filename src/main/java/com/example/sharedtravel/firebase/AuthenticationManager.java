package com.example.sharedtravel.firebase;

import android.app.Activity;
import android.net.Uri;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import com.example.sharedtravel.model.User;
import com.example.sharedtravel.services.NotificationService;
import com.example.sharedtravel.views.NotificationView;
import com.facebook.AccessToken;
import com.facebook.login.LoginManager;
import com.google.firebase.FirebaseNetworkException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FacebookAuthProvider;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthProvider;
import com.google.firebase.auth.UserProfileChangeRequest;

import java.util.Observable;
import java.util.concurrent.TimeUnit;


public final class AuthenticationManager extends Observable {

    private final String TAG = this.getClass().getSimpleName() + "DEBUG";

    private final FirebaseAuth mAuth = FirebaseAuth.getInstance();

    private FirebaseUser currentFirebaseUser;
    private User currentLoggedInUser;

    private volatile static AuthenticationManager _this;

    public static AuthenticationManager getInstance() {
        if (_this == null)
            synchronized (AuthenticationManager.class) {
                if (_this == null)
                    return new AuthenticationManager();
            }
        return _this;
    }

    private AuthenticationManager() {
        currentFirebaseUser = mAuth.getCurrentUser();
        if (currentFirebaseUser != null)
            UsersManager.getUser(currentFirebaseUser.getUid(), (user) -> currentLoggedInUser = (User) user);
        _this = this;
    }

    public FirebaseUser getLoggedInFirebaseUser() {
        return currentFirebaseUser;
    }

    public boolean isLoggedIn() {
        return this.currentFirebaseUser != null;
    }

    public void login(String email, String password, AppCompatActivity activity, AsyncListener listener) {

        listener.onStart();
        mAuth.signInWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        currentFirebaseUser = mAuth.getCurrentUser();
                        NotificationService.addNewMessageListener(currentFirebaseUser.getUid(), currentFirebaseUser.getMetadata().getLastSignInTimestamp());
                        UsersManager.getUser(currentFirebaseUser.getUid(), (user) -> currentLoggedInUser = (User) user);
                        setChanged();
                        notifyObservers(mAuth.getCurrentUser());
                        listener.onSuccess();
                    } else {
                        if (task.getException() instanceof FirebaseNetworkException)
                            return;
                        listener.onFail(task.getException());
                    }
                });
    }

    public void loginWithFacebook(AccessToken token, Activity activity) {
        AuthCredential credential = FacebookAuthProvider.getCredential(token.getToken());
        mAuth.signInWithCredential(credential)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful()) {
                        // Sign in success, update UI with the signed-in user's information
                        FirebaseUser user = mAuth.getCurrentUser();
                        //TODO: figure out a timesstamp for starter point of messages
                        NotificationService
                                .addNewMessageListener(
                                        user.getUid(), 0);
                        if (task.getResult().getAdditionalUserInfo().isNewUser()) {
                            UsersManager.addUserToDb(user);
                        }
                        currentFirebaseUser = user;
                        UsersManager.getUser(currentFirebaseUser.getUid(), (user1) -> currentLoggedInUser = (User) user1);
                        setChanged();
                        notifyObservers(user);
                    } else {
                        Log.w("fb_tag", "signInWithCredential:failure", task.getException());
                    }

                });
    }

    public void createAccount(String email,
                              String password,
                              String username,
                              AppCompatActivity activity,
                              AsyncListener listener) {
        listener.onStart();
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnCompleteListener(activity, task -> {
                    if (task.isSuccessful() && task.getResult() != null) {
                        currentFirebaseUser = mAuth.getCurrentUser();
                        UsersManager.addUserToDb(task.getResult().getUser());
                        this.updateUsername(username);
                        UsersManager.getUser(currentFirebaseUser.getUid(), (user) -> currentLoggedInUser = (User) user);
                        NotificationService
                                .addNewMessageListener(currentFirebaseUser.getUid(),
                                        currentFirebaseUser.getMetadata().getLastSignInTimestamp());
                        setChanged();
                        notifyObservers(mAuth.getCurrentUser());
                        listener.onSuccess();
                    } else {
                        listener.onFail(task.getException());
                    }
                });
    }

    public void logout() {
        NotificationService.unRegisterListener();
        currentFirebaseUser = null;
        mAuth.signOut();
        LoginManager.getInstance().logOut();
        setChanged();
        notifyObservers(currentFirebaseUser);
    }

    public void updateUsername(String username) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setDisplayName(username)
                .build();

        currentFirebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UsersManager.updateFirebaseUser(currentFirebaseUser);
                        setChanged();
                        notifyObservers(currentFirebaseUser);
                    }
                });
    }

    public void updatePhoto(String photoUrl) {
        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder()
                .setPhotoUri(Uri.parse(photoUrl))
                .build();
        currentFirebaseUser.updateProfile(profileUpdates)
                .addOnCompleteListener(task -> {
                    if (task.isSuccessful()) {
                        UsersManager.updateFirebaseUser(currentFirebaseUser);
                        setChanged();
                        notifyObservers(currentFirebaseUser);
                    } else {
                        Log.e(TAG, "updatePhoto: ERROR ");
                    }
                });
    }

    public void updatePhoneNumber(String phoneNumber, Activity activity, PhoneAuthProvider.OnVerificationStateChangedCallbacks listener) {
        PhoneAuthProvider.getInstance().verifyPhoneNumber(
                phoneNumber,        // Phone number to verify
                10,                 // Timeout duration
                TimeUnit.SECONDS,   // Unit of timeout
                activity,               // Activity (for callback binding)
                listener);
    }

    public User getCurrentLoggedInUser() {
        return currentLoggedInUser;
    }
}
