package com.example.sharedtravel;

import android.annotation.SuppressLint;
import android.graphics.Typeface;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.design.widget.TextInputEditText;
import android.support.v4.app.Fragment;
import android.support.v7.app.AlertDialog;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.text.TextUtils;
import android.text.method.ScrollingMovementMethod;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.RatingBar;
import android.widget.TextView;

import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.firebase.UsersManager;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;
import com.example.sharedtravel.model.Review;
import com.example.sharedtravel.model.User;
import com.google.firebase.auth.FirebaseUser;
import com.squareup.picasso.Picasso;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

import static com.example.sharedtravel.firebase.UsersManager.DISPLAY_NAME;
import static com.example.sharedtravel.firebase.UsersManager.EMAIL;
import static com.example.sharedtravel.firebase.UsersManager.PHONE_NUMBER;
import static com.example.sharedtravel.firebase.UsersManager.PROFILE_PICTURE;

@SuppressWarnings("ALL")
public class SingleUserActivity extends AppCompatActivity implements ChatBox.ChatBoxListener {

    public static final String USER_EXTRA = "user";

    private TextView username;
    private TextView email;
    private TextView phoneNumberTextView;
    private TextView moreInfoTextView;
    private ImageView profilePicture;
    private RatingBar ratingBar;
    private User user;
    private AlertDialog reviewDialog;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_single_user);
        String userId = getIntent().getStringExtra(USER_EXTRA);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        ratingBar = findViewById(R.id.user_rating_bar);

        Button openChatWindowButton = findViewById(R.id.btn_send_message);
        openChatWindowButton.setOnClickListener((v) -> {
            if (getSupportFragmentManager().findFragmentByTag(ChatBox.TAG) != null
                    && getSupportFragmentManager().findFragmentByTag(ChatBox.TAG).isAdded()) {

                return;
            }
            ChatBox chatBox = new ChatBox();

            Bundle users = new Bundle();
            users.putString(
                    ChatBox.RECEIVER_USER,
                    userId);
            users.putString(
                    ChatBox.RECEIVER_USER_DISPLAY_NAME,
                    (String) user.getFirebaseUser().get(UsersManager.DISPLAY_NAME));
            chatBox.setArguments(users);
            getSupportFragmentManager().beginTransaction()
                    .setCustomAnimations(R.anim.anim_chatbox_show, R.anim.anim_chatbox_hide)
                    .add(R.id.chat_box_container, chatBox, ChatBox.TAG)
                    .commitAllowingStateLoss();

            ChatBox.scrollToBottom(findViewById(R.id.single_user_scroll_view_container),
                    findViewById(R.id.chat_box_container));

        });


        UsersManager.getUser(userId, (user) -> {
            this.user = (User) user;
            initActivity();
        });

        username = findViewById(R.id.tv_profile_username);
        email = findViewById(R.id.tv_user_email);
        profilePicture = findViewById(R.id.profile_image_view);
        phoneNumberTextView = findViewById(R.id.tv_user_phone_number);
        moreInfoTextView = findViewById(R.id.tv_user_more_info);


    }

    @SuppressLint("ClickableViewAccessibility")
    private void initActivity() {
        String usernameText = (String) user.getFirebaseUser().get(DISPLAY_NAME);
        String emailText = (String) user.getFirebaseUser().get(EMAIL);
        String phoneNumberText = (String) user.getFirebaseUser().get(PHONE_NUMBER);
        String profilePictureUrl = (String) user.getFirebaseUser().get(PROFILE_PICTURE);
        String moreInfoText = user.getMoreInfo();

        username.setText(usernameText);

        email.setText(emailText != null ? emailText : getResources().getString(R.string.not_specified));

        phoneNumberTextView.setText(phoneNumberText != null ?
                phoneNumberText : getResources().getString(R.string.not_specified));

        moreInfoTextView.setText(moreInfoText);
        if (profilePictureUrl != null)
            Picasso.get().load(profilePictureUrl)
                    .into(profilePicture);


        ratingBar.setVisibility(View.VISIBLE);
        float rating = user.getRating();
        //noinspection LawOfDemeter
        boolean canRate = !user.getUsersWhoRatedThisUser()
                .contains(AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid());

        ratingBar.setRating(rating);
        if (canRate)
            ratingBar.setIsIndicator(false);
        else
            ratingBar.setIsIndicator(true);
        ratingBar.setOnTouchListener(getOnTouchListener(canRate));
    }

    @NotNull
    private View.OnTouchListener getOnTouchListener(boolean canRate) {
        return (view, motionEvent) -> {
            if (canRate) {
                view.onTouchEvent(motionEvent);
                view.performClick();
                if (reviewDialog != null) {
                    if (reviewDialog.isShowing()) {
                        return true;
                    }
                }
                reviewDialog = new AlertDialog.Builder(this)
                        .setCancelable(true)
                        .setView(R.layout.add_review_layout)
                        .create();

                reviewDialog.setOnDismissListener(dialog -> {
                    reviewDialog = null;
                    ratingBar.setRating(user.getRating());
                });
                reviewDialog.setOnShowListener(dialog -> {

                    float votersRating = ratingBar.getRating();
                    ((TextView) reviewDialog.findViewById(R.id.rating_text_view)).setText(String.valueOf(votersRating));
                    reviewDialog.findViewById(R.id.user_review_dont_write).setOnClickListener((v) -> reviewDialog.dismiss());
                    reviewDialog.findViewById(R.id.user_review_confirm).setOnClickListener((v) -> {
                        Review review = new Review();
                        FirebaseUser loggedInUser = AuthenticationManager.getInstance().getLoggedInFirebaseUser();
                        review.setCreatorId(loggedInUser.getUid());
                        if (loggedInUser.getPhotoUrl() != null)
                            review.setCreatorProfilePic(loggedInUser.getPhotoUrl().toString());
                        review.setCreatorUsername(loggedInUser.getDisplayName());
                        review.setRating(votersRating);
                        review.setReviewText(
                                ((TextInputEditText) reviewDialog.findViewById(R.id.user_rate_review_content))
                                        .getText().toString());
                        UsersManager.rateUser(user, review, (newUser) -> {
                            this.user = (User) newUser;
                            initActivity();
                            reviewDialog.dismiss();
                            DialogBuilder
                                    .showSuccessDialog(getDelegate().findViewById(R.id.single_user_container),
                                            getString(R.string.successfully_rated_user));
                        });

                    });

                });

                reviewDialog.show();
            } else if (!canRate) {
                if (reviewDialog != null) {
                    if (reviewDialog.isShowing()) {
                        return true;
                    }
                    reviewDialog.dismiss();
                    return true;
                }
                reviewDialog = new AlertDialog.Builder(this, android.R.style.Theme_Panel)
                        .setView(R.layout.list_reviews_layout)
                        .create();
                reviewDialog.setOnDismissListener(dialog -> {
                    reviewDialog = null;
                    ratingBar.setRating(user.getRating());
                });
                reviewDialog.setOnShowListener(dialog -> {
                    RatingBar totalRatingBar = reviewDialog.findViewById(R.id.userTotalRating);
                    totalRatingBar.setRating(user.getRating());
                    totalRatingBar.setIsIndicator(true);

                    ImageView loggedInUserProfilePic = reviewDialog.findViewById(R.id.reviewer_profile_pic);
                    TextView loggedInUsername = reviewDialog.findViewById(R.id.reviewer_username);
                    TextView loggedInUserReviewContent = reviewDialog.findViewById(R.id.reviewerReview);
                    RatingBar loggedInUserRating = reviewDialog.findViewById(R.id.ratingBar);
                    reviewDialog.findViewById(R.id.dismiss_list_review_btn).setOnClickListener((v) -> reviewDialog.dismiss());


                    Review loggedInUserReview = null;
                    List<Review> reviewsForAdapter = new ArrayList<>(user.getReviewsFromUsers());
                    for (Review reviewsFromUser : user.getReviewsFromUsers()) {
                        if (reviewsFromUser.getCreatorId().equals(AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid())) {
                            loggedInUserReview = reviewsFromUser;
                            reviewsForAdapter.remove(loggedInUserReview);
                            break;
                        }
                    }
                    if (loggedInUserReview == null) {
                        dialog.dismiss();
                        return;
                    }

                    if (loggedInUserReview.getCreatorProfilePic() != null)
                        Picasso.get().load(loggedInUserReview.getCreatorProfilePic()).fit().into(loggedInUserProfilePic);
                    else
                        Picasso.get().load(R.mipmap.blank_profile_picture).fit().into(loggedInUserProfilePic);

                    //Only first word of the username
                    loggedInUsername.setText(loggedInUserReview.getCreatorUsername().split(" ")[0]);
                    loggedInUsername.setMaxEms(10);
                    loggedInUsername.setEllipsize(TextUtils.TruncateAt.START);

                    if (loggedInUserReview.getReviewText().length() < 1) {
                        loggedInUserReviewContent.setText(getString(R.string.no_review));
                        loggedInUserReviewContent.setAllCaps(true);
                        loggedInUserReviewContent.setTypeface(Typeface.defaultFromStyle(Typeface.BOLD_ITALIC));
                    } else
                        loggedInUserReviewContent.setText(loggedInUserReview.getReviewText());

                    loggedInUserRating.setRating(loggedInUserReview.getRating());
                    loggedInUserRating.setIsIndicator(true);
                    loggedInUserRating.setNumStars(5);

                    RecyclerView userReviewsList = reviewDialog.findViewById(R.id.rv_user_reviews);
                    userReviewsList.setLayoutManager(new LinearLayoutManager(this));
                    UserReviewsAdapter adapter = new UserReviewsAdapter(reviewsForAdapter);
                    userReviewsList.setAdapter(adapter);

                    //since findViewById returns null i use this trick FUCK YOU ANDROID!!!
                    CardView cardView = (CardView) loggedInUserReviewContent.getParent().getParent();
                    loggedInUserReviewContent.setMovementMethod(new ScrollingMovementMethod());
                    cardView.setClickable(false);
                });

                reviewDialog.show();
            }
            return true;
        }

                ;
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
    public boolean onOptionsItemSelected(MenuItem item) {
        if (item.getItemId() == android.R.id.home) {
            super.onBackPressed();
        }
        return super.onOptionsItemSelected(item);
    }

    private class UserReviewsAdapter extends RecyclerView.Adapter<ReviewViewHolder> {
        private List<Review> userReviews;

        UserReviewsAdapter(List<Review> reviews) {
            userReviews = reviews;
        }

        @NonNull
        @Override
        public ReviewViewHolder onCreateViewHolder(@NonNull ViewGroup viewGroup, int i) {
            View v = getLayoutInflater().inflate(R.layout.rv_user_reviews_row, viewGroup, false);

            return new ReviewViewHolder(v);
        }

        @Override
        public void onBindViewHolder(@NonNull ReviewViewHolder reviewViewHolder, int i) {
            reviewViewHolder.bind(userReviews.get(i));
        }

        @Override
        public int getItemCount() {
            return userReviews.size();
        }
    }

    private class ReviewViewHolder extends RecyclerView.ViewHolder {

        private TextView username;
        private TextView reviewContent;
        private ImageView profilePic;
        private RatingBar ratingBar;
        private CardView singleReviewContainer;

        ReviewViewHolder(@NonNull View itemView) {
            super(itemView);
            username = itemView.findViewById(R.id.reviewer_username);
            reviewContent = itemView.findViewById(R.id.reviewerReview);
            profilePic = itemView.findViewById(R.id.reviewer_profile_pic);
            ratingBar = itemView.findViewById(R.id.ratingBar);
            singleReviewContainer = itemView.findViewById(R.id.single_review_container);
        }

        @SuppressLint("ClickableViewAccessibility")
        void bind(Review review) {
            ratingBar.setIsIndicator(true);
            ratingBar.setRating(review.getRating());

            if (review.getCreatorUsername() != null)
                username.setText(review.getCreatorUsername().split(" ")[0]);
            if (review.getReviewText() != null)
                if (review.getReviewText().length() < 1) {
                    reviewContent.setText(getString(R.string.no_review));
                    reviewContent.setTypeface(Typeface.defaultFromStyle(Typeface.ITALIC));
                } else {
                    reviewContent.setText(review.getReviewText());
                    reviewContent.setVerticalScrollBarEnabled(false);
                    reviewContent.setMaxLines(3);
                    reviewContent.setEllipsize(TextUtils.TruncateAt.END);
                }

            if (review.getCreatorProfilePic() != null)
                Picasso.get().load(review.getCreatorProfilePic()).fit().into(profilePic);
            else
                Picasso.get().load(R.mipmap.blank_profile_picture).fit().into(profilePic);

            singleReviewContainer.setOnClickListener((cardView) -> {
                AlertDialog showReviewDialog = new AlertDialog.Builder(SingleUserActivity.this)
                        .setView(R.layout.single_user_review)
                        .setPositiveButton("OK", (b, which) -> {
                            b.dismiss();
                        })
                        .create();

                showReviewDialog.setOnShowListener((dialog -> {
                    AlertDialog dialogView = (AlertDialog) dialog;
                    ((TextView) dialogView.findViewById(R.id.reviewer_username)).setText(review.getCreatorUsername());
                    ImageView profilePic = dialogView.findViewById(R.id.reviewer_profile_pic);
                    if (review.getCreatorProfilePic() != null)
                        Picasso.get().load(review.getCreatorProfilePic()).fit().into(profilePic);
                    else
                        Picasso.get().load(R.mipmap.blank_profile_picture).fit().into(profilePic);
                    ((RatingBar) dialogView.findViewById(R.id.ratingBar)).setRating(review.getRating());
                    TextView reviewText = (TextView) dialogView.findViewById(R.id.reviewerReview);
                    reviewText.setText(review.getReviewText());

                }));
                showReviewDialog.show();
            });
        }

    }
}
