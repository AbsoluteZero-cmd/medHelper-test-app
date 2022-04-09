package com.example.mytestapp;

import static android.app.Activity.RESULT_OK;

import android.app.ProgressDialog;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.os.Handler;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.UploadTask;
import com.squareup.picasso.Picasso;


public class ProfileFragment extends Fragment {

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference userReference;
    private StorageReference storageReference;

    private Uri imageUri;
    private ProgressDialog progressDialog;

    private static final int GET_IMAGE_REQUEST = 1;

    User user;

    TextView profileFullName, profileEmail, profilePhone, profileDate, signOutBtn, trustedContactsBtn;
    ImageView profileImage;

    FloatingActionButton editProfileBtn;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View mView = inflater.inflate(R.layout.fragment_profile, container, false);

        mAuth = FirebaseAuth.getInstance();
        userReference = FirebaseDatabase.getInstance().getReference("Users");
        storageReference = FirebaseStorage.getInstance().getReference("Images");

        profileFullName = mView.findViewById(R.id.profile_fullname);
        profileEmail = mView.findViewById(R.id.profile_email);
        profilePhone = mView.findViewById(R.id.profile_phone);
        profileDate = mView.findViewById(R.id.profile_date);
        profileImage = mView.findViewById(R.id.profile_image);
        signOutBtn = mView.findViewById(R.id.sign_out_button);
        trustedContactsBtn = mView.findViewById(R.id.trust_contacts_button);
        editProfileBtn = mView.findViewById(R.id.profile_edit_button);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading page...");
        progressDialog.show();

        getData();

        trustedContactsBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), TrustedContacts.class));
            }
        });

        signOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mAuth.signOut();
                startActivity(new Intent(getContext(), Login.class));
            }
        });

        editProfileBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), EditProfile.class));
            }
        });

        profileImage.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImageChooser();
                System.out.println(imageUri);
            }
        });

        return mView;
    }


    private void openImageChooser() {
        Intent intent = new Intent();
        intent.setType("image/*");
        intent.setAction(Intent.ACTION_GET_CONTENT);
        startActivityForResult(intent, GET_IMAGE_REQUEST);
    }

    @Override
    public void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);
        if (requestCode == GET_IMAGE_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            imageUri = data.getData();

            Picasso.get()
                    .load(imageUri)
                    .fit()
                    .centerCrop()
                    .into(profileImage);
            uploadImage();
        }
    }

    private void uploadImage() {
        if(imageUri != null){
            final ProgressDialog progressDialog = new ProgressDialog(getContext());
            progressDialog.setMessage("Uploading image...");
            progressDialog.show();

            final StorageReference imageReference = storageReference.child(currentUser.getUid());
            imageReference.putFile(imageUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Upload successful!", Toast.LENGTH_LONG).show();

                            imageReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();
                                    userReference.child(currentUser.getUid()).child("imageUri").setValue(url);
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            progressDialog.dismiss();
                            Toast.makeText(getContext(), "Upload failed", Toast.LENGTH_LONG).show();
                        }
                    })
                    .addOnProgressListener(new OnProgressListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onProgress(@NonNull UploadTask.TaskSnapshot snapshot) {
                            double progressPercentage = (100.0 * snapshot.getBytesTransferred() / snapshot.getTotalByteCount());
                            progressDialog.setProgress((int) progressPercentage);
                        }
                    });
        }
        else{
            Toast.makeText(getContext(), "No image selected", Toast.LENGTH_SHORT).show();
        }
    }

    private void getData() {
        userReference.addListenerForSingleValueEvent(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                user = snapshot.child(currentUser.getUid()).getValue(User.class);

                profileFullName.setText(profileFullName.getText().toString() + user.getFullName());
                profileEmail.setText(profileEmail.getText().toString() + user.getEmail());
                profilePhone.setText(profilePhone.getText().toString() + user.getPhone());
                profileDate.setText(profileDate.getText().toString() + user.getDate());

                String currentUri = user.getImageUri();
                System.out.println("My current uri is " + currentUri);
                if(currentUri != null){
                    Picasso.get()
                            .load(currentUri)
//                            .placeholder(R.mipmap.ic_launcher)
                            .fit()
                            .centerCrop()
                            .into(profileImage);
                }
                else{
                    Picasso.get()
//                            .load("http://i.imgur.com/DvpvklR.png")
                            .load("https://www.parkamerica.net/wp-content/uploads/2020/12/placeholder-profile-female.jpg")
//                            .placeholder(R.mipmap.ic_launcher)
                            .fit()
                            .centerCrop()
                            .into(profileImage);
                }

                progressDialog.dismiss();
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
                progressDialog.dismiss();
            }
        });
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(getContext(), Login.class));
        }
    }
}
