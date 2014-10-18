package com.example.bijil.example1;

import android.location.Location;
import android.os.Bundle;
import android.support.v4.app.FragmentActivity;

import com.example.rest.api.ParkingLocations;
import com.google.android.gms.location.LocationClient;
import com.google.android.gms.location.LocationRequest;
import com.google.android.gms.maps.CameraUpdateFactory;
import com.google.android.gms.maps.GoogleMap;
import com.google.android.gms.maps.SupportMapFragment;
import com.google.android.gms.maps.model.BitmapDescriptor;
import com.google.android.gms.maps.model.BitmapDescriptorFactory;
import com.google.android.gms.maps.model.LatLng;
import com.google.android.gms.maps.model.MarkerOptions;

import org.json.JSONArray;
import org.json.JSONObject;

//location related imports

public class MapsActivity extends FragmentActivity {

    private GoogleMap mMap; // Might be null if Google Play services APK is not available.

    //variables for getting location start
    // A request to connect to Location Services
    private LocationRequest mLocationRequest;

    // Stores the current instantiation of the location client in this object
    private LocationClient mLocationClient;

    boolean mUpdatesRequested = false;
    //end of location related variables

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        //location
        // Create a new global location parameters object
        mLocationRequest = LocationRequest.create();
        //Set the update interval
        //mLocationRequest.setInterval(LocationUtils.UPDATE_INTERVAL_IN_MILLISECONDS);
        // Use high accuracy
        //mLocationRequest.setPriority(LocationRequest.PRIORITY_HIGH_ACCURACY);
        // Set the interval ceiling to one minute
        //mLocationRequest.setFastestInterval(LocationUtils.FAST_INTERVAL_CEILING_IN_MILLISECONDS);
        // Note that location updates are off until the user turns them on
        //mUpdatesRequested = false;
        /*
         * Create a new location client, using the enclosing class to
         * handle callbacks.
         */
       // mLocationClient = new LocationClient(this, this, this);
        //end of location

        setContentView(R.layout.activity_maps);
        setUpMapIfNeeded();

    }

    @Override
    protected void onResume() {
        super.onResume();
        setUpMapIfNeeded();
    }

    /**
     * Sets up the map if it is possible to do so (i.e., the Google Play services APK is correctly
     * installed) and the map has not already been instantiated.. This will ensure that we only ever
     * call {@link #setUpMap()} once when {@link #mMap} is not null.
     * <p>
     * If it isn't installed {@link SupportMapFragment} (and
     * {@link com.google.android.gms.maps.MapView MapView}) will show a prompt for the user to
     * install/update the Google Play services APK on their device.
     * <p>
     * A user can return to this FragmentActivity after following the prompt and correctly
     * installing/updating/enabling the Google Play services. Since the FragmentActivity may not
     * have been completely destroyed during this process (it is likely that it would only be
     * stopped or paused), {@link #onCreate(Bundle)} may not be called again so we should call this
     * method in {@link #onResume()} to guarantee that it will be called.
     */
    private void setUpMapIfNeeded() {
        // Do a null check to confirm that we have not already instantiated the map.
        if (mMap == null) {
            // Try to obtain the map from the SupportMapFragment.
            mMap = ((SupportMapFragment) getSupportFragmentManager().findFragmentById(R.id.map))
                    .getMap();
            // Check if we were successful in obtaining the map.
            if (mMap != null) {
                setUpMap();
            }
        }
    }

    /**
     * This is where we can add markers or lines, add listeners or move the camera. In this case, we
     * just add a marker near Africa.
     * <p>
     * This should only be called once and when we are sure that {@link #mMap} is not null.
     */
    private void setUpMap() {
        //LocationClient locCl = new LocationClient();
        //Location mCurrentLocation;
        mMap.setMyLocationEnabled(true);

        mMap.setOnMyLocationChangeListener(new GoogleMap.OnMyLocationChangeListener() {

            @Override
            public void onMyLocationChange(Location arg0) {
                // TODO Auto-generated method stub
               float zoomLevel = 14;
                LatLng currentPosition = new LatLng(arg0.getLatitude(), arg0.getLongitude());
                mMap.clear();
                mMap.addMarker(new MarkerOptions().position(currentPosition).title("Me"));
                mMap.moveCamera(CameraUpdateFactory.newLatLngZoom(currentPosition, zoomLevel));
                mMap.moveCamera(CameraUpdateFactory.newLatLng(currentPosition));

                //fetching the parking locations
                try {
                    ParkingLocations parkloc = new ParkingLocations();
                    String url = "http://54.69.152.156:55321/csp/data/parking/query/point";
                    String radius="5000";
                    parkloc.execute(url,""+arg0.getLatitude(), arg0.getLongitude()+"" ,radius);

                    String resultJSON = parkloc.get();
                    addParkingMarker(resultJSON);
                }
                catch(Exception e){
                    e.printStackTrace();
                }
            }
        });
        //LatLng currentPosition = new LatLng(34.0205, -118.2856);
        //mMap.addMarker(new MarkerOptions().position(currentPosition).title("Me"));

    }

    private void addParkingMarker(String resultJSON){
        //parse the result json
        try {
            JSONArray parkingLocations = new JSONArray(resultJSON);
            for(int i=0; i<parkingLocations.length(); i++){
                JSONObject parkingLoc = parkingLocations.getJSONObject(i);
                String location = parkingLoc.getString("location");
                location=location.replace("POINT (","");
                location=location.replace(")","");
                String[] coordinates = location.split(" ");
                float longitude = Float.parseFloat(coordinates[0]);
                float latitude = Float.parseFloat(coordinates[1]);
                String name = parkingLoc.getString("name");
                LatLng parkingSpot = new LatLng(latitude, longitude);

               BitmapDescriptor parkingBitmap = BitmapDescriptorFactory.fromResource(R.drawable.parking_marker);
               mMap.addMarker(new MarkerOptions().position(parkingSpot).icon(parkingBitmap).title(name));
            }
        }
        catch(Exception e){
            e.printStackTrace();
        }
    }
}
