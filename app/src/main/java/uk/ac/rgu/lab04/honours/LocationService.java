package uk.ac.rgu.lab04.honours;

import android.Manifest;
import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Binder;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.ContextCompat;
import android.util.Log;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Calendar;
import java.util.List;

import static android.app.Notification.CATEGORY_SERVICE;
import static android.app.Notification.VISIBILITY_SECRET;
import static android.support.v4.app.NotificationCompat.PRIORITY_MIN;


public class LocationService extends Service {
    private final LocationServiceBinder binder = new LocationServiceBinder();
    private final String TAG = "oklololol";
    //private LocationListener mLocationListener;
    private LocationManager mLocationManager;

    @Override
    public IBinder onBind(Intent intent) {
        return binder;
    }

    /*
    private class LocationListener implements android.location.LocationListener {

        private final String TAG = "LocationListener";
        private Location mLastLocation;

        LocationListener(String provider) {
            mLastLocation = new Location(provider);
        }

        @Override
        public void onLocationChanged(Location location) {
            mLastLocation = location;
            Log.i(TAG, "LocationChanged: "+location);
        }

        @Override
        public void onProviderDisabled(String provider) {
            Log.e(TAG, "onProviderDisabled: " + provider);
        }

        @Override
        public void onProviderEnabled(String provider) {
            Log.e(TAG, "onProviderEnabled: " + provider);
        }

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {
            Log.e(TAG, "onStatusChanged: " + status);
        }
    }

     */

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        Log.d(TAG, "onStartCommand:");
        super.onStartCommand(intent, flags, startId);
        startTracking();
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onCreate() {
        Log.i(TAG, "onCreate");
        startForeground(1, getNotification());
    }

    @Override
    public void onDestroy(){

        //Log.d(TAG, "onDestroy: ");
        PendingIntent pendingIntent = null;
        Intent intent = new Intent(this,LocationService.class);
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
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+ AlarmManager.INTERVAL_FIFTEEN_MINUTES, pendingIntent);
    }

    private void initializeLocationManager() {
        //log.d(TAG, "initializeLocationManager:");
        if (mLocationManager == null) {
            mLocationManager = (LocationManager) getApplicationContext().getSystemService(Context.LOCATION_SERVICE);
        }
    }

    public void startTracking() {
        Log.d(TAG, "startTracking: ");
        initializeLocationManager();
        android.location.Location l=getLocation();
        insertData(String.valueOf(l.getLatitude()),String.valueOf(l.getLongitude()));
        /*
        mLocationListener = new LocationListener(LocationManager.GPS_PROVIDER);
        try {
            int LOCATION_INTERVAL = 60000;
            int LOCATION_DISTANCE = 20;
            mLocationManager.requestLocationUpdates( LocationManager.GPS_PROVIDER, LOCATION_INTERVAL, LOCATION_DISTANCE, mLocationListener );
            insertData(String.valueOf(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLatitude()),String.valueOf(mLocationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER).getLongitude()));
        } catch (java.lang.SecurityException ex) {
            Log.i(TAG, "fail to request location update, ignore", ex);
        } catch (IllegalArgumentException ex) {
            Log.d(TAG, "gps provider does not exist " + ex.getMessage());
        }

         */

    }

    private android.location.Location getLocation() {
        mLocationManager = (LocationManager) getApplicationContext().getSystemService(LOCATION_SERVICE);
        List<String> providers = mLocationManager.getProviders(true);
        android.location.Location bestLocation = null;
        if (ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_FINE_LOCATION) == PackageManager.PERMISSION_GRANTED && ActivityCompat.checkSelfPermission(this, Manifest.permission.ACCESS_COARSE_LOCATION) == PackageManager.PERMISSION_GRANTED) {
            for (String provider : providers) {
                android.location.Location l = mLocationManager.getLastKnownLocation(provider);
                if (l == null) {
                    continue;
                }
                if (bestLocation == null || l.getAccuracy() < bestLocation.getAccuracy()) {
                    bestLocation = l;
                }
            }
        }
        return bestLocation;
    }

    private Notification getNotification() {


        // Create mandatory notification channel
        String channelId = "foregroundService";
        if (Build.VERSION.SDK_INT >= 26) {
            NotificationChannel channel = new NotificationChannel(channelId,
                    getString(R.string.app_name),
                    NotificationManager.IMPORTANCE_NONE);
            channel.setLockscreenVisibility(VISIBILITY_SECRET);
            NotificationManager nm =
                    (NotificationManager) getSystemService(NOTIFICATION_SERVICE);
            nm.createNotificationChannel(channel);
        }
        NotificationCompat.Builder nc =
                new NotificationCompat.Builder(this, channelId);
        nc.setSmallIcon(R.drawable.ic_launcher_foreground);
        nc.setColor(ContextCompat.getColor(this, R.color.colorPrimary));
        nc.setContentTitle("Service running");
        nc.setWhen(0); // Don't show the time
        nc.setOngoing(true);
        nc.setCategory(CATEGORY_SERVICE);
        nc.setVisibility(VISIBILITY_SECRET);
        nc.setPriority(PRIORITY_MIN);
        return nc.build();

        /*



        //Log.d(TAG, "getNotification:");
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("locationService", "Location", NotificationManager.IMPORTANCE_DEFAULT);
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(getApplicationContext(), "locationService").setAutoCancel(true);
        }
        return builder.build();

         */


    }

    public class LocationServiceBinder extends Binder {
        public LocationService getService() {
            //log.d(TAG, "getService:");
            return LocationService.this;
        }
    }

    //send data to phpmyadmin
    public static void insertData(final String latitude, final String longitude){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                String reg_url="http://35.178.169.116/insertLocation.php";
                String lat=params[0];
                String lon=params[1];
                try {
                    URL url = new URL(reg_url);
                    HttpURLConnection httpURLConnection =
                            (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    OutputStream OS = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new
                            OutputStreamWriter(OS, StandardCharsets.UTF_8));
                    String data= URLEncoder.encode("latitude","UTF-8")+"="+URLEncoder.encode(lat,"UTF-8")
                            +"&"+URLEncoder.encode("longitude","UTF-8")+"="+URLEncoder.encode(lon,"UTF-8");
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

        sendPostReqAsyncTask.execute(latitude,longitude);
    }

}

