package uk.ac.rgu.lab04.honours;

import android.database.Cursor;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.HashMap;
import java.util.Map;

public class Contacts  extends AppCompatActivity {

    Map<String, String> namePhoneMap = new HashMap<String, String>();
    String TAG = "honours";

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.contacts_list_view);

        TextView tv=findViewById(R.id.tv);
        tv.setText(getPhoneNumbers());

    }

    private String getPhoneNumbers() {

        Cursor phones = getContentResolver().query(ContactsContract.CommonDataKinds.Phone.CONTENT_URI, null, null, null, null);

        // Loop Through All The Numbers
        while (phones.moveToNext()) {

            String name = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.DISPLAY_NAME));
            String phoneNumber = phones.getString(phones.getColumnIndex(ContactsContract.CommonDataKinds.Phone.NUMBER));

            // Cleanup the phone number
            phoneNumber = phoneNumber.replaceAll("[()\\s-]+", "");

            // Enter Into Hash Map
            namePhoneMap.put(phoneNumber, name);

        }

        // Get The Contents of Hash Map in Log
        for (Map.Entry<String, String> entry : namePhoneMap.entrySet()) {
            String key = entry.getKey();
            Log.d(TAG, "Phone :" + key);
            String value = entry.getValue();
            Log.d(TAG, "Name :" + value);
        }

        String list="";
        Integer i=0;
        for (String key : namePhoneMap.keySet()) {
            list+=key + " - " + namePhoneMap.get(key)+"\n";
            i++;
        }
        list+="\n"+i+" contacts";

        phones.close();

        return list;
    }
}
