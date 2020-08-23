package com.sistec.redspot;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

public class LocationMarkedBy {
    private int marked_count;
    private String submitted_by;
    private String marked_by;
    private double latitude;
    private double longitude;

    public LocationMarkedBy(){} //Default constructor for DatabaseSnapshot.getValue(AddressStructure.class);

    public LocationMarkedBy(int marked_count, String submitted_by,
                            double latitude, double longitude){
        this.marked_count = marked_count;
        this.submitted_by = submitted_by;
        this.latitude = latitude;
        this.longitude = longitude;
    }

    public void setSubmitted_by(String submitted_by) {this.submitted_by = submitted_by;}
    public void setMarked_count(int marked_count) {this.marked_count = marked_count;}
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    public void setLongitude(double longitude){
        this.longitude = longitude;
    }

    public String getSubmitted_by() {return submitted_by;}
    public int getMarked_count() {return marked_count;}
    public double getLatitude(){return latitude;}
    public double getLongitude(){return longitude;}

    @Exclude
    public HashMap<String, Object> mapLocationMarkedData(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("latitude",latitude);
        result.put("longitude",longitude);
        result.put("marked_count",marked_count);
        result.put("submitted_by",submitted_by);
        return result;
    }
}
