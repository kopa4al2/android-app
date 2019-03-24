package com.example.sharedtravel.fragments;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.Fragment;
import android.support.v4.content.ContextCompat;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.SeekBar;
import android.widget.Switch;
import android.widget.TextView;

import com.example.sharedtravel.R;
import com.example.sharedtravel.fragments.dialogs.DialogBuilder;
import com.facebook.network.connectionclass.ConnectionClassManager;
import com.facebook.network.connectionclass.ConnectionQuality;
import com.firebase.geofire.GeoLocation;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;

import org.jetbrains.annotations.Contract;
import org.jetbrains.annotations.NotNull;

import java.util.Objects;

import static android.content.Context.LOCATION_SERVICE;
import static com.example.sharedtravel.MainActivity.PERMISSION_REQUEST_CODE;

@SuppressWarnings("ALL")
public class FindUrbanTravelFragment extends Fragment implements OnMapReadyCallback,
        GoogleMap.OnMapClickListener,
        GoogleMap.OnInfoWindowClickListener,
        ConnectionClassManager.ConnectionClassStateChangeListener,
        SeekBar.OnSeekBarChangeListener {

    private static final int DEFAULT_SEARCH_RADIUS = 200;
    private static final int START_DIALOG = 0;
    private static final int DESTINATION_DIALOG = 1;
    private static final int CONFIRMATION_DIALOG = 2;

    private int currentDialog = 0;

    private Button prevButton;
    private Button nextButton;
    private TextView infoText;
    private GoogleMap map;
    private View rootView;
    private Switch layoutSwitch;
    private Circle currentSearchRadius;
    private TextView tvCurrentRadius;

    private Marker currentMarker;

    private UrbanDestination startPoint;
    private UrbanDestination endPoint;


    public FindUrbanTravelFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        ConnectionClassManager.getInstance().register(this);

        super.onCreate(savedInstanceState);
    }


    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {

        rootView = inflater.inflate(R.layout.fragment_find_urban_travel, container, false);

        tvCurrentRadius = rootView.findViewById(R.id.tv_current_radius);
        String text = String.valueOf(DEFAULT_SEARCH_RADIUS) + getString(R.string.meters);
        tvCurrentRadius.setText(text);
        prevButton = rootView.findViewById(R.id.btn_find_urban_back);
        nextButton = rootView.findViewById(R.id.btn_find_urban_next);
        infoText = rootView.findViewById(R.id.tv_find_urban_information);

        if (savedInstanceState == null)
            currentDialog = START_DIALOG;
        handleDialog();

        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager().findFragmentById(R.id.find_urban_map);
        mapFragment.getMapAsync(this);
        return rootView;
    }

    private void handleDialog() {
        infoText.setTextColor(getContext().getResources().getColor(R.color.colorOnSurface));
        switch (currentDialog) {
            case START_DIALOG:
                handleStartDialog();
                break;
            case DESTINATION_DIALOG:
                handleDestinationDialog();
                break;
            case CONFIRMATION_DIALOG:
                handleConfirmationDialog();
                break;
        }
    }

    private void handleConfirmationDialog() {

    }

    private void handleDestinationDialog() {
        if (endPoint != null) {
            setCurrentMarker(endPoint.getLatLng(), endPoint.getRadius());
        }
        infoText.setText(R.string.select_destination_and_radius);
        prevButton.setEnabled(true);
        prevButton.setOnClickListener((v) -> {
            currentDialog = START_DIALOG;
            handleDialog();
        });
        nextButton.setOnClickListener((v) -> {
            if (currentMarker != null && currentSearchRadius != null) {
                endPoint = createUrbanPoint();
                currentDialog = CONFIRMATION_DIALOG;
                handleDialog();
            } else {
                showError(R.string.end_city_not_selected);
            }
        });
    }

    @NotNull
    private FindUrbanTravelFragment.UrbanDestination createUrbanPoint() {
        return new UrbanDestination(
                new GeoLocation(currentMarker.getPosition().latitude, currentMarker.getPosition().longitude),
                currentSearchRadius.getRadius());
    }

    private void handleStartDialog() {
        if (startPoint != null) {
            setCurrentMarker(startPoint.getLatLng(), startPoint.getRadius());
        }
        infoText.setText(R.string.select_start_location_and_radius);
        prevButton.setEnabled(false);
        nextButton.setOnClickListener((v) -> {
            if (currentMarker != null) {
                startPoint = createUrbanPoint();

                currentMarker.remove();
                currentSearchRadius.remove();
                currentDialog = DESTINATION_DIALOG;
                handleDialog();
            } else {
                showError(R.string.start_loc_required);
            }
        });
    }

    private void setCurrentMarker(LatLng latLng, double radius) {
        if (currentMarker != null && currentSearchRadius != null) {
            currentMarker.remove();
            currentSearchRadius.remove();
        }

        currentMarker = map.addMarker(new MarkerOptions()
                .position(latLng));
        currentSearchRadius = map.addCircle(getCircleOptions(latLng, radius));
    }

    private void showError(int errorStringId) {
        infoText.setText(errorStringId);
        infoText.setTextColor(getContext().getResources().getColor(R.color.colorError));
    }


    @Override
    public void onAttach(Context context) {
        super.onAttach(context);
    }

    @Override
    public void onDetach() {
        super.onDetach();
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        map = googleMap;
        layoutSwitch = rootView.findViewById(R.id.map_layout_switch);
        map.setOnMapClickListener(this);

        SeekBar searchRadiusSeekBar = rootView.findViewById(R.id.set_search_radius);
        searchRadiusSeekBar.setProgress(DEFAULT_SEARCH_RADIUS);
        searchRadiusSeekBar.setOnSeekBarChangeListener(this);
        if (ConnectionClassManager.getInstance().getCurrentBandwidthQuality() != ConnectionQuality.MODERATE
                || ConnectionClassManager.getInstance().getCurrentBandwidthQuality() != ConnectionQuality.POOR) {
            layoutSwitch.setChecked(false);
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        } else {
            layoutSwitch.setChecked(true);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
        }

        layoutSwitch.setOnCheckedChangeListener((buttonView, isChecked) -> {
            if (map == null)
                return;
            if (isChecked) {
                map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            } else
                map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
        });
        if (!checkPermission()) {
            showPermissionDialog();
        }
        if (checkPermission()) {
            LocationManager locationManager = (LocationManager) getContext().getSystemService(LOCATION_SERVICE);
            Location lastKnownLocation = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            LatLng lastKnownLocationLatLng = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(lastKnownLocationLatLng)
                    .zoom(18)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            setCurrentMarker(lastKnownLocationLatLng, DEFAULT_SEARCH_RADIUS);
        } else {
            //DEFAULT POSITION
            CameraPosition cameraPosition = new CameraPosition.Builder()
                    .target(new LatLng(42, 25))
                    .zoom(8)
                    .build();
            map.moveCamera(CameraUpdateFactory.newCameraPosition(cameraPosition));
            setCurrentMarker(new LatLng(42, 25), DEFAULT_SEARCH_RADIUS);
        }
    }

    private CircleOptions getCircleOptions(LatLng center, double radius) {
        return new CircleOptions()
                .center(center)
                .radius(radius)
                .strokeWidth(3)
                .fillColor(getContext().getResources().getColor(R.color.colorAccentWithAlpha))
                .strokeColor(Color.parseColor("#00000000"));
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

    private boolean checkPermission() {
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
            layoutSwitch.setChecked(true);
            map.setMapType(GoogleMap.MAP_TYPE_NORMAL);
            DialogBuilder.showInfoDialog(getContext(), getString(R.string.changed_map_layout_due_to_low_latency_network));
        } else if (bandwidthState == ConnectionQuality.UNKNOWN) {
            DialogBuilder.showInfoDialog(getContext(), getString(R.string.changed_map_layout_due_no_network));
            map.setMapType(GoogleMap.MAP_TYPE_NONE);
            layoutSwitch.setEnabled(false);
        } else {
            DialogBuilder.showInfoDialog(getContext(), getString(R.string.changed_map_layout_due_to_high_latency_network));
            map.setMapType(GoogleMap.MAP_TYPE_HYBRID);
            layoutSwitch.setChecked(false);
        }
    }

    @Override
    public void onInfoWindowClick(Marker marker) {

    }

    @Override
    public void onMapClick(LatLng latLng) {
        setCurrentMarker(latLng, currentSearchRadius.getRadius());
    }

    @Override
    public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
        int MINIMUM_SEARCH_RADIUS = 50;
        if (progress < MINIMUM_SEARCH_RADIUS)
            return;
        if (currentSearchRadius != null) {
            String text = String.valueOf(progress) + " " + getString(R.string.meters);
            tvCurrentRadius.setText(text);
            currentSearchRadius.setRadius(progress);
        }
    }

    @Override
    public void onStartTrackingTouch(SeekBar seekBar) {

    }

    @Override
    public void onStopTrackingTouch(SeekBar seekBar) {

    }

    @SuppressWarnings({"ObjectInstantiationInEqualsHashCode", "NonFinalFieldReferenceInEquals", "NonFinalFieldReferencedInHashCode"})
    private class UrbanDestination {

        private GeoLocation center;
        private double radius;

        public UrbanDestination() {
        }

        public UrbanDestination(GeoLocation center, double radius) {
            this.center = center;
            this.radius = radius;
        }

        public GeoLocation getCenter() {
            return center;
        }

        public void setCenter(GeoLocation center) {
            this.center = center;
        }

        public double getRadius() {
            return radius;
        }

        public void setRadius(double radius) {
            this.radius = radius;
        }

        public LatLng getLatLng() {
            return new LatLng(center.latitude, center.longitude);
        }

        @Contract(value = "null -> false", pure = true)
        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            UrbanDestination that = (UrbanDestination) o;
            return Double.compare(that.radius, radius) == 0 &&
                    Double.compare(center.latitude, that.center.latitude) == 0 &&
                    Double.compare(center.longitude, that.center.longitude) == 0;
        }

        @Override
        public int hashCode() {
            return Objects.hash(center.latitude, center.longitude, radius);
        }
    }
}
