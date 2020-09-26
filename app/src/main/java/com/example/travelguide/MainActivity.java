package com.example.travelguide;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.ActivityNotFoundException;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Address;
import android.location.Geocoder;
import android.location.Location;
import android.net.Uri;
import android.os.Bundle;
import android.text.Html;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import com.google.android.gms.common.api.ApiException;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.CameraPosition;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.OnSuccessListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;


import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;

import static android.Manifest.permission.ACCESS_FINE_LOCATION;
import static android.text.TextUtils.substring;

public class MainActivity extends AppCompatActivity implements OnMapReadyCallback, GoogleMap.OnInfoWindowClickListener {

    ArrayList<Totem> totems = new ArrayList<>();

    private static final String TAG = "TotemGuide";
    private double latC, lonC;
    Button getLo;
    TextView lat;
    TextView lon;

    FusedLocationProviderClient fusedLocationProviderClient;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Toolbar toolbar = findViewById(R.id.toolbar);
        toolbar.setTitle("Vancouver Totem Guide");
        setSupportActionBar(toolbar);

        try {
            loadTotemsJSON();
        } catch (JSONException e) {
            Log.d("xx", "failure");

            e.printStackTrace();
        }

//        getLo = findViewById(R.id.getCurLocation);
//        lat = findViewById(R.id.latitude);
//        lon = findViewById(R.id.longitude);
//
          fusedLocationProviderClient = LocationServices.getFusedLocationProviderClient(this);
//
//        getLo.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View view) {
//                //check permissions
//                if (ActivityCompat.checkSelfPermission(MainActivity.this, ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
//                    //when permission granted
//                    getLocation();
//
//                } else {
//                    //when denied
//                    ActivityCompat.requestPermissions(MainActivity.this, new String[]{ACCESS_FINE_LOCATION}, 44);
//                }
//            }
//        });
        SupportMapFragment mapFragment = (SupportMapFragment) getSupportFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

    }

    @SuppressLint("MissingPermission")
    @Override
    public void onMapReady(GoogleMap map) {
        for(Totem t : totems)
        {
            Log.i("cc", t.getSiteName()+ t.getLonC() + " " +  t.getLatC());

            Marker totem = map.addMarker(new MarkerOptions()
                    .position(new LatLng(t.getLonC(),t.getLatC()))
                    .title(t.getSiteName()).snippet("URL for image:" + t.getImageURL()));
            totem.showInfoWindow();

        }
        map.setOnInfoWindowClickListener(this);

            fusedLocationProviderClient.getLastLocation()
                    .addOnSuccessListener(this, new OnSuccessListener<Location>() {
                        @Override
                        public void onSuccess(Location location) {
                            // Got last known location. In some rare situations this can be null.
                            if (location != null) {
                                // Logic to handle location object
                                double lat, lon;
                                lat = location.getLatitude();
                                lon = location.getLongitude();
                                Log.i(TAG + "here", String.valueOf(lat));
                                Log.i(TAG + "here", String.valueOf(lon));
                                LatLng latLng = new LatLng(lat, lon);

                                CameraPosition cameraPosition = new CameraPosition.Builder()
                                        .target(latLng)
                                        .zoom(20)
                                        .build();
                                map.moveCamera(CameraUpdateFactory.newLatLngZoom(new LatLng(lat,lon), 4.0f));
                                map.animateCamera( CameraUpdateFactory.zoomTo( 10.0f ) );

                                Marker loc = map.addMarker(new MarkerOptions()
                                    .position(new LatLng(location.getLatitude(),location.getLongitude()))
                                    .title("Marker").snippet("Current Phone Location")
                                );
                                loc.showInfoWindow();
                            }
                        }
                    });

    }

    @SuppressLint("MissingPermission")
    private void getLocation() {
        fusedLocationProviderClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {

                //initialize location
                Location location = task.getResult();
                if(location!= null){

                    try {
                        //geocoder
                        Geocoder geocoder = new Geocoder(MainActivity.this, Locale.getDefault());
                        List<Address> addresses = geocoder.getFromLocation(location.getLatitude(), location.getLongitude(), 1);
                        lat.setText(Html.fromHtml("<font color= '#6200EE'><b> latitude : </b><br></font>" + addresses.get(0).getLatitude()));
                        lon.setText(Html.fromHtml("<font color= '#6200EE'><b> longitude : </b><br></font>" + addresses.get(0).getLongitude()));

                        latC = addresses.get(0).getLatitude();
                        lonC = addresses.get(0).getLongitude();
                        Log.i(TAG, String.valueOf(latC));
                        Log.i(TAG, String.valueOf(lonC));

                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        });

    }

    public void loadTotemsJSON() throws JSONException {
        String jsonFile = loadJSONFromAsset(this);
        JSONArray arr = new JSONArray(jsonFile);

        for(int i=0;i<arr.length();i++){
            JSONObject obj = arr.getJSONObject(i);
            JSONObject fields = (JSONObject) obj.get("fields");

            if(fields.get("status").equals("Removed")){
                continue;
            }
            if(obj.get("recordid").equals("1fb74d9af3ff8af8193ca970fa1b1cc1d97254a3")){
                continue;
            }
            if(obj.get("recordid").equals("1933a0c4656dbd94026a5906d58a16b928da9134")){
                continue;
            }

            Log.i("w", String.valueOf(fields.get("sitename")));
            JSONObject coor = (JSONObject) fields.get("geom");

            Log.i("w", String.valueOf(fields.get("url")));
            JSONArray co = (JSONArray) coor.get("coordinates");

            Log.i("w", String.valueOf(co.get(0)));
            Log.i("w", String.valueOf(co.get(1)));

            Totem t = new Totem((String) fields.get("sitename"), (double)co.get(0), (double)co.get(1), (String) fields.get("url"));
            totems.add(t);

            if(i == 21){
                break;
            }

        }
        Log.i("w", String.valueOf(totems.size()));

    }

    public String loadJSONFromAsset(Context context) {
        String json = null;
        try {
            InputStream is = context.getAssets().open("totems.json");
            int size = is.available();
            byte[] buffer = new byte[size];
            is.read(buffer);
            is.close();
            json = new String(buffer, "UTF-8");

        } catch (IOException ex) {
            ex.printStackTrace();
            return null;
        }
        return json;
    }

    @Override
    public void onInfoWindowClick(Marker marker) {
        String info = marker.getSnippet().substring(marker.getSnippet().indexOf(":") + 1);
        Toast.makeText(this, "Info window clicked " + marker.getTitle() + info,
                Toast.LENGTH_SHORT).show();

        Log.i("y", info);

        Intent intent = new Intent(Intent.ACTION_VIEW,Uri.parse(info));
        intent.addFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
        intent.setPackage("com.android.chrome");
        try {
            startActivity(intent);
        } catch (ActivityNotFoundException e) {
            // Chrome is probably not installed
            intent.setPackage(null);
            startActivity(intent);
        }
    }
}