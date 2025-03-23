package com.evchargers.westyorkshire.utils;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import androidx.annotation.NonNull;
import com.evchargers.westyorkshire.model.Chargepoint;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.firestore.FirebaseFirestore;
import com.google.firebase.firestore.DocumentSnapshot;
import com.google.firebase.firestore.ListenerRegistration;
import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import com.google.firebase.firestore.DocumentReference;
import com.google.firebase.firestore.WriteBatch;
import com.google.firebase.firestore.SetOptions;


public class FirebaseHelper {
    private static final String TAG = "FirebaseHelper";
    private final FirebaseFirestore db;
    private final FirebaseAuth mAuth;
    private static final String COLLECTION_CHARGEPOINTS = "chargepoints";
    private static final String COLLECTION_USERS = "users";
    private ListenerRegistration chargepointsListener;

    public FirebaseHelper() {
        db = FirebaseFirestore.getInstance();
        mAuth = FirebaseAuth.getInstance();
    }

    public void createUserWithRole(String email, String password, boolean isAdmin, OnCompleteListener listener) {
        mAuth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener(authResult -> {
                    String userId = authResult.getUser().getUid();
                    Map<String, Object> userData = new HashMap<>();
                    userData.put("isAdmin", isAdmin);

                    db.collection("users").document(userId)
                            .set(userData)
                            .addOnSuccessListener(aVoid -> listener.onComplete(true))
                            .addOnFailureListener(e -> listener.onComplete(false));
                })
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    public void addChargepointsListener(OnChargepointsUpdateListener listener) {
        chargepointsListener = db.collection(COLLECTION_CHARGEPOINTS)
                .addSnapshotListener((snapshots, e) -> {
                    if (e != null) {
                        Log.e(TAG, "Listen failed.", e);
                        return;
                    }

                    List<Chargepoint> chargepoints = new ArrayList<>();
                    if (snapshots != null) {
                        for (DocumentSnapshot doc : snapshots) {
                            Chargepoint chargepoint = doc.toObject(Chargepoint.class);
                            if (chargepoint != null) {
                                chargepoints.add(chargepoint);
                            }
                        }
                    }
                    listener.onUpdate(chargepoints);
                });
    }

    public void removeChargepointsListener() {
        if (chargepointsListener != null) {
            chargepointsListener.remove();
        }
    }

    public void importChargepointsToFirestore(List<Chargepoint> chargepoints, OnCompleteListener listener) {
        WriteBatch batch = db.batch();

        for (Chargepoint chargepoint : chargepoints) {
            DocumentReference docRef = db.collection("chargepoints").document();
            batch.set(docRef, chargepoint);
        }

        batch.commit()
                .addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> listener.onComplete(false));
    }

    public void importChargepoints(@NonNull Context context, @NonNull Uri fileUri, @NonNull OnImportCompleteListener listener) {
        try {
            InputStream inputStream = context.getContentResolver().openInputStream(fileUri);
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            List<Map<String, Object>> chargepoints = new ArrayList<>();
            String line;

            // Skip header
            reader.readLine();

            while ((line = reader.readLine()) != null) {
                String[] data = line.split(",");
                if (data.length >= 8) {
                    Map<String, Object> chargepoint = new HashMap<>();
                    chargepoint.put("id", data[0].trim());
                    chargepoint.put("latitude", Double.parseDouble(data[1].trim()));
                    chargepoint.put("longitude", Double.parseDouble(data[2].trim()));
                    chargepoint.put("name", data[3].trim());
                    chargepoint.put("county", data[4].trim());
                    chargepoint.put("status", data[6].trim());
                    chargepoint.put("chargerType", data[8].trim());
                    chargepoints.add(chargepoint);
                }
            }

            db.runBatch(batch -> {
                for (Map<String, Object> chargepoint : chargepoints) {
                    String docId = (String) chargepoint.get("id");
                    batch.set(db.collection(COLLECTION_CHARGEPOINTS).document(docId), chargepoint);
                }
            }).addOnSuccessListener(aVoid -> {
                Log.d(TAG, "Batch write successful");
                listener.onImportComplete(true);
            }).addOnFailureListener(e -> {
                Log.e(TAG, "Batch write failed", e);
                listener.onImportComplete(false);
            });

        } catch (Exception e) {
            Log.e(TAG, "Import failed", e);
            listener.onImportComplete(false);
        }
    }

