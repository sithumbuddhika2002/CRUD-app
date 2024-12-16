package com.example.crudapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.Toast;

import androidx.activity.EdgeToEdge;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.Firebase;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseAuthWeakPasswordException;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.io.Reader;
import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class RegisterActivity extends AppCompatActivity {

    private EditText editTextRegisterFullName, editTextRegisterEmail, editTextRegisterDOB, editTextRegisterMobile,
                    editTextRegisterPWD, editTextRegisterConfirmPWD;

    private ProgressBar progressBar;
    private RadioGroup radioGroupRegisterGender;
    private RadioButton radioButtonRegisterGenderSelected;
    private static final String TAG = "RegisterActivity";
    private DatePickerDialog picker;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_register);

        //getSupportActionBar().setTitle("Register");

        Toast.makeText(RegisterActivity.this,"you can register now",Toast.LENGTH_LONG).show();

        progressBar = findViewById(R.id.progress_bar);
        editTextRegisterFullName = findViewById(R.id.editText_register_full_name);
        editTextRegisterEmail = findViewById(R.id.editText_register_email);
        editTextRegisterDOB = findViewById(R.id.editText_register_dob);
        editTextRegisterMobile = findViewById(R.id.editText_register_mobile);
        editTextRegisterPWD = findViewById(R.id.editText_register_password);
        editTextRegisterConfirmPWD = findViewById(R.id.editText_register_confirm_password);

        //radio button for gender
        radioGroupRegisterGender = findViewById(R.id.radio_group_register_gender);
        radioGroupRegisterGender.clearCheck();

        //setting date picker on edittext
        editTextRegisterDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                final Calendar calendar = Calendar.getInstance();
                int day = calendar.get(Calendar.DAY_OF_MONTH);
                int month = calendar.get(Calendar.MONTH);
                int year = calendar.get(Calendar.YEAR);

                //date picker dialog
                picker = new DatePickerDialog(RegisterActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextRegisterDOB.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                    }
                }, year,month,day);
                picker.show();
            }
        });

        Button buttonRegister = findViewById(R.id.button_register);
        buttonRegister.setOnClickListener((new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                int selectGenderId = radioGroupRegisterGender.getCheckedRadioButtonId();
                radioButtonRegisterGenderSelected = findViewById(selectGenderId);

                //obtain the entered data
                String textFullName = editTextRegisterFullName.getText().toString();
                String textEmail = editTextRegisterEmail.getText().toString();
                String textDOB = editTextRegisterDOB.getText().toString();
                String textMobile = editTextRegisterMobile.getText().toString();
                String textPWD = editTextRegisterPWD.getText().toString();
                String textConfirmPWD = editTextRegisterConfirmPWD.getText().toString();
                String textGender;

                //validate mobile number as pattern
                String mobileRegex = "^(07[0-9]{8})$";
                Matcher mobileMatcher;
                Pattern mobilePattern = Pattern.compile(mobileRegex);
                mobileMatcher = mobilePattern.matcher(textMobile);


                if(TextUtils.isEmpty(textFullName)){
                    Toast.makeText(RegisterActivity.this,"Please enter your full name", Toast.LENGTH_LONG).show();
                    editTextRegisterFullName.setError("Full name is required");
                    editTextRegisterFullName.requestFocus();
                } else if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(RegisterActivity.this,"Please enter Email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(RegisterActivity.this,"Please re enter your Email", Toast.LENGTH_LONG).show();
                    editTextRegisterEmail.setError("Valid Email is required");
                    editTextRegisterEmail.requestFocus();
                } else if (TextUtils.isEmpty(textDOB)) {
                    Toast.makeText(RegisterActivity.this,"Please enter your Date of Birth", Toast.LENGTH_LONG).show();
                    editTextRegisterDOB.setError("Date of Birth is required");
                    editTextRegisterDOB.requestFocus();
                } else if (radioGroupRegisterGender.getCheckedRadioButtonId() == -1) {
                    Toast.makeText(RegisterActivity.this,"Please select your Gnder", Toast.LENGTH_LONG).show();
                    radioButtonRegisterGenderSelected.setError("Gender is required");
                    radioButtonRegisterGenderSelected.requestFocus();
                } else if (TextUtils.isEmpty(textMobile)) {
                    Toast.makeText(RegisterActivity.this,"Please enter your Mobile number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile number is required");
                    editTextRegisterMobile.requestFocus();
                } else if(!mobileMatcher.find()){
                    Toast.makeText(RegisterActivity.this,"Please re enter your Mobile number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile number is not valid");
                    editTextRegisterMobile.requestFocus();
                } else if (textMobile.length() != 10) {
                    Toast.makeText(RegisterActivity.this,"Please re enter your mobile number", Toast.LENGTH_LONG).show();
                    editTextRegisterMobile.setError("Mobile number. should be 10 digits");
                    editTextRegisterMobile.requestFocus();
                } else if (TextUtils.isEmpty(textPWD)) {
                    Toast.makeText(RegisterActivity.this,"Please enter your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterPWD.setError("Password is required");
                    editTextRegisterPWD.requestFocus();
                } else if (textPWD.length() < 6) {
                    Toast.makeText(RegisterActivity.this,"Please re enter your Password", Toast.LENGTH_LONG).show();
                    editTextRegisterPWD.setError("Password to week");
                    editTextRegisterPWD.requestFocus();
                } else if (TextUtils.isEmpty(textConfirmPWD)) {
                    Toast.makeText(RegisterActivity.this,"Please enter your confirm password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPWD.setError("Confirm Password is required");
                    editTextRegisterConfirmPWD.requestFocus();
                } else if (!textPWD.equals((textConfirmPWD))) {
                    Toast.makeText(RegisterActivity.this,"Please re enter your confirm password", Toast.LENGTH_LONG).show();
                    editTextRegisterConfirmPWD.setError("Password did not match");
                    editTextRegisterConfirmPWD.requestFocus();
                } else {
                    textGender = radioButtonRegisterGenderSelected.getText().toString();
                    progressBar.setVisibility(View.VISIBLE);
                    registerUser(textFullName, textEmail, textGender, textDOB, textMobile,textPWD);
                }

            }
        }));

    }

    //register user using
    private void registerUser(String textFullName, String textEmail, String textGender, String textDOB, String textMobile, String textPWD) {
        FirebaseAuth auth = FirebaseAuth.getInstance();

        //create user profile
        auth.createUserWithEmailAndPassword(textEmail, textPWD).addOnCompleteListener(RegisterActivity.this, new OnCompleteListener<AuthResult>() {
                    @Override
                    public void onComplete(@NonNull Task<AuthResult> task) {
                        if (task.isSuccessful()) {
                            FirebaseUser firebaseUser = auth.getCurrentUser();


                            //update display name of the user
                            UserProfileChangeRequest profileChangeRequest = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                            firebaseUser.updateProfile(profileChangeRequest);

                            //enter user data into the firebase realtime database
                            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textFullName, textDOB,textGender,textMobile);
                            //Extracting user reference from Database for "Registered users"
                            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered users");

                            referenceProfile.child(firebaseUser.getUid()).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                                @Override
                                public void onComplete(@NonNull Task<Void> task) {
                                     if (task.isSuccessful()){
                                         //send verification email
                                         firebaseUser.sendEmailVerification();

                                         Toast.makeText(RegisterActivity.this, "User registed successfully. please verify your email", Toast.LENGTH_LONG).show();

                                         //open user profile after successful registration
                                         Intent intent = new Intent(RegisterActivity.this,UserProfileActivity.class);

                                         //to prevent user from returning back to register activity on pressing back button after registration
                                         intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);

                                         startActivity(intent);
                                         finish();
                                     }else {
                                         Toast.makeText(RegisterActivity.this, "User registed failed. please try again", Toast.LENGTH_LONG).show();

                                     }
                                    progressBar.setVisibility(View.GONE);

                                }
                            });
                            Toast.makeText(RegisterActivity.this, "User registered successfully. Please verify your email.", Toast.LENGTH_LONG).show();

                            // Optional: Redirect to another activity
                        } else {
                            Toast.makeText(RegisterActivity.this, "Registration failed: " + task.getException().getMessage(), Toast.LENGTH_LONG).show();
                            try {
                                throw task.getException();
                            } catch (FirebaseAuthWeakPasswordException e){
                                editTextRegisterPWD.setError("Your password so weak. kindly use a mix alphabets, numbers are including");
                                editTextRegisterPWD.requestFocus();
                            } catch(FirebaseAuthInvalidCredentialsException e){
                                editTextRegisterEmail.setError("your email is invalid or already using, kindly re enter");
                                editTextRegisterEmail.requestFocus();
                            } catch(FirebaseAuthUserCollisionException e){
                                editTextRegisterEmail.setError("User is already registered this email, use another email");
                                editTextRegisterEmail.requestFocus();
                            } catch (Exception e){
                                Log.e(TAG, e.getMessage());
                                Toast.makeText(RegisterActivity.this, e.getMessage(),Toast.LENGTH_LONG).show();

                            }
                            progressBar.setVisibility(View.GONE);

                        }
                    }
                });

    }
}