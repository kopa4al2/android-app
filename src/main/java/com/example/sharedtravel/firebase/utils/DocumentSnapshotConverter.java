package com.example.sharedtravel.firebase.utils;

import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.firebase.IntercityTravelRepository;
import com.example.sharedtravel.firebase.MessageRepository;
import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.model.Message;
import com.example.sharedtravel.model.MessageSender;
import com.example.sharedtravel.model.ModelConverter;
import com.example.sharedtravel.model.Review;
import com.example.sharedtravel.model.User;
import com.google.firebase.firestore.DocumentSnapshot;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.example.sharedtravel.firebase.UsersManager.FIREBASE_USER;
import static com.example.sharedtravel.firebase.UsersManager.MORE_INFO;
import static com.example.sharedtravel.firebase.UsersManager.REVIEWS;
import static com.example.sharedtravel.firebase.UsersManager.TOTAL_RATING;
import static com.example.sharedtravel.firebase.UsersManager.USER_ID;

public class DocumentSnapshotConverter {

    public static List<IntercityTravel> converListIntercity(List<DocumentSnapshot> documentSnapshots) {
        if (documentSnapshots == null)
            return null;
        List<IntercityTravel> travels = new ArrayList<>(15);
        for (DocumentSnapshot documentSnapshot : documentSnapshots) {
            travels.add(convertSingleIntercity(documentSnapshot));
        }
        return travels;
    }

    @SuppressWarnings("unchecked")
    public static IntercityTravel convertSingleIntercity(DocumentSnapshot documentSnapshot) {
        IntercityTravel travel = documentSnapshot.toObject(IntercityTravel.class);
        travel.setTravelID(documentSnapshot.getId());
        User user = convertToUser((Map<String, Object>) documentSnapshot.get(IntercityTravelRepository.DB_CREATOR));
        travel.setCreator(user);
        travel.setCreatorUUID(user.getUserId());
        return travel;
    }

    @SuppressWarnings({"ConstantConditions", "unchecked"})
    public static List<Message> convertToMessage(DocumentSnapshot document) {
        Map<String, Object> documentData = document.getData();
        return convertToMessage(documentData);
    }

    @SuppressWarnings({"unchecked", "ConstantConditions", "MethodWithMultipleLoops"})
    public static List<Message> convertToMessage(Map<String, Object> documentData) {
        List<Message> messages = new ArrayList<>(20);
        MessageSender sender = ModelConverter.converMapToMessageSender(
                (Map<String, Object>) documentData.get(MessageRepository.SENDER));
        for (Object messageObject : (List<Object>) documentData.get(MessageRepository.RECEIVED_MESSAGES)) {
            Message message = ModelConverter.convertMapToMessage((Map<String, Object>) messageObject);
            message.setSender(sender);
            messages.add(message);
        }
        for (Object messageObject : (List<Object>) documentData.get(MessageRepository.SENT_MESSAGES)) {
            Message message = ModelConverter.convertMapToMessage((Map<String, Object>) messageObject);
            message.setSender(
                    ModelConverter.convertUserToMessageSender(
                            AuthenticationManager.getInstance().getLoggedInFirebaseUser()));
            messages.add(message);
        }
        Collections.sort(messages, (o1, o2) -> o1.getDateOfCreation().compareTo(o2.getDateOfCreation()));
        return messages;
    }

    @SuppressWarnings("unchecked")
    public static User convertToUser(DocumentSnapshot docuSnapShot) {
        User user = new User();

        user.setFirebaseUser((Map<String, Object>) docuSnapShot.get(FIREBASE_USER));
        user.setMoreInfo((String) docuSnapShot.get(MORE_INFO));
        user.setUserId(docuSnapShot.getId());
        try {
            Double rating = (Double) docuSnapShot.get(TOTAL_RATING);
            user.setRating(Float.valueOf(String.valueOf(rating)));
            List<Object> reviews = (List<Object>) docuSnapShot.get(REVIEWS);
            List<Review> userReviews = new ArrayList<>();
            Set<String> usersWhoRatedThisUser = new HashSet<>();
            if (reviews != null) {
                for (Object review : reviews) {
                    Review rev = convertToReview((HashMap) review);
                    userReviews.add(rev);
                    usersWhoRatedThisUser.add(rev.getCreatorId());
                }
            }

            user.setReviewsFromUsers(userReviews);
            user.setUsersWhoRatedThisUser(usersWhoRatedThisUser);
        } catch (Exception ignored) {
            String m = ignored.getMessage();

        }

        return user;
    }

    public static User convertToUser(Map<String, Object> userMap) {
        User user = new User();

        user.setFirebaseUser((Map<String, Object>) userMap.get(FIREBASE_USER));
        user.setMoreInfo((String) userMap.get(MORE_INFO));
        try {
            user.setUserId((String) userMap.get(USER_ID));
            Double rating = (Double) userMap.get(TOTAL_RATING);
            user.setRating(Float.valueOf(String.valueOf(rating)));
            List<Object> reviews = (List<Object>) userMap.get(REVIEWS);
            List<Review> userReviews = new ArrayList<>(3);
            Set<String> usersWhoRatedThisUser = new HashSet<>(3);
            if (reviews != null) {
                for (Object review : reviews) {
                    Review rev = convertToReview((HashMap) review);
                    userReviews.add(rev);
                    usersWhoRatedThisUser.add(rev.getCreatorId());
                }
            }

            user.setReviewsFromUsers(userReviews);
            user.setUsersWhoRatedThisUser(usersWhoRatedThisUser);
        } catch (Exception ignored) {
        }

        return user;
    }


    private static Review convertToReview(HashMap review) {

        Review r = new Review();
        r.setCreatorId((String) review.get("creatorId"));
        r.setReviewText((String) review.get("reviewText"));
        r.setCreatorUsername((String) review.get("creatorUsername"));
        r.setCreatorProfilePic((String) review.get("creatorProfilePic"));

        //Transforming from double to float throught String.valueOf
        r.setRating(Float.valueOf(String.valueOf(review.get("rating"))));

        return r;
    }
}
