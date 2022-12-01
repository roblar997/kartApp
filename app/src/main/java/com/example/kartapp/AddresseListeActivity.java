package com.example.kartapp;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.location.Location;
import android.location.LocationListener;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.util.TypedValue;
import android.view.MenuItem;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.LinearLayout;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.cardview.widget.CardView;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import com.google.android.gms.common.ConnectionResult;
import com.google.android.gms.common.api.GoogleApiClient;
import com.google.android.gms.location.FusedLocationProviderClient;
import com.google.android.gms.location.LocationCallback;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.location.LocationResult;
import com.google.android.gms.location.LocationServices;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.MapFragment;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.stream.Collectors;

public class AddresseListeActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,
        GoogleApiClient.OnConnectionFailedListener, MyDialog.DialogClickListener, LocationListener, OnMapReadyCallback {

    public static final String TAG = AddresseListeActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;
    private TextView textView;
    private List<String> addresser;
    String lokasjon;

    LatLng tmpPoint;
    LinearLayout addresseListe;

    public void visAddresserFunnet(LinearLayout layout) {

        for (String adresse : addresser) {

            CardView cardView = new CardView(this);
            cardView.setBackgroundColor(Color.WHITE);

            cardView.setContentPadding(10,10,10,10);

            LinearLayout layoutet = new LinearLayout(this);
            layoutet.setOrientation(LinearLayout.VERTICAL);

            layoutet.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));

            TextView tittelTekst = new TextView(this);
            tittelTekst.setText(adresse);

            tittelTekst.setTextColor(Color.BLACK);
            tittelTekst.setTextSize(TypedValue.COMPLEX_UNIT_SP, 32);
            tittelTekst.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.MATCH_PARENT,
                    LinearLayout.LayoutParams.MATCH_PARENT
            ));

            Button velg = new Button(this);

            velg.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            velg.setText("Velg");
            velg.setLayoutParams(new LinearLayout.LayoutParams(
                    LinearLayout.LayoutParams.WRAP_CONTENT,
                    LinearLayout.LayoutParams.WRAP_CONTENT
            ));
            velg.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view) {

                    Intent lagreIntent = new Intent(AddresseListeActivity.this, LagreActivity.class);
                    String lat = lokasjon.split(":")[0];
                    String lng = lokasjon.split(":")[1];
                    lagreIntent.putExtra("lat",lat);
                    lagreIntent.putExtra("lng",lng);
                    lagreIntent.putExtra("gateadresse",adresse);

                    startActivity(lagreIntent);

                }
            });
            layoutet.addView(tittelTekst);
            layoutet.addView(velg);
            cardView.addView(layoutet);
            layout.addView(cardView);
        }

    }

    private class GetAddressTask extends AsyncTask<Void, Void, List<String>> {
        JSONObject jsonObject;

        String lokasjon;

        public GetAddressTask(String lokasjon) {
            this.lokasjon = lokasjon;
        }

        @Override
        protected List<String> doInBackground(Void... params) {
            String s = "";
            String output = "";
            if(lokasjon == null)
                return new ArrayList<String>();

            String lat = lokasjon.split(":")[0].replaceAll(",", ".");
            String lng = lokasjon.split(":")[1].replaceAll(",", ".");
            String query = "https://maps.googleapis.com/maps/api/geocode/json?latlng=" + lat + "," + lng + "&key=AIzaSyA7kkXcZ4w6rEBFWJy2X0dWuMzC9g_rJjk";
            try {
                System.out.println("addressTask");
                URL urlen = new URL(query);
                HttpURLConnection conn = (HttpURLConnection) urlen.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "application/json");
                if (conn.getResponseCode() != 200) {

                    throw new RuntimeException("Failed : HTTP error code : "
                            + conn.getResponseCode());
                }

                BufferedReader br = new BufferedReader(new
                        InputStreamReader((conn.getInputStream())));
                while ((s = br.readLine()) != null) {
                    output = output + s;
                }
                jsonObject = new JSONObject(output.toString());
                conn.disconnect();
                String status = jsonObject.getString("status");
                if (!status.equals("OK"))
                    throw new JSONException("JSON status was not set to OK");
                JSONArray result = jsonObject.getJSONArray("results");
                List<String> toReturn = new ArrayList<String>();

                for (int i = 0; i < result.length(); i++) {
                    //Får gateaddresse, Postnummer, land
                    // hvis gateadresse.
                    String[] delerAvAddresse = result.getJSONObject(i).getString("formatted_address").split(",");
                    if(delerAvAddresse.length == 3){
                        toReturn.add(delerAvAddresse[0]);
                    }

                }
                return toReturn.stream().distinct().collect(Collectors.toList());
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            return new ArrayList<String>();
        }
    }
    @Override
    public boolean onOptionsItemSelected(@NonNull MenuItem item) {
        switch (item.getItemId()) {
            case android.R.id.home:
                Intent mainIntent2= new Intent(AddresseListeActivity.this, MapsActivity.class);
                startActivity(mainIntent2);
                return true;


        }
        return super.onOptionsItemSelected(item);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_addresseliste);
        lokasjon = getIntent().getStringExtra("lokasjon");
        addresseListe = (LinearLayout) findViewById(R.id.addresseListe);

        ActionBar actionBar = getSupportActionBar();

        actionBar.setDisplayHomeAsUpEnabled(true);

        mGoogleApiClient = new GoogleApiClient.Builder(this)
                .addConnectionCallbacks(this)
                .addOnConnectionFailedListener(this)
                .addApi(LocationServices.API)
                .build();

        mLocationRequest = LocationRequest.create()
                .setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY)
                .setInterval(10 * 1000)        // 10 seconds, in milliseconds
                .setFastestInterval(1 * 1000); // 1 second, in milliseconds
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);


        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {

            return;
        }
        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());

        GetAddressTask task = new GetAddressTask(lokasjon);
        try {
            this.addresser = task.execute().get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        visAddresserFunnet(addresseListe);

    }

    @Override
    public void onYesClick() {
        MarkerOptions marker = new MarkerOptions().position(new LatLng(tmpPoint.latitude, tmpPoint.longitude)).title("Beskrivelse:ffoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqfoqfqoqfq");
        mMap.addMarker(marker);
    }

    @Override
    public void onNoClick() {
        return;
    }

    public void visDialog(View v) {
        String lokasjon = String.valueOf(tmpPoint.latitude) + "," + String.valueOf(tmpPoint.longitude);
        GetAddressTask task = new GetAddressTask(lokasjon);
        System.out.println(lokasjon);
        task.execute();
        DialogFragment dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), "Avslutt");
    }


    private class getJSON extends AsyncTask<String, Void, String> {
        JSONObject jsonObject;

        @Override
        protected String doInBackground(String... urls) {
            String retur = "";
            String s = "";
            String output = "";
            for (String url : urls) {
                try {
                    URL urlen = new URL("http://192.168.100.77:82/jsonout.php");
                    HttpURLConnection conn = (HttpURLConnection)
                            urlen.openConnection();
                    conn.setRequestMethod("GET");
                    conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                    if (conn.getResponseCode() != 200) {
                        System.out.println(conn.getResponseCode());
                        throw new RuntimeException("Failed : HTTP errorcode: "
                                + conn.getResponseCode());
                    }
                    System.out.println("Before reading... .... \n");
                    BufferedReader br = new BufferedReader(new InputStreamReader((conn.getInputStream())));
                    System.out.println("Output from Server .... \n");
                    while ((s = br.readLine()) != null) {
                        output = output + s;
                    }
                    conn.disconnect();
                    try {
                        JSONArray mat = new JSONArray(output);


                        return mat.toString();
                    } catch (JSONException e) {
                        e.printStackTrace();
                    }
                    return retur;
                } catch (Exception e) {
                    return e.getLocalizedMessage();
                }
            }
            return retur;
        }

        @Override
        protected void onPostExecute(String ss) {
            textView.setText(ss);
        }
    }


    private LocationCallback mLocationCallback = new LocationCallback() {

        @Override
        public void onLocationResult(LocationResult locationResult) {
            Location mLastLocation = locationResult.getLastLocation();
            handleNewLocation(mLastLocation);
        }
    };


    public void handleNewLocation(Location location) {

        Log.d(TAG, location.toString());

        double currentLatitude = location.getLatitude();
        double currentLongitude = location.getLongitude();
        LatLng latLng = new LatLng(currentLatitude, currentLongitude);

    }

    @Override
    public void onConnected(Bundle bundle) {
        Log.i(TAG, "Location services connected.");

        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, 0);
        }
        if (checkSelfPermission(Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED
                && checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            return;
        }


        fusedLocationClient.getLastLocation().addOnCompleteListener(new OnCompleteListener<Location>() {
            @Override
            public void onComplete(@NonNull Task<Location> task) {
                Location location = task.getResult();
                if (location == null) {
                    requestNewLocationData();
                } else {
                    handleNewLocation(location);
                }
            }
        });


    }

    @SuppressLint("MissingPermission")
    private void requestNewLocationData() {

        // Initializing LocationRequest
        // object with appropriate methods
        LocationRequest mLocationRequest = new LocationRequest();
        mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        mLocationRequest.setInterval(5);
        mLocationRequest.setFastestInterval(0);
        mLocationRequest.setNumUpdates(1);


        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this);
        fusedLocationClient.requestLocationUpdates(mLocationRequest, mLocationCallback, Looper.myLooper());
    }

    @Override
    public void onConnectionSuspended(int i) {
        Log.i(TAG, "Location services suspended. Please reconnect.");

    }

    @Override
    public void onConnectionFailed(ConnectionResult connectionResult) {
        if (connectionResult.hasResolution()) {
            try {
                // Start an Activity that tries to resolve the error
                connectionResult.startResolutionForResult(this, CONNECTION_FAILURE_RESOLUTION_REQUEST);
            } catch (IntentSender.SendIntentException e) {
                e.printStackTrace();
            }
        } else {
            Log.i(TAG, "Location services connection failed with code " + connectionResult.getErrorCode());
        }

    }


    @Override
    protected void onResume() {
        super.onResume();

        mGoogleApiClient.connect();


    }

    @Override
    protected void onPause() {
        super.onPause();
        if (mGoogleApiClient.isConnected()) {
            mGoogleApiClient.disconnect();
        }

    }

    @Override
    public void onLocationChanged(Location location) {

        handleNewLocation(location);


    }


    @Override
    public void onMapReady(GoogleMap googleMap) {


        };


}
