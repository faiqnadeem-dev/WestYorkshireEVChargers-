package com.evchargers.westyorkshire.ui.auth.map;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.maps.model.MarkerOptions;
import com.evchargers.westyorkshire.R;
import com.evchargers.westyorkshire.model.Chargepoint;
import com.evchargers.westyorkshire.viewmodel.SharedViewModel;
import java.util.List;

public class MapFragment extends Fragment {
    private GoogleMap mMap;
    private SharedViewModel sharedViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        return inflater.inflate(R.layout.fragment_map, container, false);
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        SupportMapFragment mapFragment = (SupportMapFragment) getChildFragmentManager()
                .findFragmentById(R.id.map);
        if (mapFragment != null) {
            mapFragment.getMapAsync(googleMap -> {
                mMap = googleMap;
                setupMap();
                List<Chargepoint> currentFiltered = sharedViewModel.getFilteredChargepoints().getValue();
                if (currentFiltered != null && !currentFiltered.isEmpty()) {
                    updateMapMarkers(currentFiltered);
                }
                observeChargepoints();
            });
        }
    }

    private void setupMap() {
        if (mMap != null) {
            LatLng westYorkshire = new LatLng(53.8008, -1.5491);
            mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(westYorkshire, 10f));
            mMap.getUiSettings().setZoomControlsEnabled(true);
            mMap.getUiSettings().setCompassEnabled(true);
        }
    }

    private void observeChargepoints() {
        sharedViewModel.getFilteredChargepoints().observe(getViewLifecycleOwner(), chargepoints -> {
            if (chargepoints != null && !chargepoints.isEmpty()) {
                updateMapMarkers(chargepoints);
                focusMapOnMarkers(chargepoints);
            }
        });
    }

    private void updateMapMarkers(List<Chargepoint> chargepoints) {
        if (mMap == null) return;

        mMap.clear();
        for (Chargepoint chargepoint : chargepoints) {
            LatLng position = new LatLng(chargepoint.getLatitude(), chargepoint.getLongitude());
            float markerColor = chargepoint.getStatus().toLowerCase().contains("service") ?
                    BitmapDescriptorFactory.HUE_GREEN : BitmapDescriptorFactory.HUE_RED;

            mMap.addMarker(new MarkerOptions()
                    .position(position)
                    .title(chargepoint.getName())
                    .snippet(String.format("Status: %s\nType: %s",
                            chargepoint.getStatus(),
                            chargepoint.getChargerType()))
                    .icon(BitmapDescriptorFactory.defaultMarker(markerColor)));
        }
    }

    private void focusMapOnMarkers(List<Chargepoint> chargepoints) {
        if (chargepoints.isEmpty()) return;

        LatLngBounds.Builder builder = new LatLngBounds.Builder();
        for (Chargepoint chargepoint : chargepoints) {
            builder.include(new LatLng(chargepoint.getLatitude(), chargepoint.getLongitude()));
        }

        int padding = 100;
        LatLngBounds bounds = builder.build();
        mMap.animateCamera(CameraUpdateFactory.newLatLngBounds(bounds, padding));
    }
}
