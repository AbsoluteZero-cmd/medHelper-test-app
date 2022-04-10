package com.example.mytestapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.content.ClipData;
import android.content.ClipboardManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.maps.model.LatLng;
import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.util.List;
import java.util.Locale;

public class HospitalDetail extends AppCompatActivity {

    private Hospital currentHospital;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_hospital_detail);

        // Init views
        TextView hospitalName = findViewById(R.id.hospital_name);
        TextView hospitalAddress = findViewById(R.id.hospital_address);
        TextView hospitalInfo = findViewById(R.id.hospital_info);
        TextView hospitalPhone = findViewById(R.id.hospital_phone);
        ImageView imageView = findViewById(R.id.image_view);

        Button phoneButton = findViewById(R.id.hospital_phone_button);
        Button webButton = findViewById(R.id.hospital_web_button);
        ImageButton copyPhoneBtn = findViewById(R.id.copy_phone_button);

        currentHospital = (Hospital) getIntent().getSerializableExtra("my_obj");
        hospitalName.setText(hospitalName.getText() + currentHospital.getName());
        hospitalInfo.setText(hospitalInfo.getText() + currentHospital.getDetailText());
        hospitalPhone.setText(hospitalPhone.getText() + currentHospital.getPhone());

        Picasso.get()
                .load(currentHospital.getImageUri())
                .fit()
                .centerCrop()
                .into(imageView);

        try {
            String hospitalAddressText = getFormattedAddress(currentHospital.getLat(), currentHospital.getLng());
            hospitalAddress.setText(hospitalAddress.getText() + hospitalAddressText);
        } catch (IOException e) {
            e.printStackTrace();
        }

        phoneButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                makePhoneCall();
            }
        });

        copyPhoneBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                copyPhoneNumber();
            }
        });

        webButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                openWeb();
            }
        });
    }

    private void openWeb() {
        Intent webIntent = new Intent(Intent.ACTION_VIEW, Uri.parse(currentHospital.getWebAddress()));
        startActivity(webIntent);
    }

    private void copyPhoneNumber() {
        ClipboardManager clipboard = (ClipboardManager) getSystemService(Context.CLIPBOARD_SERVICE);
        ClipData clip = ClipData.newPlainText("phone number", currentHospital.getPhone());
        clipboard.setPrimaryClip(clip);

        Toast.makeText(this, "The phone copied successfully", Toast.LENGTH_SHORT).show();
    }

    private void makePhoneCall() {
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.CALL_PHONE) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.CALL_PHONE}, 1);
        }
        else {
                String phoneNum = "tel:" + currentHospital.getPhone();
                startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(phoneNum)));
            }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == 1) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                makePhoneCall();
            } else {
                Toast.makeText(this, "Permission denied", Toast.LENGTH_SHORT).show();
            }
        }
    }

    private String getFormattedAddress(String lat, String lng) throws IOException {
        String address = "";

        Geocoder geocoder = new Geocoder(this);
        List<Address> addresses = geocoder.getFromLocation(Double.parseDouble(lat), Double.parseDouble(lng), 1);
        address = addresses.get(0).getAddressLine(0);
        return address;
    }
}