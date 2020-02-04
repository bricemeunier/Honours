package uk.ac.rgu.lab04.honours;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.List;

public class Sms extends AppCompatActivity {

    private static final String TAG = "lol";
    List<String> list = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_list);

        TextView tv=findViewById(R.id.tv_mobile);
        tv.setText(readSms());

    }

    public String readSms(){

        Uri uri = Uri.parse("content://sms/inbox");
        Cursor c = getContentResolver().query(uri, null, null ,null,null);

        int numberSms=c.getCount();

        if (c.getCount()>500){
            numberSms=500;
        }
        // Read the sms data
        if(c.moveToFirst()) {
            for(int i = 0; i < numberSms; i++) {
                String mobile = c.getString(c.getColumnIndexOrThrow("address"));
                String message = c.getString(c.getColumnIndexOrThrow("body"));
                list.add("num: "+mobile+"\n"+message);

                c.moveToNext();
            }

        }
        String messages="";
        for (int i=0;i<list.size();i++){
            messages+=list.get(i)+"\n";
        }
        messages+="\n"+numberSms+" sms displayed";
        messages+="\n"+c.getCount()+" total sms";


        c.close();

        return messages;

    }
}
