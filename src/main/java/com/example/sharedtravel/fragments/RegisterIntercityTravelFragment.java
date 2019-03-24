package com.example.sharedtravel.fragments;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.databinding.DataBindingUtil;
import android.graphics.Typeface;
import android.os.AsyncTask;
import android.os.Bundle;
import android.support.annotation.NonNull;
import android.support.design.button.MaterialButton;
import android.support.design.widget.TextInputEditText;
import android.support.design.widget.TextInputLayout;
import android.support.v4.app.Fragment;
import android.support.v4.content.res.ResourcesCompat;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.LinearLayout;
import android.widget.TableRow;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.SharedTravelApplication;
import com.example.sharedtravel.databinding.IntercityTravelPreviewBinding;
import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.firebase.IntercityTravelRepository;
import com.example.sharedtravel.firebase.utils.AsyncListenerWithResult;
import com.example.sharedtravel.fragments.dialogs.DatePickerFragment;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;
import com.example.sharedtravel.fragments.dialogs.TimePickerFragment;
import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.utils.BindOnScreenTouchHideSoftKeyboard;
import com.example.sharedtravel.utils.DateFormatUtils;
import com.example.sharedtravel.views.CitiesSpinner;
import com.google.firebase.firestore.DocumentReference;

import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;


@SuppressWarnings("LocalVariableNamingConvention")
public class RegisterIntercityTravelFragment extends Fragment {

    private static final String START_LOCATION_SAVED_INSTANCE = "startLoc";
    private static final String END_LOCATION_SAVED_INSTANCE = "endLoc";
    private static final String INTERMEDIATE_STOPS_SAVED_INSTANCE = "middleStops";
    private static final String DATE_OF_DEPARTURE_SAVED_INSTANCE = "date";
    private static final String SPACE_FOR_PASSANGERS_SAVED_INSTANCE = "spaceForPassenger";
    private static final String SPACE_FOR_LUGGAGE_SAVED_INSTANCE = "spaceForLuggage";
    private static final String MORE_INFO_SAVED_INSTANCE = "moreInfo";
    private static final String IS_HOUR_SELECTED_SAVE_INSTANCE = "isHourSelected";

    private TextInputEditText spaceForPassengersEditText;
    private TextInputEditText moreInfoEditText;
    private CheckBox spaceForLuggage;
    private CitiesSpinner citiesSpinnerStartCity;
    private CitiesSpinner citiesSpinnerEndCity;

    //this field is so i don't make additional Date object for hour of departure
    private boolean isHourSelected = false;
    private ArrayList<String> intermediateStops;
    private Date dateOfDeparture;
    private OnFragmentInteractionListener mListener;
    private String startCity;
    private String destinationCity;
    private int passengersSpace;
    private Button addTravelBtn;
    private CitiesSpinner intermediateStopsPicker;


    public RegisterIntercityTravelFragment() {
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_register_intercity_travel, container, false);

        BindOnScreenTouchHideSoftKeyboard.bindListener(view, getActivity());

        if (intermediateStops == null)
            intermediateStops = new ArrayList<>(4);
        initViews(view);

        restoreSavedState(savedInstanceState, view);


