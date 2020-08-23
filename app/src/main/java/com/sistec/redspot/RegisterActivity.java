package com.sistec.redspot;

import android.app.ProgressDialog;
import android.content.Intent;
import android.graphics.Color;
import android.os.Bundle;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.HashMap;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

public class RegisterActivity extends AppCompatActivity {

    LinearLayout regActivityBaseLayout;
    Button btnSignup;

    EditText etName;
    EditText etPassword;
    EditText etMobileNumber;
    EditText etEmail;
    Button btotp;
    String strName, strPassword, strMobileNumber, strEmail;

    HashMap<String, Object> var = new HashMap<>();
    FirebaseDatabase firebaseDatabase = FirebaseDatabase.getInstance();

    private FirebaseAuth mAuth;
    private ProgressDialog processDialog;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        regActivityBaseLayout = findViewById(R.id.reg_activity_base_layout);
        btnSignup = findViewById(R.id.BTSignup);
        etName = findViewById(R.id.ETname);
        etPassword = findViewById(R.id.ETpassword);
        etEmail = findViewById(R.id.ETmail);
        etMobileNumber = findViewById(R.id.ETphone);
        mAuth = FirebaseAuth.getInstance();
        processDialog = new ProgressDialog(this);
        btnSignup.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (validateEditTextFields()) {
                    registerNewUser();
                }
            }
        });
    }

    private void showSnackbar(String msg) {
        Snackbar snackbar = Snackbar.make(regActivityBaseLayout, msg, 5000);
        // Changing action button text color
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void registerNewUser() {
        processDialog.setTitle("Registering");
        processDialog.setMessage("Please Wait....");
        processDialog.show();
        mAuth.createUserWithEmailAndPassword(strEmail, strPassword)
                .addOnCompleteListener(this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            sendVerificationMail();
                            //Toast.makeText(RegisterActivity.this, "Register successfully", Toast.LENGTH_SHORT).show();
                            updateUserDatabase();
                        } else {
                            Toast.makeText(RegisterActivity.this, "" + task.getException().getMessage(), Toast.LENGTH_SHORT).show();
                        }
                        processDialog.dismiss();
                    }
                });
    }

    private void sendVerificationMail() {
        final FirebaseUser tUser = FirebaseAuth.getInstance().getCurrentUser();
        if (tUser != null) {
            tUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    showSnackbar("Please check your mail for verification.");
                    FirebaseAuth.getInstance().signOut();
                    startActivity(new Intent(RegisterActivity.this, LoginActivity.class));
                    finish();
                }
            });
        }
    }


    private void updateUserDatabase() {
        DatabaseReference prof = firebaseDatabase.getReference("users").child(mAuth.getCurrentUser().getUid());
        var.put("email", strEmail.toLowerCase());
        var.put("name", strName.toLowerCase());
        var.put("mobile", strMobileNumber);
        var.put("user_type", "user");
        prof.updateChildren(var);
        prof.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot dataSnapshot) {
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {

            }
        });
    }

    private boolean validateEditTextFields() {
        strName = etName.getText().toString().trim();
        strEmail = etEmail.getText().toString().trim();
        strMobileNumber = etMobileNumber.getText().toString().trim();
        strPassword = etPassword.getText().toString().trim();
        if (strName.isEmpty()) {
            etName.setError("Please Enter Your Name");
            return false;
        }
        if (strEmail.isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(strEmail).matches()) {
            etEmail.setError("Please Enter Valid Email");
            return false;
        }
        if (strMobileNumber.length() < 10) {
            etMobileNumber.setError("Please Enter Valid Mobile Number");
            return false;
        }
        if (strPassword.length() < 6) { //Firebase password rule
            etPassword.setError("Please Enter a password (minimum 6 char)");
            return false;
        }
        return true;
    }

}

