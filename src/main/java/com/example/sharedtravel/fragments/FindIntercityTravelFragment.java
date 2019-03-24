package com.example.sharedtravel.fragments;

import android.arch.paging.PagedList;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import com.example.sharedtravel.R;
import com.example.sharedtravel.firebase.IntercityTravelRepository;
import com.example.sharedtravel.firebase.utils.DocumentSnapshotConverter;
import com.example.sharedtravel.fragments.dialogs.DatePickerFragment;
import com.example.sharedtravel.fragments.dialogs.MoreFilterOptionsDialogFragment;
import com.example.sharedtravel.model.IntercityTravel;
import com.example.sharedtravel.utils.DateFormatUtils;
import com.example.sharedtravel.views.CitiesSpinner;
import com.example.sharedtravel.views.IntercityListAdapter;
import com.firebase.ui.firestore.SnapshotParser;
import com.firebase.ui.firestore.paging.FirestorePagingOptions;
import com.google.firebase.firestore.Query;

import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static com.example.sharedtravel.fragments.dialogs.MoreFilterOptionsDialogFragment.ALLOW_INTERMEDIATE_STOPS;
import static com.example.sharedtravel.fragments.dialogs.MoreFilterOptionsDialogFragment.EXACT_DATE_OF_DEPARTURE;
import static com.example.sharedtravel.fragments.dialogs.MoreFilterOptionsDialogFragment.LUGGAGE_SPACE;

@SuppressWarnings({"ConstantConditions", "MethodWithMoreThanThreeNegations"})
public class FindIntercityTravelFragment extends Fragment {

    private static final String TRAVELS_BUNDLE = "travels";
    private static final String START_CITY_BUNDLE = "start_city";
    private static final String END_CITY_BUNDLE = "end_city";
    private static final String DATE_BUNDLE = "date";

    private View fragmentView;

    private CitiesSpinner spinnerStartLocation;
    private CitiesSpinner spinnerDestination;
    private Button btnPickDate;
    private Button btnAdvancedFilter;

    private String startCity;
    private String endCity;
    private Date dateOfDeparture;
    private Button showResultsBtn;

    private List<IntercityTravel> travels;
    private Button btnClearFilter;
    private Bundle advancedFilterOptions;

    public FindIntercityTravelFragment() {
    }


    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        fragmentView = LayoutInflater.from(container.getContext())
                .inflate(R.layout.fragment_find_intercity, container, false);

        bindViews();
        bindListeners();

        fetchTravels(null);

