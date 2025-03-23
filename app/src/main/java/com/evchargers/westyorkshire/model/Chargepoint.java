package com.evchargers.westyorkshire.model;

import androidx.annotation.NonNull;
import com.google.firebase.firestore.Exclude;

public class Chargepoint {
    private String id;
    private String name;
    private String county;
    private String chargerType;
    private double latitude;
    private double longitude;
    private String status;

    public Chargepoint(String id, String name, String county, String chargerType,
                       double latitude, double longitude, String status) {
        this.id = id;
        this.name = name;
        this.county = county;
        this.chargerType = chargerType;
        this.latitude = latitude;
        this.longitude = longitude;
        this.status = status;
    }

    public Chargepoint() {}

    @NonNull
    public String getId() { return id != null ? id : ""; }
    @NonNull
    public String getName() { return name != null ? name : ""; }
    @NonNull
    public String getCounty() { return county != null ? county : ""; }
    @NonNull
    public String getChargerType() { return chargerType != null ? chargerType : ""; }
    public double getLatitude() { return latitude; }
    public double getLongitude() { return longitude; }
    @NonNull
    public String getStatus() { return status != null ? status : ""; }

    public void setId(@NonNull String id) { this.id = id; }
    public void setName(@NonNull String name) { this.name = name; }
    public void setCounty(@NonNull String county) { this.county = county; }
    public void setChargerType(@NonNull String chargerType) { this.chargerType = chargerType; }
    public void setLatitude(double latitude) { this.latitude = latitude; }
    public void setLongitude(double longitude) { this.longitude = longitude; }
    public void setStatus(@NonNull String status) { this.status = status; }

    @Override
    @NonNull
    public String toString() {
        return "Chargepoint{" +
                "id='" + getId() + '\'' +
                ", name='" + getName() + '\'' +
                ", county='" + getCounty() + '\'' +
                ", chargerType='" + getChargerType() + '\'' +
                ", latitude=" + getLatitude() +
                ", longitude=" + getLongitude() +
                ", status='" + getStatus() + '\'' +
                '}';
    }

    @Exclude
    public Chargepoint copy() {
        return new Chargepoint(
                this.id,
                this.name,
                this.county,
                this.chargerType,
                this.latitude,
                this.longitude,
                this.status
        );
    }
}