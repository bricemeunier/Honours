package uk.ac.rgu.lab04.honours;

import android.Manifest;
import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.provider.Settings;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;


public class MainActivity extends AppCompatActivity {


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        Button btnContact = (Button) findViewById(R.id.button);
        Button btnApp = (Button) findViewById(R.id.button1);

        if (ContextCompat.checkSelfPermission(getApplicationContext(), Manifest.permission.READ_CONTACTS)
                == PackageManager.PERMISSION_GRANTED) {
            btnContact.setOnClickListener(new View.OnClickListener(){

                public void onClick(View v) {
                    Intent intentMain = new Intent(MainActivity.this,
                            Contacts.class);
                    MainActivity.this.startActivity(intentMain);
                    Log.i("Content "," Main layout ");
                }
            });
        }
        else {
            requestPermissions(new String[]{Manifest.permission.READ_CONTACTS}, 200);
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
                    Log.i("Content ", " Main layout ");
                }
            });
        }
        else {
            startActivityForResult(new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS), 200);
        }

    }

}
