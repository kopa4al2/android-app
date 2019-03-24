package com.example.sharedtravel;

import android.Manifest;
import android.animation.Animator;
import android.animation.AnimatorListenerAdapter;
import android.app.Activity;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.NavigationView;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Gravity;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.cloudinary.android.MediaManager;
import com.cloudinary.android.ResponsiveUrl;
import com.example.sharedtravel.callbacks.LoginCallback;
import com.example.sharedtravel.firebase.AsyncListener;
import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.fragments.AddUrbanTravelFragment;
import com.example.sharedtravel.fragments.AllChatsFragment;
import com.example.sharedtravel.fragments.FindIntercityTravelFragment;
import com.example.sharedtravel.fragments.FindUrbanTravelFragment;
import com.example.sharedtravel.fragments.RegisterIntercityTravelFragment;
import com.example.sharedtravel.fragments.UserProfileFragment;
import com.example.sharedtravel.fragments.dialogs.AuthenticationDialogBuilder;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;
import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.model.MessageSender;
import com.example.sharedtravel.services.NotificationService;
import com.example.sharedtravel.utils.BindOnScreenTouchHideSoftKeyboard;
import com.example.sharedtravel.utils.CloudinaryCallbackImpl;
import com.example.sharedtravel.utils.Constants;
import com.example.sharedtravel.utils.SecurePreferences;
import com.example.sharedtravel.views.NotificationView;
import com.example.sharedtravel.views.NotificationsRecyclerAdapter;
import com.facebook.CallbackManager;
import com.facebook.FacebookCallback;
import com.facebook.FacebookException;
import com.facebook.login.LoginResult;
import com.facebook.login.widget.LoginButton;
import com.google.common.collect.Lists;
import com.google.firebase.FirebaseException;
import com.google.firebase.FirebaseTooManyRequestsException;
import com.google.firebase.auth.AuthCredential;
import com.google.firebase.auth.FirebaseAuthException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.PhoneAuthCredential;
import com.google.firebase.auth.PhoneAuthProvider;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.lang.reflect.InvocationTargetException;
import java.util.Map;
import java.util.Observable;
import java.util.Observer;
import java.util.Set;

import de.hdodenhof.circleimageview.CircleImageView;

import static android.view.View.GONE;

