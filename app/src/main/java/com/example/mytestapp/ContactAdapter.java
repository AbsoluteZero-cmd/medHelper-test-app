package com.example.mytestapp;

import android.content.Context;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;

import java.util.List;

public class ContactAdapter extends RecyclerView.Adapter<ContactAdapter.ContactViewHolder> {
    public Uri currentUri;
    public FirebaseUser currentUser;
    private Context mContext;
    private List<Contact> mContacts;

    public ContactAdapter(Context context, List<Contact> contacts, FirebaseAuth mAuth){
        mContext = context;
        mContacts = contacts;

        currentUser = mAuth.getCurrentUser();
    }

    @NonNull
    @Override
    public ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(mContext).inflate(R.layout.contact_item, parent, false);
        return new ContactAdapter.ContactViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ContactViewHolder holder, int position) {
        Contact contactCurrent = mContacts.get(position);

        holder.contactName.setText("Name: " + contactCurrent.getName());
        holder.contactPhone.setText("Phone number: " + contactCurrent.getPhone());
    }

    @Override
    public int getItemCount() {
        return mContacts.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {

        public TextView contactName, contactPhone;
        public ImageButton deleteContact;

        public ContactViewHolder(@NonNull View itemView) {
            super(itemView);

            contactName = itemView.findViewById(R.id.contact_name);
            contactPhone = itemView.findViewById(R.id.contact_phone);
            deleteContact = itemView.findViewById(R.id.delete_contact);
        }
    }

    public void clear() {
        int size = mContacts.size();
        mContacts.clear();
        notifyItemRangeRemoved(0, size);
    }
}
