package com.example.sharedtravel.fragments;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.constraint.ConstraintLayout;
import android.support.constraint.ConstraintSet;
import android.support.design.widget.TabLayout;
import android.support.design.widget.TextInputLayout;
import android.support.transition.ChangeBounds;
import android.support.transition.Transition;
import android.support.transition.TransitionManager;
import android.support.v4.app.Fragment;
import android.support.v4.view.ViewPager;
import android.support.v7.app.AlertDialog;
import android.text.InputType;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.AnticipateOvershootInterpolator;
import android.widget.EditText;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.firebase.UsersManager;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;
import com.example.sharedtravel.model.User;
import com.example.sharedtravel.model.Validator;
import com.example.sharedtravel.utils.Constants;
import com.example.sharedtravel.views.EditUserTabsAdapter;
import com.facebook.Profile;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.Observable;
import java.util.Observer;

import de.hdodenhof.circleimageview.CircleImageView;


public class UserProfileFragment extends Fragment implements Observer {

    public static final String SHOW_INNER_FRAGMENT_BUNDLE_TAG = "show_inner_fragment";

    private static final String LOGGED_IN_USER = "logged_in_user";
    private static final String FIREBASE_USER = "firebaseUser";
    private static final String USERS_PREFERENCES = "user_pref";
    private static final String USER_MORE_INFO = "more_info";

    private CircleImageView profilePicture;
    private TextView emailTextView;
    private TextView usernameTextView;
    private TextView phoneTextView;
    private TextView moreInfoTextView;

    private TabLayout tabLayout;
    private FirebaseUser firebaseUser;
    private Profile facebookProfile;
    private User loggedInUser;

    private OnFragmentInteractionListener mListener;
    private View view;
    private SharedPreferences preferences;

    //For swipe-able profile information
    private ConstraintLayout currentLayoutParent;
    private ConstraintSet constraintSet;
    private boolean isShrinked = false;
    private float y1, y2, dy;
    private RatingBar ratingBar;

    public UserProfileFragment() {
        // Required empty public constructor
    }


    public static UserProfileFragment newInstance() {
        return new UserProfileFragment();
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        loggedInUser = AuthenticationManager.getInstance().getCurrentLoggedInUser();
        if (savedInstanceState != null) {
            loggedInUser = savedInstanceState.getParcelable(LOGGED_IN_USER);
            firebaseUser = savedInstanceState.getParcelable(FIREBASE_USER);
        }
        view = inflater.inflate(R.layout.fragment_user_info, container, false);
        preferences = getActivity().getSharedPreferences(USERS_PREFERENCES, Context.MODE_PRIVATE);

        ViewPager viewPager = view.findViewById(R.id.edit_profile_tabs_view_pager);
        EditUserTabsAdapter adapter = new EditUserTabsAdapter(getContext(), getChildFragmentManager());
        viewPager.setAdapter(adapter);

        tabLayout = view.findViewById(R.id.tabs);
        tabLayout.setupWithViewPager(viewPager);
        tabLayout.getTabAt(1).setIcon(R.drawable.ic_car);
        tabLayout.getTabAt(0).setIcon(R.drawable.ic_message);
        //If we passed arguments to show a certain tab -> DO IT !
        if (getArguments() != null &&
                getArguments().getString(SHOW_INNER_FRAGMENT_BUNDLE_TAG) != null) {
            showFragmentByTag(getArguments().getString(SHOW_INNER_FRAGMENT_BUNDLE_TAG));
        }

        profilePicture = view.findViewById(R.id.profile_image_view);


        usernameTextView = view.findViewById(R.id.tv_profile_username);
        emailTextView = view.findViewById(R.id.tv_user_email);
        phoneTextView = view.findViewById(R.id.tv_user_phone_number);
        moreInfoTextView = view.findViewById(R.id.tv_user_more_info);
        ratingBar = view.findViewById(R.id.user_rating_bar);
        facebookProfile = Profile.getCurrentProfile();

        updateUI();
        addEditIcons();
        addOnClickListeners();


        currentLayoutParent = view.findViewById(R.id.userProfileConstraintLayout);
        constraintSet = new ConstraintSet();
        View profileGroup = view.findViewById(R.id.profileInfoEditProfileContainer);

        setOnSwipeListener(profileGroup);

        return view;

    }


