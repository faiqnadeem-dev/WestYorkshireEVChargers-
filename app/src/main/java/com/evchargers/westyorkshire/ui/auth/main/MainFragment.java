package com.evchargers.westyorkshire.ui.auth.main;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.DividerItemDecoration;
import com.google.firebase.auth.FirebaseAuth;
import com.evchargers.westyorkshire.databinding.FragmentMainBinding;
import com.evchargers.westyorkshire.model.Chargepoint;
import com.evchargers.westyorkshire.utils.CSVParser;
import com.evchargers.westyorkshire.utils.FirebaseHelper;
import java.util.List;
import com.evchargers.westyorkshire.R;
import android.widget.ArrayAdapter;
import androidx.appcompat.widget.SearchView;
import java.util.ArrayList;
import androidx.lifecycle.ViewModelProvider;
import com.evchargers.westyorkshire.viewmodel.SharedViewModel;

public class MainFragment extends Fragment implements ChargepointAdapter.OnChargepointClickListener {
    private static final String[] FILTER_OPTIONS = {"All", "County", "Charger Type"};

    private FragmentMainBinding binding;
    private FirebaseAuth mAuth;
    private ChargepointAdapter adapter;
    private List<Chargepoint> chargepoints;
    private FirebaseHelper firebaseHelper;
    private SharedViewModel sharedViewModel;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mAuth = FirebaseAuth.getInstance();
        adapter = new ChargepointAdapter(this);
        firebaseHelper = new FirebaseHelper();
        sharedViewModel = new ViewModelProvider(requireActivity()).get(SharedViewModel.class);
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentMainBinding.inflate(inflater, container, false);
        setupRecyclerView();
        setupUI();
        setupObservers();
        loadChargepoints();
        return binding.getRoot();
    }

    private void setupRecyclerView() {
        binding.chargepointsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.chargepointsRecyclerView.addItemDecoration(new DividerItemDecoration(requireContext(), DividerItemDecoration.VERTICAL));
        binding.chargepointsRecyclerView.setAdapter(adapter);
    }

    private void setupUI() {
        binding.logoutButton.setOnClickListener(v -> handleLogout());
        binding.mapButton.setOnClickListener(v -> handleMapView());
        binding.importButton.setOnClickListener(v -> importToDatabase());
        setupSearch();
        checkAdminStatus();

        String userEmail = mAuth.getCurrentUser() != null ? mAuth.getCurrentUser().getEmail() : "";
        if (getActivity() != null && userEmail != null) {
            getActivity().setTitle("Welcome " + userEmail);
        }
    }

    private void setupObservers() {
        firebaseHelper.addChargepointsListener(updatedChargepoints -> {
            chargepoints = updatedChargepoints;
            adapter.setChargepoints(updatedChargepoints);
        });
    }

    private void performSearch(String query) {
        if (chargepoints == null) return;

        List<Chargepoint> filteredList = new ArrayList<>();
        String selectedFilter = binding.filterSpinner.getSelectedItem().toString();

        for (Chargepoint chargepoint : chargepoints) {
            boolean matches = false;
            if (selectedFilter.equals("County")) {
                matches = chargepoint.getCounty().toLowerCase().contains(query.toLowerCase());
            } else if (selectedFilter.equals("Charger Type")) {
                matches = chargepoint.getChargerType().toLowerCase().contains(query.toLowerCase());
            } else if (selectedFilter.equals("All")) {
                matches = chargepoint.getName().toLowerCase().contains(query.toLowerCase()) ||
                        chargepoint.getCounty().toLowerCase().contains(query.toLowerCase()) ||
                        chargepoint.getChargerType().toLowerCase().contains(query.toLowerCase()) ||
                        chargepoint.getStatus().toLowerCase().contains(query.toLowerCase());
            }
            if (matches) {
                filteredList.add(chargepoint);
            }
        }

        adapter.setChargepoints(filteredList);
        sharedViewModel.setFilteredChargepoints(filteredList);
    }

    private void checkAdminStatus() {
        firebaseHelper.isCurrentUserAdmin(isAdmin -> {
            requireActivity().runOnUiThread(() -> {
                if (isAdmin) {
                    binding.adminButton.setVisibility(View.VISIBLE);
                    binding.importButton.setVisibility(View.VISIBLE);
                    binding.adminButton.setOnClickListener(v -> {
                        Navigation.findNavController(requireView())
                                .navigate(R.id.action_mainFragment_to_adminFragment);
                    });
                } else {
                    binding.adminButton.setVisibility(View.GONE);
                    binding.importButton.setVisibility(View.GONE);
                }
            });
        });
    }


    private void setupSearch() {
        ArrayAdapter<String> spinnerAdapter = new ArrayAdapter<>(
                requireContext(),
                android.R.layout.simple_spinner_item,
                FILTER_OPTIONS
        );
        spinnerAdapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        binding.filterSpinner.setAdapter(spinnerAdapter);

        binding.searchView.setQueryHint("Search chargepoints...");
        binding.searchView.setIconifiedByDefault(false);

        binding.searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                performSearch(query);
                return true;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                performSearch(newText);
                return true;
            }
        });
    }

    private void handleLogout() {
        mAuth.signOut();
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_loginFragment);
    }

    private void handleMapView() {
        Navigation.findNavController(requireView())
                .navigate(R.id.action_mainFragment_to_mapFragment);
    }

    private void importToDatabase() {
        if (chargepoints != null && !chargepoints.isEmpty()) {
            firebaseHelper.importChargepointsToFirestore(chargepoints, success -> {
                String message = success ? "Data imported successfully" : "Import failed";
                Toast.makeText(requireContext(), message, Toast.LENGTH_SHORT).show();
            });
        } else {
            Toast.makeText(requireContext(), "No data to import", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onChargepointClick(Chargepoint chargepoint) {
        String status = chargepoint.getStatus();
        String message = String.format("%s\nStatus: %s\nType: %s",
                chargepoint.getName(),
                status,
                chargepoint.getChargerType());
        Toast.makeText(requireContext(), message, Toast.LENGTH_LONG).show();
    }

    private void loadChargepoints() {
        chargepoints = CSVParser.parseChargepoints(requireContext());
        if (chargepoints.isEmpty()) {
            Toast.makeText(requireContext(), "No chargepoints available", Toast.LENGTH_LONG).show();
        } else {
            adapter.setChargepoints(chargepoints);
            sharedViewModel.setFilteredChargepoints(chargepoints);
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
