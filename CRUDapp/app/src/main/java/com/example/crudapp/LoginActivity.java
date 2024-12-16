package com.example.crudapp;

import android.content.DialogInterface;
import android.content.Intent;
import android.nfc.Tag;
import android.os.Bundle;
import android.text.TextUtils;
import android.text.method.HideReturnsTransformationMethod;
import android.text.method.PasswordTransformationMethod;
import android.util.Log;
import android.util.Patterns;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.graphics.Insets;
import androidx.core.view.ViewCompat;
import androidx.core.view.WindowInsetsCompat;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseAuthInvalidCredentialsException;
import com.google.firebase.auth.FirebaseAuthInvalidUserException;
import com.google.firebase.auth.FirebaseAuthUserCollisionException;
import com.google.firebase.auth.FirebaseUser;

public class LoginActivity extends AppCompatActivity {

    private EditText editTextLoginEmail, editTextLoginPWD;
    private ProgressBar progressBar;
    private FirebaseAuth authProfile;
    private static final String TAG = "LoginActivity";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_login);

        //getSupportActionBar().setTitle("Login");

        editTextLoginEmail = findViewById(R.id.editText_login_email);
        editTextLoginPWD =findViewById(R.id.editText_login_password);
        progressBar = findViewById(R.id.progress_bar);

        authProfile = FirebaseAuth.getInstance();

        //show hide password using eye icon
        ImageView imageViewShowHidePWD = findViewById(R.id.imageView_show_hde_pwd);
        imageViewShowHidePWD.setImageResource(R.drawable.ic_hide_pwd);
        imageViewShowHidePWD.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                if (editTextLoginPWD.getTransformationMethod().equals(HideReturnsTransformationMethod.getInstance())){

                    //if password is visible then hide it
                    editTextLoginPWD.setTransformationMethod(PasswordTransformationMethod.getInstance());

                    //change Icon
                    imageViewShowHidePWD.setImageResource(R.drawable.ic_hide_pwd);
                }else {
                    editTextLoginPWD.setTransformationMethod(HideReturnsTransformationMethod.getInstance());
                    imageViewShowHidePWD.setImageResource(R.drawable.ic_show_pwd);
                }
            }
        });

        //login user
        Button buttonLogin = findViewById(R.id.button_login);
        buttonLogin.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {

                String textEmail = editTextLoginEmail.getText().toString();
                String textPWD = editTextLoginPWD.getText().toString();

                if (TextUtils.isEmpty(textEmail)) {
                    Toast.makeText(LoginActivity.this, "Please enter your email", Toast.LENGTH_LONG).show();
                    editTextLoginEmail.setError("Email is required");
                    editTextLoginPWD.requestFocus();
                } else if (!Patterns.EMAIL_ADDRESS.matcher(textEmail).matches()) {
                    Toast.makeText(LoginActivity.this, "Please re enter your Email", Toast.LENGTH_LONG).show();
                    editTextLoginEmail.setError("Valid Email is required");
                    editTextLoginEmail.requestFocus();
                } else if (TextUtils.isEmpty(textPWD)) {
                    Toast.makeText(LoginActivity.this, "Please enter your Password", Toast.LENGTH_LONG).show();
                    editTextLoginPWD.setError("Password is required");
                    editTextLoginPWD.requestFocus();
                } else {
                    progressBar.setVisibility(View.VISIBLE);
                    loginUser(textEmail, textPWD);
                }
            }
        });

    }


    private void loginUser(String email, String PWD) {
         authProfile.signInWithEmailAndPassword(email,PWD).addOnCompleteListener(LoginActivity.this, new OnCompleteListener<AuthResult>() {
             @Override
             public void onComplete(@NonNull Task<AuthResult> task) {
                 if(task.isSuccessful()){
                     //get instance of the current user
                     FirebaseUser firebaseUser = authProfile.getCurrentUser();

                     //check if email is verified before user can access their profile
                     if (firebaseUser.isEmailVerified()){
                         Toast.makeText(LoginActivity.this,"You are logged in now",Toast.LENGTH_SHORT).show();

                         //open user profile
                         //start the userProfileActivity
                         startActivity(new Intent(LoginActivity.this,UserProfileActivity.class));
                         finish();
                     } else {
                         firebaseUser.sendEmailVerification();
                         authProfile.signOut();
                         showAlertDialog();
                     }
                 }

                 if(task.isSuccessful()){
                     Toast.makeText(LoginActivity.this,"You are logged in now", Toast.LENGTH_LONG).show();
                 } else {
                     try {
                         throw task.getException();
                     }catch (FirebaseAuthInvalidUserException e){
                         editTextLoginEmail.setError("user does not exist or is no longer valid. please register again.");
                         editTextLoginEmail.requestFocus();
                     } catch (FirebaseAuthInvalidCredentialsException e) {
                         editTextLoginEmail.setError("Invalid credientials. kindly, check and re enter");
                         editTextLoginEmail.requestFocus();
                     } catch (Exception e){
                         Log.e(TAG, e.getMessage());
                         Toast.makeText(LoginActivity.this, e.getMessage(), Toast.LENGTH_SHORT).show();
                     }
                     Toast.makeText(LoginActivity.this,"something went wrong", Toast.LENGTH_LONG).show();
                 }
                 progressBar.setVisibility(View.GONE);
             }
         });
    }

    private void showAlertDialog() {
        //setup the alert builder
        AlertDialog.Builder builder = new AlertDialog.Builder(LoginActivity.this);
        builder.setTitle("Email not verified");
        builder.setMessage("Please verify your email now. you can not log in without email verification");

        //open email apps if user clicks continue button
        builder.setPositiveButton("Continue", new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int which) {
                Intent intent = new Intent(Intent.ACTION_MAIN);
                intent.addCategory(Intent.CATEGORY_APP_EMAIL);
                intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK); //to email app in new window and not within our app
                startActivity(intent);
            }
        });

        //create the alert dialog
        AlertDialog alertDialog = builder.create();

        //show the alert dialog
        alertDialog.show();
    }

    //check if user is already logged in. in such case , straightway take the user to the user's process

    protected void oonStart(){
        super.onStart();
        if (authProfile.getCurrentUser() != null){
            Toast.makeText(LoginActivity.this, "Already logged in!", Toast.LENGTH_SHORT).show();

            //start the userProfileActvity
            startActivity(new Intent(LoginActivity.this, UserProfileActivity.class));
            finish();
        } else {
            Toast.makeText(LoginActivity.this, "You can login now!", Toast.LENGTH_SHORT).show();

        }
    }
}