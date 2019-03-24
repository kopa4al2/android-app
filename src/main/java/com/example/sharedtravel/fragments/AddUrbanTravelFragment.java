package com.example.sharedtravel.fragments;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.annotation.NonNull;
import android.support.annotation.Nullable;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.text.SpannableString;
import android.text.Spanned;
import android.text.style.ForegroundColorSpan;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.CompoundButton;
import android.widget.Switch;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.firebase.AuthenticationManager;
import com.example.sharedtravel.firebase.UrbanTravelRepository;
import com.example.sharedtravel.firebase.utils.AsyncListenerWithResult;
import com.example.sharedtravel.fragments.dialogs.DatePickerFragment;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;
import com.example.sharedtravel.fragments.dialogs.TimePickerFragment;
import com.example.sharedtravel.model.UrbanTravel;
import com.example.sharedtravel.utils.DateFormatUtils;
import com.example.sharedtravel.utils.WeekDaysConstants;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.common.util.ArrayUtils;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.maps.model.Polyline;
import com.google.android.gms.maps.model.PolylineOptions;
import com.google.android.gms.maps.model.RoundCap;

import java.io.Serializable;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.ListIterator;
import java.util.Map;
import java.util.Set;

import static android.content.Context.LOCATION_SERVICE;
import static android.view.View.GONE;
import static android.view.View.VISIBLE;
import static com.example.sharedtravel.MainActivity.PERMISSION_REQUEST_CODE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_HYBRID;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NONE;
import static com.google.android.gms.maps.GoogleMap.MAP_TYPE_NORMAL;

