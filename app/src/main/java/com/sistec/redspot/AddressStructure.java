package com.sistec.redspot;

import android.location.Location;

import com.google.firebase.database.Exclude;

import java.util.HashMap;

public class AddressStructure {
    private int time_interval;
    private int day_of_week;
    private int day_of_month;
    private int month;
    private int year;
    private int marked_count;
    private String submitted_by;
    private double latitude;
    private double longitude;
    private Location currLocation;
    private String locality;
    private String sub_locality;
    private String vehicle_type;

    public AddressStructure(){} //Default constructor for DatabaseSnapshot.getValue(AddressStructure.class);

    public AddressStructure(int time_interval,
                            int day_of_week,
                            int day_of_month,
                            int month, int year, int marked_count, String submitted_by,
                            double latitude, double longitude,
                            String locality, String sub_locality,
                            String vehicle_type){
        this.time_interval = time_interval;
        this.day_of_week = day_of_week;
        this.day_of_month = day_of_month;
        this.month = month;
        this.year = year;
        this.marked_count = marked_count;
        this.submitted_by = submitted_by;
        this.latitude = latitude;
        this.longitude = longitude;
        this.locality = locality;
        this.sub_locality = sub_locality;
        this.vehicle_type = vehicle_type;

    }
    public void setCurrLocation(Location ll){currLocation = ll;}

    public void setDay_of_week(int day_of_week){
        this.day_of_week = day_of_week;
    }
    public void setDay_of_month(int day_of_month){
        this.day_of_month = day_of_month;
    }
    public void setMonth(int month){
        this.month = month;
    }
    public void setYear(int year){
        this.year = year;
    }
    public void setSubmitted_by(String submitted_by) {this.submitted_by = submitted_by;}
    public void setMarked_count(int marked_count) {this.marked_count = marked_count;}
    public void setLatitude(double latitude){
        this.latitude = latitude;
    }
    public void setLongitude(double longitude){
        this.longitude = longitude;
}
    public void setLocality(String locality){
        this.locality = locality;
    }
    public void setSub_locality(String sub_locality){
        this.sub_locality = sub_locality;
    }
    public void setVehicle_type(String vehicle_type){
        this.vehicle_type = vehicle_type;
    }
    public void setTime_interval(int time_interval){
        this.time_interval = time_interval;
    }

    public Location getCurrLocation(){return currLocation;}
    public int getTime_interval(){return time_interval;}
    public int getDay_of_week(){return day_of_week;}
    public int getDay_of_month(){return day_of_month;}
    public int getMonth(){return month;}
    public int getYear(){return year;}
    public String getSubmitted_by() {return submitted_by;}
    public int getMarked_count() {return marked_count;}
    public double getLatitude(){return latitude;}
    public double getLongitude(){return longitude;}
    public String getLocality(){return locality;}
    public String getSub_locality(){return sub_locality;}
    public String getVehicle_type(){return vehicle_type;}

    @Exclude
    public HashMap<String, Object> mapAddressData(){
        HashMap<String, Object> result = new HashMap<>();
        result.put("day_of_month",day_of_month);
        result.put("day_of_week",day_of_week);
        result.put("latitude",latitude);
        result.put("locality",locality);
        result.put("longitude",longitude);
        result.put("month",month);
        result.put("marked_count",marked_count);
        result.put("submitted_by",submitted_by);
        result.put("sub_locality",sub_locality);
        result.put("time_interval",time_interval);
        result.put("vehicle_type",vehicle_type);
        result.put("year",year);
        return result;
    }

}
