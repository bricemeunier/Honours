package uk.ac.rgu.lab04.honours;

import android.Manifest;
import android.app.AlarmManager;
import android.app.AppOpsManager;
import android.app.PendingIntent;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Calendar;


public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        //Only for testing purpose
        forUITesting();

        //service sending location to server
        startLocationService();

        //service sending app usage stats
        startUsageStats();
    }

    /***************************
    **                        **
    **  USAGE STATS SERVICE   **
    **                        **
    ***************************/
    private void startUsageStats() {
        Intent intent = new Intent(this,UsageStatService.class);
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
    }


    /***************************
    **                        **
    **    LOCATION SERVICE    **
    **                        **
    ***************************/
    public void startLocationService(){
        Intent intent = new Intent(this,LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
    }

    /***************************
    **                        **
    **       UI TESTING       **
    **                        **
    ***************************/
    public void forUITesting(){
        Button btnContact = findViewById(R.id.button);
        Button btnApp = findViewById(R.id.button1);
        Button btnSms = findViewById(R.id.button2);
        Button btnLocation= findViewById(R.id.button3);


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 200);
        }

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS)
                != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, 200);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }

        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        if (mode != AppOpsManager.MODE_ALLOWED) {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 200);
        }


        btnContact.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                Intent intentMain = new Intent(MainActivity.this,
                        Contacts.class);
                MainActivity.this.startActivity(intentMain);
                Log.i("Content "," Contact ");
            }
        });

        btnSms.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                Intent intentMain = new Intent(MainActivity.this,
                        Sms.class);
                MainActivity.this.startActivity(intentMain);
                Log.i("Content "," SMS ");
            }
        });

        btnLocation.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intentMain = new Intent(MainActivity.this,
                        Location.class);
                MainActivity.this.startActivity(intentMain);
                Log.i("Content ", " Location ");
            }
        });

        btnApp.setOnClickListener(new View.OnClickListener() {

            public void onClick(View v) {
                Intent intentMain = new Intent(MainActivity.this,
                        Apps.class);
                MainActivity.this.startActivity(intentMain);
                Log.i("Content ", " Data ");
            }
        });





    }

}
