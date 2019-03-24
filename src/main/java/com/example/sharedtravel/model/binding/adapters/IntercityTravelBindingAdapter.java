package com.example.sharedtravel.model.binding.adapters;

import android.databinding.BindingAdapter;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.utils.DateFormatUtils;
import com.squareup.picasso.Picasso;

import java.text.DateFormat;
import java.util.Date;

public class IntercityTravelBindingAdapter {
    @BindingAdapter("intermediateStops")
    public static void setIntermediateStops(ViewGroup parent, String[] stops) {
        for (String stop : stops) {
            TextView tv = new TextView(parent.getContext());
            tv.setPadding(5, 5, 5, 5);
            tv.setTextSize(15);
            tv.setText(stop);
            tv.setTextColor(parent.getResources().getColor(R.color.colorPrimary));
            parent.addView(tv);
        }
    }

    @BindingAdapter(value = {"setHour", "isHourSelected"})
    public static void setHour(TextView textView, Date date, boolean isHourSet) {
        if (date == null)
            return;

        String hourOfDepartureFormatted =
                isHourSet ?
                        DateFormatUtils.dateToHourString(date)
                        :
                        textView.getResources().getString(R.string.not_specified);
        textView.setText(hourOfDepartureFormatted);


    }

    @BindingAdapter("departureDate")
    public static void setDepartureDate(TextView textView, Date date) {
        if (date == null)
            return;

        textView.setText(DateFormatUtils.dateToString(date, DateFormat.SHORT));
    }

    @BindingAdapter("profileUrl")
    public static void setProfileUrl(ImageView img, IntercityTravel travel) {
        String imgUrl = travel.getCreator().getProfilePicture();
        if (imgUrl != null)
            Picasso.get().load(imgUrl).into(img);
        else
            img.setImageResource(R.mipmap.blank_profile_picture);

    }
}