        return view;
    }

    @SuppressWarnings("MethodWithMoreThanThreeNegations")
    private void restoreSavedState(Bundle savedInstanceState, View view) {
        if (savedInstanceState != null) {

            spaceForPassengersEditText.setText(savedInstanceState.getString(SPACE_FOR_PASSANGERS_SAVED_INSTANCE));
            moreInfoEditText.setText(savedInstanceState.getString(MORE_INFO_SAVED_INSTANCE));
            dateOfDeparture = (Date) savedInstanceState.getSerializable(DATE_OF_DEPARTURE_SAVED_INSTANCE);
            isHourSelected = savedInstanceState.getBoolean(IS_HOUR_SELECTED_SAVE_INSTANCE);
            if (isHourSelected) {
                view.findViewById(R.id.btn_intercity_remove_time).setVisibility(View.VISIBLE);
            }
            if (dateOfDeparture != null) {
                ((Button) view.findViewById(R.id.btn_intercity_choose_day))
                        .setText(DateFormatUtils.dateToString(dateOfDeparture, java.text.DateFormat.FULL));
                if (isHourSelected) {
                    ((Button) view.findViewById(R.id.btn_intercity_choose_time))
                            .setText(DateFormatUtils.dateToHourString(dateOfDeparture));
                }
            }
            spaceForLuggage.setChecked(
                    savedInstanceState.getBoolean(SPACE_FOR_LUGGAGE_SAVED_INSTANCE));
            startCity = savedInstanceState.getString(START_LOCATION_SAVED_INSTANCE);
            destinationCity = savedInstanceState.getString(END_LOCATION_SAVED_INSTANCE);
            if (startCity != null && !startCity.isEmpty())
                citiesSpinnerStartCity.setText(startCity);
            if (destinationCity != null && !destinationCity.isEmpty())
                citiesSpinnerEndCity.setText(destinationCity);
            intermediateStops = savedInstanceState.getStringArrayList(INTERMEDIATE_STOPS_SAVED_INSTANCE);
            assert intermediateStops != null;
            for (String intermediateStop : intermediateStops) {
                addTextView(view.findViewById(R.id.intermediate_stops_view), intermediateStop);
            }

        }
    }

    private void initViews(@NotNull View view) {
        addTravelBtn = view.findViewById(R.id.btn_register_intercity);

        citiesSpinnerStartCity = view.findViewById(R.id.sp_intercity_start_location);
        citiesSpinnerEndCity = view.findViewById(R.id.sp_intercity_end_location);
        moreInfoEditText = view.findViewById(R.id.et_intercity_additional_info);
        spaceForLuggage = view.findViewById(R.id.cb_intercity_place_for_luggage);

        spaceForPassengersEditText = view.findViewById(R.id.et_intercity_space_for_ppl);
        spaceForPassengersEditText.setTransformationMethod(null);

        ((TextInputLayout)view.findViewById(R.id.et_intercity_additional_info_layout))
                .setTypeface(ResourcesCompat.getFont(getContext(), R.font.lobster));
        ((TextInputLayout)view.findViewById(R.id.et_intercity_additional_info_layout))
                .setHelperTextTextAppearance(R.style.HelperTextAppearance);
        ((TextInputLayout)view.findViewById(R.id.et_intercity_space_for_people_layout))
                .setTypeface(ResourcesCompat.getFont(getContext(), R.font.lobster));
        ((TextInputLayout)view.findViewById(R.id.et_intercity_space_for_people_layout))
                .setHelperTextTextAppearance(R.style.HelperTextAppearance);

        moreInfoEditText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.cormorant_garamond));
        moreInfoEditText.setTextSize(16);
        spaceForPassengersEditText.setTypeface(ResourcesCompat.getFont(getContext(), R.font.oswald));

        intermediateStopsPicker = view.findViewById(R.id.sp_intermediate_stops);
        intermediateStopsPicker.setAllowMultiple(true);

        spaceForLuggage.setTypeface(ResourcesCompat.getFont(getContext(), R.font.crimson_text));

        bindOnClickListeners(view);
    }

    @SuppressWarnings("OverlyLongMethod")
    private void bindOnClickListeners(@NotNull View view) {
        view.findViewById(R.id.btn_intercity_remove_time).setOnClickListener((v) -> {
            v.setVisibility(View.GONE);
            this.isHourSelected = false;
            ((MaterialButton) view.findViewById(R.id.btn_intercity_choose_time)).setText(getString(R.string.time_of_departure));
        });

        //SINCE ITS A SINGLE CHOOSE OPTION ITEM IS ALWAYS THE FIRST
        //INDEX OF THE LIST
        citiesSpinnerStartCity.setItemSelectedListener((selectedCities) -> {
            if (selectedCities.size() > 0 &&
                    !selectedCities.get(0).isEmpty()) {
                startCity = selectedCities.get(0);
                citiesSpinnerStartCity.setText(startCity);
            }
        });
        citiesSpinnerEndCity.setItemSelectedListener((selectedCities) -> {
            if (selectedCities.size() > 0 &&
                    !selectedCities.get(0).isEmpty()) {
                destinationCity = selectedCities.get(0);
                citiesSpinnerEndCity.setText(destinationCity);
            }
        });

        intermediateStopsPicker.setItemSelectedListener((selectedCities) -> {
            intermediateStops = (ArrayList<String>) selectedCities;
            LinearLayout showIntermediateStopsContainer = view.findViewById(R.id.intermediate_stops_view);
            showIntermediateStopsContainer.removeAllViews();
            for (String intermediateStop : intermediateStops) {
                addTextView(showIntermediateStopsContainer, intermediateStop);
            }
        });

        view.findViewById(R.id.btn_intercity_choose_day).setOnClickListener((v) ->
                new DatePickerFragment().showWithCallback(getActivity().getSupportFragmentManager(),
                        () -> {
                            dateOfDeparture = DatePickerFragment.getDate();
                            ((Button) view.findViewById(R.id.btn_intercity_choose_day))
                                    .setText(DateFormatUtils.dateToString(dateOfDeparture, java.text.DateFormat.FULL));
                        }));
        view.findViewById(R.id.btn_intercity_choose_time).setOnClickListener((v) ->
                new TimePickerFragment().showWithCallback(getActivity().getSupportFragmentManager(),
                        () -> {
                            isHourSelected = true;
                            if (dateOfDeparture == null)
                                dateOfDeparture = new Date();
                            dateOfDeparture.setHours(TimePickerFragment.getDate().getHours());
                            dateOfDeparture.setMinutes(TimePickerFragment.getDate().getMinutes());
                            view.findViewById(R.id.btn_intercity_remove_time).setVisibility(View.VISIBLE);
                            ((Button) view.findViewById(R.id.btn_intercity_choose_time))
                                    .setText(DateFormatUtils.dateToHourString(dateOfDeparture));
                        }));


        addTravelBtn.setOnClickListener((v) -> {
            PrepareTravelPOJO(view);
        });
    }

    private static void showEditTextError(@NotNull TextInputEditText inputEditText, String errorMessage) {
        inputEditText.setError(errorMessage);
    }

    private void addTextView(@NotNull ViewGroup container, String intermediateStop) {
        TextView newTextView = new TextView(getContext());
        newTextView.setTextSize(14);
        TableRow.LayoutParams layoutParams = new TableRow.LayoutParams(ViewGroup.LayoutParams.WRAP_CONTENT, ViewGroup.LayoutParams.WRAP_CONTENT);
        layoutParams.setMargins(0, 3, 0, 3);

        newTextView.setLayoutParams(layoutParams);
        newTextView.setTypeface(ResourcesCompat.getFont(getContext(), R.font.crimson_text));
        newTextView.setCompoundDrawablesWithIntrinsicBounds(
                null, null, getResources().getDrawable(R.drawable.ic_remove), null);
        newTextView.setBackgroundResource(R.drawable.spinner_selected_city);
        newTextView.setText(intermediateStop);
        newTextView.setGravity(Gravity.CENTER);
        newTextView.setTextColor(getResources().getColor(R.color.colorOnSurface));
        newTextView.setVisibility(View.VISIBLE);
        newTextView.setOnClickListener((v) -> {
            v.setVisibility(View.GONE);
            intermediateStops.remove(newTextView.getText().toString());
        });
        container.addView(newTextView);
    }

    private void PrepareTravelPOJO(View view) {
        IntercityTravel intercityTravel = new IntercityTravel();
        boolean spaceForLuggageValue = spaceForLuggage.isChecked();
        String additionalInfo = moreInfoEditText.getText().toString();

        if (!validateInput())
            return;

        intercityTravel.setStartCity(startCity);
        intercityTravel.setDestinationCity(destinationCity);
        intercityTravel.setPassengersSpace(passengersSpace);
        intercityTravel.setDepartureDate(dateOfDeparture);
        intercityTravel.setIsHourSelected(isHourSelected);
        intercityTravel.setSpaceForLuggage(spaceForLuggageValue);
        intercityTravel.setAdditionalInfo(additionalInfo);
        intercityTravel.setIntermediateCityStops(intermediateStops);
        intercityTravel.setDateOfCreation(Calendar.getInstance().getTime());

        intercityTravel.setCreator(AuthenticationManager.getInstance().getCurrentLoggedInUser());
        intercityTravel.setCreatorUUID(AuthenticationManager.getInstance().getLoggedInFirebaseUser().getUid());


        IntercityTravelPreviewBinding binding = DataBindingUtil.inflate(
                getLayoutInflater(), R.layout.intercity_travel_preview, null, false);
        binding.setTravel(intercityTravel);

        AlertDialog travelPreviewDialog = new AlertDialog.Builder(getContext())
                .setTitle(getResources().getString(R.string.confirm_register_travel_title))
                .setView(binding.getRoot())
                .setPositiveButton(R.string.confirm, (dialog, which) -> {
                    registerTravel(view, intercityTravel, dialog);
                })
                .setNegativeButton(R.string.cancel, (dialog, which) -> {
                    dialog.dismiss();
                })
                .setCancelable(false)
                .create();

        travelPreviewDialog.show();
    }

    private void registerTravel(View view, IntercityTravel intercityTravel, DialogInterface dialog) {
        IntercityTravelRepository repository = new IntercityTravelRepository();
        repository.registerIntercityTravel(intercityTravel, new AsyncListenerWithResult() {
            @Override
            public void onStart() {
                view.findViewById(R.id.loading_overlay).setVisibility(View.VISIBLE);
            }

            @Override
            public void onSuccess(Object data) {
                DocumentReference travelReference = (DocumentReference) data;
                intercityTravel.setTravelID(travelReference.getId());
                new registerTravelToLocalDb(intercityTravel).execute();

                view.findViewById(R.id.loading_overlay).setVisibility(View.GONE);
                mListener.addTravelSuccessful(intercityTravel);
                dialog.dismiss();
                DialogBuilder.showSuccessDialog(
                        getContext(), getResources().getString(R.string.travel_successfully_added));
            }
        });
    }

    private boolean validateInput() {


        if (spaceForPassengersEditText.getText() == null ||
                spaceForPassengersEditText.getText().toString().isEmpty()) {
            showEditTextError(spaceForPassengersEditText, getString(R.string.empty_passanger_space));
            spaceForPassengersEditText.requestFocus();
            return false;
        }
        passengersSpace = Integer.parseInt(spaceForPassengersEditText.getText().toString());

        if (passengersSpace <= 0 || passengersSpace > 4) {
            showEditTextError(spaceForPassengersEditText, getString(R.string.invalid_passenger_space));
            return false;
        }

        if (startCity == null || startCity.isEmpty()) {
            DialogBuilder.showErrorMessage(getContext(),
                    getResources().getString(R.string.start_city_not_selected));
            return false;
        }
        if (destinationCity == null || destinationCity.isEmpty()) {
            DialogBuilder.showErrorMessage(getContext(),
                    getResources().getString(R.string.end_city_not_selected));
            return false;
        }

        if (startCity.equals(destinationCity)) {
            DialogBuilder.showErrorMessage(getContext(),
                    getResources().getString(R.string.start_city_same_as_destination));
            return false;
        }

        if (dateOfDeparture == null) {
            DialogBuilder.showErrorMessage(getContext(),
                    getResources().getString(R.string.date_of_departure_not_seleted));
            return false;

        }


        for (String intermediateStop : intermediateStops) {
            if (startCity.contains(intermediateStop)
                    || destinationCity.contains(intermediateStop)) {
                DialogBuilder.showErrorMessage(getContext(),
                        getResources().getString(R.string.intermediate_stop_same_as_start_or_destination));
                return false;
            }
        }

        return true;
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putString(START_LOCATION_SAVED_INSTANCE, startCity);
        outState.putString(END_LOCATION_SAVED_INSTANCE, destinationCity);
        outState.putStringArrayList(INTERMEDIATE_STOPS_SAVED_INSTANCE, intermediateStops);
        outState.putSerializable(DATE_OF_DEPARTURE_SAVED_INSTANCE, dateOfDeparture);
        outState.putString(SPACE_FOR_PASSANGERS_SAVED_INSTANCE, spaceForPassengersEditText.getText().toString());
        outState.putBoolean(SPACE_FOR_LUGGAGE_SAVED_INSTANCE, spaceForLuggage.isChecked());
        outState.putString(MORE_INFO_SAVED_INSTANCE, moreInfoEditText.getText().toString());
        outState.putBoolean(IS_HOUR_SELECTED_SAVE_INSTANCE, isHourSelected);
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

    public interface OnFragmentInteractionListener {
        void addTravelSuccessful(IntercityTravel travel);
    }

    private static class registerTravelToLocalDb extends AsyncTask<Void, Void, Void> {

        IntercityTravel travel;

        registerTravelToLocalDb(IntercityTravel travelToPersist) {
            travel = travelToPersist;
        }

        @Override
        protected Void doInBackground(Void... params) {
            SharedTravelApplication.dbSQL.intercityTravelDAO().insertAll(travel);
            return null;
        }
    }

}
