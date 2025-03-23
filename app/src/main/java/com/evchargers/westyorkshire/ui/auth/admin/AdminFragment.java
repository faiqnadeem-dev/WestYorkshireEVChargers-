package com.evchargers.westyorkshire.ui.auth.admin;

import android.app.Activity;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.LinearLayoutManager;
import com.google.android.material.dialog.MaterialAlertDialogBuilder;
import com.google.android.material.textfield.TextInputEditText;
import com.evchargers.westyorkshire.R;
import com.evchargers.westyorkshire.databinding.FragmentAdminBinding;
import com.evchargers.westyorkshire.model.Chargepoint;
import com.evchargers.westyorkshire.viewmodel.AdminViewModel;

public class AdminFragment extends Fragment implements AdminChargepointAdapter.OnChargepointActionListener {
    private static final String TAG = "AdminFragment";
    private FragmentAdminBinding binding;
    private AdminViewModel viewModel;
    private AdminChargepointAdapter adapter;
    private ActivityResultLauncher<Intent> filePickerLauncher;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        Log.d(TAG, "1. onCreate started");
        try {
            viewModel = new ViewModelProvider(this).get(AdminViewModel.class);
            Log.d(TAG, "2. ViewModel created");

            adapter = new AdminChargepointAdapter();
            Log.d(TAG, "3. Adapter created");

            adapter.setListener(this);
            Log.d(TAG, "4. Listener set");

            filePickerLauncher = registerForActivityResult(
                    new ActivityResultContracts.StartActivityForResult(),
                    result -> {
                        if (result.getResultCode() == Activity.RESULT_OK && result.getData() != null) {
                            handleFileImport(result.getData().getData());
                        }
                    }
            );
            Log.d(TAG, "5. FilePicker launcher created");
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreate", e);
        }
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        Log.d(TAG, "6. onCreateView started");
        try {
            binding = FragmentAdminBinding.inflate(inflater, container, false);
            Log.d(TAG, "7. Binding inflated");

            setupRecyclerView();
            Log.d(TAG, "8. RecyclerView setup complete");

            setupUI();
            Log.d(TAG, "9. UI setup complete");

            setupObservers();
            Log.d(TAG, "10. Observers setup complete");

            checkAdminStatus();
            Log.d(TAG, "11. Admin status check initiated");

            return binding.getRoot();
        } catch (Exception e) {
            Log.e(TAG, "Error in onCreateView", e);
            throw e;
        }
    }

    private void setupRecyclerView() {
        try {
            binding.adminRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
            binding.adminRecyclerView.setAdapter(adapter);
        } catch (Exception e) {
            Log.e(TAG, "Error setting up RecyclerView", e);
        }
    }

    private void setupUI() {
        try {
            binding.addChargepointFab.setOnClickListener(v -> showAddChargepointDialog());
            binding.importButton.setOnClickListener(v -> launchFilePicker());
        } catch (Exception e) {
            Log.e(TAG, "Error setting up UI", e);
        }
    }

    private void setupObservers() {
        Log.d(TAG, "Setting up observers");
        viewModel.getChargepoints().observe(getViewLifecycleOwner(), chargepoints -> {
            Log.d(TAG, "Received chargepoints: " + (chargepoints != null ? chargepoints.size() : 0));
            adapter.setChargepoints(chargepoints);
            updateEmptyState(chargepoints != null && !chargepoints.isEmpty());
        });

        viewModel.loadChargepoints();
    }

    private void updateEmptyState(boolean hasData) {
        binding.adminRecyclerView.setVisibility(hasData ? View.VISIBLE : View.GONE);
        binding.emptyStateText.setVisibility(hasData ? View.GONE : View.VISIBLE);
    }

    private void checkAdminStatus() {
        viewModel.checkAdminStatus(isAdmin -> {
            if (!isAdmin) {
                Toast.makeText(requireContext(), R.string.admin_access_denied, Toast.LENGTH_LONG).show();
                requireActivity().onBackPressed();
            }
        });
    }

    private void launchFilePicker() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.setType("text/csv");
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        filePickerLauncher.launch(Intent.createChooser(intent, "Select CSV File"));
    }

    private void handleFileImport(Uri fileUri) {
        viewModel.importChargepoints(requireContext(), fileUri, success -> {
            int messageResId = success ? R.string.import_success : R.string.import_failed;
            Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
        });
    }

    @Override
    public void onEditClick(Chargepoint chargepoint) {
        showEditDialog(chargepoint);
    }

    private void showEditDialog(Chargepoint chargepoint) {
        Log.d(TAG, "Starting edit dialog for chargepoint: " + chargepoint.getId());

        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_chargepoint, null);

        setupDialogFields(dialogView, chargepoint);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.edit_chargepoint)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    String name = getTextFromDialog(dialogView, R.id.editName);
                    String status = getTextFromDialog(dialogView, R.id.editStatus);
                    String type = getTextFromDialog(dialogView, R.id.editType);

                    Log.d(TAG, "Collected dialog values - Name: " + name + ", Status: " + status + ", Type: " + type);

                    if (name.isEmpty() || status.isEmpty() || type.isEmpty()) {
                        Toast.makeText(requireContext(), R.string.all_fields_required, Toast.LENGTH_SHORT).show();
                        return;
                    }

                    Chargepoint updatedChargepoint = new Chargepoint(
                            chargepoint.getId(),
                            name,
                            "West Yorkshire",
                            type,
                            chargepoint.getLatitude(),
                            chargepoint.getLongitude(),
                            status
                    );

                    Log.d(TAG, "Sending update for chargepoint: " + updatedChargepoint.toString());

                    // Send directly to ViewModel
                    viewModel.updateChargepoint(updatedChargepoint, success -> {
                        Log.d(TAG, "Update result received: " + success);
                        int messageResId = success ? R.string.update_success : R.string.update_failed;
                        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
                        if (success) {
                            viewModel.loadChargepoints();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }




    @Override
    public void onDeleteClick(Chargepoint chargepoint) {
        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.delete_confirmation)
                .setMessage(R.string.delete_message)
                .setPositiveButton(R.string.delete, (dialog, which) -> {
                    viewModel.deleteChargepoint(chargepoint.getId(), success -> {
                        int messageResId = success ? R.string.delete_success : R.string.delete_failed;
                        Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
                        if (success) {
                            viewModel.loadChargepoints();
                        }
                    });
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void showAddChargepointDialog() {
        View dialogView = LayoutInflater.from(requireContext())
                .inflate(R.layout.dialog_edit_chargepoint, null);

        new MaterialAlertDialogBuilder(requireContext())
                .setTitle(R.string.add_chargepoint)
                .setView(dialogView)
                .setPositiveButton(R.string.save, (dialog, which) -> {
                    Chargepoint newChargepoint = createChargepointFromDialog(dialogView);
                    if (newChargepoint != null) {
                        viewModel.addChargepoint(newChargepoint, success -> {
                            int messageResId = success ? R.string.add_success : R.string.add_failed;
                            Toast.makeText(requireContext(), messageResId, Toast.LENGTH_SHORT).show();
                            if (success) {
                                viewModel.loadChargepoints();
                            }
                        });
                    }
                })
                .setNegativeButton(R.string.cancel, null)
                .show();
    }

    private void setupDialogFields(View dialogView, Chargepoint chargepoint) {
        ((TextInputEditText) dialogView.findViewById(R.id.editName)).setText(chargepoint.getName());
        ((TextInputEditText) dialogView.findViewById(R.id.editStatus)).setText(chargepoint.getStatus());
        ((TextInputEditText) dialogView.findViewById(R.id.editType)).setText(chargepoint.getChargerType());
    }

    private boolean updateChargepointFromDialog(View dialogView, Chargepoint chargepoint) {
        String name = getTextFromDialog(dialogView, R.id.editName);
        String status = getTextFromDialog(dialogView, R.id.editStatus);
        String type = getTextFromDialog(dialogView, R.id.editType);

        Log.d(TAG, "Dialog values - Name: " + name + ", Status: " + status + ", Type: " + type);

        if (name.isEmpty() || status.isEmpty() || type.isEmpty()) {
            Toast.makeText(requireContext(), R.string.all_fields_required, Toast.LENGTH_SHORT).show();
            return false;
        }

        double originalLat = chargepoint.getLatitude();
        double originalLong = chargepoint.getLongitude();
        String originalId = chargepoint.getId();

        Chargepoint updatedChargepoint = new Chargepoint(
                originalId,
                name,
                "West Yorkshire",
                type,
                originalLat,
                originalLong,
                status
        );

        chargepoint.setName(updatedChargepoint.getName());
        chargepoint.setCounty(updatedChargepoint.getCounty());
        chargepoint.setChargerType(updatedChargepoint.getChargerType());
        chargepoint.setStatus(updatedChargepoint.getStatus());

        Log.d(TAG, "Updated chargepoint: " + chargepoint.toString());
        return true;
    }

    private Chargepoint createChargepointFromDialog(View dialogView) {
        String name = getTextFromDialog(dialogView, R.id.editName);
        String status = getTextFromDialog(dialogView, R.id.editStatus);
        String type = getTextFromDialog(dialogView, R.id.editType);

        if (name.isEmpty() || status.isEmpty() || type.isEmpty()) {
            Toast.makeText(requireContext(), R.string.all_fields_required, Toast.LENGTH_SHORT).show();
            return null;
        }

        return new Chargepoint(
                String.valueOf(System.currentTimeMillis()),
                name,
                "West Yorkshire",
                type,
                0.0,
                0.0,
                status
        );
    }

    private String getTextFromDialog(View dialogView, int editTextId) {
        TextInputEditText editText = dialogView.findViewById(editTextId);
        return editText.getText() != null ? editText.getText().toString().trim() : "";
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}