    private void showFragmentByTag(String fragmentTag) {
        switch (fragmentTag) {
            case AllChatsFragment.TAG: {
                tabLayout.getTabAt(0).select();
            }
            break;
            default:
        }
    }

    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
        if (context instanceof OnFragmentInteractionListener) {
            mListener = (OnFragmentInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString()
                    + " must implement OnFragmentInteractionListener");
        }
    }

    @Override
    public void onDetach() {
        super.onDetach();
        mListener = null;
    }

    private void addOnClickListeners() {

        profilePicture.setOnClickListener((v) -> {
            Intent photoPickerIntent = new Intent(Intent.ACTION_PICK, MediaStore.Images.Media.INTERNAL_CONTENT_URI);
            getActivity().startActivityForResult(photoPickerIntent, Constants.INTENT_PICK_PROFILE_PHOTO_REQUEST_CODE);
        });

        view.findViewById(R.id.user_email_label).setOnClickListener((v) -> {
            showEdiTextDialog(emailTextView.getText().toString(), EditDialogType.EMAIL, (email) -> {
                DialogBuilder.showSuccessDialog(getActivity(), getResources().getString(R.string.email_change_initiated));
                mListener.changeEmail(email);
            });
        });

        usernameTextView.setOnClickListener((v) -> {
            showEdiTextDialog(usernameTextView.getText().toString(), EditDialogType.USERNAME, (username) ->
                    mListener.changeUsername(username));
        });

        view.findViewById(R.id.label_user_phone).setOnClickListener((v) -> {
            showEdiTextDialog("", EditDialogType.PHONE, (phoneNumber) -> {
                String formattedPhoneNum = "+359" + phoneNumber.substring(1);
                mListener.initiatePhoneNumberLinking(formattedPhoneNum);
            });
        });

        view.findViewById(R.id.label_more_info).setOnClickListener((v) -> {
            showEdiTextDialog(moreInfoTextView.getText().toString(), EditDialogType.MORE_INFO, (moreInfo) -> {
                preferences.edit().putString(USER_MORE_INFO, moreInfo).apply();
                UsersManager.addMoreInfo(firebaseUser.getUid(), moreInfo, () -> {
                    moreInfoTextView.setText(moreInfo);
                });
            });
        });
    }

    private void updateUI() {

        if (firebaseUser == null) {
            firebaseUser = AuthenticationManager.getInstance().getLoggedInFirebaseUser();
            updateUI();
        }
        if(loggedInUser == null) {
            loggedInUser = AuthenticationManager.getInstance().getCurrentLoggedInUser();
            updateUI();
        }


        if (firebaseUser != null) {

            String usernameText = facebookProfile == null ?
                    firebaseUser.getDisplayName()
                    :
                    facebookProfile.getName();

            Uri profilePicUrl = facebookProfile == null ?
                    firebaseUser.getPhotoUrl()
                    :
                    firebaseUser.getPhotoUrl() == null ?
                            facebookProfile.getProfilePictureUri(100, 100)
                            :
                            firebaseUser.getPhotoUrl();


            String phoneNumber = firebaseUser.getPhoneNumber();

            if (profilePicUrl != null)
                Picasso.get().load(profilePicUrl).into(profilePicture);
            else
                Picasso.get().load(R.mipmap.blank_profile_picture).into(profilePicture);

            usernameTextView.setText(usernameText);

            emailTextView.setAutoLinkMask(0);
            phoneTextView.setAutoLinkMask(0);
            if (getContext() != null) {
                emailTextView.setTextColor(getContext().getResources().getColor(R.color.colorOnSecondary));
                phoneTextView.setTextColor(getContext().getResources().getColor(R.color.colorOnSecondary));
            }
            emailTextView.setText(firebaseUser.getEmail());

            phoneTextView.setText(phoneNumber);

            moreInfoTextView.setText(loggedInUser.getMoreInfo());

            ratingBar.setRating(loggedInUser.getRating());


        }
    }

    private void addEditIcons() {
        final int EDIT_ICON_ID = R.drawable.ic_edit;

        ((TextView) view.findViewById(R.id.user_email_label)).setCompoundDrawablesWithIntrinsicBounds(0, 0, EDIT_ICON_ID, 0);
        ((TextView) view.findViewById(R.id.label_user_phone)).setCompoundDrawablesWithIntrinsicBounds(0, 0, EDIT_ICON_ID, 0);
        ((TextView) view.findViewById(R.id.label_more_info)).setCompoundDrawablesWithIntrinsicBounds(0, 0, EDIT_ICON_ID, 0);

        usernameTextView.setCompoundDrawablesWithIntrinsicBounds(0, 0, EDIT_ICON_ID, 0);


    }


    private void showEdiTextDialog(String previousValue, EditDialogType type, EditDialogListener callback) {
        if (getActivity() == null)
            return;

        AlertDialog alertDialog = new AlertDialog.Builder(getActivity(), R.style.slideInView)
                .setView(R.layout.change_field_layout)
                .setCancelable(false)
                .create();
        alertDialog.setOnShowListener(dialog -> {
            ((AlertDialog) dialog).getWindow().setBackgroundDrawableResource(android.R.color.transparent);
            EditText editText = ((AlertDialog) dialog).findViewById(R.id.et_change_field);
            TextInputLayout editTextContainer = ((AlertDialog) dialog).findViewById(R.id.change_field_container);
            switch (type) {
                case EMAIL:
                    editTextContainer.setHelperText(getResources().getString(R.string.email_will_change_after_verification));
                    editTextContainer.setHint(getString(R.string.email_example));
                    break;
                case PHONE:
                    editTextContainer.setHint(getString(R.string.phone_example_hint));
                    editTextContainer.setHelperText(getResources().getString(R.string.phone_will_change_after_confirm));
                    editText.setInputType(InputType.TYPE_CLASS_PHONE);
                    break;
                case USERNAME:
                    editTextContainer.setHelperText(getString(R.string.username_represents_you));
                    editTextContainer.setHint(getString(R.string.username_hint));
                    break;
                case MORE_INFO:
                    editTextContainer.setHint(getString(R.string.more_info_hint));
                    editTextContainer.setHelperText(getResources().getString(R.string.leave_blank_to_remove));
                    break;
            }
            editText.setText(previousValue);
            ((AlertDialog) dialog)
                    .findViewById(R.id.btn_change_field_confirm).setOnClickListener((btn) -> {

                if (editText.getText().toString().equals(previousValue) && !editText.getText().toString().isEmpty())
                    dialog.dismiss();
                else {
                    String invalidText = validateField(type, editText.getText().toString());
                    //ALL IS VALID
                    if (invalidText.equals("")) {
                        callback.onConfirmEdit(editText.getText().toString());
                        dialog.dismiss();
                    } else {
                        editTextContainer.setError(invalidText);
                        editText.setCompoundDrawablesWithIntrinsicBounds(0, 0, R.drawable.ic_error_outline, 0);
                    }
                }
            });
            ((AlertDialog) dialog)
                    .findViewById(R.id.btn_change_field_cancel).setOnClickListener((btn) -> dialog.dismiss());
        });
        alertDialog.show();
    }

    private String validateField(@NotNull EditDialogType type, String fieldValue) {
        String wrongValueText = "";
        switch (type) {
            case USERNAME:
                wrongValueText = Validator.validateUsername(fieldValue) ?
                        "" : getResources().getString(R.string.invalid_username);
                break;
            case EMAIL:
                wrongValueText = Validator.validateEmail(fieldValue) ?
                        "" : getResources().getString(R.string.invalid_email);
                break;
            case PHONE:
                wrongValueText = Validator.validatePhone(fieldValue) ?
                        "" : getResources().getString(R.string.invalid_phone_number);
                break;
            case MORE_INFO:
                wrongValueText = Validator.validateMoreInfo(fieldValue) ?
                        "" : getResources().getString(R.string.invalid_more_info);
                break;
        }

        return wrongValueText;
    }

    @Override
    public void update(Observable o, Object arg) {
        if (arg instanceof FirebaseUser)
            firebaseUser = (FirebaseUser) arg;
        updateUI();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);
        outState.putParcelable(LOGGED_IN_USER, loggedInUser);
        outState.putParcelable(FIREBASE_USER, firebaseUser);
    }

    @SuppressLint("ClickableViewAccessibility")
    private void setOnSwipeListener(View view) {
        view.setOnTouchListener((v, event) -> {
            switch (event.getAction()) {
                case (MotionEvent.ACTION_DOWN):

                    y1 = event.getY();
                    break;

                case (MotionEvent.ACTION_UP): {
                    y2 = event.getY();
                    dy = y2 - y1;

                    if (dy < 0) {
                        isShrinked = true;

                        constraintSet.clone(getActivity(), R.layout.fragment_user_info_shrinked);

                        Transition transition = new ChangeBounds();
                        transition.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
                        transition.setDuration(1000);

                        TransitionManager.beginDelayedTransition(currentLayoutParent, transition);
                        constraintSet.applyTo(currentLayoutParent);
                    } else {
                        isShrinked = false;

                        constraintSet.clone(getActivity(), R.layout.fragment_user_info);

                        Transition transition = new ChangeBounds();
                        transition.setInterpolator(new AnticipateOvershootInterpolator(1.0f));
                        transition.setDuration(1000);

                        TransitionManager.beginDelayedTransition(currentLayoutParent, transition);
                        constraintSet.applyTo(currentLayoutParent);
                    }
                }
            }


            return true;
        });
    }

    public interface OnFragmentInteractionListener {

        void changeEmail(String email);

        void changeUsername(String username);

        void changePassword(String password);

        void initiatePhoneNumberLinking(String phoneNumber);

    }

    private interface EditDialogListener {
        void onConfirmEdit(String text);
    }

    public enum EditDialogType {
        PHONE, EMAIL, USERNAME, MORE_INFO
    }
}
