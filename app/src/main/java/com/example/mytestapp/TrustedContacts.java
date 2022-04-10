package com.example.mytestapp;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import android.app.Dialog;
import android.app.ProgressDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.util.AttributeSet;
import android.view.View;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;
import android.widget.Toast;

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
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrustedContacts extends AppCompatActivity {

    private DatabaseReference databaseReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;

    private FloatingActionButton documentAddBtn;
    private RecyclerView recyclerView;
    private TextView noContactsText;
    private FloatingActionButton addContactBtn;

    private List<Contact> mContacts;
    private ContactAdapter adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_trusted_contacts);

        final SwipeRefreshLayout pullToRefresh = findViewById(R.id.pull_to_refresh);
        pullToRefresh.setOnRefreshListener(new SwipeRefreshLayout.OnRefreshListener() {
            @Override
            public void onRefresh() {
                refreshData();
                pullToRefresh.setRefreshing(false);
            }
        });

        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Contacts");

        documentAddBtn = findViewById(R.id.document_add_btn);
        recyclerView = findViewById(R.id.recycler_view);
        noContactsText = findViewById(R.id.no_contacts_text);
        addContactBtn = findViewById(R.id.contact_add_btn);

        getData();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(this));

        mContacts = new ArrayList<>();

        addContactBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                addContactDialogue();
            }
        });
    }

    private void refreshData() {
        finish();
        startActivity(getIntent());
    }

    private void addContactDialogue() {
        Dialog dialog = new Dialog(this, R.style.Dialog);
        dialog.setContentView(R.layout.add_contact_dialogue_layout);
        dialog.setTitle("Add new contact");
        dialog.setCanceledOnTouchOutside(true);

        TextView addContactName = dialog.findViewById(R.id.add_contact_name);
        TextView addContactPhone = dialog.findViewById(R.id.add_contact_phone);

        AppCompatButton button = dialog.findViewById(R.id.add_contact_submit);
        button.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String mName = addContactName.getText().toString().trim();
                String mPhone = addContactPhone.getText().toString().trim();

                if(mName.isEmpty()){
                    addContactName.setError("Name shouldn't be empty");
                    addContactName.requestFocus();
                }
                else if(mPhone.isEmpty()){
                    addContactPhone.setError("Phone shouldn't be empty");
                    addContactPhone.requestFocus();
                }
                else{
                    final ProgressDialog progressDialog = new ProgressDialog(TrustedContacts.this);
                    progressDialog.setMessage("Uploading contact...");
                    progressDialog.show();

                    clearData();

                    Contact mContact = new Contact(mName, mPhone, UUID.randomUUID().toString());
                    databaseReference.child(currentUser.getUid()).child(mContact.getId()).setValue(mContact)
                            .addOnSuccessListener(new OnSuccessListener<Void>() {
                                @Override
                                public void onSuccess(Void unused) {
                                    Toast.makeText(TrustedContacts.this, "Contact uploaded successfully", Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                }
                            })
                            .addOnFailureListener(new OnFailureListener() {
                                @Override
                                public void onFailure(@NonNull Exception e) {
                                    Toast.makeText(TrustedContacts.this, "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                                    progressDialog.dismiss();
                                    dialog.dismiss();
                                }
                            });

                }
            }
        });

        dialog.show();
    }

    private void getData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                mContacts = new ArrayList<>();
                for (DataSnapshot postSnapshot : snapshot.child(currentUser.getUid()).getChildren()) {
                    Contact contact = postSnapshot.getValue(Contact.class);
                    mContacts.add(contact);
                }

                adapter = new ContactAdapter(getApplicationContext(), mContacts, mAuth);
                recyclerView.setAdapter(adapter);
                if(mContacts.isEmpty()){
                    noContactsText.setVisibility(View.VISIBLE);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getApplicationContext(), error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    @Override
    protected void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if (currentUser == null) {
            startActivity(new Intent(this, Login.class));
        }
    }

    public void clearData() {
        mContacts.clear();
        adapter.notifyDataSetChanged();
    }
}