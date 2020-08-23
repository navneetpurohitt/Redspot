package com.sistec.redspot;

import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.database.annotations.Nullable;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.TimeZone;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class DangerLocationActivity extends AppCompatActivity {

    private static final int PICKUP_LOCATION_REQUEST = 112;
    TextView tvLat, tvLng, tvLocality, tvSubLocality, tvDate, tvTime, tvSubmittedBy;
    ImageView ivDatePicker, ivTimePicker;
    Spinner sVehiclePicker;
    Button btnLocationPicker, btnSubmitData;
    AddressStructure addressStructure;
    LinearLayout llDangerActivityBaseLayout;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_danger_location);
        llDangerActivityBaseLayout = findViewById(R.id.danger_activity_base_layout);
        tvLat = findViewById(R.id.tv_add_lat);
        tvLng = findViewById(R.id.tv_add_lng);
        tvLocality = findViewById(R.id.tv_add_locality);
        tvSubLocality = findViewById(R.id.tv_add_sub_locality);
        tvSubmittedBy = findViewById(R.id.tv_add_submitted_by);
        getCurrentUserName();
        tvDate = findViewById(R.id.tv_add_date);
        tvTime = findViewById(R.id.tv_add_time);
        ivDatePicker = findViewById(R.id.pick_date);
        ivTimePicker = findViewById(R.id.pick_time);
        btnLocationPicker = findViewById(R.id.btn_pickup_location);
        btnSubmitData = findViewById(R.id.btn_submit_data);
        sVehiclePicker = findViewById(R.id.spin_vehicle_type);
        ArrayAdapter<CharSequence> adapter = ArrayAdapter.createFromResource(this,
                R.array.vehicle_type, android.R.layout.simple_spinner_item);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        sVehiclePicker.setAdapter(adapter);
        sVehiclePicker.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                switch (position) {
                    case 1:
                        addressStructure.setVehicle_type("car");
                        break;
                    case 2:
                        addressStructure.setVehicle_type("bike");
                        break;
                    case 3:
                        addressStructure.setVehicle_type("bus");
                        break;
                    case 4:
                        addressStructure.setVehicle_type("truck");
                        break;
                    case 5:
                        addressStructure.setVehicle_type("auto");
                        break;
                    case 6:
                        addressStructure.setVehicle_type("other(light)");
                        break;
                    case 7:
                        addressStructure.setVehicle_type("other(heavy)");
                        break;
                        default: addressStructure.setVehicle_type("not set");
                }
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                Toast.makeText(DangerLocationActivity.this, "Please Select", Toast.LENGTH_SHORT).show();
            }
        });
        addressStructure = new AddressStructure();
        btnLocationPicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(DangerLocationActivity.this, DangerLocationPickerActivity.class);
                intent.putExtra("lat", 0 + "");
                intent.putExtra("lng", 0 + "");
                startActivityForResult(intent, PICKUP_LOCATION_REQUEST);
            }
        });
        ivDatePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance(TimeZone.getDefault());

                DatePickerDialog dialog = new DatePickerDialog(DangerLocationActivity.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                addressStructure.setYear(year);
                                addressStructure.setMonth(month+1);
                                addressStructure.setDay_of_month(dayOfMonth);
                                SimpleDateFormat simpledateformat = new SimpleDateFormat("EEEE");
                                Date date = new Date(year, month, dayOfMonth-1);
                                String dayOfWeek = simpledateformat.format(date);
                                addressStructure.setDay_of_week(getNumericalDayOfWeek(dayOfWeek));
                                tvDate.setText(dayOfWeek + ", " + dayOfMonth + "/" + (month+1) + "/" + year);
                                //Toast.makeText(DangerLocationActivity.this, "" + addressStructure.getYear(), Toast.LENGTH_SHORT).show();
                            }
                        },
                        calendar.get(Calendar.YEAR), calendar.get(Calendar.MONTH),
                        calendar.get(Calendar.DAY_OF_MONTH));
                dialog.show();
            }
        });

        ivTimePicker.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar cal = Calendar.getInstance(TimeZone.getDefault());
                TimePickerDialog timePickerDialog = new TimePickerDialog(DangerLocationActivity.this,
                        new TimePickerDialog.OnTimeSetListener() {
                            @Override
                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                addressStructure.setTime_interval(getTimeIntervalFor(hourOfDay));
                                tvTime.setText(hourOfDay + ":" + minute);
                            }
                        },
                        cal.get(Calendar.HOUR_OF_DAY),
                        cal.get(Calendar.MINUTE),
                        DateFormat.is24HourFormat(DangerLocationActivity.this));
                timePickerDialog.show();
            }
        });
        btnSubmitData.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateFields()){
                    DatabaseReference myRef = FirebaseDatabase.getInstance().getReference().child("locations");
                    myRef.push().setValue(addressStructure.mapAddressData(), new DatabaseReference.CompletionListener() {
                        @Override
                        public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                            if (databaseError != null){
                                showSnackbar("Error. " + databaseError.getMessage());
                            } else
                                addNewLocationMarker();
                        }
                    });


                }
            }
        });
    }

    private void addNewLocationMarker(){
        DatabaseReference myLocationMarkedByRef = FirebaseDatabase.getInstance().getReference().child("loc_mark_by");
        LocationMarkedBy locationMarkedBy = new LocationMarkedBy(1, addressStructure.getSubmitted_by(),
                addressStructure.getLatitude(),
                addressStructure.getLongitude());
        myLocationMarkedByRef.push().setValue(locationMarkedBy.mapLocationMarkedData(), new DatabaseReference.CompletionListener() {
            @Override
            public void onComplete(@Nullable DatabaseError databaseError, @NonNull DatabaseReference databaseReference) {
                if (databaseError != null)
                    showSnackbar("Error. " + databaseError.getMessage());
                else
                    dataUpdatedSuccessfullyAlert();
            }
        });
    }
    private void getCurrentUserName(){
        DatabaseReference userRef = FirebaseDatabase.getInstance().getReference().child("users");
        String uid = FirebaseAuth.getInstance().getCurrentUser().getUid();
        userRef = userRef.child(uid).child("email");
        userRef.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
                if (dataSnapshot.exists()){
                    String sub = dataSnapshot.getValue(String.class);
                    tvSubmittedBy.setText(sub);
                    addressStructure.setSubmitted_by(sub);
                    addressStructure.setMarked_count(1);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });

    }
    private boolean validateFields(){
        if (tvLocality.getText().length()<1) {
            showSnackbar("Please Select Location from Location Picker");
            return false;
        } else if (tvDate.getText().length()<1){
            showSnackbar("Please Select Date from calender icon");
            return false;
        } else if (tvTime.getText().length()<1){
            showSnackbar("Please Select Time from clock icon");
            return false;
        }  else if (addressStructure.getVehicle_type().equals("not set")){
            showSnackbar("Please Select Vehicle Type from Dropdown");
            return false;
        } else
            return true;

    }
    private void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(llDangerActivityBaseLayout,msg, 5000);
        // Changing action button text color
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }
    private int getTimeIntervalFor(int hod){
        if (hod < 6) return 4;
        if (hod < 12) return 1;
        if (hod < 17) return 2;
        return 3;

    }

    private int getNumericalDayOfWeek(String dow){

        if (dow.equals("Monday")) return 1;
        if (dow.equals("Tuesday")) return  2;
        if (dow.equals("Wednesday")) return  3;
        if (dow.equals("Thursday")) return  4;
        if (dow.equals("Friday")) return  5;
        if (dow.equals("Saturday")) return  6;
        if (dow.equals("Sunday")) return  7;
        return 9;
    }

    /**
     * Dispatch incoming result to the correct fragment.
     *
     * @param requestCode
     * @param resultCode
     * @param data
     */
    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == PICKUP_LOCATION_REQUEST) {
            if (resultCode == RESULT_OK) {

                tvLat.setText(data.getStringExtra("lat"));
                tvLng.setText(data.getStringExtra("lng"));
                tvLocality.setText(data.getStringExtra("locality"));
                tvSubLocality.setText(data.getStringExtra("sub_locality"));
                addressStructure.setLatitude(Double.parseDouble(data.getStringExtra("lat")));
                addressStructure.setLongitude(Double.parseDouble(data.getStringExtra("lng")));
                addressStructure.setLocality(data.getStringExtra("locality").toLowerCase());
                addressStructure.setSub_locality(data.getStringExtra("sub_locality").toLowerCase());
            }

        }
    }
    private void dataUpdatedSuccessfullyAlert() {
        AlertDialog.Builder builder;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(DangerLocationActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(DangerLocationActivity.this);
        builder.setTitle("Upload Done")
                .setMessage("Data updated successfully!!!")
                .setNegativeButton("Done", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        startActivity(new Intent(DangerLocationActivity.this, MainActivity.class));
                        DangerLocationActivity.this.finish();
                    }
                })
                .setIcon(R.drawable.ic_cloud_done_24dp)
                .setCancelable(false)
                .show();
    }
}
