package com.example.mytestapp;

import android.app.ProgressDialog;
import android.content.Intent;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.ArrayList;
import java.util.List;

public class DocumentsFragment extends Fragment {

    View view;
    private DatabaseReference databaseReference;
    private StorageReference storageReference;
    private FirebaseAuth mAuth;
    private FirebaseUser currentUser;
    private RecyclerView recyclerView;
    private DocumentAdapter adapter;
    private FloatingActionButton documentAddBtn;
    private List<Document> mDocuments;

    private ProgressDialog progressDialog;
    private TextView noDocumentsText;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        mAuth = FirebaseAuth.getInstance();
        databaseReference = FirebaseDatabase.getInstance().getReference("Documents");
        storageReference = FirebaseStorage.getInstance().getReference("Documents");

        view = inflater.inflate(R.layout.fragment_documents, container, false);

        progressDialog = new ProgressDialog(getContext());
        progressDialog.setMessage("Loading page...");
        progressDialog.show();

        documentAddBtn = view.findViewById(R.id.document_add_btn);
        recyclerView = view.findViewById(R.id.recycler_view);
        noDocumentsText = view.findViewById(R.id.no_documents_text);

        getData();

        recyclerView.setHasFixedSize(true);
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));

        mDocuments = new ArrayList<>();

        documentAddBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                startActivity(new Intent(getContext(), AddDocument.class));
            }
        });

        return view;
    }

    private void getData() {
        databaseReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for (DataSnapshot postSnapshot : snapshot.child(currentUser.getUid()).getChildren()) {
                    Document document = postSnapshot.getValue(Document.class);
                    mDocuments.add(document);
                }

                adapter = new DocumentAdapter(getContext(), mDocuments, mAuth);
                recyclerView.setAdapter(adapter);
                if(mDocuments.isEmpty()){
                    noDocumentsText.setVisibility(View.VISIBLE);
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
        if (currentUser == null) {
            startActivity(new Intent(getContext(), Login.class));
        }
    }
}
