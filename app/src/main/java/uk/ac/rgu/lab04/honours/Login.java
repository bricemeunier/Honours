package uk.ac.rgu.lab04.honours;

import android.app.AppOpsManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.inputmethod.EditorInfo;
import android.widget.EditText;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.security.crypto.EncryptedSharedPreferences;
import androidx.security.crypto.MasterKeys;

import java.io.IOException;
import java.net.HttpURLConnection;
import java.net.URL;
import java.security.GeneralSecurityException;
import java.util.concurrent.ExecutionException;

public class Login extends AppCompatActivity {

    private String masterKeyAlias;

    private SharedPreferences sharedPreferences = null;

    private EditText edittext = null;

    private TextView errorMsg = null;

    private static boolean isKeyValid=false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        if (Login() && checkingPermissions()) {
            validLogin();
        }
        else {
            setContentView(R.layout.activity_login);
            edittext = findViewById(R.id.editText);
            errorMsg = findViewById(R.id.textView4);
            StartLogin();
        }
    }

    public boolean checkingPermissions(){

        boolean usageAllowed=false;

        AppOpsManager appOps = (AppOpsManager)
                getSystemService(Context.APP_OPS_SERVICE);
        int mode = appOps.checkOpNoThrow(AppOpsManager.OPSTR_GET_USAGE_STATS,
                android.os.Process.myUid(), getPackageName());

        if (mode != AppOpsManager.MODE_ALLOWED) {
            Intent intent = new Intent(Settings.ACTION_USAGE_ACCESS_SETTINGS);
            startActivity(intent);
        }
        else {
            usageAllowed=true;
        }

        // The request code used in ActivityCompat.requestPermissions()
        // and returned in the Activity's onRequestPermissionsResult()
        int PERMISSION_ALL = 1;
        String[] PERMISSIONS = {
                android.Manifest.permission.READ_CONTACTS,
                android.Manifest.permission.READ_SMS,
                android.Manifest.permission.ACCESS_FINE_LOCATION,
        };

        if (!hasPermissions(this, PERMISSIONS)) {
            ActivityCompat.requestPermissions(this, PERMISSIONS, PERMISSION_ALL);
        }
        else {
            return usageAllowed;
        }
        return false;
    }

    public static boolean hasPermissions(Context context, String... permissions) {
        if (context != null && permissions != null) {
            for (String permission : permissions) {
                if (ActivityCompat.checkSelfPermission(context, permission) != PackageManager.PERMISSION_GRANTED) {
                    return false;
                }
            }
        }
        return true;
    }

    public boolean Login() {
        try {
            masterKeyAlias = MasterKeys.getOrCreate(MasterKeys.AES256_GCM_SPEC);
            sharedPreferences = EncryptedSharedPreferences.create(
                    "secret_shared_prefs",
                    masterKeyAlias,
                    this,
                    EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
                    EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
            );
        } catch (GeneralSecurityException e) {
            e.printStackTrace();
        } catch (IOException e) {
            e.printStackTrace();
        }

        // FOR TESTING PURPOSE
        //
        /*
        SharedPreferences.Editor editor = sharedPreferences.edit();
        editor.remove("privateKey");
        editor.apply();
        */
        ///////////////////////
        return sharedPreferences.contains("privateKey");
    }

    public void validLogin(){
        Intent intentMain = new Intent(Login.this,MainActivity.class);
        Login.this.startActivity(intentMain);
    }

    public void StartLogin() {

        edittext.setOnEditorActionListener(new TextView.OnEditorActionListener() {
            @Override
            public boolean onEditorAction(TextView textView, int i, KeyEvent keyEvent) {
                if(i== EditorInfo.IME_ACTION_DONE && checkingPermissions()){
                    validateKey(edittext.getText().toString());
                    return true;
                }
                return false;
            }
        });
    }

    public void validateKey(String key){

        SharedPreferences.Editor editor = sharedPreferences.edit();

        VerifyKey vk=new VerifyKey();
        try {
            vk.execute(key).get();
        } catch (ExecutionException e) {
            e.printStackTrace();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }

        if (isKeyValid()){
            editor.putString("privateKey",key);
            editor.apply();
            validLogin();
        }
        else {
            edittext.setText("");
            errorMsg.setText("Error, the private key you entered does not exist");
        }
    }

    public static void setIsKeyValid(boolean isKeyValid) {
        Login.isKeyValid = isKeyValid;
    }

    public boolean isKeyValid() {
        return isKeyValid;
    }

    private class VerifyKey extends AsyncTask<String, String, String> {
        HttpURLConnection conn;
        URL url = null;
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
        }

        // This method does not interact with UI, You need to pass result to onPostExecute to display
        @Override
        protected String doInBackground(String... params) {
            String key=params[0];
            try {
                // Enter URL address where your php file resides
                url = new URL(Constants.URL_SERVER+"verifyKey.php?key="+key);

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            }
            try {
                // Setup HttpURLConnection class to send and receive data from php
                conn = (HttpURLConnection) url.openConnection();
                conn.setReadTimeout(10000);
                conn.setConnectTimeout(15000);
                conn.setRequestMethod("GET");

            } catch (IOException e1) {
                e1.printStackTrace();
                return e1.toString();
            }

            try {
                int response_code = conn.getResponseCode();

                // Check if successful connection made
                if (response_code == HttpURLConnection.HTTP_OK) {
                    Login.setIsKeyValid(true);
                    return ("ok");

                } else {

                    return ("unsuccessful");
                }

            } catch (IOException e) {
                e.printStackTrace();
                return e.toString();
            } finally {
                conn.disconnect();
            }


        }

    }

}
