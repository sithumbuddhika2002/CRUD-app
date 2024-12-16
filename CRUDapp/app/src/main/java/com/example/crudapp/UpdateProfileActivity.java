package com.example.crudapp;

import android.app.DatePickerDialog;
import android.content.Intent;
import android.os.Bundle;
import android.text.TextUtils;
import android.util.Log;
import android.util.Patterns;
import android.view.Menu;
import android.view.MenuItem;
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
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.auth.UserProfileChangeRequest;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.Calendar;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class UpdateProfileActivity extends AppCompatActivity {

    private EditText editTextUpdateName, editTextUpdateDOB, editTextUpdateMobile;
    private RadioGroup radioGroupUpdateGender;
    private RadioButton radioButtonUpdateGenderSelected;
    private String textFullName, textDOB, textGender, textMobile;
    private FirebaseAuth authProfile;
    private ProgressBar progressBar;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_update_profile);
        //getSupportActionBar().setTitle("Update Profile");


        progressBar = findViewById(R.id.progress_bar);
        editTextUpdateName = findViewById(R.id.editText_update_profile_name);
        editTextUpdateDOB = findViewById(R.id.editText_update_profile_DOB);
        editTextUpdateMobile = findViewById(R.id.editText_update_profile_mobile);

        radioGroupUpdateGender = findViewById(R.id.radio_group_update_gender);

        authProfile = FirebaseAuth.getInstance();
        FirebaseUser firebaseUser = authProfile.getCurrentUser();

        //show profile data
        showProfile(firebaseUser);

        //upload profile pic
        Button buttonUploadProfilePic = findViewById(R.id.button_upload_profile_pic);
        buttonUploadProfilePic.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UploadProfilePictureActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //update email
        Button buttonUpdateEmail = findViewById(R.id.button_update_profile_email);
        buttonUpdateEmail.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(UpdateProfileActivity.this, UpdateEmailActivity.class);
                startActivity(intent);
                finish();
            }
        });

        //setting date picker on edittext
        editTextUpdateDOB.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //extracting saved dd,m,yyyy into different variable by creating array delimited by "/"
                String textSADOB[] = textDOB.split("/");

                int day = Integer.parseInt(textSADOB[0]);
                int month = Integer.parseInt(textSADOB[1]) -1 ;
                int year = Integer.parseInt(textSADOB[2]);

                DatePickerDialog picker;

                //date picker dialog
                picker = new DatePickerDialog( UpdateProfileActivity.this, new DatePickerDialog.OnDateSetListener() {
                    @Override
                    public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                        editTextUpdateDOB.setText(dayOfMonth + "/" + (month+1) + "/" + year);
                    }
                }, year,month,day);
                picker.show();
            }
        });

        //update profile
        Button buttonUpdateProfile = findViewById(R.id.button_update_profile);
        buttonUpdateProfile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                updateProfile(firebaseUser);
            }

        });


    }

    private void updateProfile(FirebaseUser firebaseUser) {
        int selectedGenderID = radioGroupUpdateGender.getCheckedRadioButtonId();
        radioButtonUpdateGenderSelected = findViewById(selectedGenderID);

        //validate mobile number as pattern
        String mobileRegex = "^(07[0-9]{8})$";
        Matcher mobileMatcher;
        Pattern mobilePattern = Pattern.compile(mobileRegex);
        mobileMatcher = mobilePattern.matcher(textMobile);


        if(TextUtils.isEmpty(textFullName)){
            Toast.makeText(UpdateProfileActivity.this,"Please enter your full name", Toast.LENGTH_LONG).show();
            editTextUpdateName.setError("Full name is required");
            editTextUpdateName.requestFocus();
        } else if (TextUtils.isEmpty(textDOB)) {
            Toast.makeText(UpdateProfileActivity.this,"Please enter your Date of Birth", Toast.LENGTH_LONG).show();
            editTextUpdateDOB.setError("Date of Birth is required");
            editTextUpdateDOB.requestFocus();
        } else if (radioGroupUpdateGender.getCheckedRadioButtonId() == -1) {
            Toast.makeText(UpdateProfileActivity.this,"Please select your Gnder", Toast.LENGTH_LONG).show();
            radioButtonUpdateGenderSelected.setError("Gender is required");
            radioButtonUpdateGenderSelected.requestFocus();
        } else if (TextUtils.isEmpty(textMobile)) {
            Toast.makeText(UpdateProfileActivity.this,"Please enter your Mobile number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile number is required");
            editTextUpdateMobile.requestFocus();
        } else if(!mobileMatcher.find()){
            Toast.makeText(UpdateProfileActivity.this,"Please re enter your Mobile number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile number is not valid");
            editTextUpdateMobile.requestFocus();
        } else if (textMobile.length() != 10) {
            Toast.makeText(UpdateProfileActivity.this,"Please re enter your mobile number", Toast.LENGTH_LONG).show();
            editTextUpdateMobile.setError("Mobile number. should be 10 digits");
            editTextUpdateMobile.requestFocus();
        }  else {
            //obtain the data entered by user
            textGender = radioButtonUpdateGenderSelected.getText().toString();
            textFullName = editTextUpdateName.getText().toString();
            textDOB = editTextUpdateDOB.getText().toString();
            textMobile = editTextUpdateMobile.getText().toString();

            //enter user data into the firebase realtime database. set up dependencies
            ReadWriteUserDetails writeUserDetails = new ReadWriteUserDetails(textFullName, textDOB, textGender, textMobile);

            //extract user reference from database for "register users"
            DatabaseReference referenceProfile = FirebaseDatabase.getInstance().getReference("Registered users");

            String userID = firebaseUser.getUid();

            progressBar.setVisibility(View.VISIBLE);

            referenceProfile.child(userID).setValue(writeUserDetails).addOnCompleteListener(new OnCompleteListener<Void>() {
                @Override
                public void onComplete(@NonNull Task<Void> task) {
                    if(task.isSuccessful()){
                        //setting new display name
                        UserProfileChangeRequest profileUpdates = new UserProfileChangeRequest.Builder().setDisplayName(textFullName).build();
                        firebaseUser.updateProfile(profileUpdates);

                        Toast.makeText(UpdateProfileActivity.this, "Update successfully",Toast.LENGTH_LONG).show();

                        //stop user from returning to updateProfileActivity on pressing back button and close activity
                        Intent intent = new Intent(UpdateProfileActivity.this, UserProfileActivity.class);
                        intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
                        startActivity(intent);
                        finish();
                    } else {
                        try {
                            throw task.getException();
                        } catch (Exception e){
                            Toast.makeText(UpdateProfileActivity.this, e.getMessage(), Toast.LENGTH_LONG).show();
                        }
                    }
                    progressBar.setVisibility(View.GONE);
                }
            });
        }
    }

    private void showProfile(FirebaseUser firebaseUser) {
        if (firebaseUser == null) {
            Toast.makeText(UpdateProfileActivity.this, "User not logged in!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        String userID = firebaseUser.getUid();
        Log.d("updateUserProfile: userID", userID);
        DatabaseReference referenceProfile  = FirebaseDatabase.getInstance().getReference("Registered users");
        progressBar.setVisibility(View.VISIBLE);


        referenceProfile.child(userID).addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                Log.d("showUserProfile: snapshot", snapshot.toString());
                progressBar.setVisibility(View.GONE);

                ReadWriteUserDetails readUserDetails = snapshot.getValue(ReadWriteUserDetails.class);

                if (readUserDetails != null) {
                    textFullName = firebaseUser.getDisplayName();
                    textDOB = readUserDetails.DOB;
                    textGender = readUserDetails.gender;
                    textMobile = readUserDetails.mobile;

                    editTextUpdateName.setText(textFullName);
                    editTextUpdateDOB.setText(textDOB);
                    editTextUpdateMobile.setText(textMobile);

                    if ("Male".equals(textGender)) {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_male);
                    } else if ("Female".equals(textGender)) {
                        radioButtonUpdateGenderSelected = findViewById(R.id.radio_female);
                    } else {
                        Toast.makeText(UpdateProfileActivity.this, "Invalid gender value!", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    radioButtonUpdateGenderSelected.setChecked(true);
                } else {
                    Toast.makeText(UpdateProfileActivity.this, "Failed to load profile. Please try again.", Toast.LENGTH_LONG).show();
                }
            }


            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                progressBar.setVisibility(View.GONE);
                Toast.makeText(UpdateProfileActivity.this, "Database error: " + error.getMessage(), Toast.LENGTH_LONG).show();
            }
        });
    }


    //creating action bar menu


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        //inflate menu item
        getMenuInflater().inflate(R.menu.common_menu, menu);
        return super.onCreateOptionsMenu(menu);
    }

    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.menu_refresh){
            //refresh activity
            startActivity(getIntent());
            finish();
            overridePendingTransition(0,0);
        } else if (id == R.id.menu_update_profile) {
            Intent intent = new Intent(UpdateProfileActivity.this,UpdateProfileActivity.class);
            startActivity(intent);
            finish();
        } else if (id == R.id.menu_update_email) {
            Intent intent = new Intent(UpdateProfileActivity.this,UpdateEmailActivity.class);
            startActivity(intent);
            finish();
        }  else if (id == R.id.menu_setting) {
            Toast.makeText(UpdateProfileActivity.this,"menu setting" , Toast.LENGTH_SHORT).show();

        } else if (id == R.id.menu_delete_profile) {
            Intent intent = new Intent(UpdateProfileActivity.this,DeleteProfileActivity.class);
            startActivity(intent);
        } else if (id == R.id.menu_logout) {
            authProfile.signOut();
            Toast.makeText(UpdateProfileActivity.this, "Logged out", Toast.LENGTH_LONG).show();
            Intent intent = new Intent(UpdateProfileActivity.this,MainActivity.class);

            //clear stack to prevent user coming back
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP | Intent.FLAG_ACTIVITY_CLEAR_TASK | Intent.FLAG_ACTIVITY_NEW_TASK);
            startActivity(intent);
            finish();
        } else {
            Toast.makeText(UpdateProfileActivity.this, "something went wrong", Toast.LENGTH_LONG).show();

        }

        return super.onOptionsItemSelected(item);
    }

}