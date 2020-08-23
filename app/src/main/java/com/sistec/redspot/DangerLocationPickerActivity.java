package com.sistec.redspot;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.common.api.ResolvableApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.LocationSettingsRequest;
import com.google.android.gms.location.LocationSettingsResponse;
import com.google.android.gms.location.LocationSettingsStatusCodes;
import com.google.android.gms.location.places.AutocompleteFilter;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlaceAutocomplete;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.LatLngBounds;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.maps.android.SphericalUtil;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.FragmentActivity;

public class DangerLocationPickerActivity extends FragmentActivity implements OnMapReadyCallback {

    private static final int LOCATION_REQUEST = 999;
    private static final int REQUEST_FINE_LOCATION = 100;
    private static boolean FirstTimeFlag = true;
    private GoogleMap mMap;
    private LocationRequest mLocationRequest;
    private long UPDATE_INTERVAL = 10 * 1000;  /* 10 secs */
    private long FASTEST_INTERVAL = 2000; /* 2 sec */
    private int LOCATION_SETTINGS_REQUEST = 444;
    private LocationCallback mLocationCallback;
    private String Tag = "jsdgcjhsd";
    private LatLng MyLocation, CuruntLocation;
    private List<Address> addresses;
    private Geocoder geocoder;
    private TextView MyLocationTxt;
    private AutocompleteFilter filter;
    private LatLngBounds bounds;
    private ImageView MyLocationBtn;
    private Button Btn;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger_location_picker);
        // Obtain the SupportMapFragment and get notified when the map is ready to be used.
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        init();

        Btn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                if (MyLocation != null) {
                    Intent returnIntent = new Intent();
                    returnIntent.putExtra("lat", "" + MyLocation.latitude);
                    returnIntent.putExtra("lng", "" + MyLocation.longitude);
                    returnIntent.putExtra("sub_locality", "" + addresses.get(0).getSubLocality());
                    returnIntent.putExtra("locality", "" + addresses.get(0).getLocality());
                    setResult(Activity.RESULT_OK, returnIntent);
                    finish();
                    //Toast.makeText(DangerLocationPickerActivity.this, MyLocationTxt.getText().toString(), Toast.LENGTH_SHORT).show();
                }

            }
        });

        MyLocationTxt.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
                    Intent intent =
                            new PlaceAutocomplete.IntentBuilder(PlaceAutocomplete.MODE_FULLSCREEN)
                                    .setBoundsBias(bounds)
                                    .setFilter(filter)
                                    .build(DangerLocationPickerActivity.this);
                    startActivityForResult(intent, LOCATION_REQUEST);
                } catch (GooglePlayServicesRepairableException e) {
                    Toast.makeText(DangerLocationPickerActivity.this, "Repair", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                } catch (GooglePlayServicesNotAvailableException e) {
                    e.printStackTrace();
                }
            }
        });

        MyLocationBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (CuruntLocation != null) {

                    try {
                        addresses = geocoder.getFromLocation(CuruntLocation.latitude, CuruntLocation.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    if (addresses != null) {
                        if (addresses.size() > 0) {
                            final String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                            Log.i("salnflsanflaf", "onClick: " +
                                    addresses.get(0).getAddressLine(1) + "  " +
                                    addresses.get(0).getAddressLine(2) + "  " +
                                    addresses.get(0).getAddressLine(0) + "  " +
                                    addresses.get(0).getSubLocality());

                            MyLocationTxt.setText(address);
                            mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(CuruntLocation, 17f));

                        }
                    }


                }
            }
        });

    }


    private void init() {
        MyLocationTxt = findViewById(R.id.picup_location);
        geocoder = new Geocoder(DangerLocationPickerActivity.this, Locale.getDefault());
        MyLocationBtn = findViewById(R.id.mylocation);
        Btn = findViewById(R.id.btn);

        LatLng Center = new LatLng(23.259933, 77.412613);

        LatLng east = SphericalUtil.computeOffset(Center, 30000, 0);
        LatLng west = SphericalUtil.computeOffset(Center, 30000, 90);

        LatLng north = SphericalUtil.computeOffset(Center, 30000, 270);
        LatLng south = SphericalUtil.computeOffset(Center, 30000, 180);

        bounds = LatLngBounds.builder()
                .include(east)
                .include(west)
                .include(north)
                .include(south)
                .build();

        filter = new AutocompleteFilter.Builder()
                .setTypeFilter(AutocompleteFilter.TYPE_FILTER_ADDRESS)
                .setTypeFilter(3)
                .build();


    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == LOCATION_REQUEST) {
            if (resultCode == RESULT_OK) {
                Place place = PlaceAutocomplete.getPlace(DangerLocationPickerActivity.this, data);
                Log.i("salnflsanflaf", "Place:" + place.getName());
                MyLocationTxt.setText(place.getAddress());
                MyLocation = place.getLatLng();
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(MyLocation.latitude, MyLocation.longitude), 17f));
            }
        }
    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        Log.i("adsnkcadsn", "onMapReady: ");
        mMap = googleMap;
        mMap.getUiSettings().setMyLocationButtonEnabled(false);

        mMap.setOnCameraIdleListener(new GoogleMap.OnCameraIdleListener() {
            @Override
            public void onCameraIdle() {

                if (MyLocation != null) {
                    //get latlng at the center by calling
                    LatLng midLatLng = mMap.getCameraPosition().target;

                    Location startLocation = new Location("startLocation");
                    startLocation.setLatitude(MyLocation.latitude);
                    startLocation.setLongitude(MyLocation.longitude);


                    Location NewLocation = new Location("NewLocation");
                    NewLocation.setLatitude(midLatLng.latitude);
                    NewLocation.setLongitude(midLatLng.longitude);

                    double dis = startLocation.distanceTo(NewLocation);
                    if (dis > 10) {
                        MyLocation = midLatLng;
                        Log.i("knvcosnd", "df " + MyLocation.toString());
                        try {
                            addresses = geocoder.getFromLocation(midLatLng.latitude, midLatLng.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        if (addresses.size() > 0) {
                            final String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()

                            Log.i("salnflsanflaf", "onClick: " +
                                    addresses.get(0).getAddressLine(1) + "  " +
                                    addresses.get(0).getAddressLine(2) + "  " +
                                    addresses.get(0).getAddressLine(0) + "  " +
                                    addresses.get(0).getSubLocality());

                            MyLocationTxt.setText(address + "");

                        }


                    }
                }


            }
        });
        if (checkPermissions()) {
            startLocationUpdates();
            getLastLocation();
        }


    }

    public void onLocationChanged(Location location) throws IOException {

        CuruntLocation = new LatLng(location.getLatitude(), location.getLongitude());
        MyLocation = new LatLng(location.getLatitude(), location.getLongitude());

        if(FirstTimeFlag){
            Log.i("fhbbbbbbjd", "onLocationChanged: else 1");
            if (MyLocation != null) {
                Log.i("fhbbbbbbjd", "onLocationChanged: else 2");

                MyLocation = new LatLng(location.getLatitude(), location.getLongitude());
                mMap.animateCamera(CameraUpdateFactory.newLatLngZoom(MyLocation, 17f));
                addresses = geocoder.getFromLocation(MyLocation.latitude, MyLocation.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5

                if (addresses != null) {
                    Log.i("fhbbbbbbjd", "onLocationChanged: else 3");

                    if (addresses.size() > 0) {
                        Log.i("fhbbbbbbjd", "onLocationChanged: else 4");

                        addresses = geocoder.getFromLocation(MyLocation.latitude, MyLocation.longitude, 1); // Here 1 represent max location result to returned, by documents it recommended 1 to 5
                        final String address = addresses.get(0).getAddressLine(0); // If any additional address line present than only, check with max available address lines by getMaxAddressLineIndex()
                        FirstTimeFlag = false;
                        MyLocationTxt.setText(address);
                    }
                }
            }
        }
        else {
            Log.i("fhbbbbbbjd", "onLocationChanged: else");
        }



    }


    private boolean checkPermissions() {
        if (ContextCompat.checkSelfPermission(DangerLocationPickerActivity.this,
                android.Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            return true;
        } else {
            requestPermissions();
            return false;
        }
    }

    public void getLastLocation() {
        // Get last known recent location using new Google Play Services SDK (v11+)
        FusedLocationProviderClient locationClient = LocationServices.getFusedLocationProviderClient(DangerLocationPickerActivity.this);

        if (ActivityCompat.checkSelfPermission(DangerLocationPickerActivity.this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(DangerLocationPickerActivity.this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }
        locationClient.getLastLocation()
                .addOnSuccessListener(new OnSuccessListener<Location>() {
                    @Override
                    public void onSuccess(Location location) {
                        // GPS location can be null if GPS is switched off
                        if (location != null) {
                            try {
                                onLocationChanged(location);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Log.d("MapDemoActivity", "Error trying to get last GPS location");
                        e.printStackTrace();
                    }
                });
    }

    private void requestPermissions() {
        ActivityCompat.requestPermissions(DangerLocationPickerActivity.this,
                new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                REQUEST_FINE_LOCATION);
    }

    public void onRequestPermissionsResult(int requestCode,
                                           String[] permissions,
                                           int[] grantResults) {
        if (requestCode == REQUEST_FINE_LOCATION) {
            if (grantResults.length == 1
                    && grantResults[0] == PackageManager.PERMISSION_GRANTED) {

                startLocationUpdates();
                getLastLocation();
            } else {

            }
        }
    }

    @SuppressLint("MissingPermission")
    protected void startLocationUpdates() {

        Log.i(Tag, "startLocationUpdates");

        // Create the location request to start receiving updates
        mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(UPDATE_INTERVAL);
        mLocationRequest.setFastestInterval(FASTEST_INTERVAL);

        // Create LocationSettingsRequest object using location request
        LocationSettingsRequest.Builder builder = new LocationSettingsRequest.Builder();
        builder.addLocationRequest(mLocationRequest);
        LocationSettingsRequest locationSettingsRequest = builder.build();

        // Check whether location settings are satisfied
        // https://developers.google.com/android/reference/com/google/android/gms/location/SettingsClient
        Task<LocationSettingsResponse> result = LocationServices.getSettingsClient(DangerLocationPickerActivity.this)
                .checkLocationSettings(locationSettingsRequest);

        result.addOnCompleteListener(new OnCompleteListener<LocationSettingsResponse>() {
            @Override
            public void onComplete(@NonNull Task<LocationSettingsResponse> task) {
                try {
                    LocationSettingsResponse response =
                            task.getResult(ApiException.class);
                } catch (ApiException ex) {
                    switch (ex.getStatusCode()) {
                        case LocationSettingsStatusCodes.RESOLUTION_REQUIRED:
                            try {
                                ResolvableApiException resolvableApiException =
                                        (ResolvableApiException) ex;
                                resolvableApiException
                                        .startResolutionForResult(DangerLocationPickerActivity.this,
                                                LOCATION_SETTINGS_REQUEST);
                            } catch (IntentSender.SendIntentException e) {

                            }
                            break;
                        case LocationSettingsStatusCodes.SETTINGS_CHANGE_UNAVAILABLE:

                            break;
                    }
                }
            }
        });

        // new Google API SDK v11 uses getFusedLocationProviderClient(this)

        mLocationCallback = new LocationCallback() {
            @Override
            public void onLocationResult(LocationResult locationResult) {
                // do work here

                try {
                    onLocationChanged(locationResult.getLastLocation());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        };

        LocationServices.getFusedLocationProviderClient(DangerLocationPickerActivity.this).requestLocationUpdates(mLocationRequest, mLocationCallback,
                Looper.myLooper());

    }

    @Override
    public void onPause() {
        super.onPause();
        FirstTimeFlag = true;
        if (mLocationCallback != null)
            LocationServices.getFusedLocationProviderClient(this).removeLocationUpdates(mLocationCallback);
    }

}
