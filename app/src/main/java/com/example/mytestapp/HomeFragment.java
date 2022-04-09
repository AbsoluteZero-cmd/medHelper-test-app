package com.example.mytestapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.telephony.SmsManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.location.places.PlaceDetectionClient;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapView;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
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

import java.io.IOException;
import java.net.URLEncoder;
import java.util.Locale;
import java.util.UUID;

import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

public class HomeFragment extends Fragment {

    // Init basic vars
    View view;
    AppCompatButton addBtn;
    Button sosButton;

    // Init map vars
    private MapView mapView;
    private GoogleMap googleMap;
    private SupportMapFragment supportMapFragment;

    // locations
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private static final int CAMERA_ZOOM = 15;
    private MarkerOptions currentLocationMarker;

    // send sms
    private static final int PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION = 1;
    private static final int PERMISSIONS_REQUEST_SEND_SMS = 2;
    private static final int PERMISSIONS_REQUEST_ALL = 100;
    private boolean locationPermissionGranted;
    private boolean sendSmsPermissionGranted;
    private boolean phoneCallPermissionGranted;

    FirebaseUser currentUser;
    FirebaseAuth mAuth;

    Hospital chosenHospital;

    String messageSMS, phoneSMS;

    String[] permissions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        mAuth = FirebaseAuth.getInstance();

        sosButton = view.findViewById(R.id.sos_button);

        sosButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                sendSosMessages();
            }
        });

        askAllPermissions();

        setUpMap();
        return view;
    }

    private void askAllPermissions() {
        permissions = new String[]{
                Manifest.permission.SEND_SMS,
                Manifest.permission.ACCESS_FINE_LOCATION,
                Manifest.permission.ACCESS_COARSE_LOCATION,
                Manifest.permission.CALL_PHONE,
        };

        if(!hasPermissions(getContext(), permissions)){
            ActivityCompat.requestPermissions(getActivity(), permissions, PERMISSIONS_REQUEST_ALL);
        }
    }

    private boolean hasPermissions(Context context, String[] permissions) {
        if(context != null && permissions != null){
            for(String permission: permissions){
                if(ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED){
                    return false;
                }
            }
        }

        return true;
    }

    private void sendSosMessages() {
        phoneSMS = "1234567890";
        messageSMS = "SOS! I need you right now!";
        if(lastKnownLocation != null){
            messageSMS = "This is SOS message! I have to see you here: \nhttps://maps.google.com/?q=" + String.valueOf(lastKnownLocation.getLatitude())
                    + "," + String.valueOf(lastKnownLocation.getLongitude());
        }

        if((ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED)){
            SmsManager smsManager = SmsManager.getDefault();
            smsManager.sendTextMessage(phoneSMS, null, messageSMS, null, null);
            Toast.makeText(getContext(), "All messages delivered successfully", Toast.LENGTH_SHORT).show();
        }
        else{
            Toast.makeText(getContext(), "Permission required to send messages", Toast.LENGTH_SHORT).show();
        }

        if((ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED)){
            String phoneNum = "tel:" + phoneSMS;
            startActivity(new Intent(Intent.ACTION_CALL, Uri.parse(phoneNum)));
        }
        else{
            Toast.makeText(getContext(), "Permission required to phone call", Toast.LENGTH_SHORT).show();
        }
    }

    private void setUpMap() {
        supportMapFragment = (SupportMapFragment)
                getChildFragmentManager().findFragmentById(R.id.google_map);

        fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(getContext());

        getDeviceLocation();

        supportMapFragment.getMapAsync(new OnMapReadyCallback() {
            @Override
            public void onMapReady(GoogleMap googleMap) {

                googleMap.setOnMarkerClickListener(new GoogleMap.OnMarkerClickListener() {
                    @Override
                    public boolean onMarkerClick(@NonNull Marker marker) {

                        if(marker.getPosition().latitude == currentLocationMarker.getPosition().latitude
                            && marker.getPosition().longitude == currentLocationMarker.getPosition().longitude){
                            System.out.println("Pos is same");
                        }
                        else{
                            chosenHospital = null;
                            getCurrentHospitalByMarker(marker);
                        }
                        return false;
                    }
                });
            }
        });
    }

    private void getCurrentHospitalByMarker(Marker marker) {
        String hospitalLat = String.valueOf(marker.getPosition().latitude);
        String hospitalLng = String.valueOf(marker.getPosition().longitude);
        String hospitalID = (hospitalLat + " " + hospitalLng).replace('.', 'a').replace(' ', 'b');
//        Toast.makeText(getContext(), hospitalID, Toast.LENGTH_SHORT).show();

        DatabaseReference hospitalReference = FirebaseDatabase.getInstance().getReference("Hospitals");
        hospitalReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chosenHospital = snapshot.child(hospitalID).getValue(Hospital.class);
                System.out.println("hosp: " + chosenHospital.getName());

                Intent intent = new Intent(getContext(), HospitalDetail.class);
//                intent.putExtra("currentHospitalName", chosenHospital.getName());

                intent.putExtra("my_obj", chosenHospital);

                startActivity(intent);
            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });
    }

    private void getDeviceLocation() {
        try {
            if (ContextCompat.checkSelfPermission(getActivity(),
                    android.Manifest.permission.ACCESS_FINE_LOCATION)
                    == PackageManager.PERMISSION_GRANTED) {
                Task<Location> locationResult  = fusedLocationProviderClient.getLastLocation();

                locationResult.addOnCompleteListener(getActivity(), new OnCompleteListener<Location>() {
                    @Override
                    public void onComplete(@NonNull Task<Location> task) {
                        if(task.isSuccessful()){
                            lastKnownLocation = task.getResult();
                            if(lastKnownLocation != null){
                                currentLocationMarker = new MarkerOptions();
                                currentLocationMarker.position(new LatLng(lastKnownLocation.getLatitude(),
                                        lastKnownLocation.getLongitude()));
                                currentLocationMarker.title("My current location");
                                supportMapFragment.getMapAsync(new OnMapReadyCallback() {
                                    @Override
                                    public void onMapReady(GoogleMap googleMap) {
                                        googleMap.clear();
                                        putHospitalsMarkers(googleMap);

                                        googleMap.animateCamera(CameraUpdateFactory.newLatLngZoom(currentLocationMarker.getPosition(),CAMERA_ZOOM));
                                        googleMap.addMarker(currentLocationMarker);
                                    }
                                });
                            }
                        }
                    }
                });
            }
        } catch (SecurityException e)  {
            Toast.makeText(getContext(), "Error: " + e.getMessage(), Toast.LENGTH_SHORT).show();
        }
    }

    private void putHospitalsMarkers(GoogleMap googleMap) {
        DatabaseReference hospitalReference = FirebaseDatabase.getInstance().getReference("Hospitals");
        hospitalReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                for(DataSnapshot childSnapshot: snapshot.getChildren()){
                    Hospital currentHospital = childSnapshot.getValue(Hospital.class);

                    final LatLng currentLatLng = new LatLng(Double.parseDouble(currentHospital.getLat()),
                            Double.parseDouble(currentHospital.getLng()));
                    final LatLng userLocation = new LatLng(lastKnownLocation.getLatitude(), lastKnownLocation.getLongitude());

                    Marker marker = googleMap.addMarker(
                            new MarkerOptions()
                                    .position(currentLatLng)
                                    .title(currentHospital.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE))
                                    .snippet("Some snippet"));
                    marker.showInfoWindow();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
            }
        });

    }

    private void checkLocationPermission() {
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                android.Manifest.permission.ACCESS_FINE_LOCATION)
                == PackageManager.PERMISSION_GRANTED) {
            locationPermissionGranted = true;
        }
        else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{android.Manifest.permission.ACCESS_FINE_LOCATION},
                    PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION);
        }
    }

    private void checkSmsPermission() {
        if (ContextCompat.checkSelfPermission(this.getActivity(),
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            sendSmsPermissionGranted = true;
        }
        else {
            ActivityCompat.requestPermissions(getActivity(),
                    new String[]{Manifest.permission.SEND_SMS},
                    PERMISSIONS_REQUEST_SEND_SMS);
        }
    }

//    @Override
//    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
//        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
//        System.out.println("I am here");
//        switch (requestCode){
//            case PERMISSIONS_REQUEST_SEND_SMS: {
//                if(grantResults.length > 0
//                    && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    sendSmsPermissionGranted = true;
//                    Toast.makeText(getContext(), "Permission granted, try again", Toast.LENGTH_SHORT).show();
//
//                }
//            }
//            case PERMISSIONS_REQUEST_ACCESS_FINE_LOCATION: {
//                if(grantResults.length > 0
//                        && grantResults[0] == PackageManager.PERMISSION_GRANTED){
//                    locationPermissionGranted = true;
//                    Toast.makeText(getContext(), "Permission granted, try again", Toast.LENGTH_SHORT).show();
//                }
//            }
//        }
//
//    }

    @Override
    public void onStart() {
        super.onStart();
        currentUser = mAuth.getCurrentUser();
        if(currentUser == null){
            startActivity(new Intent(getContext(), Login.class));
        }
    }
}
