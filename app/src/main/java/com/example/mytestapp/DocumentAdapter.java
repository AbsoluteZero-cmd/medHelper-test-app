package com.example.mytestapp;

import android.app.DownloadManager;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.widget.AppCompatButton;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.gms.tasks.OnFailureListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.storage.FirebaseStorage;
import com.google.firebase.storage.StorageReference;

import java.util.List;
import java.util.UUID;

public class DocumentAdapter extends RecyclerView.Adapter<DocumentAdapter.DocumentViewHolder> {
    public Uri currentUri;
    public FirebaseUser currentUser;
    private Context mContext;
    private List<Document> mDocuments;

    public DocumentAdapter(Context context, List<Document> documents, FirebaseAuth mAuth) {
        mContext = context;
        mDocuments = documents;

        currentUser = mAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public DocumentViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.document_item, parent, false);
        return new DocumentViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DocumentViewHolder holder, int position) {
        Document documentCurrent = mDocuments.get(position);
        holder.docTitle.setText(documentCurrent.getTitle());
        holder.docInfo.setText(documentCurrent.getInfo());
        holder.docDate.setText(documentCurrent.getDate());

        currentUri = Uri.parse(documentCurrent.getDocumentUri());
        System.out.println("My current uri: " + currentUri.toString());

        holder.downloadBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                DownloadManager.Request request = new DownloadManager.Request(currentUri);
                String mimeType = mContext.getContentResolver().getType(currentUri);
                request.setMimeType(mimeType);

                System.out.println("My mimetype : " + mimeType);

                request.allowScanningByMediaScanner();
                request.setAllowedOverMetered(true);
                request.setNotificationVisibility(DownloadManager.Request.VISIBILITY_VISIBLE_NOTIFY_COMPLETED);
                request.setDestinationInExternalPublicDir(Environment.DIRECTORY_DOWNLOADS, documentCurrent.getTitle() + "_" + UUID.randomUUID().toString() + "." + documentCurrent.getDocumentExtension());
                DownloadManager dm = (DownloadManager) mContext.getSystemService(Context.DOWNLOAD_SERVICE);
                dm.enqueue(request);
            }
        });

        holder.deleteBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                final StorageReference storageReference = FirebaseStorage.getInstance().getReference("Documents").child(currentUser.getUid()).child(documentCurrent.getFileName());
                System.out.println(documentCurrent.getFileName());

                storageReference.delete().addOnSuccessListener(new OnSuccessListener<Void>() {
                    @Override
                    public void onSuccess(Void unused) {
                        final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Documents").child(currentUser.getUid()).child(documentCurrent.getFileName());
                        databaseReference.setValue(null);
                        Toast.makeText(mContext, "Document successfully deleted", Toast.LENGTH_SHORT).show();

                        mContext.startActivity(new Intent(mContext, MainActivity.class));
                    }
                })
                .addOnFailureListener(new OnFailureListener() {
                    @Override
                    public void onFailure(@NonNull Exception e) {
                        Toast.makeText(mContext, "Delete error : " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                });
            }
        });
    }

    @Override
    public int getItemCount() {
        return mDocuments.size();
    }

    public class DocumentViewHolder extends RecyclerView.ViewHolder {

        public TextView docTitle, docInfo, docDate;
        public AppCompatButton downloadBtn;
        public ImageButton deleteBtn;

        public DocumentViewHolder(@NonNull View itemView) {
            super(itemView);

            docTitle = itemView.findViewById(R.id.doc_title);
            docInfo = itemView.findViewById(R.id.doc_info);
            docDate = itemView.findViewById(R.id.doc_date);
            downloadBtn = itemView.findViewById(R.id.download_button);
            deleteBtn = itemView.findViewById(R.id.delete_document);
        }
    }
}
