package com.example.kartapp;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.DialogFragment;

import android.Manifest;
import android.annotation.SuppressLint;
import android.content.Intent;
import android.content.IntentSender;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationListener;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Looper;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;

import com.example.kartapp.database.DbHandlerSeverdighet;
import com.example.kartapp.database.models.Severdighet;
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
import com.google.android.gms.maps.MapsInitializer;
import com.google.android.gms.maps.OnMapReadyCallback;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.Marker;
import com.google.android.gms.maps.model.MarkerOptions;
import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.json.JSONStringer;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutionException;

public class MapsActivity extends AppCompatActivity implements
        GoogleApiClient.ConnectionCallbacks,    GoogleMap.OnMarkerClickListener,
        GoogleApiClient.OnConnectionFailedListener, MyDialog.DialogClickListener,EndreSlettDialog.DialogClickListener, LocationListener, OnMapReadyCallback {

    public static final String TAG = MapsActivity.class.getSimpleName();
    private final static int CONNECTION_FAILURE_RESOLUTION_REQUEST = 9000;
    private GoogleApiClient mGoogleApiClient;
    private LocationRequest mLocationRequest;
    private GoogleMap mMap;
    private FusedLocationProviderClient fusedLocationClient;

    LatLng tmpPoint;
    Button sokBtn;
    Button resetZoomBtn;
    Button zoomInBtn;
    Button zoomOutBtn;
    EditText adresseInp;
    Marker tmpMarker;
    DbHandlerSeverdighet dbHelperSeverdighet;
    SQLiteDatabase db;
    private void slettSeverdighetDB(Double lat, Double lng){
        dbHelperSeverdighet.slettSeverdighet(db, lat,lng);
    }
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_maps);
        dbHelperSeverdighet = new DbHandlerSeverdighet(this);
        db=dbHelperSeverdighet.getWritableDatabase();

        dbHelperSeverdighet.onCreate(db);

        MapFragment mapFragment = (MapFragment) getFragmentManager()
                .findFragmentById(R.id.map);
        mapFragment.getMapAsync(this);

        sokBtn = (Button) findViewById(R.id.sokBtn);
        resetZoomBtn = (Button) findViewById(R.id.resetZoomBtn);
        zoomInBtn = (Button) findViewById(R.id.zoomInBtn);
        zoomOutBtn = (Button) findViewById(R.id.zoomOutBtn);
        adresseInp = (EditText) findViewById(R.id.adresseInp);
        resetZoomBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.moveCamera(CameraUpdateFactory.zoomTo(1));

            };
            });
        zoomInBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.moveCamera(CameraUpdateFactory.zoomBy(1));

            };
        });
        zoomOutBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                mMap.moveCamera(CameraUpdateFactory.zoomBy(-1));

            };
        });


        sokBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                String adresse = adresseInp.getText().toString();
                GetLocationTask task = new GetLocationTask(adresse);
                CameraUpdateFactory.zoomTo(12);
                try {
                    String lokasjon =task.execute().get();
                    if(lokasjon != null){
                        String lat = lokasjon.split(":")[0];
                        String lng = lokasjon.split(":")[1];
                        LatLng latlng = new LatLng(Double.parseDouble(lat),Double.parseDouble(lng));
                        mMap.moveCamera(CameraUpdateFactory.zoomTo(16));
                        mMap.moveCamera(CameraUpdateFactory.newLatLng(latlng));
                    }


                } catch (ExecutionException e) {
                    e.printStackTrace();
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

            }
        });
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


    }

    @Override
    public void onYesClick() {
        String lokasjon = String.valueOf(tmpPoint.latitude) + ":" + String.valueOf(tmpPoint.longitude);
        Intent intentAddresseListe = new Intent(MapsActivity.this, AddresseListeActivity.class);
        intentAddresseListe.putExtra("lokasjon", lokasjon);
        startActivity(intentAddresseListe);
    }

    @Override
    public void onNoClick() {
        return;
    }

    public void visDialog(View v) {

        DialogFragment dialog = new MyDialog();
        dialog.show(getSupportFragmentManager(), "Avslutt");
    }
    public void visEndreSlettDialog(View v) {

        DialogFragment dialog = new EndreSlettDialog();
        dialog.show(getSupportFragmentManager(), "Avslutt");
    }
    @Override
    public void onEndreClick() {
        LatLng latlng = this.tmpMarker.getPosition();
        Intent intentEndreSlett = new Intent(MapsActivity.this, EndreSlettActivity.class);
        intentEndreSlett.putExtra("gateadresse", this.tmpMarker.getTitle());
        intentEndreSlett.putExtra("beskrivelse",this.tmpMarker.getSnippet());
        intentEndreSlett.putExtra("lat",Double.toString(latlng.latitude));
        intentEndreSlett.putExtra("lng",Double.toString(latlng.longitude));

        startActivity(intentEndreSlett);
    }

    @Override
    public void onSlettClick() {
        LatLng latlng = this.tmpMarker.getPosition();
        deleteJson task = new deleteJson(Double.toString(latlng.latitude),Double.toString(latlng.longitude));
        task.execute();
        slettSeverdighetDB(latlng.latitude,latlng.longitude);

        mMap.clear();
        getJSON getJSONTask = new getJSON();
        try {
            String res = getJSONTask.execute(new String[]{"http://data1500.cs.oslomet.no:80/~s349967/jsonout.php"}).get();
            System.out.println(res);
            JSONArray jsonObject = new JSONArray(res);
            for (int i = 0; i < jsonObject.length(); i++) {
                JSONObject jsonobject = jsonObject.getJSONObject(i);
                double lng = jsonobject.getDouble("lat");
                double lat = jsonobject.getDouble("lng");
                String beskrivelse = jsonobject.getString("beskrivelse");
                String gateadresse = jsonobject.getString("gateadresse");
                LatLng latLng = new LatLng(lng, lat);
                System.out.println(beskrivelse);
                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(gateadresse)
                        .snippet(beskrivelse);

                Marker markerMoreInfo = mMap.addMarker(options);
                mMap.setOnMarkerClickListener(this);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
    }

    @Override
    public void onAvbrytClick() {
        return;
    }

    @Override
    public boolean onMarkerClick(@NonNull Marker marker) {
        visEndreSlettDialog(null);
        this.tmpMarker = marker;
        return false;
    }

    private class deleteJson extends AsyncTask<String, Void,String> {
        JSONObject jsonObject;
        String lat;
        String lng;


        public deleteJson(String lat, String lng) {

            this.lat = lat;
            this.lng = lng;

        }


        @Override
        protected String doInBackground(String... urls) {
            String retur = "";
            String s = "";
            String output = "";

            try{
                URL urlen= new URL("http://data1500.cs.oslomet.no/~s349967/jsonDelete.php?lat="+lat+"&lng="+lng);
                HttpURLConnection conn= (HttpURLConnection)
                        urlen.openConnection();
                conn.setRequestMethod("GET");
                conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                if (conn.getResponseCode() != 200) {
                    System.out.println(conn.getResponseCode());
                    throw new RuntimeException("Failed : HTTP errorcode: "
                            + conn.getResponseCode());
                }
                System.out.println("Before reading... .... \n");
                BufferedReader br= new BufferedReader(new InputStreamReader((conn.getInputStream())));
                System.out.println("Output from Server .... \n");
                while ((s = br.readLine()) != null) {
                    output = output + s;
                }
                conn.disconnect();
                try{
                    JSONArray mat = new JSONArray(output);


                    return mat.toString();
                } catch(JSONException e) {
                    e.printStackTrace();
                }
                return retur;
            } catch(Exception e) {
                return e.getLocalizedMessage();
            }


        }
        @Override
        protected void onPostExecute(String ss) {

        }
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
                    URL urlen = new URL(urls[0]);
                    HttpURLConnection conn = (HttpURLConnection)
                            urlen.openConnection();
                    conn.setRequestMethod("GET");

                    conn.setRequestProperty("Accept", "text/html,application/xhtml+xml,application/xml;q=0.9,image/webp,image/apng,*/*;q=0.8,application/signed-exchange;v=b3;q=0.9");
                    if (conn.getResponseCode() != 200) {
                        List<Severdighet> severdighetList = dbHelperSeverdighet.finnAlleSeverdigheter(db);
                        JSONArray jsonArray = new JSONArray();
                        for (int i = 0; i < severdighetList.size(); i++){
                            System.out.println("TREEEEEEEEEEEEEEEEEEEEE");
                            JSONObject myJsonObject = new JSONObject();
                            myJsonObject.put("lat", severdighetList.get(i).getLat());
                            myJsonObject.put("lng",  severdighetList.get(i).getLng());
                            myJsonObject.put("gateadresse",  severdighetList.get(i).getGateadresse());
                            myJsonObject.put("beskrivelse",  severdighetList.get(i).getBeskrivelse());
                            jsonArray.put(i,myJsonObject);

                        }
                        System.out.println((jsonArray.toString()));
                        return jsonArray.toString();
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

        MarkerOptions options = new MarkerOptions()
                .position(latLng)
                .title("Jeg er her!");
        mMap.addMarker(options);
       // mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));

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
        mMap = googleMap;
        getJSON task = new getJSON();
        try {
            String res = task.execute(new String[]{"http://data1500.cs.oslomet.no/~s349967/jsonout.php"}).get();
            System.out.println(res);
            JSONArray jsonObject = new JSONArray(res);
            for (int i = 0; i < jsonObject.length(); i++) {
                JSONObject jsonobject = jsonObject.getJSONObject(i);
                double lng = jsonobject.getDouble("lng");
                double lat = jsonobject.getDouble("lat");
                String beskrivelse = jsonobject.getString("beskrivelse");
                String gateadresse = jsonobject.getString("gateadresse");
               LatLng latLng = new LatLng(lat, lng);

                MarkerOptions options = new MarkerOptions()
                        .position(latLng)
                        .title(gateadresse)
                        .snippet(beskrivelse);

                Marker markerMoreInfo = mMap.addMarker(options);
                mMap.setOnMarkerClickListener(this);
                mMap.moveCamera(CameraUpdateFactory.newLatLng(latLng));
            }

        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } catch (JSONException e) {
            e.printStackTrace();
        }
        mMap.setOnMapClickListener(new GoogleMap.OnMapClickListener() {
            @Override
            public void onMapClick(LatLng point) {
                tmpPoint = point;
                visDialog(null);
            }

        });

    }



    private class GetLocationTask extends AsyncTask<Void, Void, String> {
        JSONObject jsonObject;
        String address;
        String lokasjon;

        public GetLocationTask(String a) {
            this.address = a;
        }

        @Override
        protected String doInBackground(Void... params) {
            String s = "";
            String output = "";
            System.out.println(address);
            String query = "https://maps.googleapis.com/maps/api/geocode/json?address="+
             address.replaceAll(" ", "%20") + "&key=AIzaSyA7kkXcZ4w6rEBFWJy2X0dWuMzC9g_rJjk";
            try {
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
                Double lng = Double.valueOf(0);
                Double lat = Double.valueOf(0);
                lng =
                        ((JSONArray)
                                jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lng");
                lat = ((JSONArray)
                        jsonObject.get("results")).getJSONObject(0).getJSONObject("geometry").getJSONObject("location").getDouble("lat");
                lokasjon = String.valueOf(lat) + " : " + String.valueOf(lng);
                return lokasjon;
            } catch (IOException ex) {
                ex.printStackTrace();
            } catch (JSONException ex) {
                ex.printStackTrace();
            }
            return lokasjon;
        }

    }
    @Override
    protected void onDestroy() {
        dbHelperSeverdighet.close();
        super.onDestroy();
    }
}
