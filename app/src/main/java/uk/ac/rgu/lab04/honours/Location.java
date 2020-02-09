package uk.ac.rgu.lab04.honours;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.location.LocationManager;
import android.os.Bundle;
import android.support.v4.app.ActivityCompat;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

//ONLY FOR UI PURPOSE
//WILL BE DELETED ONCE APP IN BETA
public class Location extends AppCompatActivity{

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.location);

        getLocation();

    }


    public boolean getLocation() {
        Log.d("ici gg", "getLocation: ");
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            LocationManager lm = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
            android.location.Location location = lm.getLastKnownLocation(LocationManager.GPS_PROVIDER);
            String longitude = String.valueOf(location.getLongitude());
            String latitude = String.valueOf(location.getLatitude());

            TextView tv=findViewById(R.id.tv_location);
            tv.setText("lat: "+latitude+"\nlon: "+longitude);

            return true;
        }
        else return false;

    }
}
