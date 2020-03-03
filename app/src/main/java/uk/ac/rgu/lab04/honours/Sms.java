package uk.ac.rgu.lab04.honours;

import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import androidx.appcompat.app.AppCompatActivity;
import android.widget.TextView;

public class Sms extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.sms_list);

        TextView tv=findViewById(R.id.tv_mobile);
        tv.setText(readSms());

    }

    public String readSms(){

        Uri uri = Uri.parse("content://sms/sent");
        Cursor c = getContentResolver().query(uri, null, null ,null,null);

        String messages="";
        int numberSms=c.getCount();

        if (c.getCount()>500){
            numberSms=500;
        }
        // Read the sms data
        if(c.moveToFirst()) {
            for(int i = 0; i < numberSms; i++) {
                String mobile = c.getString(c.getColumnIndexOrThrow("address"));
                String message = c.getString(c.getColumnIndexOrThrow("body"));
                messages+="num: "+mobile+"\n"+message+"\n";
                c.moveToNext();
            }
        }
        messages+="\n"+numberSms+" sms displayed";
        messages+="\n"+c.getCount()+" total sms";


        c.close();

        return messages;

    }
}
