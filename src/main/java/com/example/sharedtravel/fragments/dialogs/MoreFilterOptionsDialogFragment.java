package com.example.sharedtravel.fragments.dialogs;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.drawable.ColorDrawable;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.CheckBox;
import android.widget.EditText;

import com.example.sharedtravel.R;

public class MoreFilterOptionsDialogFragment extends DialogFragment {

    public static final String EXACT_DATE_OF_DEPARTURE = "exact_date_of_departure";
    public static final String ALLOW_INTERMEDIATE_STOPS = "no_intermediate_stops";
    public static final String LUGGAGE_SPACE = "luggage_space";

    private CheckBox luggageSpace;
    private CheckBox allowIntermediateStops;
    private CheckBox exactDateOfDeparture;


    private OnCloseMoreFilterOptions listener;

    public MoreFilterOptionsDialogFragment() {
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.intercity_advanced_search, container, false);


        luggageSpace = view.findViewById(R.id.cb_space_for_luggage_find_travel);
        allowIntermediateStops = view.findViewById(R.id.cb_direct_travel);
        exactDateOfDeparture = view.findViewById(R.id.cb_exact_departure_date);

        luggageSpace.setTypeface(ResourcesCompat.getFont(getContext(), R.font.crimson_text));
        allowIntermediateStops.setTypeface(ResourcesCompat.getFont(getContext(), R.font.crimson_text));
        exactDateOfDeparture.setTypeface(ResourcesCompat.getFont(getContext(), R.font.crimson_text));

        boolean luggageSpaceValue;
        boolean allowIntermediateStopsValue;
        boolean exactDateOfDepartureValue;
        if(getArguments() != null) {
            luggageSpaceValue = getArguments().getBoolean(LUGGAGE_SPACE);
            allowIntermediateStopsValue = getArguments().getBoolean(ALLOW_INTERMEDIATE_STOPS);
            exactDateOfDepartureValue = getArguments().getBoolean(EXACT_DATE_OF_DEPARTURE);

            luggageSpace.setChecked(luggageSpaceValue);
            allowIntermediateStops.setChecked(allowIntermediateStopsValue);
            exactDateOfDeparture.setChecked(exactDateOfDepartureValue);
        }
        if (savedInstanceState != null) {
            luggageSpaceValue = savedInstanceState.getBoolean(LUGGAGE_SPACE);
            allowIntermediateStopsValue = savedInstanceState.getBoolean(ALLOW_INTERMEDIATE_STOPS);
            exactDateOfDepartureValue = savedInstanceState.getBoolean(EXACT_DATE_OF_DEPARTURE);

            luggageSpace.setChecked(luggageSpaceValue);
            allowIntermediateStops.setChecked(allowIntermediateStopsValue);
            exactDateOfDeparture.setChecked(exactDateOfDepartureValue);
        }


        view.findViewById(R.id.btn_more_options_confirm).setOnClickListener((v) -> {
            if (listener != null) {
                if (savedInstanceState != null) {
                    listener.confirm(savedInstanceState);
                } else {
                    Bundle options = new Bundle();
                    options.putBoolean(LUGGAGE_SPACE, luggageSpace.isChecked());
                    options.putBoolean(ALLOW_INTERMEDIATE_STOPS, allowIntermediateStops.isChecked());
                    options.putBoolean(EXACT_DATE_OF_DEPARTURE, exactDateOfDeparture.isChecked());
                    listener.confirm(options);
                }
            }
            dismiss();
        });
        view.findViewById(R.id.btn_more_options_cancel).setOnClickListener((v) -> {
            dismiss();
        });
        return view;
    }


    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        getDialog().requestWindowFeature(Window.FEATURE_NO_TITLE);
        setStyle(DialogFragment.STYLE_NO_FRAME, 0);
        getDialog().getWindow().setBackgroundDrawable(new ColorDrawable(Color.TRANSPARENT));
        super.onViewCreated(view, savedInstanceState);
    }

    public void setOnConfirmListener(OnCloseMoreFilterOptions listener) {
        this.listener = listener;
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {

        outState.putBoolean(LUGGAGE_SPACE, luggageSpace.isChecked());
        outState.putBoolean(ALLOW_INTERMEDIATE_STOPS, allowIntermediateStops.isChecked());
        outState.putBoolean(EXACT_DATE_OF_DEPARTURE, exactDateOfDeparture.isChecked());
        super.onSaveInstanceState(outState);
    }

    public interface OnCloseMoreFilterOptions {
        void confirm(Bundle options);

    }
}
