package com.sistec.redspot;

import android.Manifest;
import android.app.Dialog;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.GoogleApiAvailability;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdate;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.Circle;
import com.google.android.gms.maps.model.CircleOptions;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.Query;
import com.google.firebase.database.ValueEventListener;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback {

    private static final int MY_PERMISSIONS_REQUEST_LOCATION = 101;
    private static final int MAP_UPDATE_TIME = 60000; //1 Min
    private static final long MAP_UPDATE_DISTANCE = 100; //50 meters
    private static final float ZOOM_LEVEL = 14.0f;
    private float MAX_ZONE_TO_COVER = 1000; //1000 meters
    private boolean IS_LOCATION_PERMISSION_ENABLED = false;

    SharedPreferences mPref;
    TextView tvDangerCount; //tvProbabilityCount;
    LinearLayout llNoDanger;
    FloatingActionButton fabAddDengerLocation;
    //TextView tvPlaceDetails;


    GoogleMap mGoogleMap;
    GoogleApiClient mGoogleApiClient;
    LocationRequest mLocationRequest;
    FusedLocationProviderClient mFusedLocationProviderClient;
    LocationCallback locationCallback;
    Marker marker;
    Circle circle;
    MarkerOptions markerOptions;
    Location userCurrentLocation = null;
    boolean isTrackingStarted = false;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference addressRef;
    Query fetchAddQuery;

    ArrayList<AddressStructure> addressStructuresArrayList = new ArrayList<AddressStructure>();
    AddressStructureAdapter adapter;
    ListView listView;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (googleServicesAvailable()) {
            setContentView(R.layout.activity_main);
            //tvPlaceDetails = findViewById(R.id.place_details);
            mFusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
            mAuth = FirebaseAuth.getInstance();
            tvDangerCount = findViewById(R.id.denger_count);
            llNoDanger = findViewById(R.id.no_danger_icon);
            fabAddDengerLocation = findViewById(R.id.fab_add_danger_loc);
            //tvProbabilityCount = findViewById(R.id.probability_count);
            fabAddDengerLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    startActivity(new Intent(MainActivity.this, DangerLocationActivity.class));
                }
            });
            database = FirebaseDatabase.getInstance();
            addressRef = database.getReference().child("locations");
            adapter = new AddressStructureAdapter(this, addressStructuresArrayList);
            listView = findViewById(R.id.place_details);
            listView.setAdapter(adapter);
            checkLocationPermission();
            if (!IS_LOCATION_PERMISSION_ENABLED) {
                getLocationPermission();
            }
            mPref = getSharedPreferences("service", Context.MODE_PRIVATE);
            locationCallback = new LocationCallback() {
                @Override
                public void onLocationResult(LocationResult locationResult) {
                    if (locationResult == null) {
                        Toast.makeText(MainActivity.this, "Can't get location", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    userCurrentLocation = locationResult.getLastLocation();
                    LatLng ll = new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude());
                    Geocoder gc = new Geocoder(MainActivity.this);
                    try {
                        List<Address> list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                        setMarker(list.get(0), new LatLng(ll.latitude, ll.longitude));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, ZOOM_LEVEL);
                    mGoogleMap.animateCamera(update);
                }

                ;
            };
            initMap();
        } else {
            // No Google Maps Layout
            setContentView(R.layout.activity_main_error);
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        getMenuInflater().inflate(R.menu.options_menu, menu);
        return true;
    }

    @Override
    public boolean onPrepareOptionsMenu(Menu menu) {
        MenuItem item = menu.findItem(R.id.location_service);
        if (mPref.getString("service", "").equals(""))
            item.setTitle(getResources().getString(R.string.start_tracking));
        else
            item.setTitle(getResources().getString(R.string.stop_tracking));
        return super.onPrepareOptionsMenu(menu);
    }


    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        switch (id) {
            case R.id.logout:
                mAuth.signOut();
                NotificationHelper.hideOldNotification(this);
                startActivity(new Intent(MainActivity.this, LoginActivity.class));
                MainActivity.this.finish();
                break;
            case R.id.location_service:
                SharedPreferences.Editor mEdit = mPref.edit();
                if (mPref.getString("service", "").equals("")) {
                    Toast.makeText(this, "Background service started", Toast.LENGTH_SHORT).show();
                    mEdit.putString("service", "service").apply();
                    Intent intent = new Intent(getApplicationContext(), MyMapServices.class);
                    startService(intent);
                } else {
                    Toast.makeText(this, "Background service stopped", Toast.LENGTH_SHORT).show();
                    mEdit.putString("service", "").apply();
                    Intent intent = new Intent(getApplicationContext(), MyMapServices.class);
                    //startService(intent);
                    stopService(intent);

//                    item.setIcon(getResources().getDrawable(R.drawable.ic_start_tracking_24dp));
                }
                break;
            case R.id.about_us:
                startActivity(new Intent(MainActivity.this, AboutActivity.class));
                break;
            case R.id.long_distance:
                MAX_ZONE_TO_COVER = 10000;
                item.setVisible(false);
                break;
        }
        return super.onOptionsItemSelected(item);
    }


    private void fetchDangerAddress(final Address address) {
        fetchAddQuery = addressRef.orderByChild("sub_locality").startAt(address.getSubLocality().toLowerCase());
        fetchAddQuery.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()) {
                    int dgCount = 0;
                    addressStructuresArrayList.clear();
                    for (DataSnapshot result : dataSnapshot.getChildren()) {
                        AddressStructure tempHolder = result.getValue(AddressStructure.class);
                        if (tempHolder != null) {
                            tempHolder.setCurrLocation(userCurrentLocation);
                            Location loc = new Location("");
                            loc.setLatitude(tempHolder.getLatitude());
                            loc.setLongitude(tempHolder.getLongitude());
                            float dist = loc.distanceTo(tempHolder.getCurrLocation());
                            if (dist < MAX_ZONE_TO_COVER) {
                                addressStructuresArrayList.add(tempHolder);
                                adapter.notifyDataSetChanged();
                                dgCount++;
                            }
                        }

                    }
                    tvDangerCount.setText("" + dgCount);
                    if (dgCount == 0) {
                        addressStructuresArrayList.clear();
                        adapter.notifyDataSetChanged();
                        tvDangerCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.safe_dark));
                        setSafeCircle();
                        NotificationHelper.hideOldNotification(MainActivity.this);
                        llNoDanger.setVisibility(View.VISIBLE);
                    } else {
                        NotificationHelper.showNewNotification(MainActivity.this,
                                "Your Location: " + address.getSubLocality(),
                                "You are in accident prone area.",
                                "Accident Counts : " + dgCount
                        );
                        tvDangerCount.setTextColor(ContextCompat.getColor(getApplicationContext(), R.color.danger_highest));
                        setDangerCircle();
                        llNoDanger.setVisibility(View.GONE);
                    }
                } else {
                    Toast.makeText(MainActivity.this, "Error in data fetching. Retry", Toast.LENGTH_SHORT).show();
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
                Toast.makeText(MainActivity.this, databaseError.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void setDangerCircle() {
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude()))
                .radius(MAX_ZONE_TO_COVER)
                .strokeWidth(10)
                .strokeColor(ContextCompat.getColor(this, R.color.danger_highest))
                .fillColor(ContextCompat.getColor(this, R.color.danger_four));

        // Get back the mutable Polyline
        if (circle != null)
            circle.remove();
        circle = mGoogleMap.addCircle(circleOptions);
    }

    private void setSafeCircle() {
        CircleOptions circleOptions = new CircleOptions()
                .center(new LatLng(userCurrentLocation.getLatitude(), userCurrentLocation.getLongitude()))
                .radius(MAX_ZONE_TO_COVER)
                .strokeWidth(10)
                .strokeColor(ContextCompat.getColor(this, R.color.safe_dark))
                .fillColor(ContextCompat.getColor(this, R.color.safe_one));

// Get back the mutable Polyline
        if (circle != null)
            circle.remove();
        circle = mGoogleMap.addCircle(circleOptions);
    }

    private void setMarker(Address address, LatLng ll) {
        if (address.getSubLocality() == null) {
            address.setSubLocality(address.getLocality());
        }
        markerOptions = new MarkerOptions()
                .title(address.getSubLocality())
                .draggable(true)
                .snippet("You are here")
                .position(ll);
        if (marker != null) {
            marker.remove();
        }
        fetchDangerAddress(address);
        marker = mGoogleMap.addMarker(markerOptions);
    }

    private void updateMarker(Address address) {
        if (address.getSubLocality() == null)
            address.setSubLocality(address.getLocality());
        userCurrentLocation.setLatitude(address.getLatitude());
        userCurrentLocation.setLongitude(address.getLongitude());
        marker.setSnippet("You are not here");
        marker.setTitle(address.getSubLocality());
        fetchDangerAddress(address);
        marker.showInfoWindow();
    }

    public boolean googleServicesAvailable() {
        GoogleApiAvailability api = GoogleApiAvailability.getInstance();
        int isAvailable = api.isGooglePlayServicesAvailable(this);
        if (isAvailable == ConnectionResult.SUCCESS) {
            return true;
        } else if (api.isUserResolvableError(isAvailable)) {
            Dialog dialog = api.getErrorDialog(this, isAvailable, 0);
            dialog.show();
        } else {
            Toast.makeText(this, "Cant connect to play services", Toast.LENGTH_LONG).show();
        }
        return false;
    }

    private void initMap() {
        MapFragment mapFragment = (MapFragment) getFragmentManager().findFragmentById(R.id.mapFragment);
        mapFragment.getMapAsync(this);

    }

    @Override
    public void onMapReady(GoogleMap googleMap) {
        mGoogleMap = googleMap;
        if (mGoogleMap != null) {

            mGoogleMap.setOnMarkerDragListener(new GoogleMap.OnMarkerDragListener() {
                @Override
                public void onMarkerDragStart(Marker marker) {

                }

                @Override
                public void onMarkerDrag(Marker marker) {

                }

                @Override
                public void onMarkerDragEnd(Marker marker) {
                    Geocoder gc = new Geocoder(MainActivity.this);
                    LatLng ll = marker.getPosition();
                    List<Address> list = null;
                    try {
                        list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    Address address = list.get(0);
                    updateMarker(address);
                }
            });

            mGoogleMap.setInfoWindowAdapter(new GoogleMap.InfoWindowAdapter() {
                @Override
                public View getInfoWindow(Marker marker) {
                    return null;
                }

                @Override
                public View getInfoContents(Marker marker) {
                    View v = getLayoutInflater().inflate(R.layout.marker_discription, null);

                    TextView tvLocality = v.findViewById(R.id.tv_locality);
                    TextView tvLatitude = v.findViewById(R.id.tv_lat);
                    TextView tvLongitude = v.findViewById(R.id.tv_lng);
                    TextView tvSnippet = v.findViewById(R.id.tv_snippet);

                    LatLng ll = marker.getPosition();

                    tvLocality.setText(marker.getTitle());
                    tvLatitude.setText("Latitude: " + ll.latitude);
                    tvLongitude.setText("Longitude: " + ll.longitude);
                    tvSnippet.setText(marker.getSnippet());

                    return v;
                }
            });

            mGoogleMap.setOnMapLongClickListener(new GoogleMap.OnMapLongClickListener() {
                @Override
                public void onMapLongClick(LatLng ll) {
                    Geocoder gc = new Geocoder(MainActivity.this);
                    try {
                        List<Address> list = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                        userCurrentLocation.setLatitude(ll.latitude);
                        userCurrentLocation.setLongitude(ll.longitude);
                        setMarker(list.get(0), new LatLng(ll.latitude, ll.longitude));
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, ZOOM_LEVEL);
                    mGoogleMap.animateCamera(update);
                }
            });
        }
        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addApi(LocationServices.API)
                .addConnectionCallbacks(new GoogleApiClient.ConnectionCallbacks() {
                    @Override
                    public void onConnected(@Nullable Bundle bundle) {
                        mLocationRequest = LocationRequest.create();
                        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
                        mLocationRequest.setInterval(MAP_UPDATE_TIME);
                        mLocationRequest.setSmallestDisplacement(MAP_UPDATE_DISTANCE);
                        try {
                            mFusedLocationProviderClient.requestLocationUpdates(mLocationRequest, locationCallback, null);
                        } catch (SecurityException ex) {
                            ex.printStackTrace();
                        }
                    }

                    @Override
                    public void onConnectionSuspended(int i) {

                    }
                })
                .addOnConnectionFailedListener(new GoogleApiClient.OnConnectionFailedListener() {
                    @Override
                    public void onConnectionFailed(@NonNull ConnectionResult connectionResult) {

                    }
                })
                .build();
        mGoogleApiClient.connect();
        //goToLocationZoom(23.3036179,77.3375503, 15.0f);
        /*
        if (checkLocationPermission())
            mGoogleMap.setMyLocationEnabled(true);
        else
            getLocationPermission();
         */

    }

    private void checkLocationPermission() {
        if (ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && ActivityCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            Toast.makeText(this, "No Permission", Toast.LENGTH_SHORT).show();

            IS_LOCATION_PERMISSION_ENABLED = false;   //if permission is not granted
        } else
            IS_LOCATION_PERMISSION_ENABLED = true;    //if permission is already granted
    }

    private void getLocationPermission() {
        ActivityCompat.requestPermissions(this,
                new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION},
                MY_PERMISSIONS_REQUEST_LOCATION);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        switch (requestCode) {
            case MY_PERMISSIONS_REQUEST_LOCATION: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    IS_LOCATION_PERMISSION_ENABLED = true;
                    initMap();
                } else {
                    // permission denied, boo! Disable the
                    // functionality that depends on this permission.
                    IS_LOCATION_PERMISSION_ENABLED = false;
                    MainActivity.this.finish();
                }
            }
        }
    }

    private void goToLocationZoom(double lat, double lng, float zoom) {
        LatLng ll = new LatLng(lat, lng);
        CameraUpdate update = CameraUpdateFactory.newLatLngZoom(ll, zoom);
        mGoogleMap.moveCamera(update);
    }

    @Override
    protected void onResume() {
        super.onResume();
        registerReceiver(broadcastReceiver, new IntentFilter(MyMapServices.str_receiver));
    }

    @Override
    protected void onPause() {
        super.onPause();
        unregisterReceiver(broadcastReceiver);
    }

    private BroadcastReceiver broadcastReceiver = new BroadcastReceiver() {
        @Override
        public void onReceive(Context context, Intent intent) {

            LatLng ll = new LatLng(Double.valueOf(intent.getStringExtra("latitude")), Double.valueOf(intent.getStringExtra("longitude")));
            Geocoder gc = new Geocoder(MainActivity.this);
            List<Address> addresses = null;
            try {
                addresses = gc.getFromLocation(ll.latitude, ll.longitude, 1);
                Log.v("Location", addresses.get(0).getAdminArea());
            } catch (IOException e) {
                e.printStackTrace();
            }
            //tv_area.setText(addresses.get(0).getAdminArea());

        }
    };
}
