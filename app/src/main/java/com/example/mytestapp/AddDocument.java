package com.example.mytestapp;

import android.app.ProgressDialog;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.provider.OpenableColumns;
import android.view.View;
import android.webkit.MimeTypeMap;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.OnProgressListener;
import com.google.firebase.storage.StorageReference;
import com.google.firebase.storage.StorageTask;
import com.google.firebase.storage.UploadTask;

import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.UUID;

public class AddDocument extends AppCompatActivity {

    private static final int GET_DOCUMENT_REQUEST = 1;

    AppCompatButton chooseDocBtn, chooseImgBtn, uploadFile;
    EditText docTitle, docInfo;
    TextView docFileName;
    ProgressDialog progressDialog;

    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private Uri documentUri;

    private StorageTask mUploadTask;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_add_document);

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Documents");
        storageReference = FirebaseStorage.getInstance().getReference("Documents");

        chooseDocBtn = findViewById(R.id.choose_document_button);
        chooseImgBtn = findViewById(R.id.choose_image_button);
        uploadFile = findViewById(R.id.upload_file_button);
        docTitle = findViewById(R.id.document_title);
        docInfo = findViewById(R.id.document_info);
        docFileName = findViewById(R.id.document_file_name);

        progressDialog = new ProgressDialog(getApplicationContext());
        progressDialog.setMessage("Changing data...");

//        docFileName.setVisibility(View.INVISIBLE);

        chooseDocBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openDocChooser();
            }
        });

        chooseImgBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openImgChooser();
            }
        });

        uploadFile.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                if (mUploadTask != null && mUploadTask.isInProgress()) {
                    Toast.makeText(getApplicationContext(), "Upload in progress", Toast.LENGTH_SHORT).show();
                }
                else{
                    uploadDocument();
                }
            }
        });
    }

    private void openDocChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("application/*");
        startActivityForResult(intent, GET_DOCUMENT_REQUEST);
    }

    private void openImgChooser() {
        Intent intent = new Intent(Intent.ACTION_GET_CONTENT);
        intent.addCategory(Intent.CATEGORY_OPENABLE);
        intent.setType("image/*");
        startActivityForResult(intent, GET_DOCUMENT_REQUEST);
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == GET_DOCUMENT_REQUEST && resultCode == RESULT_OK
                && data != null && data.getData() != null) {
            documentUri = data.getData();

            docFileName.setText(docFileName.getText() + queryName(getContentResolver(), documentUri));
        }
    }

    private void uploadDocument() {
        if (documentUri != null) {
            SimpleDateFormat sdf = new SimpleDateFormat("yyyy.MM.dd HH:mm");
            Date currentTime = Calendar.getInstance().getTime();

            String mTitle = docTitle.getText().toString().trim();
            String mInfo = docInfo.getText().toString().trim();
            String mDate = sdf.format(currentTime);

            String key = UUID.randomUUID().toString();
            final StorageReference documentReference = storageReference.child(currentUser.getUid()).child(key);
            mUploadTask = documentReference.putFile(documentUri)
                    .addOnSuccessListener(new OnSuccessListener<UploadTask.TaskSnapshot>() {
                        @Override
                        public void onSuccess(UploadTask.TaskSnapshot taskSnapshot) {
                            Toast.makeText(AddDocument.this, "Upload successful!", Toast.LENGTH_LONG).show();

                            documentReference.getDownloadUrl().addOnSuccessListener(new OnSuccessListener<Uri>() {
                                @Override
                                public void onSuccess(Uri uri) {
                                    String url = uri.toString();

                                    Document document = new Document(mTitle, mInfo, mDate, url, MimeTypeMap.getFileExtensionFromUrl(documentUri.toString()), key);
                                    if(document.getTitle().isEmpty()){
                                        document.setTitle("No title");
                                    }
                                    if(document.getInfo().isEmpty()){
                                        document.setInfo("No info");
                                    }
                                    databaseReference.child(currentUser.getUid()).child(key).setValue(document);

                                    startActivity(new Intent(AddDocument.this, MainActivity.class));
                                }
                            });
                        }
                    })
                    .addOnFailureListener(new OnFailureListener() {
                        @Override
                        public void onFailure(@NonNull Exception e) {
                            Toast.makeText(AddDocument.this, "Upload failed", Toast.LENGTH_LONG).show();
                        }
                    });
        } else {
            Toast.makeText(this, "No file selected", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, Login.class));
        }
    }

    private String queryName(ContentResolver resolver, Uri uri) {
        Cursor returnCursor =
                resolver.query(uri, null, null, null, null);
        assert returnCursor != null;
        int nameIndex = returnCursor.getColumnIndex(OpenableColumns.DISPLAY_NAME);
        returnCursor.moveToFirst();
        String name = returnCursor.getString(nameIndex);
        returnCursor.close();
        return name;
    }
}