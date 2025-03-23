package com.evchargers.westyorkshire.viewmodel;

import androidx.lifecycle.LiveData;
import androidx.lifecycle.MutableLiveData;
import androidx.lifecycle.ViewModel;
import com.evchargers.westyorkshire.model.Chargepoint;
import com.evchargers.westyorkshire.utils.FirebaseHelper;
import java.util.List;

public class SharedViewModel extends ViewModel {
    private final FirebaseHelper firebaseHelper = new FirebaseHelper();
    private final MutableLiveData<List<Chargepoint>> chargepoints = new MutableLiveData<>();
    private final MutableLiveData<List<Chargepoint>> filteredChargepoints = new MutableLiveData<>();

    public SharedViewModel() {
        firebaseHelper.addChargepointsListener(updatedChargepoints -> {
            chargepoints.setValue(updatedChargepoints);
        });
    }

    public LiveData<List<Chargepoint>> getChargepoints() {
        return chargepoints;
    }

    public void setFilteredChargepoints(List<Chargepoint> chargepoints) {
        filteredChargepoints.setValue(chargepoints);
    }

    public LiveData<List<Chargepoint>> getFilteredChargepoints() {
        return filteredChargepoints;
    }

    @Override
    protected void onCleared() {
        super.onCleared();
        firebaseHelper.removeChargepointsListener();
    }
}
