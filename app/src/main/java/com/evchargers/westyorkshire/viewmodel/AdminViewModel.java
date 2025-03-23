package com.evchargers.westyorkshire.viewmodel;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.evchargers.westyorkshire.model.Chargepoint;
import com.evchargers.westyorkshire.utils.FirebaseHelper;
import java.util.List;


public class AdminViewModel extends ViewModel {
    private static final String TAG = "AdminViewModel";
    private final FirebaseHelper firebaseHelper;
    private final MutableLiveData<List<Chargepoint>> chargepoints = new MutableLiveData<>();
    private final MutableLiveData<Boolean> isLoading = new MutableLiveData<>();
    private final MutableLiveData<String> errorMessage = new MutableLiveData<>();

    public AdminViewModel() {
        firebaseHelper = new FirebaseHelper();
    }

    public LiveData<List<Chargepoint>> getChargepoints() {
        return chargepoints;
    }

    public LiveData<Boolean> getIsLoading() {
        return isLoading;
    }

    public LiveData<String> getErrorMessage() {
        return errorMessage;
    }

    public void loadChargepoints() {
        isLoading.setValue(true);
        firebaseHelper.loadChargepoints(result -> {
            chargepoints.postValue(result);
            isLoading.postValue(false);
        });
    }

    public void addChargepoint(Chargepoint chargepoint, FirebaseHelper.OnCompleteListener listener) {
        isLoading.setValue(true);
        Log.d(TAG, "Adding new chargepoint: " + chargepoint.getName());
        firebaseHelper.updateChargepoint(chargepoint, success -> {
            isLoading.postValue(false);
            if (success) {
                Log.d(TAG, "Successfully added chargepoint");
                loadChargepoints();
            } else {
                Log.e(TAG, "Failed to add chargepoint");
            }
            listener.onComplete(success);
        });
    }

    public void updateChargepoint(Chargepoint chargepoint, FirebaseHelper.OnCompleteListener listener) {
        isLoading.setValue(true);
        Log.d(TAG, "Updating chargepoint with ID: " + chargepoint.getId());

        Chargepoint updatedChargepoint = new Chargepoint(
                chargepoint.getId(),
                chargepoint.getName(),
                chargepoint.getCounty(),
                chargepoint.getChargerType(),
                chargepoint.getLatitude(),
                chargepoint.getLongitude(),
                chargepoint.getStatus()
        );

        firebaseHelper.updateChargepoint(updatedChargepoint, success -> {
            isLoading.postValue(false);
            if (success) {
                Log.d(TAG, "Successfully updated chargepoint");
                loadChargepoints();
            } else {
                Log.e(TAG, "Failed to update chargepoint");
            }
            listener.onComplete(success);
        });
    }

    public void deleteChargepoint(String chargepointId, FirebaseHelper.OnCompleteListener listener) {
        isLoading.setValue(true);
        Log.d(TAG, "Deleting chargepoint with ID: " + chargepointId);
        firebaseHelper.deleteChargepoint(chargepointId, success -> {
            isLoading.postValue(false);
            if (success) {
                Log.d(TAG, "Successfully deleted chargepoint");
                loadChargepoints();
            } else {
                Log.e(TAG, "Failed to delete chargepoint");
            }
            listener.onComplete(success);
        });
    }

    public void importChargepoints(Context context, Uri fileUri, FirebaseHelper.OnCompleteListener listener) {
        isLoading.setValue(true);
        Log.d(TAG, "Starting chargepoint import");
        firebaseHelper.importChargepoints(context, fileUri, new FirebaseHelper.OnImportCompleteListener() {
            @Override
            public void onImportComplete(boolean success) {
                isLoading.postValue(false);
                if (success) {
                    Log.d(TAG, "Successfully imported chargepoints");
                    loadChargepoints();
                } else {
                    Log.e(TAG, "Failed to import chargepoints");
                }
                listener.onComplete(success);
            }

            @Override
            public void onError(String errorMessage) {
                isLoading.postValue(false);
                Log.e(TAG, "Import error: " + errorMessage);
                listener.onComplete(false);
            }
        });
    }







    public void checkAdminStatus(FirebaseHelper.OnAdminCheckListener listener) {
        Log.d(TAG, "Checking admin status");
        firebaseHelper.isCurrentUserAdmin(listener);
    }
}