        return fragmentView;
    }

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        restoreSavedInstance(savedInstanceState);
        super.onViewStateRestored(savedInstanceState);
    }

    private void restoreSavedInstance(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {


            if (savedInstanceState.getParcelableArrayList(TRAVELS_BUNDLE) != null) {
                travels = savedInstanceState.getParcelableArrayList(TRAVELS_BUNDLE);
            }
            if (savedInstanceState.getString(START_CITY_BUNDLE) != null) {
                startCity = savedInstanceState.getString(START_CITY_BUNDLE);
                spinnerStartLocation.setText(startCity);
            }
            if (savedInstanceState.getString(END_CITY_BUNDLE) != null) {
                endCity = savedInstanceState.getString(END_CITY_BUNDLE);
                spinnerDestination.setText(endCity);
            }
            if (savedInstanceState.getSerializable(DATE_BUNDLE) != null) {
                dateOfDeparture = (Date) savedInstanceState.getSerializable(DATE_BUNDLE);
                btnPickDate.setText(DateFormatUtils.dateToString(dateOfDeparture, DateFormat.LONG));
            }
        }
    }


    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        super.onSaveInstanceState(outState);

        outState.putParcelableArrayList(TRAVELS_BUNDLE, (ArrayList<? extends Parcelable>) travels);
        outState.putString(START_CITY_BUNDLE, startCity);
        outState.putString(END_CITY_BUNDLE, endCity);
        outState.putSerializable(DATE_BUNDLE, dateOfDeparture);
    }

    private void bindListeners() {

        btnClearFilter.setOnClickListener((v) -> {
            this.startCity = null;
            this.endCity = null;
            dateOfDeparture = null;
            spinnerStartLocation.setText(getResources().getString(R.string.from));
            spinnerDestination.setText(getResources().getString(R.string.to));
            btnPickDate.setText(getResources().getString(R.string.traveling_after));
            advancedFilterOptions = null;
        });

        btnAdvancedFilter.setOnClickListener((v) -> {
            if (getActivity().getSupportFragmentManager().findFragmentByTag("advanced_options") != null) {
                getActivity().getSupportFragmentManager().beginTransaction()
                        .remove(getActivity().getSupportFragmentManager().findFragmentByTag("advanced_options"))
                        .commit();
            }
            MoreFilterOptionsDialogFragment dialog = new MoreFilterOptionsDialogFragment();
            dialog.setOnConfirmListener(options -> {
                advancedFilterOptions = options;
            });

            dialog.setArguments(advancedFilterOptions);
            dialog.show(getFragmentManager(), "advanced_options");


        });

        spinnerStartLocation.setItemSelectedListener((citiesName) -> {
            if (citiesName != null) {
                startCity = citiesName.get(0);
                spinnerStartLocation.setText(startCity);
            }
        });

        spinnerDestination.setItemSelectedListener((citiesName) -> {
            if (citiesName != null) {
                endCity = citiesName.get(0);
                spinnerDestination.setText(endCity);
            }
        });

        btnPickDate.setOnClickListener(
                (v) -> {
                    new DatePickerFragment().showWithCallback(getActivity().getSupportFragmentManager(), () -> {
                        dateOfDeparture = DatePickerFragment.getDate();
                        btnPickDate.setText(DateFormatUtils.dateToString(dateOfDeparture, DateFormat.LONG));
                    });

                });

        bindFilterResultsListener();
    }

    private void bindFilterResultsListener() {
        showResultsBtn.setOnClickListener((v) -> {
            List<Query> queriesToExecute = initQuery();
            fetchTravels(queriesToExecute);
        });
    }


    private void fetchTravels(List<Query> queries) {
        PagedList.Config config = new PagedList.Config.Builder()
                .setEnablePlaceholders(false)
                .setPrefetchDistance(5)
                .setPageSize(15)
                .build();

        FirestorePagingOptions<IntercityTravel> options;
        if (queries == null || queries.isEmpty()) {
            Query getTravelsByDate = new IntercityTravelRepository().getTravelsByDepartureDate();
            getTravelsByDate = new IntercityTravelRepository().removePastDatesFromQuery(getTravelsByDate);
            options = new FirestorePagingOptions.Builder<IntercityTravel>()
                    .setLifecycleOwner(this)
                    .setQuery(getTravelsByDate, config, getSnapshotParser())
                    .build();
        } else if (queries.get(1) != null) {

            options = new FirestorePagingOptions.Builder<IntercityTravel>()
                    .setLifecycleOwner(this)
                    .setQuery(queries.get(0), config, getSnapshotParser())
                    .setQuery(queries.get(1), config, getSnapshotParser())
                    .build();
        } else {
            options = new FirestorePagingOptions.Builder<IntercityTravel>()
                    .setLifecycleOwner(this)
                    .setQuery(queries.get(0), config, getSnapshotParser())
                    .build();
        }

        RecyclerView rv = fragmentView.findViewById(R.id.rv_travel_results);
        IntercityListAdapter adapter = new IntercityListAdapter(options, getActivity(), fragmentView.findViewById(R.id.loading_overlay));
        removeParentScrollInRecyclerView(rv);
        rv.setAdapter(adapter);
    }

    private static SnapshotParser<IntercityTravel> getSnapshotParser() {
        return DocumentSnapshotConverter::convertSingleIntercity;
    }


    //TODO: FIX THIS MAGIC METHOD
    //Try to make sense at your own risk
    @SuppressWarnings({"MethodWithMoreThanThreeNegations", "OverlyLongMethod"})
    private List<Query> initQuery() {
        IntercityTravelRepository repository = new IntercityTravelRepository();
        Query baseQuery = null;
        Query intermediateQuery = null;

        boolean allowIntermediateStops = false;
        boolean searchExactDate = false;
        boolean spaceForLuggage = false;
        if (advancedFilterOptions != null) {
            if (advancedFilterOptions.get(EXACT_DATE_OF_DEPARTURE) != null)
                searchExactDate = (boolean) advancedFilterOptions.get(EXACT_DATE_OF_DEPARTURE);
            if (advancedFilterOptions.get(LUGGAGE_SPACE) != null)
                spaceForLuggage = (boolean) advancedFilterOptions.get(LUGGAGE_SPACE);
            if (advancedFilterOptions.get(ALLOW_INTERMEDIATE_STOPS) != null)
                allowIntermediateStops = (boolean) advancedFilterOptions.get(ALLOW_INTERMEDIATE_STOPS);
        }
        if (startCity == null && endCity == null && dateOfDeparture == null) {
            return null;
        }
        if (startCity != null && endCity == null && dateOfDeparture == null) {
            baseQuery = repository.getTravelsByStartCity(startCity, baseQuery);
        }
        if (startCity != null && endCity != null && dateOfDeparture == null) {
            baseQuery = repository.getTravelsByStartCity(startCity, baseQuery);
            if (allowIntermediateStops)
                intermediateQuery = repository.searchDestinationInIntermediateStops(endCity, baseQuery);
            baseQuery = repository.getTravelsByDestination(endCity, baseQuery);
        }
        if (startCity != null && endCity != null && dateOfDeparture != null) {
            baseQuery = repository.getTravelsByStartCity(startCity, baseQuery);

            if (searchExactDate)
                baseQuery = repository.getTravelsAtExactDay(dateOfDeparture,
                        baseQuery);
            else
                baseQuery = repository.getTravelsByDateGraterThan(dateOfDeparture, baseQuery);

            if (allowIntermediateStops)
                intermediateQuery = repository.searchDestinationInIntermediateStops(endCity, baseQuery);
            baseQuery = repository.getTravelsByDestination(endCity, baseQuery);
        }
        if (startCity == null && endCity != null && dateOfDeparture != null) {
            if (searchExactDate)
                baseQuery = repository.getTravelsAtExactDay(dateOfDeparture, baseQuery);
            else
                baseQuery = repository.getTravelsByDateGraterThan(dateOfDeparture, baseQuery);
            if (allowIntermediateStops) {
                intermediateQuery = repository.searchDestinationInIntermediateStops(endCity, baseQuery);
            }
            baseQuery = repository.getTravelsByDestination(endCity, baseQuery);
        }
        if (startCity == null && endCity != null && dateOfDeparture == null) {
            if (allowIntermediateStops)
                intermediateQuery = repository.searchDestinationInIntermediateStops(endCity, baseQuery);

            baseQuery = repository.getTravelsByDestination(endCity, baseQuery);
        }
        if (startCity != null && endCity == null && dateOfDeparture != null) {
            baseQuery = repository.getTravelsByStartCity(startCity, baseQuery);
            if (searchExactDate)
                baseQuery = repository.getTravelsAtExactDay(dateOfDeparture, baseQuery);
            else
                baseQuery = repository.getTravelsByDateGraterThan(dateOfDeparture, baseQuery);
        }
        if (startCity == null && endCity == null && dateOfDeparture != null) {
            if (searchExactDate)
                baseQuery = repository.getTravelsAtExactDay(dateOfDeparture, baseQuery);
            else
                baseQuery = repository.getTravelsByDateGraterThan(dateOfDeparture, baseQuery);
        }


        if (spaceForLuggage)
            baseQuery = repository.getTravelsWithSpaceForLuggage(baseQuery);
        if (allowIntermediateStops && intermediateQuery != null) {
            intermediateQuery = repository.removePastDatesFromQuery(intermediateQuery);
            intermediateQuery = repository.orderByDate(intermediateQuery);
        }

        if (baseQuery == null)
            return null;

        List<Query> queriesToExecute = new ArrayList<>(2);
        baseQuery = repository.removePastDatesFromQuery(baseQuery);
        baseQuery = repository.orderByDate(baseQuery);

        queriesToExecute.add(baseQuery);
        queriesToExecute.add(intermediateQuery);

        return queriesToExecute;
    }

    private static void removeParentScrollInRecyclerView(RecyclerView rv) {
        rv.addOnItemTouchListener(new RecyclerView.OnItemTouchListener() {
            @Override
            public boolean onInterceptTouchEvent(RecyclerView rv, MotionEvent e) {
                int action = e.getAction();
                switch (action) {
                    case MotionEvent.ACTION_MOVE:
                        rv.getParent().requestDisallowInterceptTouchEvent(true);
                        break;
                }
                return false;
            }

            @Override
            public void onTouchEvent(RecyclerView recyclerView, MotionEvent motionEvent) {

            }

            @Override
            public void onRequestDisallowInterceptTouchEvent(boolean b) {

            }
        });
    }

    private void bindViews() {
        spinnerDestination = fragmentView.findViewById(R.id.sp_find_city_destination);
        spinnerStartLocation = fragmentView.findViewById(R.id.sp_find_city_start);
        btnPickDate = fragmentView.findViewById(R.id.btn_choose_date_find_travel);
        btnAdvancedFilter = fragmentView.findViewById(R.id.btn_advanced_search_intercity);

        showResultsBtn = fragmentView.findViewById(R.id.btn_show_results_intercity);

        btnClearFilter = fragmentView.findViewById(R.id.btn_clear_filters);
    }


}