    public void loadChargepoints(@NonNull OnChargepointsLoadedListener listener) {
        db.collection(COLLECTION_CHARGEPOINTS)
                .get()
                .addOnSuccessListener(querySnapshot -> {
                    List<Chargepoint> chargepoints = new ArrayList<>();
                    for (DocumentSnapshot doc : querySnapshot.getDocuments()) {
                        Chargepoint chargepoint = doc.toObject(Chargepoint.class);
                        if (chargepoint != null) {
                            chargepoints.add(chargepoint);
                        }
                    }
                    listener.onChargepointsLoaded(chargepoints);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error loading chargepoints", e);
                    listener.onChargepointsLoaded(new ArrayList<>());
                });
    }

    public void updateChargepoint(@NonNull Chargepoint chargepoint, @NonNull OnCompleteListener listener) {
        String documentId = chargepoint.getId();
        Log.d(TAG, "🔄 Starting update for ID: " + documentId);

        if (documentId == null || documentId.isEmpty()) {
            Log.e(TAG, "❌ Invalid document ID");
            listener.onComplete(false);
            return;
        }

        Map<String, Object> updates = new HashMap<>();
        updates.put("id", documentId); // Include ID in the update
        updates.put("name", chargepoint.getName());
        updates.put("county", "West Yorkshire");
        updates.put("chargerType", chargepoint.getChargerType());
        updates.put("latitude", chargepoint.getLatitude());
        updates.put("longitude", chargepoint.getLongitude());
        updates.put("status", chargepoint.getStatus());

        DocumentReference docRef = db.collection(COLLECTION_CHARGEPOINTS).document(documentId);

        docRef.set(updates, SetOptions.merge())
                .addOnSuccessListener(aVoid -> {
                    Log.d(TAG, "✅ Document updated successfully");
                    listener.onComplete(true);
                })
                .addOnFailureListener(e -> {
                    Log.e(TAG, "❌ Update failed: " + e.getMessage());
                    listener.onComplete(false);
                });
    }





    public void deleteChargepoint(@NonNull String chargepointId, @NonNull OnCompleteListener listener) {
        db.collection(COLLECTION_CHARGEPOINTS)
                .document(chargepointId)
                .delete()
                .addOnSuccessListener(aVoid -> listener.onComplete(true))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error deleting chargepoint", e);
                    listener.onComplete(false);
                });
    }

    public void isCurrentUserAdmin(@NonNull OnAdminCheckListener listener) {
        if (mAuth.getCurrentUser() == null) {
            listener.onResult(false);
            return;
        }

        db.collection(COLLECTION_USERS)
                .document(mAuth.getCurrentUser().getUid())
                .get()
                .addOnSuccessListener(document ->
                        listener.onResult(document.exists() && Boolean.TRUE.equals(document.getBoolean("isAdmin"))))
                .addOnFailureListener(e -> {
                    Log.e(TAG, "Error checking admin status", e);
                    listener.onResult(false);
                });
    }

    public interface OnChargepointsUpdateListener {
        void onUpdate(List<Chargepoint> chargepoints);
    }

    public interface OnCompleteListener {
        void onComplete(boolean success);
    }

    public interface OnImportCompleteListener {
        void onImportComplete(boolean success);
        void onError(String errorMessage);
    }


    public interface OnAdminCheckListener {
        void onResult(boolean isAdmin);
    }

    public interface OnChargepointsLoadedListener {
        void onChargepointsLoaded(List<Chargepoint> chargepoints);
    }
}