@SuppressWarnings("UnnecessaryBoxing")
public class AddUrbanTravelFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowClickListener,
        CompoundButton.OnCheckedChangeListener,
        ConnectionClassManager.ConnectionClassStateChangeListener {

    public static final String TAG = AddUrbanTravelFragment.class.getSimpleName();

    private static final int CURRENT_DIALOG_START = 0;
    private static final int CURRENT_DIALOG_INBETWEEN = 1;
    private static final int CURRENT_DIALOG_END = 2;
    private static final int CURRENT_DIALOG_ADDITIONAL_OPTIONS = 3;
    private static final int CURRENT_DIALOG_ADVANCED_OPTIONS = 4;
    private static final int CURRENT_DIALOG_CONFIRMATION = 5;
    private static final int CURRENT_DIALOG_CONFIRMED = 5;

    private static final int FREQUENCY_EVERY_DAY = 0;
    private static final int FREQUENCY_EVERY_WORK_DAY = 1;
    private static final int FREQUENCY_ONCE = 2;
    private static final int FREQUENCY_OTHER = 3;

    private static final int MARKER_JUST_CREATED = 0;
    private static final int MARKER_CLICKED = 1;
    private static final int MARKER_READY_TO_BE_DELETED = 2;

    private static final String CURRENT_DIALOG_SAVED_INSTANCE = "current_dialog";
    private static final String MARKERS_SAVED_STATE = "markers_in_dialogs";
    private static final String START_LOC_SAVED_STATE = "start_loc";
    private static final String END_LOC_SAVED_STATE = "end_loc";
    private static final String INBETWEEN_LOCS_SAVED_STATE = "inbetween_locs";
    private static final String TRAVEL_FREQUENCY_SAVED_STATE = "travel_frequency";
    private static final String TIME_OF_TRAVEL_SAVED_STATE = "time_of_travel";
    private static final String DATE_OF_TRAVEL_SAVED_STATE = "date_of_travel";
    private static final String DAYS_OF_WEEK_SAVED_STATE = "date_of_travel";
    private static final String MAP_TYPE_SAVED_STATE = "map_type";

    private int mapType = -100;

    //Map<Marker, The dialog phase it belongs to>
    private Map<Marker, Integer> markersInDialogs;

    private GoogleMap map;

    private TextView informationText;
    private Button nextButton;
    private Button backButton;

    private ViewGroup optionsViewGroup;
    private ViewGroup advancedOptionsViewGroup;

    private int currentDialog;

    private Marker currentMarker;

    private LatLng startLoc;
    private LatLng endLoc;
    private List<LatLng> inBetweenLocs;
    private int travelFrequency;
    private Date timeOfEveryDayTravel;
    private Date dateOfSingleTravel;
    private Set<Integer> daysOfWeek;

    private Polyline confirmTravelPolyLine;
    private View rootView;
    private Switch mapLayoutSwitch;

    @Override
    public void onViewStateRestored(@Nullable Bundle savedInstanceState) {
        if (savedInstanceState != null) {
            mapType = savedInstanceState.getInt(MAP_TYPE_SAVED_STATE);
            currentDialog = savedInstanceState.getInt(CURRENT_DIALOG_SAVED_INSTANCE);
            startLoc = savedInstanceState.getParcelable(START_LOC_SAVED_STATE);
            endLoc = savedInstanceState.getParcelable(END_LOC_SAVED_STATE);
            inBetweenLocs = savedInstanceState.getParcelableArrayList(INBETWEEN_LOCS_SAVED_STATE);
            if (inBetweenLocs == null)
                inBetweenLocs = new ArrayList<>(1);
            travelFrequency = savedInstanceState.getInt(TRAVEL_FREQUENCY_SAVED_STATE);
            try {
                timeOfEveryDayTravel = (Date) savedInstanceState.getSerializable(TIME_OF_TRAVEL_SAVED_STATE);
            } catch (ClassCastException ignored) {
            }
            try {
                dateOfSingleTravel = (Date) savedInstanceState.getSerializable(DATE_OF_TRAVEL_SAVED_STATE);
            } catch (ClassCastException ignored) {
            }
            int[] array = savedInstanceState.getIntArray(DAYS_OF_WEEK_SAVED_STATE);
            daysOfWeek = new HashSet<>(7);
            if (array != null)
                for (int anArray : array) {
                    daysOfWeek.add(anArray);
                }
            markersInDialogs = (Map<Marker, Integer>) savedInstanceState.getSerializable(MARKERS_SAVED_STATE);

        }
        super.onViewStateRestored(savedInstanceState);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        rootView = inflater.inflate(R.layout.add_urban_fragment, container, false);
        if (savedInstanceState == null) {
            currentDialog = CURRENT_DIALOG_START;
            inBetweenLocs = new ArrayList<>(1);
            markersInDialogs = new HashMap<>(3);
        }

        initViews();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    private void initViews() {
        informationText = rootView.findViewById(R.id.information_text);
        nextButton = rootView.findViewById(R.id.btn_add_urban_next);
        backButton = rootView.findViewById(R.id.btn_add_urban_back);

        advancedOptionsViewGroup = rootView.findViewById(R.id.add_urban_options_advanced);
        optionsViewGroup = rootView.findViewById(R.id.add_urban_options_panel);
    }

    @Override
    public void onActivityCreated(@Nullable Bundle savedInstanceState) {
        super.onActivityCreated(savedInstanceState);

    }


    @SuppressWarnings({"MethodWithMoreThanThreeNegations", "OverlyLongMethod"})
    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        if (markersInDialogs != null && !markersInDialogs.isEmpty()) {
            for (Marker marker : markersInDialogs.keySet()) {
                LatLng loc = marker.getPosition();
                Marker m = map.addMarker(new MarkerOptions().position(loc));
                m.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
                m.setTag(marker.getTag());
                m.setTitle(marker.getTitle());
            }
        }
        mapLayoutSwitch = rootView.findViewById(R.id.map_layout_switch);
        mapLayoutSwitch.setOnCheckedChangeListener(this);
        //Initial mapType, if its different, then we have preference
        if (mapType != -100) {
            map.setMapType(mapType);
        } else {
            if (ConnectionClassManager.getInstance().getCurrentBandwidthQuality() != ConnectionQuality.MODERATE
                    || ConnectionClassManager.getInstance().getCurrentBandwidthQuality() != ConnectionQuality.POOR) {
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);

            } else
                map.setMapType(MAP_TYPE_NORMAL);
        }
        mapLayoutSwitch.setChecked(map.getMapType() != GoogleMap.MAP_TYPE_HYBRID);
        mapLayoutSwitch.setTextColor(map.getMapType() == GoogleMap.MAP_TYPE_HYBRID ?
                Color.parseColor("#FFFFFF")
                :
                Color.parseColor("#000000"));


        if (!hasPermission()) {
            showPermissionDialog();
        }
        if (hasPermission()) {
            LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
            @SuppressLint("MissingPermission") Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude()))
                    .zoom(18)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        } else {
            //DEFAULT POSITION
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(42, 25))
                    .zoom(8)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
        }

        initDialog();
        addAdapterToMap();
        map.setOnMapClickListener(this);
        map.setOnInfoWindowClickListener(this);
        map.setOnMarkerClickListener(marker -> {
            if (currentMarker != null)
                currentMarker.remove();
            marker.showInfoWindow();
            return false;
        });

    }

    @Override
    public void onMapClick(LatLng latLng) {
        if (map.getCameraPosition().zoom < 10)
            return;
        //Add markers only if the user is in the appropriate dialog stage
        //0 - START , 1 - INBBETWEEN, 2 - END
        if (currentDialog < 0 || currentDialog > 2)
            return;
        if (currentMarker != null)
            currentMarker.remove();
        if (currentDialog == CURRENT_DIALOG_START && startLoc != null)
            return;
        if (currentDialog == CURRENT_DIALOG_END && endLoc != null)
            return;

        Marker marker = map.addMarker(new MarkerOptions()
                .position(latLng)
                .draggable(true)
                .title(getString(R.string.choose)));
        currentMarker = marker;
        String markerTag = String.valueOf(currentDialog) + '\n' + String.valueOf(MARKER_JUST_CREATED);
        marker.setTag(markerTag);

        marker.showInfoWindow();

    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        if (getMarkerState(marker) == MARKER_READY_TO_BE_DELETED && getMarkerDialog(marker) == currentDialog) {
            removeMarker(marker);
            return;
        }
        if (getMarkerDialog(marker) == currentDialog) {
            marker.setIcon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_GREEN));
            currentMarker = null;

            switch (getMarkerDialog(marker)) {
                case CURRENT_DIALOG_START: {
                    startLoc = marker.getPosition();
                    marker.setTitle(getString(R.string.you_start_from_here));
                    marker.showInfoWindow();
                }
                break;
                case CURRENT_DIALOG_INBETWEEN: {
                    inBetweenLocs.add(marker.getPosition());
                    marker.setTitle(getString(R.string.you_can_stop_here));
                    marker.showInfoWindow();
                }
                break;
                case CURRENT_DIALOG_END: {
                    endLoc = marker.getPosition();
                    marker.setTitle(getString(R.string.you_arrive_here));
                    marker.showInfoWindow();
                }
                break;
            }
        }
    }


    private void initDialog() {
        currentMarker = null;
        informationText.setTextColor(getResources().getColor(R.color.colorOnSurface));
        nextButton.setText(R.string.next);
        switch (currentDialog) {
            case CURRENT_DIALOG_START: {
                informationText.setText(R.string.where_do_you_start_from);
                nextButton.setOnClickListener((v) -> {
                    if (startLoc != null) {
                        hideAllMarkersInfoWindows();
                        currentDialog = CURRENT_DIALOG_INBETWEEN;
                        initDialog();
                    } else {
                        informationText.setText(R.string.start_loc_required);
                        informationText.setTextColor(getResources().getColor(R.color.colorError));
                    }
                });
                backButton.setEnabled(false);
            }
            break;
            case CURRENT_DIALOG_INBETWEEN: {
                String optional = getString(R.string.optional);
                String inBetweenText = getString(R.string.inbetween_stops);
                SpannableString spannable = new SpannableString(optional);
                spannable.setSpan(
                        new ForegroundColorSpan(getResources().getColor(R.color.colorGray)),
                        0, spannable.length(),
                        Spanned.SPAN_EXCLUSIVE_EXCLUSIVE
                );

                informationText.setText(String.format("%s%n%s*", inBetweenText, optional));
                nextButton.setOnClickListener((v) -> {
                    if (currentMarker != null && getMarkerState(currentMarker) == MARKER_CLICKED)
                        currentMarker.remove();
                    hideAllMarkersInfoWindows();
                    currentDialog = CURRENT_DIALOG_END;
                    initDialog();
                });
                backButton.setEnabled(true);
                backButton.setOnClickListener((v) -> {
                    hideAllMarkersInfoWindows();
                    currentDialog = CURRENT_DIALOG_START;
                    initDialog();
                });
            }
            break;
            case CURRENT_DIALOG_END: {
                informationText.setText(R.string.where_is_your_end_point);
                optionsViewGroup.setVisibility(GONE);
                nextButton.setOnClickListener((v) -> {
                    if (endLoc != null) {
                        hideAllMarkersInfoWindows();
                        currentDialog = CURRENT_DIALOG_ADDITIONAL_OPTIONS;
                        initDialog();
                    } else {
                        informationText.setText(R.string.end_loc_required);
                        informationText.setTextColor(getResources().getColor(R.color.colorError));
                    }
                });
                backButton.setOnClickListener((v) -> {
                    hideAllMarkersInfoWindows();
                    currentDialog = CURRENT_DIALOG_INBETWEEN;
                    initDialog();
                });
            }
            break;
            case CURRENT_DIALOG_ADDITIONAL_OPTIONS: {
                initOptionsView();
                informationText.setText(R.string.how_often_will_travel);
                advancedOptionsViewGroup.setVisibility(GONE);
                optionsViewGroup.setVisibility(VISIBLE);
                nextButton.setOnClickListener((v) -> {
                    //IF IT IS GREATER THAN , THE USER HAS CHOSEN AN OPTION
                    if (travelFrequency > -1) {
                        daysOfWeek = new HashSet<>(7);
                        currentDialog = CURRENT_DIALOG_ADVANCED_OPTIONS;
                        initDialog();
                    } else {
                        informationText.setText(R.string.choose_how_often_will_travel);
                        informationText.setTextColor(getResources().getColor(R.color.colorError));
                    }
                });
                backButton.setOnClickListener((v) -> {
                    hideAllMarkersInfoWindows();
                    currentDialog = CURRENT_DIALOG_END;
                    initDialog();
                });
            }
            break;
            case CURRENT_DIALOG_ADVANCED_OPTIONS: {
                initAdvancedView();
                informationText.setText(R.string.when_will_this_travel_occure);
                advancedOptionsViewGroup.setVisibility(VISIBLE);
                optionsViewGroup.setVisibility(View.GONE);
                nextButton.setOnClickListener((v) -> {
                    if (userSelectedNecessaryOptions()) {
                        currentDialog = CURRENT_DIALOG_CONFIRMATION;
                        initDialog();
                    } else {
                        informationText.setText(R.string.please_select_when_you_will_travel);
                        informationText.setTextColor(getResources().getColor(R.color.colorError));
                    }
                });
                backButton.setOnClickListener((v) -> {
                    currentDialog = CURRENT_DIALOG_ADDITIONAL_OPTIONS;
                    initDialog();
                });

            }
            break;
            case CURRENT_DIALOG_CONFIRMATION: {
                if (confirmTravelPolyLine != null)
                    confirmTravelPolyLine.remove();


                if (travelFrequency == FREQUENCY_EVERY_DAY)
                    addAllDays();
                if (travelFrequency == FREQUENCY_EVERY_WORK_DAY)
                    addAllWorkDays();

                advancedOptionsViewGroup.setVisibility(GONE);
                initConfirmView();
                nextButton.setText(R.string.confirm);
                nextButton.setOnClickListener((v) -> {

                    UrbanTravel travel = preparePOJO();
                    UrbanTravelRepository.addUrbanTravel(travel, getCallback());
                });
                backButton.setOnClickListener((v) -> {
                    if (confirmTravelPolyLine != null)
                        confirmTravelPolyLine.remove();

                    rootView.findViewById(R.id.confirm_urban_info_panel).setVisibility(View.GONE);
                    currentDialog = CURRENT_DIALOG_ADVANCED_OPTIONS;
                    initDialog();
                });
            }
            break;
        }
    }

    private AsyncListenerWithResult getCallback() {
        return new AsyncListenerWithResult() {

            @Override
            public void onStart() {
                rootView.findViewById(R.id.loading_overlay).setVisibility(VISIBLE);
            }

            @Override
            public void onFail(Exception exception) {
                DialogBuilder.showErrorMessage(getContext(), exception.getMessage());
            }

            @Override
            public void onSuccess(Object data) {
                DialogBuilder.showSuccessDialog(getContext(), getString(R.string.travel_successfully_added));
                informationText.setText(R.string.travel_successfully_added);
                confirmTravelPolyLine.remove();
                for (Map.Entry<Marker, Integer> markerIntegerEntry : markersInDialogs.entrySet()) {
                    markerIntegerEntry.getKey().remove();
                }
                currentDialog = CURRENT_DIALOG_CONFIRMED;
                startLoc = null;
                endLoc = null;
                inBetweenLocs.clear();

                //TODO: store in Local DB
//                new AddUrbanToSQLTask(data).execute();
            }
        };
    }

    private UrbanTravel preparePOJO() {
        UrbanTravel travel = new UrbanTravel();
        travel.setCreator(AuthenticationManager.getInstance().getCurrentLoggedInUser());
        travel.setStartPoint(new GeoLocation(startLoc.latitude, startLoc.longitude));
        travel.setEndPoint(new GeoLocation(endLoc.latitude, endLoc.longitude));
        travel.setFrequency(new ArrayList<>(daysOfWeek));
        travel.setDateOfTravel(travelFrequency == FREQUENCY_ONCE ? dateOfSingleTravel : timeOfEveryDayTravel);
        List<GeoLocation> inBetweensGeoLocs = new ArrayList<>(inBetweenLocs.size());
        for (LatLng inBetweenLoc : inBetweenLocs) {
            inBetweensGeoLocs.add(new GeoLocation(inBetweenLoc.latitude, inBetweenLoc.longitude));
        }
        travel.setInBetweenStops(inBetweensGeoLocs);
        return travel;
    }

    private void hideAllMarkersInfoWindows() {
        for (Marker marker : markersInDialogs.keySet()) {
            marker.hideInfoWindow();
        }
    }

    private void addAllWorkDays() {
        daysOfWeek.add(Calendar.MONDAY);
        daysOfWeek.add(Calendar.TUESDAY);
        daysOfWeek.add(Calendar.WEDNESDAY);
        daysOfWeek.add(Calendar.THURSDAY);
        daysOfWeek.add(Calendar.FRIDAY);
    }

    private void addAllDays() {
        addAllWorkDays();
        daysOfWeek.add(Calendar.SATURDAY);
        daysOfWeek.add(Calendar.SUNDAY);
    }

    private void initConfirmView() {
        informationText.setText(null);
        rootView.findViewById(R.id.confirm_urban_info_panel).setVisibility(VISIBLE);
        ViewGroup travelDaysContainer = rootView.findViewById(R.id.urban_travel_days_container);
        travelDaysContainer.removeAllViews();
        if (travelFrequency == FREQUENCY_EVERY_DAY || travelFrequency == FREQUENCY_EVERY_WORK_DAY || travelFrequency == FREQUENCY_OTHER) {
            rootView.findViewById(R.id.urban_travel_days).setVisibility(VISIBLE);
            rootView.findViewById(R.id.urban_date_of_travel_container).setVisibility(GONE);
            ((TextView) rootView.findViewById(R.id.urban_time_of_travel))
                    .setText(DateFormatUtils.dateToHourString(timeOfEveryDayTravel));
            for (Integer integer : daysOfWeek) {
                addDayTextView(travelDaysContainer, new WeekDaysConstants(getContext()).getDayName(integer));
            }
        } else if (travelFrequency == FREQUENCY_ONCE) {
            rootView.findViewById(R.id.urban_travel_days).setVisibility(GONE);
            rootView.findViewById(R.id.urban_date_of_travel_container).setVisibility(VISIBLE);
            ((TextView) rootView.findViewById(R.id.urban_date_of_travel))
                    .setText(DateFormatUtils.dateToString(dateOfSingleTravel, DateFormat.FULL));
            ((TextView) rootView.findViewById(R.id.urban_time_of_travel))
                    .setText(DateFormatUtils.dateToHourString(timeOfEveryDayTravel));
        }


        PolylineOptions polylineOptions = new PolylineOptions();
        polylineOptions.startCap(new RoundCap());
        polylineOptions.add(startLoc);
        for (LatLng inBetweenLoc : inBetweenLocs) {
            polylineOptions.add(inBetweenLoc);
        }
        polylineOptions.add(endLoc);
        polylineOptions.width(4);
        polylineOptions.color(getResources().getColor(R.color.colorPrimary));

        confirmTravelPolyLine = map.addPolyline(polylineOptions);
    }

    private void addDayTextView(ViewGroup travelDaysContainer, String text) {
        TextView textView = new TextView(getContext());
        textView.setTextSize(13);
        textView.setPadding(3, 3, 3, 3);
        textView.setText(text);

        travelDaysContainer.addView(textView);
    }

    private boolean userSelectedNecessaryOptions() {
        switch (travelFrequency) {
            case FREQUENCY_EVERY_DAY: {
                return timeOfEveryDayTravel != null;
            }
            case FREQUENCY_EVERY_WORK_DAY: {
                return timeOfEveryDayTravel != null;
            }
            case FREQUENCY_ONCE: {
                return dateOfSingleTravel != null && timeOfEveryDayTravel != null;
            }
            case FREQUENCY_OTHER: {
                return timeOfEveryDayTravel != null && !daysOfWeek.isEmpty();
            }
        }
        return false;
    }


    private void initAdvancedView() {
        ViewGroup daysOfWeekContainer = rootView.findViewById(R.id.week_days);
        Button pickTimeBtn = rootView.findViewById(R.id.btn_time_of_travel);
        Button pickDateBtn = rootView.findViewById(R.id.btn_date_of_travel);
        if (timeOfEveryDayTravel != null)
            pickTimeBtn.setText(DateFormatUtils.dateToHourString(timeOfEveryDayTravel));
        if (dateOfSingleTravel != null)
            pickDateBtn.setText(DateFormatUtils.dateToString(dateOfSingleTravel, DateFormat.FULL));

        for (int i = 0; i < daysOfWeekContainer.getChildCount(); i++) {
            ((CheckBox) daysOfWeekContainer.getChildAt(i)).setOnCheckedChangeListener(this);
            try {
                //Since i use Calendar.DaysOfWeek which start from 2 (Monday)
                @SuppressWarnings("UnnecessaryBoxing") boolean isChecked = daysOfWeek.contains(Integer.valueOf(i + 2));
                ((CheckBox) daysOfWeekContainer.getChildAt(i))
                        .setChecked(isChecked);
            } catch (IndexOutOfBoundsException ignored) {
                ((CheckBox) daysOfWeekContainer.getChildAt(i))
                        .setChecked(false);
            }
        }

        pickTimeBtn.setOnClickListener((v) -> new TimePickerFragment().showWithCallback(getFragmentManager(), () -> {
            this.timeOfEveryDayTravel = TimePickerFragment.getDate();
            pickTimeBtn.setText(DateFormatUtils.dateToHourString(timeOfEveryDayTravel));
        }));
        pickDateBtn.setOnClickListener((v) -> new DatePickerFragment().showWithCallback(getFragmentManager(), () -> {
            this.dateOfSingleTravel = DatePickerFragment.getDate();
            pickDateBtn.setText(DateFormatUtils.dateToString(dateOfSingleTravel, DateFormat.FULL));
        }));
        switch (travelFrequency) {
            case FREQUENCY_OTHER: {
                pickDateBtn.setVisibility(GONE);
                daysOfWeekContainer.setVisibility(VISIBLE);
                pickTimeBtn.setVisibility(VISIBLE);
            }
            break;
            case FREQUENCY_ONCE: {
                pickDateBtn.setVisibility(VISIBLE);
                daysOfWeekContainer.setVisibility(GONE);
            }
            break;
            default: {
                pickDateBtn.setVisibility(GONE);
                daysOfWeekContainer.setVisibility(GONE);
                pickTimeBtn.setVisibility(VISIBLE);
            }
            break;
        }
    }

    private void initOptionsView() {
        rootView.findViewById(R.id.radio_every_day).setOnClickListener((v) -> travelFrequency = FREQUENCY_EVERY_DAY);
        rootView.findViewById(R.id.radio_every_work_day).setOnClickListener((v) -> travelFrequency = FREQUENCY_EVERY_WORK_DAY);
        rootView.findViewById(R.id.radio_once).setOnClickListener((v) -> travelFrequency = FREQUENCY_ONCE);
        rootView.findViewById(R.id.radio_other).setOnClickListener((v) -> travelFrequency = FREQUENCY_OTHER);
    }

    private void addAdapterToMap() {
        map.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {

            @Override
            public View getInfoWindow(Marker marker) {
                return null;
            }

            @Override
            public View getInfoContents(Marker marker) {
                if (marker == null)
                    return null;
                View v = getActivity().getLayoutInflater().inflate(R.layout.map_info_layout, null);


                TextView locInfo = v.findViewById(R.id.location_info);
                TextView extraText = v.findViewById(R.id.extra_text);
                locInfo.setText(marker.getTitle());

                if (getMarkerDialog(marker) == currentDialog && getMarkerState(marker) == MARKER_CLICKED) {
                    markersInDialogs.put(marker, currentDialog);
                    extraText.setVisibility(VISIBLE);
                    extraText.setText(R.string.tap_to_remove);
                    setMarkerState(MARKER_READY_TO_BE_DELETED, marker);
                } else if (getMarkerDialog(marker) != currentDialog) {
                    extraText.setVisibility(GONE);
                } else if (getMarkerState(marker) == MARKER_JUST_CREATED) {
                    setMarkerState(MARKER_CLICKED, marker);
                    extraText.setVisibility(GONE);
                } else if (getMarkerState(marker) == MARKER_READY_TO_BE_DELETED) {
                    extraText.setVisibility(VISIBLE);
                    extraText.setText(R.string.tap_to_remove);
                }
                return v;
            }
        });
    }

    private void removeMarker(Marker marker) {
        if (currentDialog == CURRENT_DIALOG_INBETWEEN) {
            for (ListIterator<LatLng> iter = inBetweenLocs.listIterator(); iter.hasNext(); ) {
                LatLng element = iter.next();
                if (element.latitude == marker.getPosition().latitude &&
                        element.longitude == marker.getPosition().longitude)
                    iter.remove();
            }
        } else {
            if (currentDialog == CURRENT_DIALOG_START)
                startLoc = null;
            if (currentDialog == CURRENT_DIALOG_END)
                endLoc = null;
        }
        marker.remove();
        currentMarker = null;

    }

    private static int getMarkerState(Marker marker) {
        return Integer.valueOf(marker.getTag().toString().split("\n")[1]);
    }

    private static int getMarkerDialog(Marker marker) {
        return Integer.valueOf(marker.getTag().toString().split("\n")[0]);
    }

    private static void setMarkerState(int newMarkerState, Marker marker) {
        String[] markerTags = marker.getTag().toString().split("\n");
        marker.setTag(markerTags[0] + "\n" + newMarkerState);
    }

    @Override
    public void onCheckedChanged(CompoundButton buttonView, boolean isChecked) {
        switch (buttonView.getId()) {
            case R.id.map_layout_switch: {
                if (map == null)
                    return;
                if (isChecked) {
                    map.setMapType(MAP_TYPE_NORMAL);
                    mapType = MAP_TYPE_NORMAL;
                } else {
                    map.setMapType(MAP_TYPE_HYBRID);
                    mapType = MAP_TYPE_HYBRID;
                }
                mapLayoutSwitch.setTextColor(map.getMapType() == GoogleMap.MAP_TYPE_HYBRID ?
                        Color.parseColor("#FFFFFF")
                        :
                        Color.parseColor("#000000"));
            }
            break;
            case R.id.monday: {
                if (isChecked)
                    daysOfWeek.add(Calendar.MONDAY);
                else
                    daysOfWeek.remove(Integer.valueOf(Calendar.MONDAY));
            }
            break;
            case R.id.tuesday: {
                if (isChecked)
                    daysOfWeek.add(Calendar.TUESDAY);
                else
                    daysOfWeek.remove(Integer.valueOf(Calendar.TUESDAY));
            }
            break;
            case R.id.wednesday: {
                if (isChecked)
                    daysOfWeek.add(Calendar.WEDNESDAY);
                else
                    daysOfWeek.remove(Integer.valueOf(Calendar.WEDNESDAY));
            }
            break;
            case R.id.thursday: {
                if (isChecked)
                    daysOfWeek.add(Calendar.THURSDAY);
                else
                    daysOfWeek.remove(Integer.valueOf(Calendar.THURSDAY));
            }
            break;
            case R.id.friday: {
                if (isChecked)
                    daysOfWeek.add(Calendar.FRIDAY);
                else
                    daysOfWeek.remove(Integer.valueOf(Calendar.FRIDAY));
            }
            break;
            case R.id.saturday: {
                if (isChecked)
                    daysOfWeek.add(Calendar.SATURDAY);
                else
                    daysOfWeek.remove(Integer.valueOf(Calendar.SATURDAY));
            }
            break;
            case R.id.sunday: {
                if (isChecked)
                    daysOfWeek.add(Calendar.SUNDAY);
                else
                    daysOfWeek.remove(Integer.valueOf(Calendar.SUNDAY));
            }
            break;
        }
    }


    public void backPressed() {
        if(currentDialog == CURRENT_DIALOG_CONFIRMED) {
            currentDialog = CURRENT_DIALOG_START;
            initDialog();
            return;
        }
        if (currentDialog != CURRENT_DIALOG_START)
            backButton.callOnClick();
    }

    @Override
    public void onSaveInstanceState(@NonNull Bundle outState) {
        outState.putInt(MAP_TYPE_SAVED_STATE, mapType);
        outState.putInt(CURRENT_DIALOG_SAVED_INSTANCE, currentDialog);
        outState.putParcelable(START_LOC_SAVED_STATE, startLoc);
        outState.putParcelable(END_LOC_SAVED_STATE, endLoc);
        outState.putParcelableArrayList(INBETWEEN_LOCS_SAVED_STATE, (ArrayList<? extends Parcelable>) inBetweenLocs);
        outState.putInt(TRAVEL_FREQUENCY_SAVED_STATE, travelFrequency);
        outState.putSerializable(TIME_OF_TRAVEL_SAVED_STATE, timeOfEveryDayTravel);
        outState.putSerializable(DATE_OF_TRAVEL_SAVED_STATE, dateOfSingleTravel);
        outState.putIntArray(DAYS_OF_WEEK_SAVED_STATE, ArrayUtils.toPrimitiveArray(daysOfWeek));
        outState.putSerializable(MARKERS_SAVED_STATE, (Serializable) markersInDialogs);
        super.onSaveInstanceState(outState);
    }

    private void showPermissionDialog() {
        if (ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            return;
        } else {
            DialogBuilder.showConfirmationDialog(getContext(),
                    getString(R.string.ask_permission_location)
                    , this::requestPermission);
        }
    }


    private boolean hasPermission() {
        int result = 0;

        result = ContextCompat.checkSelfPermission(getContext(), Manifest.permission.ACCESS_FINE_LOCATION);
        return result == PackageManager.PERMISSION_GRANTED;
    }

    private void requestPermission() {
        if (!ActivityCompat.shouldShowRequestPermissionRationale(getActivity(), Manifest.permission.ACCESS_FINE_LOCATION)) {
            ActivityCompat.requestPermissions(getActivity(), new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, PERMISSION_REQUEST_CODE);
        }
    }

    @Override
    public void onBandwidthStateChange(ConnectionQuality bandwidthState) {

        if (bandwidthState == ConnectionQuality.MODERATE
                || bandwidthState == ConnectionQuality.POOR) {
            map.setMapType(MAP_TYPE_NORMAL);
            mapType = MAP_TYPE_NORMAL;
            DialogBuilder.showInfoDialog(getContext(), getString(R.string.changed_map_layout_due_to_low_latency_network));
        } else if (bandwidthState == ConnectionQuality.UNKNOWN) {
            map.setMapType(MAP_TYPE_NONE);
            mapType = MAP_TYPE_NONE;
            DialogBuilder.showInfoDialog(getContext(), getString(R.string.changed_map_layout_due_no_network));
        } else {
            map.setMapType(MAP_TYPE_HYBRID);
            mapType = MAP_TYPE_HYBRID;
            DialogBuilder.showInfoDialog(getContext(), getString(R.string.changed_map_layout_due_to_high_latency_network));
        }
        mapLayoutSwitch.setChecked(map.getMapType() != GoogleMap.MAP_TYPE_HYBRID);
        mapLayoutSwitch.setTextColor(map.getMapType() == GoogleMap.MAP_TYPE_HYBRID ?
                Color.parseColor("#FFFFFF")
                :
                Color.parseColor("#000000"));
    }

    private static class AddUrbanToSQLTask extends AsyncTask<Void,Void,Void> {
        UrbanTravel travel = new UrbanTravel();
        AddUrbanToSQLTask(Object o) {
        }

        @Override
        protected Void doInBackground(Void... voids) {
            return null;
        }
    }
}