public class MainActivity extends AppCompatActivity implements Observer,
        UserProfileFragment.OnFragmentInteractionListener,
        RegisterIntercityTravelFragment.OnFragmentInteractionListener,
        NotificationView.NotificationListener,
        ChatBox.ChatBoxListener {

    public static final String PREFS_NAME = "LoginInfo";
    public static final String PREF_EMAIL = "email";
    public static final String PREF_PASSWORD = "password";

    private static final String CURRENT_VERIFICATION_ID = "current_verification_id";
    private static final String IS_PHONE_VERIFICATION_IN_PROGRESS = "is_phone_initiated";

    private static final String TAG = MainActivity.class.getSimpleName() + "DEBUG";
    public static final int PERMISSION_REQUEST_CODE = 200;
    private static final String SECURITY_KEY = "qpw2aos6ldk4fjt";
    private static final int FRAGMENT_CONTAINER_ID = R.id.fragment_container;

    private static final Class<? extends Fragment> ROOT_FRAGMENT_VIEW = FindIntercityTravelFragment.class;


    private final Picasso picasso = Picasso.get();
    private final MediaManager mediaManager = MediaManager.get();

    private AuthenticationManager authenticationManager;
    private SecurePreferences securePreferences;

    private View notificationsListView;
    private DrawerLayout drawerLayout;
    public NavigationView navigationView;
    private ActionBar actionBar;

    private Fragment currentFragment;

    CallbackManager mCallbackManager;

    private boolean isPhoneVerificationInProgress = false;
    private String currentVerificationId = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        if (checkPermission()) {
            requestPermission();
        }

        authenticationManager = AuthenticationManager.getInstance();
        initActionBar();

        reportFullyDrawn();

        notificationsListView = findViewById(R.id.new_notifications_view);
        authenticationManager.addObserver(this);
        securePreferences = new SecurePreferences(this, PREFS_NAME, SECURITY_KEY, true);
        initDrawer();


    }


    public void showFragment(Class<? extends Fragment> fragmentClass, Bundle fragmentArguments) {
        if (currentFragment != null) {
            if (currentFragment instanceof Observer)
                authenticationManager.deleteObserver((Observer) currentFragment);
        }
        try {
            View loadingView = findViewById(R.id.loading_overlay_main);
            loadingView.setVisibility(View.VISIBLE);
            Fragment fragment = fragmentClass.getConstructor().newInstance();
            String currentFragmentTag = fragment.getClass().getSimpleName();
            currentFragment = fragment;
            if (fragment instanceof Observer)
                authenticationManager.addObserver((Observer) fragment);
            if (fragmentArguments != null)
                currentFragment.setArguments(fragmentArguments);
            getSupportFragmentManager().beginTransaction()
                    .addToBackStack(currentFragmentTag)
                    .replace(FRAGMENT_CONTAINER_ID, fragment, currentFragmentTag)
                    .commitAllowingStateLoss();

            View fragmentContainerView = findViewById(FRAGMENT_CONTAINER_ID);
            fragmentContainerView.setAlpha(0f);
            fragmentContainerView.setVisibility(View.VISIBLE);

            fragmentContainerView.animate()
                    .alpha(1f)
                    .setDuration(400)
                    .setListener(null);


            loadingView.animate()
                    .alpha(0f)
                    .setDuration(400)
                    .setListener(new AnimatorListenerAdapter() {
                        @Override
                        public void onAnimationEnd(Animator animation) {
                            loadingView.setAlpha(0.65f);
                            loadingView.setVisibility(GONE);
                        }
                    });

        } catch (IllegalAccessException e) {
            e.printStackTrace();
        } catch (InstantiationException e) {
            e.printStackTrace();
        } catch (NoSuchMethodException e) {
            e.printStackTrace();
        } catch (InvocationTargetException e) {
            e.printStackTrace();
        }
    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        if (requestCode == Constants.INTENT_PICK_PROFILE_PHOTO_REQUEST_CODE)
            if (resultCode == Activity.RESULT_OK) {
                if (data != null && data.getData() != null) {
                    Uri selectedImage = data.getData();

                    //TODO: make it prettier
                    LinearLayout layout = new LinearLayout(this);
                    layout.setLayoutParams(new LinearLayout.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
                    layout.setGravity(Gravity.CENTER);
                    ImageView imageView = new ImageView(this);
                    imageView.setVisibility(View.VISIBLE);
                    layout.addView(imageView);
                    picasso.load(selectedImage).resize(400, 400).into(imageView);

                    DialogBuilder.showConfirmationDialog(MainActivity.this, "", layout, () -> {
                        uploadProfilePicture(selectedImage);
                    });
                }
            }
        super.onActivityResult(requestCode, resultCode, data);
        if (mCallbackManager != null)
            mCallbackManager.onActivityResult(requestCode, resultCode, data);
    }

    private void uploadProfilePicture(Uri selectedImage) {
        String userUuid = authenticationManager.getLoggedInFirebaseUser().getUid();
        final String profilePicturePublicId = Constants.CLOUDINARY_PUBLIC_ID_PREFIX + userUuid;
        mediaManager.cancelAllRequests();
        mediaManager
                .upload(selectedImage)
                .option("resource_type", "image")
                .option("folder", Constants.CLOUDINARY_MY_FOLDER)
                .option("public_id", profilePicturePublicId)
                .option("overwrite", true)
                .callback(new CloudinaryCallbackImpl() {
                    @Override
                    public void onSuccess(String requestId, Map resultData) {
                        String secureUrl = (String) resultData.get("secure_url");
                        authenticationManager.updatePhoto(secureUrl);
                    }
                })
                .dispatch(this);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                drawerLayout.openDrawer(GravityCompat.START);
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void initDrawer() {
        drawerLayout = findViewById(R.id.drawer_layout);
        navigationView = findViewById(R.id.nav_view);
        initDrawerUI();
        navigationView.setNavigationItemSelectedListener(
                menuItem -> {
                    detachChatBox();
                    if (menuItem.isChecked())
                        return false;

                    drawerLayout.setDrawerListener(new DrawerLayout.DrawerListener() {
                        @Override
                        public void onDrawerSlide(@NonNull View view, float v) {
                        }

                        @Override
                        public void onDrawerOpened(@NonNull View view) {
                        }

                        @Override
                        public void onDrawerStateChanged(int i) {
                        }

                        @Override
                        public void onDrawerClosed(@NonNull View view) {
                            handleNavigationMenuClick(menuItem);

                            drawerLayout.removeDrawerListener(this);
                        }
                    });

                    drawerLayout.closeDrawers();
                    if (navigationView.getCheckedItem() != null)
                        navigationView.getCheckedItem().setChecked(false);
                    return true;
                });
    }

    private void handleNavigationMenuClick(MenuItem menuItem) {
        switch (menuItem.getItemId()) {
            case R.id.btn_sign_out: {
                authenticationManager.logout();
                showFragment(ROOT_FRAGMENT_VIEW, null);
                showLoginDialog();
            }
            break;
            case R.id.btn_login_drawer: {
                showLoginDialog();
            }
            break;
            case R.id.btn_register_drawer: {
                showRegisterDialog();
            }
            break;
            case R.id.drawer_register_intercity: {
                showFragment(RegisterIntercityTravelFragment.class, null);
            }
            break;
            case R.id.drawer_find_intercity: {
                showFragment(FindIntercityTravelFragment.class, null);
            }
            break;
            case R.id.edit_profile: {
                showFragment(UserProfileFragment.class, null);
            }
            break;
            case R.id.register_urban: {
                showFragment(AddUrbanTravelFragment.class, null);
            }
            break;
            case R.id.find_urban: {
                showFragment(FindUrbanTravelFragment.class, null);
            }
            break;
        }
    }

    protected void initDrawerUI() {
        View drawerHeaderView = navigationView.getHeaderView(0);

        navigationView.getMenu().clear();
        navigationView.inflateMenu(R.menu.drawer_menu);

        CircleImageView profilePic = drawerHeaderView.findViewById(R.id.drawer_profile_pic);
        TextView username = drawerHeaderView.findViewById(R.id.drawer_username);
        TextView email = drawerHeaderView.findViewById(R.id.drawer_email);

        if (authenticationManager.isLoggedIn()) {
            profilePic.setVisibility(View.VISIBLE);
            username.setVisibility(View.VISIBLE);
            email.setVisibility(View.VISIBLE);
            FirebaseUser loggedInUser = authenticationManager.getLoggedInFirebaseUser();
            if (loggedInUser.getPhotoUrl() != null) {
                mediaManager
                        .responsiveUrl(
                                profilePic,
                                String.valueOf(loggedInUser.getPhotoUrl()),
                                ResponsiveUrl.Preset.AUTO_FILL, url -> {
                                    String gen = url.generate();
                                    picasso.load(gen).into(profilePic);
                                });
            } else {
                profilePic.setImageResource(R.mipmap.blank_profile_picture);
            }

            username.setText(loggedInUser.getDisplayName());
            email.setText(loggedInUser.getEmail());
            navigationView.getMenu().setGroupVisible(R.id.drawer_user_not_logged_in_group, false);
            navigationView.getMenu().setGroupVisible(R.id.drawer_user_logged_in_group, true);
            navigationView.getMenu().findItem(R.id.drawer_register_intercity).setVisible(true);
            navigationView.getMenu().findItem(R.id.register_urban).setVisible(true);
        } else {
            profilePic.setVisibility(GONE);
            username.setVisibility(GONE);
            email.setVisibility(GONE);
            navigationView.getMenu().findItem(R.id.drawer_register_intercity).setVisible(false);
            navigationView.getMenu().findItem(R.id.register_urban).setVisible(false);
            navigationView.getMenu().setGroupVisible(R.id.drawer_user_not_logged_in_group, true);
            navigationView.getMenu().setGroupVisible(R.id.drawer_user_logged_in_group, false);
        }
    }

    private void initActionBar() {

        if (actionBar == null || getSupportActionBar() == null) {
            setSupportActionBar(findViewById(R.id.toolbar));
            actionBar = getSupportActionBar();

        }

        if (authenticationManager.getLoggedInFirebaseUser() == null) {
            setActionBarAnonymous();
            findViewById(R.id.notification_view).setVisibility(GONE);
        } else {
            setActionBarCurrentUser(authenticationManager.getLoggedInFirebaseUser());
            findViewById(R.id.notification_view).setVisibility(View.VISIBLE);
        }
    }

    private void setActionBarCurrentUser(FirebaseUser currentUser) {

        if (actionBar != null) {
            actionBar.setCustomView(R.layout.actionbar_logged_in_layout);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);

            actionBar.setHomeButtonEnabled(true);
            actionBar.setDisplayHomeAsUpEnabled(true);
            actionBar.setHomeAsUpIndicator(R.drawable.ic_menu_black);

            NotificationService
                    .addNewMessageListener(authenticationManager.getLoggedInFirebaseUser().getUid(),
                            currentUser.getMetadata().getLastSignInTimestamp());
            NotificationService.getInstance().addObserver(findViewById(R.id.notification_view));

            NotificationView notificationView = (NotificationView) findViewById(R.id.notification_view);
            notificationView.init(currentUser);
            notificationView.setNotificationListener(this);
            authenticationManager.addObserver(notificationView);
        }
    }


    private void setActionBarAnonymous() {

        if (actionBar != null) {
            actionBar.setCustomView(R.layout.actionbar_not_logged_in_layout);
            actionBar.setDisplayOptions(ActionBar.DISPLAY_SHOW_CUSTOM);
        }

//        INIT LOGIN BUTTON AND ONCLICK LISTENER
        Button loginButton = findViewById(R.id.btn_login);
        if (loginButton != null && !loginButton.hasOnClickListeners())
            loginButton.setOnClickListener((v) -> showLoginDialog());

//        INIT REGISTER BUTTON AND ONCLICK LISTENER
        Button registerButton = findViewById(R.id.btn_register);
        if (registerButton != null && !registerButton.hasOnClickListeners())
            registerButton.setOnClickListener((v) -> showRegisterDialog());

        invalidateOptionsMenu();
    }

    private void showRegisterDialog() {
        AuthenticationDialogBuilder.showRegisterAlertDialog(MainActivity.this, (email, password, username, dialog) -> authenticationManager.createAccount(email, password, username, MainActivity.this, new AsyncListener() {
            @Override
            public void onStart() {
                dialog.findViewById(R.id.loading_overlay).setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess() {
                dialog.findViewById(R.id.loading_overlay).setVisibility(GONE);
                DialogBuilder.showSuccessDialog(MainActivity.this, getString(R.string.successfully_registered));
                dialog.dismiss();
            }

            @Override
            public void onFail(Exception exception) {
                showAuthenticationErrorDialog((FirebaseAuthException) exception);
                dialog.findViewById(R.id.loading_overlay).setVisibility(GONE);
            }
        }));
    }

    public void showLoginDialog() {
        mCallbackManager = CallbackManager.Factory.create();

        AuthenticationDialogBuilder.showLoginAlertDialog(
                this,
                securePreferences,
                new LoginCallback() {
                    @Override
                    public void loginUser(String email, String password, android.support.v7.app.AlertDialog dialog, boolean rememberCredentials) {
                        authenticationManager.login(email, password, MainActivity.this, new AsyncListener() {
                            @Override
                            public void onStart() {
                                dialog.findViewById(R.id.loading_overlay).setVisibility(View.VISIBLE);
                            }

                            @Override
                            public void onSuccess() {
                                if (rememberCredentials) {
                                    securePreferences.put(PREF_EMAIL, email);
                                    securePreferences.put(PREF_PASSWORD, password);
                                } else {
                                    securePreferences.clear();
                                }

                                dialog.findViewById(R.id.loading_overlay).setVisibility(GONE);
                                dialog.dismiss();
                                AuthenticationManager.getInstance().notifyObservers();
                                DialogBuilder.showSuccessDialog(MainActivity.this, getString(R.string.successfully_logged_in));
                            }

                            @Override
                            public void onFail(Exception exception) {
                                if (exception instanceof FirebaseTooManyRequestsException)
                                    DialogBuilder.showErrorMessage(MainActivity.this,  getString(R.string.too_many_requests));
                                else
                                    showAuthenticationErrorDialog((FirebaseAuthException) exception);
                                dialog.findViewById(R.id.loading_overlay).setVisibility(GONE);
                            }
                        });
                    }

                    @Override
                    public void loginWithFacebook(AlertDialog dialog) {
                        initFacebookLogin(dialog);
                    }

                });
    }

    private void showAuthenticationErrorDialog(@NotNull FirebaseAuthException exception) {
        int id = getResources().getIdentifier(exception.getErrorCode(), "string", getPackageName());
        if (id != 0)
            DialogBuilder.showErrorMessage(MainActivity.this, getString(id));
    }


    @Override
    public void onBackPressed() {
        if (currentFragment instanceof AddUrbanTravelFragment) {
            ((AddUrbanTravelFragment) getSupportFragmentManager().findFragmentByTag(AddUrbanTravelFragment.TAG))
                    .backPressed();
            return;
        }
        if (getSupportFragmentManager().getBackStackEntryCount() == 0) {
            DialogBuilder.showConfirmationDialog(
                    this,
                    getResources().getString(R.string.exit_app_prompt),
                    this::finish);
        } else {
            if (navigationView.getCheckedItem() != null)
                navigationView.getCheckedItem().setChecked(false);
            super.onBackPressed();
        }
    }

    @Override
    public void changeEmail(String email) {
        authenticationManager.getLoggedInFirebaseUser().updateEmail(email);
    }

    @Override
    public void changeUsername(String username) {
        authenticationManager.updateUsername(username);
    }

    @Override
    public void changePassword(String password) {

    }

    @Override
    public void initiatePhoneNumberLinking(String phoneNumber) {
        if (isPhoneVerificationInProgress)
            return;

        isPhoneVerificationInProgress = true;
        authenticationManager.updatePhoneNumber(phoneNumber, this, new PhoneAuthProvider.OnVerificationStateChangedCallbacks() {
            @Override
            public void onCodeSent(String verificationID,
                                   PhoneAuthProvider.ForceResendingToken forceResendingToken) {

                currentVerificationId = verificationID;
                openConfirmationDialog();
                super.onCodeSent(verificationID, forceResendingToken);
            }


            @Override
            public void onVerificationCompleted(PhoneAuthCredential phoneAuthCredential) {

            }

            @Override
            public void onVerificationFailed(FirebaseException e) {
                DialogBuilder.showErrorMessage(MainActivity.this, e.getMessage());
            }
        });
    }

    private void openConfirmationDialog() {
        AlertDialog dialog = new AlertDialog.Builder(MainActivity.this, R.style.slideInView)
                .setView(R.layout.change_field_layout)
                .setCancelable(false)
                .create();


        dialog.setOnShowListener((dialog1) -> {
            Button cancelButton = ((AlertDialog) dialog1).findViewById(R.id.btn_change_field_cancel);
            cancelButton.setOnClickListener((v) -> {
                isPhoneVerificationInProgress = false;
                dialog.dismiss();
            });
            Button confirmButton = ((AlertDialog) dialog1).findViewById(R.id.btn_change_field_confirm);
            confirmButton.setOnClickListener((v) -> {
                ((AlertDialog) dialog1).findViewById(R.id.change_field_loading).setVisibility(View.VISIBLE);
                isPhoneVerificationInProgress = false;

                String confirmationCode = ((TextInputEditText) ((AlertDialog) dialog1).findViewById(R.id.et_change_field))
                        .getText().toString();
                if(confirmationCode == null || confirmationCode.isEmpty() || confirmationCode.length() != 6) {
                    ((TextInputEditText) ((AlertDialog) dialog1).findViewById(R.id.et_change_field))
                            .setError(getString(R.string.invalid_confirmation_code), getDrawable(R.drawable.ic_error_outline));
                    DialogBuilder.showErrorMessage(MainActivity.this, getString(R.string.invalid_confirmation_code));
                    return;
                }
                AuthCredential credential =
                        PhoneAuthProvider.getCredential(currentVerificationId, confirmationCode);
                AuthenticationManager.getInstance().getLoggedInFirebaseUser().linkWithCredential(
                        credential
                ).addOnCompleteListener((task -> {
                    dialog.dismiss();
                    ((AlertDialog) dialog1).findViewById(R.id.change_field_loading).setVisibility(View.GONE);
                    if (task.isSuccessful())
                        DialogBuilder.showSuccessDialog(MainActivity.this, getString(R.string.successfully_added_phone_number));
                    else
                        DialogBuilder.showErrorMessage(MainActivity.this, task.getException().getMessage());
                }));
            });
        });

        dialog.show();
    }


    @Override
    public void update(Observable o, Object arg) {
        //Observs for user change
        if (o instanceof AuthenticationManager) {
            initActionBar();
            initDrawerUI();
        }
    }

    private boolean checkPermission() {
        int result = 0;

        result = ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(this, Manifest.permission.READ_EXTERNAL_STORAGE)) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.READ_EXTERNAL_STORAGE}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void addTravelSuccessful(IntercityTravel travel) {
        showFragment(ROOT_FRAGMENT_VIEW, null);
    }

    private void initFacebookLogin(AlertDialog dialog) {
        LoginButton fbButton = findViewById(R.id.fb_login_button);
        fbButton.setReadPermissions("email", "public_profile", "user_link");
        fbButton.callOnClick();
        fbButton.registerCallback(mCallbackManager, new FacebookCallback<LoginResult>() {
            @Override
            public void onSuccess(LoginResult loginResult) {
                authenticationManager.loginWithFacebook(loginResult.getAccessToken(),
                        MainActivity.this);
                dialog.dismiss();
            }

            @Override
            public void onCancel() {
            }

            @Override
            public void onError(FacebookException error) {
                dialog.dismiss();
                DialogBuilder.showErrorMessage(MainActivity.this, error.getMessage());
            }
        });
    }

    @Override
    public void showNotifications(Set<MessageSender> usersWhoSentNewMessages) {
        RecyclerView notificationsView = findViewById(R.id.rv_list_new_notifications);
        notificationsView.setLayoutManager(new LinearLayoutManager(this));

        MessageSender currentUser = new MessageSender();
        currentUser.setId(AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid());
        usersWhoSentNewMessages.remove(currentUser);

        NotificationsRecyclerAdapter adapter =
                new NotificationsRecyclerAdapter(this, Lists.newArrayList(usersWhoSentNewMessages), (v) -> {
                    //I set the tag to the message sender so i can access it here
                    MessageSender sender = (MessageSender) v.getTag();
                    openChatBox(sender.getId(), sender.getDisplayName());

                    ((NotificationView) findViewById(R.id.notification_view)).seenNotificationFrom(sender);

                    closeNotifications();
                });
        notificationsView.setAdapter(adapter);
        findViewById(R.id.btn_show_all_notifications).setOnClickListener((v) -> allMessagesClicked());

        //TODO slide up to hide
        if (notificationsListView.getVisibility() == GONE) {
            notificationsListView.setVisibility(View.VISIBLE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_show_notifications);
            animation.setFillEnabled(true);
            notificationsListView.startAnimation(animation);
        } else {
            notificationsListView.setVisibility(GONE);
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_hide_notifications);
            animation.setFillEnabled(true);
            notificationsListView.startAnimation(animation);
        }
    }

    private void closeNotifications() {

        notificationsListView.setVisibility(GONE);
        Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_hide_notifications);
        animation.setFillEnabled(true);
        notificationsListView.startAnimation(animation);
    }

    private void allMessagesClicked() {
        Bundle fragmentArguments = new Bundle();
        fragmentArguments.putString(UserProfileFragment.SHOW_INNER_FRAGMENT_BUNDLE_TAG, AllChatsFragment.TAG);

        navigationView.setCheckedItem(R.id.edit_profile);
        closeNotifications();
        showFragment(UserProfileFragment.class, fragmentArguments);

    }

    public void openChatBox(String withOtherUserId, String username) {
        if (getSupportFragmentManager().findFragmentByTag(ChatBox.TAG) != null
                && getSupportFragmentManager().findFragmentByTag(ChatBox.TAG).isAdded()) {
            getSupportFragmentManager()
                    .beginTransaction()
                    .remove(getSupportFragmentManager().findFragmentByTag(ChatBox.TAG))
                    .commitAllowingStateLoss();
        }

        ChatBox chatBox = new ChatBox();

        Bundle user = new Bundle();
        user.putString(
                ChatBox.RECEIVER_USER,
                withOtherUserId);
        user.putString(
                ChatBox.RECEIVER_USER_DISPLAY_NAME,
                username);
        chatBox.setArguments(user);
        getSupportFragmentManager().beginTransaction()
                .setCustomAnimations(R.anim.anim_chatbox_show, R.anim.anim_chatbox_hide)
                .add(R.id.chat_box_container, chatBox, ChatBox.TAG)
                .commit();
    }

    @Override
    public void detachChatBox() {
        Fragment chatBoxFragment = getSupportFragmentManager().findFragmentByTag(ChatBox.TAG);
        if (chatBoxFragment != null) {
            Animation animation = AnimationUtils.loadAnimation(this, R.anim.anim_chatbox_hide);
            animation.setAnimationListener(new Animation.AnimationListener() {
                @Override
                public void onAnimationStart(Animation animation) {

                }

                @Override
                public void onAnimationEnd(Animation animation) {
                    getSupportFragmentManager().beginTransaction()
                            .remove(chatBoxFragment)
                            .commitAllowingStateLoss();

                }

                @Override
                public void onAnimationRepeat(Animation animation) {

                }
            });

            chatBoxFragment.getView().startAnimation(animation);
        }

    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        outState.putBoolean(IS_PHONE_VERIFICATION_IN_PROGRESS, isPhoneVerificationInProgress);
        outState.putString(CURRENT_VERIFICATION_ID, currentVerificationId);
        super.onSaveInstanceState(outState);
    }

    @Override
    protected void onRestoreInstanceState(Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            isPhoneVerificationInProgress = savedInstanceState.getBoolean(IS_PHONE_VERIFICATION_IN_PROGRESS);
            if (isPhoneVerificationInProgress) {
                currentVerificationId = savedInstanceState.getString(CURRENT_VERIFICATION_ID);
                openConfirmationDialog();
            }
        }
        super.onRestoreInstanceState(savedInstanceState);
    }
}
