package com.example.mytestapp;

import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
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
import androidx.appcompat.widget.AppCompatButton;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;

import com.android.volley.AuthFailureError;
import com.android.volley.DefaultRetryPolicy;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.RetryPolicy;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
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
import com.google.android.gms.tasks.Task;
import com.google.firebase.auth.FirebaseAuth;
import com.google.firebase.auth.FirebaseUser;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.firebase.messaging.Constants;
import com.google.firebase.messaging.FirebaseMessaging;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class HomeFragment extends Fragment {

    // Init basic vars
    View view;
    Button sosButton;

    AppCompatButton add_new_hosp;

    // Init map vars
    private SupportMapFragment supportMapFragment;

    // locations
    private FusedLocationProviderClient fusedLocationProviderClient;
    private Location lastKnownLocation;
    private static final int CAMERA_ZOOM = 15;
    private MarkerOptions currentLocationMarker;

    private static final int PERMISSIONS_REQUEST_ALL = 100;

    FirebaseUser currentUser;
    FirebaseAuth mAuth;

    Hospital chosenHospital;

    String messageSMS, phoneCall;

    String[] permissions;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_home, container, false);
        mAuth = FirebaseAuth.getInstance();

        sosButton = view.findViewById(R.id.sos_button);

//        for(int i = 1; i <= 10; i++){
//            Hospital hospital = new Hospital("" + String.valueOf(new Double(i)),
//                    "" + String.valueOf(new Double(i)),
//                    "Hospital #" + String.valueOf(new Double(i)),
//                    "111111111",
//                    "http://www.site.kz",
//                    "Some detail text",
//                    "https://lh5.googleusercontent.com/p/AF1QipMIM5w3i-1MvIPyku_rYELOWT-x3E5t3HnIib_u=w408-h240-k-no-pi-0-ya87.05999-ro-0-fo100");
//
//            String id = (hospital.getLat() + " " + hospital.getLng()).replace(' ', 'b').replace('.', 'a');
//
//            final DatabaseReference reference = FirebaseDatabase.getInstance().getReference("Hospitals").child(id);
//            reference.setValue(hospital).addOnCompleteListener(new OnCompleteListener<Void>() {
//                @Override
//                public void onComplete(@NonNull Task<Void> task) {
//                    if(task.isSuccessful()){
//                        Toast.makeText(getContext(), "Hospitals added successfully", Toast.LENGTH_SHORT).show();
//                    }
//                    else{
//                        Toast.makeText(getContext(), "Hospital add error", Toast.LENGTH_SHORT).show();
//                    }
//                }
//            });
//        }

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
        messageSMS = "SOS! I need you right now!";
        if(lastKnownLocation != null){
            messageSMS = "This is SOS message! I need you right now here: \nhttps://maps.google.com/?q=" + String.valueOf(lastKnownLocation.getLatitude())
                    + "," + String.valueOf(lastKnownLocation.getLongitude());
        }

        ArrayList<String> mPhones = new ArrayList<>();
        if((ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.SEND_SMS)
                == PackageManager.PERMISSION_GRANTED)){
            final DatabaseReference databaseReference = FirebaseDatabase.getInstance().getReference("Contacts");
            databaseReference.addListenerForSingleValueEvent(new ValueEventListener() {
                @Override
                public void onDataChange(@NonNull DataSnapshot snapshot) {
                    for(DataSnapshot childSnaphsot: snapshot.child(currentUser.getUid()).getChildren()){
                        Contact contact = childSnaphsot.getValue(Contact.class);
                        mPhones.add(contact.getPhone());
                    }
                    for(String phone: mPhones){
                        SmsManager smsManager = SmsManager.getDefault();
                        smsManager.sendTextMessage(phone, null, messageSMS, null, null);
                    }
                    if(mPhones.size() > 0){
                        Toast.makeText(getContext(), "All messages delivered successfully", Toast.LENGTH_SHORT).show();
                    }
                    else{
                        Toast.makeText(getContext(), "No contacts to send SOS messages", Toast.LENGTH_SHORT).show();
                    }
                }

                @Override
                public void onCancelled(@NonNull DatabaseError error) {

                }
            });
        }
        else{
            Toast.makeText(getContext(), "Permission required to send messages", Toast.LENGTH_SHORT).show();
        }

        phoneCall = "112";
        if((ContextCompat.checkSelfPermission(getActivity(),
                Manifest.permission.CALL_PHONE)
                == PackageManager.PERMISSION_GRANTED)){
            String phoneNum = "tel:" + phoneCall;
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

        DatabaseReference hospitalReference = FirebaseDatabase.getInstance().getReference("Hospitals");
        hospitalReference.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot snapshot) {
                chosenHospital = snapshot.child(hospitalID).getValue(Hospital.class);
                System.out.println("chosen hosp: " + snapshot.child(hospitalID));

                Intent intent = new Intent(getContext(), HospitalDetail.class);

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

//                    DirectionsApiRequest

                    Marker marker = googleMap.addMarker(
                            new MarkerOptions()
                                    .position(currentLatLng)
                                    .title(currentHospital.getName())
                                    .icon(BitmapDescriptorFactory.defaultMarker(BitmapDescriptorFactory.HUE_AZURE)));
                    marker.showInfoWindow();


                }

            }

            @Override
            public void onCancelled(@NonNull DatabaseError error) {
                Toast.makeText(getContext(), "Error: " + error.getMessage(), Toast.LENGTH_SHORT).show();
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
