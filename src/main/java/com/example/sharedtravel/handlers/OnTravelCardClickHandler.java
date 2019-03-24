package com.example.sharedtravel.handlers;

import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.databinding.DataBindingUtil;
import android.os.Build;
import android.support.v7.widget.CardView;
import android.text.method.ScrollingMovementMethod;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.TextView;

import com.example.sharedtravel.MainActivity;
import com.example.sharedtravel.R;
import com.example.sharedtravel.SingleUserActivity;
import com.example.sharedtravel.databinding.SingleTravelCardBinding;
import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.fragments.UserProfileFragment;
import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.model.User;
import com.example.sharedtravel.utils.ActivityStarter;

import static com.example.sharedtravel.fragments.AddUrbanTravelFragment.TAG;

public class OnTravelCardClickHandler {
    private AlertDialog cardViewDialog = null;


    public void onClickCard(Context ctx, IntercityTravel travel) {

        if (cardViewDialog != null && cardViewDialog.isShowing()) {
            cardViewDialog.dismiss();
            return;
        }


        LayoutInflater inflater = LayoutInflater.from(ctx);
        SingleTravelCardBinding binding = DataBindingUtil.inflate(inflater, R.layout.single_travel_card, null, false);
        binding.setTravel(travel);

        View newView = binding.getRoot();
        ((CardView) newView).setUseCompatPadding(false);
        ((CardView) newView).setRadius(0);
        newView.setBackgroundTintList(null);
        newView.findViewById(R.id.btn_single_travel_send_message).setVisibility(View.GONE);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            newView.setForeground(null);
        }
        newView.setPadding(10, 10, 10, 10);

        newView.findViewById(R.id.moreInfoText).setPadding(10, 10, 10, 20);
        ((TextView) newView.findViewById(R.id.moreInfoText)).setEllipsize(null);
        ((TextView) newView.findViewById(R.id.moreInfoText)).setMovementMethod(new ScrollingMovementMethod());
        ((TextView) newView.findViewById(R.id.moreInfoText)).setMaxLines(6);

        cardViewDialog = getCardViewDialog(ctx, travel, newView);

        cardViewDialog.show();
    }

    public void openChatBox(Context ctx, User user) {
        if (ctx instanceof MainActivity && user != null)
            ((MainActivity) ctx).openChatBox(user.getUserId(), user.getDisplayName());

    }

    private static AlertDialog getCardViewDialog(Context ctx, IntercityTravel travel, View newView) {
        return new AlertDialog.Builder(ctx, R.style.SingeCardViewDialog)
                .setView(newView)
                .setCancelable(false)
                .setNegativeButton(ctx.getString(R.string.close), (dialog, which) -> dialog.dismiss())
                .setPositiveButton(ctx.getString(R.string.show_user), ((dialog, which) -> {
                    if (AuthenticationManager.getInstance().getLoggedInFirebaseUser() == null) {
                        AlertDialog dialog1 = new AlertDialog.Builder(ctx)
                                .setTitle(ctx.getString(R.string.you_need_to_be_logged_in))
                                .setPositiveButton(ctx.getString(R.string.login), (btn, wh) -> {
                                    ((MainActivity) ctx).showLoginDialog();
                                })
                                .setNegativeButton(ctx.getString(R.string.cancel), (btn, wh) -> {
                                    btn.dismiss();
                                })
                                .create();
                        dialog1.show();
                        return;
                    }
                    if (travel.getCreatorUUID().equals(AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid())) {
                        //If the user tries to open a travel from himself open his own profile
                        ((MainActivity) ctx).showFragment(UserProfileFragment.class, null);
                        ((MainActivity) ctx).navigationView.setCheckedItem(R.id.edit_profile);
                        return;
                    }

                    Intent intent = new Intent(ctx, SingleUserActivity.class);
                    intent.putExtra(SingleUserActivity.USER_EXTRA, travel.getCreatorUUID());
                    ActivityStarter.startActivity(ctx, intent);
                }))
                .create();
    }
}
