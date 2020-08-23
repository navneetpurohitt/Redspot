package com.sistec.redspot;

import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Build;
import android.os.Bundle;
import android.util.Patterns;
import android.view.LayoutInflater;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.snackbar.Snackbar;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.rengwuxian.materialedittext.MaterialEditText;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;

public class LoginActivity extends AppCompatActivity {

    TextView tvSignUp, tvForget;
    Button btnLogin;
    EditText id, pass;
    private FirebaseAuth auth;
    private SharedPreferences file;
    private AlertDialog.Builder alertDialog = null;
    private AlertDialog dialog;

    MaterialEditText Edtuser;

    private ProgressDialog processDialog;
    private LinearLayout loginAcyivityBaseLayout;

    Button Validate;
    AlertDialog.Builder builder;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        processDialog = new ProgressDialog(this);
        loginAcyivityBaseLayout = findViewById(R.id.login_activity_base_layout);
        initItems();
        setButtonClickListeners();
    }

    private void initItems() {
        tvSignUp = findViewById(R.id.TVSignup);
        tvForget = findViewById(R.id.TVforget);
        btnLogin = findViewById(R.id.BTLogin);
        id = findViewById(R.id.ETemail);
        pass = findViewById(R.id.ETpass);
        auth = FirebaseAuth.getInstance();
    }


    private void setButtonClickListeners() {
        tvSignUp.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(LoginActivity.this, RegisterActivity.class);
                startActivity(intent);
            }
        });

        btnLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View _v) {
                processDialog.setTitle("Login");
                processDialog.setMessage("Please Wait");
                processDialog.show();
                startUserLogin();
            }
        });

        tvForget.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                showForgetPassDialog();
            }
        });
    }

    private void startUserLogin(){
        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(pass.getWindowToken(),
                InputMethodManager.RESULT_UNCHANGED_SHOWN);

        if (id.getText().toString().trim().isEmpty() || !Patterns.EMAIL_ADDRESS.matcher(id.getText().toString().trim()).matches()) {
            id.setError("Please Enter Valid Email");
            processDialog.dismiss();
        } else {
            if (pass.getText().toString().trim().length() < 6) {
                pass.setError("Minimum 6 character required");
                processDialog.dismiss();
            } else {
                Intent intent = new Intent(LoginActivity.this, MainActivity.class);
                startActivity(intent);
                LoginActivity.this.finish();

//                auth.signInWithEmailAndPassword(id.getText().toString(), pass.getText().toString())
//                        .addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
//                            @Override
//                            public void onComplete(Task<AuthResult> task) {
//                                String _errorMessage = task.getException() != null ? task.getException().getMessage() : "";
//                                if (task.isSuccessful()) {
//                                    processDialog.dismiss();
//                                    if (task.getResult().getUser().isEmailVerified()) {
//                                        Intent intent = new Intent(LoginActivity.this, MainActivity.class);
//                                        startActivity(intent);
//                                        LoginActivity.this.finish();
//                                    } else {
//                                        showEmailVerificationErrorDialog();
//                                    }
//                                } else {
//                                    processDialog.dismiss();
//                                    showSnackbar(_errorMessage);
//                                }
//
//                            }
//                        });
            }
        }
    }

    private void showEmailVerificationErrorDialog() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.LOLLIPOP)
            builder = new AlertDialog.Builder(LoginActivity.this, android.R.style.Theme_Material_Dialog_Alert);
        else
            builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email is Not Verified")
                .setMessage("Please verify your email from link, sent on your email")
                .setPositiveButton("Resend Link", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        resendEmailVerificationLink();
                        processDialog.dismiss();
                    }
                })
                .setNegativeButton("I'll come back", new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        Toast.makeText(LoginActivity.this, "Thank you.", Toast.LENGTH_SHORT).show();
                    }
                })
                .setIcon(R.drawable.ic_error_red_24dp)
                .setCancelable(false)
                .show();
    }

    private void resendEmailVerificationLink(){
        final FirebaseUser tUser = FirebaseAuth.getInstance().getCurrentUser();
        if (tUser != null) {
            tUser.sendEmailVerification().addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    showSnackbar("Please check your mail for verification.");
                    FirebaseAuth.getInstance().signOut();
                }
            });
        }
    }

    private void showSnackbar(String msg){
        Snackbar snackbar = Snackbar.make(loginAcyivityBaseLayout,msg, 5000);
        // Changing action button text color
        TextView textView = (TextView) snackbar.getView().findViewById(R.id.snackbar_text);
        textView.setTextColor(Color.YELLOW);
        snackbar.show();
    }

    private void showForgetPassDialog() {
        if (alertDialog == null) {
            alertDialog = new AlertDialog.Builder(LoginActivity.this);
            alertDialog.setTitle("Password Recovery");
            // alertDialog.setMessage("Please Fill Full Information");

            LayoutInflater inflater = this.getLayoutInflater();
            View sign_up_layout = inflater.inflate(R.layout.pass_recovery_layout, null);

            Edtuser = sign_up_layout.findViewById(R.id.phone);
            Validate = sign_up_layout.findViewById(R.id.BTverify);
            dialog = alertDialog.create();
            Validate.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    final String idd = Edtuser.getText().toString().trim();
                    if (idd.isEmpty()){
                        Edtuser.setError("Please Enter Email Id");
                    } else {
                        FirebaseAuth.getInstance().sendPasswordResetEmail(idd)
                                .addOnCompleteListener(new OnCompleteListener<Void>() {
                                    @Override
                                    public void onComplete(@NonNull Task<Void> task) {
                                        if (task.isSuccessful()) {
                                            showSnackbar("Email Sent to " + idd + "!");
                                            dialog.dismiss();
                                        }
                                    }
                                })
                        .addOnFailureListener(new OnFailureListener() {
                            @Override
                            public void onFailure(@NonNull Exception e) {
                                showSnackbar("" + e.getMessage());
                                Edtuser.setError(e.getMessage());
                            }
                        })
                        ;
                    }
                }

            });
            alertDialog.setView(sign_up_layout);
            alertDialog.setOnDismissListener(new DialogInterface.OnDismissListener() {
                @Override
                public void onDismiss(DialogInterface dialog) {
                    dialog.dismiss();
                    alertDialog = null;
                }
            });
            alertDialog.setIcon(R.drawable.ic_pass_recover_24dp);
            alertDialog.show();
        }

    }

}
