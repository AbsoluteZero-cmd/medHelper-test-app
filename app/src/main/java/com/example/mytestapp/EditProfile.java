package com.example.mytestapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;

public class EditProfile extends AppCompatActivity {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userReference;

    EditText editFullName, editEmail, editPhone, editPassword;
    Button confirmBtn;

    User user;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_edit_profile);

        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference("Users");

        editFullName = findViewById(R.id.edit_profile_fullName);
        editEmail = findViewById(R.id.edit_profile_email);
        editPhone = findViewById(R.id.edit_profile_phone);
        editPassword = findViewById(R.id.edit_profile_password);
        confirmBtn = findViewById(R.id.edit_profile_confirm_button);

        getData();

        confirmBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if(!validateName() || !validateEmail() || !validatePhone() || !validatePassword()){
                    return;
                }
                else{
                    System.out.println("It works");
                    updateUser();
                    startActivity(new Intent(EditProfile.this, MainActivity.class));
                }
            }
        });
    }

    private void updateUser() {
        String mFullName = editFullName.getText().toString().trim();
        String mEmail = editEmail.getText().toString().trim();
        String mPhone = editPhone.getText().toString().trim();
        String mPassword = editPassword.getText().toString().trim();

        if(isFullNameChanged(mFullName)){
            user.setFullName(mFullName);
        }
        if(isPhoneChanged(mPhone)){
            user.setPhone(mPhone);
        }
        if(isEmailChanged(mEmail)){
            user.setEmail(mEmail);
            currentUser.updateEmail(mEmail)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                System.out.println("Email successful " + mEmail);
                                user.setEmail(mEmail);
                                System.out.println("Email successful " + mEmail + " " + user.getEmail());
                            }
                            else{
                                Toast.makeText(EditProfile.this, "Email update error, re-authentication required", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }
        if(isPasswordChanged(mPassword)){
            user.setPassword(mPassword);
            currentUser.updatePassword(mPassword)
                    .addOnCompleteListener(new OnCompleteListener<Void>() {
                        @Override
                        public void onComplete(@NonNull Task<Void> task) {
                            if(task.isSuccessful()){
                                System.out.println("Password successful");
                                user.setPassword(mPassword);
                            }
                            else{
                                Toast.makeText(EditProfile.this, "Password update error, re-authentication required", Toast.LENGTH_SHORT).show();
                            }
                        }
                    });
        }

        userReference.child(currentUser.getUid()).setValue(user);
        System.out.println("My current email: " + currentUser.getEmail() + " " + user.getEmail());
        Toast.makeText(EditProfile.this, "Profile update successful", Toast.LENGTH_SHORT).show();
    }

    private void getData() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.child(currentUser.getUid()).getValue(User.class);
                editFullName.setText(user.getFullName());
                editEmail.setText(user.getEmail());
                editPassword.setText(user.getPassword());
                editPhone.setText(user.getPhone());
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {

            }
        });
    }

    // validators
    private Boolean validateName(){
        String val = editFullName.getText().toString().trim();

        if(val.isEmpty() == true){
            editFullName.setError("Field cannot be empty");
            editFullName.requestFocus();
            return false;
        }
        else{
            editFullName.setError(null);
            return true;
        }
    }
    private Boolean validatePassword(){
        String val = editPassword.getText().toString().trim();

        if(val.isEmpty() == true){
            editPassword.setError("Field cannot be empty");
            editPassword.requestFocus();
            return false;
        }
        else if(val.length() < 5){
            editPassword.setError("Password is too short");
            editPassword.requestFocus();
            return false;
        }
        else{
            editPassword.setError(null);
            return true;
        }
    }
    private Boolean validateEmail(){
        String val = editEmail.getText().toString().trim();
        String emailPattern = "[a-zA-Z0-9._-]+@[a-z]+\\.+[a-z]+";

        if(val.isEmpty()){
            editEmail.setError("Field cannot be empty");
            editEmail.requestFocus();
            return false;
        }
        if(!val.matches(emailPattern)){
            editEmail.setError("Incorrect email");
            editEmail.requestFocus();
            return false;
        }
        else{
            editEmail.setError(null);
            return true;
        }
    }
    private Boolean validatePhone(){
        String val = editPhone.getText().toString().trim();

        if(val.isEmpty()){
            editPhone.setError("Field cannot be empty");
            editPhone.requestFocus();
            return false;
        }
        else{
            editPhone.setError(null);
            return true;
        }
    }

    // isDataChanged
    private Boolean isFullNameChanged(String val){
        if(!val.equals(user.getFullName())){
            return true;
        }
        return false;
    }
    private Boolean isEmailChanged(String val){
        if(!val.equals(user.getEmail())){
            return true;
        }
        return false;
    }
    private Boolean isPasswordChanged(String val){
        if(!val.equals(user.getPassword())){
            return true;
        }
        return false;
    }
    private Boolean isPhoneChanged(String val){
        if(!val.equals(user.getPhone())){
            return true;
        }
        return false;
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(EditProfile.this, Login.class));
        }
    }
}