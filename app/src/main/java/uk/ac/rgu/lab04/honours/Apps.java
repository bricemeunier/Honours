package uk.ac.rgu.lab04.honours;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.widget.TextView;

import java.util.Map;

public class Apps extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.app_list);


        TextView tv1=findViewById(R.id.tv1);
        tv1.setText(getData());
    }

    private String getData() {

        String list="";
        Integer i=0;
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String, UsageStats> lUsageStatsMap = mUsageStatsManager.
                queryAndAggregateUsageStats(0, System.currentTimeMillis());

        for (String key:lUsageStatsMap.keySet()){

            list+=key+"\n\n"+lUsageStatsMap.get(key).getTotalTimeInForeground()+"\n";
            i++;
        }
        list+="\n"+i+" apps";
        return list;
    }

}
