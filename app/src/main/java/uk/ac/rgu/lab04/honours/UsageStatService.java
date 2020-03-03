package uk.ac.rgu.lab04.honours;

import android.app.AlarmManager;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.Service;
import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.IBinder;

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
import java.util.Date;
import java.util.GregorianCalendar;
import java.util.Map;

public class UsageStatService extends Service {

    private UsageStatsManager mUsageStatsManager;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startForeground(3, getNotification());
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        //log.d(TAG, "onStartCommand:");
        super.onStartCommand(intent, flags, startId);
        fetchUsage();
        stopSelf();
        return START_NOT_STICKY;
    }

    @Override
    public void onDestroy(){
        //Log.d(TAG, "onDestroy: ");
        PendingIntent pendingIntent = null;
        Intent intent = new Intent(this,UsageStatService.class);

        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            pendingIntent = PendingIntent.getForegroundService(this,  2100, intent, 0);
        }
        else {
            PendingIntent.getService(this,  2100, intent, 0);
        }
        //alarm manager
        AlarmManager alarmManager = (AlarmManager)this.getSystemService(Context.ALARM_SERVICE);
        Calendar calendar = Calendar.getInstance();
        calendar.setTimeInMillis(System.currentTimeMillis());
        alarmManager.setExactAndAllowWhileIdle(AlarmManager.RTC_WAKEUP, calendar.getTimeInMillis()+ AlarmManager.INTERVAL_HOUR, pendingIntent);
    }

    //fetching last straight hour usage stat (.i.e from 8:00 to 9:00)
    private void fetchUsage() {
        initializeUsageStatsManager();

        //Get current time
        Date dt=new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(dt);

        //calculate millisecond between now and last o'clock
        int min=calendar.get(Calendar.MINUTE);
        int sec=calendar.get(Calendar.SECOND);
        int milliSinceLastHour=min*60*1000+sec*1000;

        long lastOClock=System.currentTimeMillis()-milliSinceLastHour-3600000;
        long oClock=System.currentTimeMillis()-milliSinceLastHour;

        //fetching usage stat from last hour
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String, UsageStats> lUsageStatsMap = mUsageStatsManager.
                queryAndAggregateUsageStats(lastOClock, oClock);

        //get stats for each app used
        for (String key:lUsageStatsMap.keySet()){
            if (lUsageStatsMap.get(key).getTotalTimeInForeground()/1000>0) {
                insertData(Constants.getPrivateKey(this),String.valueOf(lastOClock),key, String.valueOf(lUsageStatsMap.get(key).getTotalTimeInForeground() / 1000));
            }
        }
    }

    //Initialize the usageStatsManager
    private void initializeUsageStatsManager() {
        if (mUsageStatsManager == null) {
            mUsageStatsManager = (UsageStatsManager) getApplicationContext().getSystemService(Context.USAGE_STATS_SERVICE);
        }
    }

    //Creating the notification shown for background service (must after android 8.0)
    private Notification getNotification() {
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("usageStatService", "Usage Stat", NotificationManager.IMPORTANCE_DEFAULT);
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(getApplicationContext(), "usageStatService").setAutoCancel(true);
        }
        return builder.build();
    }



    //send data to phpmyadmin
    public static void insertData(final String key, final String timePeriod, final String app, final String timeUsed){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                String reg_url=Constants.URL_SERVER+"insert/insertUsageStat.php";
                String key=params[0];
                String timePeriod=params[1];
                String app=params[2];
                String timeUsed=params[3];
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
                            +"&"+URLEncoder.encode("timePeriod","UTF-8")+"="+URLEncoder.encode(timePeriod,"UTF-8")
                            +"&"+URLEncoder.encode("app","UTF-8")+"="+URLEncoder.encode(app,"UTF-8")
                            +"&"+URLEncoder.encode("timeUsed","UTF-8")+"="+URLEncoder.encode(timeUsed,"UTF-8");
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

        sendPostReqAsyncTask.execute(key,timePeriod,app,timeUsed);
    }

}
