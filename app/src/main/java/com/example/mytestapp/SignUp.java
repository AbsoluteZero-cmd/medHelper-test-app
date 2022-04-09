package com.example.mytestapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.android.material.badge.BadgeUtils;
import com.google.firebase.auth.AuthResult;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;

public class SignUp extends AppCompatActivity {

    EditText signUpFullName, signUpEmail, signUpPassword, signUpPhone;
    Button signUpBtn, alreadyHaveBtn;
    DatePicker signUpDate;

    Date date;
    Calendar calendar;

    FirebaseAuth mAuth;
    FirebaseDatabase database;
    DatabaseReference reference;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_sign_up);

        signUpFullName = findViewById(R.id.sign_up_fullName);
        signUpEmail = findViewById(R.id.sign_up_email);
        signUpPassword = findViewById(R.id.sigh_up_password);
        signUpPhone = findViewById(R.id.sign_up_phone);
        signUpDate = findViewById(R.id.sign_up_date);

        signUpBtn = findViewById(R.id.sign_up_btn);
        alreadyHaveBtn = findViewById(R.id.already_have_btn);

        mAuth = FirebaseAuth.getInstance();
        database = FirebaseDatabase.getInstance();
        reference = database.getReference("Users");

        signUpBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                createNewUser();
            }
        });

        alreadyHaveBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(SignUp.this, Login.class));
            }
        });
    }

    private void createNewUser() {
        String mFullName = signUpFullName.getText().toString().trim();
        String mEmail = signUpEmail.getText().toString().trim();
        String mPassword = signUpPassword.getText().toString().trim();
        String mPhone = signUpPhone.getText().toString().trim();

        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd");
        calendar = Calendar.getInstance();
        calendar.set(signUpDate.getYear(), signUpDate.getMonth(), signUpDate.getDayOfMonth());
        date = calendar.getTime();

        String mDate = sdf.format(date);

        if(!validateName() || !validatePassword() || !validateEmail() || !validatePhone()){
            return;
        }
        else{
            mAuth.createUserWithEmailAndPassword(mEmail, mPassword).addOnCompleteListener(new OnCompleteListener<AuthResult>() {
                @Override
                public void onComplete(@NonNull Task<AuthResult> task) {
                    if(task.isSuccessful()){
                        User mUser = new User(mFullName, mEmail, mPassword, mPhone, mDate, null);

                        Toast.makeText(SignUp.this, "Sign Up is successful", Toast.LENGTH_SHORT).show();

                        reference.child(mAuth.getUid()).setValue(mUser);
                        System.out.println(mUser);

                        startActivity(new Intent(SignUp.this, MainActivity.class));
                    }
                }
            });
        }
    }

    private Boolean validateName(){
        String val = signUpFullName.getText().toString().trim();

        if(val.isEmpty() == true){
            signUpFullName.setError("Field cannot be empty");
            signUpFullName.requestFocus();
            return false;
        }
        else{
            signUpFullName.setError(null);
            return true;
        }
    }
    private Boolean validateEmail(){
        String val = signUpEmail.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(val.isEmpty()){
            signUpEmail.setError("Field cannot be empty");
            signUpEmail.requestFocus();
            return false;
        }
        if(!val.matches(emailPattern)){
            signUpEmail.setError("Incorrect email");
            signUpEmail.requestFocus();
            return false;
        }
        else{
            signUpEmail.setError(null);
            return true;
        }
    }
    private Boolean validatePassword(){
        String val = signUpPassword.getText().toString().trim();

        if(val.isEmpty()){
            signUpPassword.setError("Field cannot be empty");
            signUpPassword.requestFocus();
            return false;
        }
        else if(val.length() < 5){
            signUpPassword.setError("Password is too short");
            signUpPassword.requestFocus();
            return false;
        }
        else{
            signUpPassword.setError(null);
            return true;
        }
    }
    private Boolean validatePhone(){
        String val = signUpPhone.getText().toString().trim();

        if(val.isEmpty()){
            signUpPhone.setError("Field cannot be empty");
            signUpPhone.requestFocus();
            return false;
        }
        else{
            signUpPhone.setError(null);
            return true;
        }
    }
}