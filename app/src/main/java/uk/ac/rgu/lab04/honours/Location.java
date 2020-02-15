package uk.ac.rgu.lab04.honours;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;

import java.util.List;

//ONLY FOR UI PURPOSE
//WILL BE DELETED ONCE APP IN BETA
public class Location extends AppCompatActivity {


    LocationManager mLocationManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);
        android.location.Location myLocation = getLastKnownLocation();
        Log.d("lolol", String.valueOf(myLocation.getLatitude()));
        //getLocation();

    }


    private android.location.Location getLastKnownLocation() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        android.location.Location bestLocation = null;
        for (String provider : providers) {
            if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                //temp
            }
            android.location.Location l = mLocationManager.getLastKnownLocation(provider);
            if (l == null) {
                continue;
            }
            if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                // Found best last known location: %s", l);
                bestLocation = l;
            }
        }
        return bestLocation;
    }


    public boolean getLocation() {
        Log.d("ici gg", "getLocation: ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            android.location.Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
                Log.d("lolol", String.valueOf(lm.isLocationEnabled()));
                Log.d("lolol", String.valueOf(lm.isProviderEnabled(LocationManager.GPS_PROVIDER)));
            }
            /*
            String longitude = String.valueOf(location.getLongitude());
            String latitude = String.valueOf(location.getLatitude());

            TextView tv=findViewById(R.id.tv_location);
            tv.setText("lat: "+latitude+"\nlon: "+longitude);

             */
            return true;
        }
        else return false;

    }
}
