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

        //service receiving sms
        startSmsService();

    }


    /***************************
    **                        **
    **       SMS SERVICE      **
    **                        **
    ***************************/
    public void startSmsService() {
        Intent intentSms = new Intent(this,SmsService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intentSms);
        }
        else {
            startService(intentSms);
        }
    }


    /***************************
    **                        **
    **    LOCATION SERVICE    **
    **                        **
    ***************************/
    public void startLocationService(){
        Intent intent = new Intent(this,LocationService.class);
        PendingIntent pendingIntent = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            pendingIntent = PendingIntent.getForegroundService(this,  0, intent, 0);
        }
        else {
            PendingIntent.getService(this,  0, intent, 0);
        }
        //alarm manager
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        calendar.add(Calendar.SECOND,0); // first time
        long frequency= 600 * 1000; // in ms
        alarmManager.setRepeating(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis(), frequency, pendingIntent);
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
                == PackageManager.PERMISSION_GRANTED) {
            btnContact.setOnClickListener(new View.OnClickListener(){

                public void onClick(View v) {
                    Intent intentMain = new Intent(MainActivity.this,
                            Contacts.class);
                    MainActivity.this.startActivity(intentMain);
                    Log.i("Content "," Contact ");
                }
            });
        }
        else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 200);
        }


        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_SMS)
                == PackageManager.PERMISSION_GRANTED) {
            btnSms.setOnClickListener(new View.OnClickListener(){

                public void onClick(View v) {
                    Intent intentMain = new Intent(MainActivity.this,
                            Sms.class);
                    MainActivity.this.startActivity(intentMain);
                    Log.i("Content "," SMS ");
                }
            });
        }
        else {
            requestPermissions(new String[]{Manifest.permission.READ_SMS}, 200);
        }


        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        if (mode == AppOpsManager.MODE_ALLOWED) {
            btnApp.setOnClickListener(new View.OnClickListener() {

                public void onClick(View v) {
                    Intent intentMain = new Intent(MainActivity.this,
                            Apps.class);
                    MainActivity.this.startActivity(intentMain);
                    Log.i("Content ", " Data ");
                }
            });
        }
        else {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 200);
        }

        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            btnLocation.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Intent intentMain = new Intent(MainActivity.this,
                            Location.class);
                    MainActivity.this.startActivity(intentMain);
                    Log.i("Content ", " Location ");
                }
            });
        }
        else {
            requestPermissions(new String[]{Manifest.permission.ACCESS_FINE_LOCATION}, 200);
        }
    }

}
