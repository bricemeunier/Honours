package uk.ac.rgu.lab04.honours;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
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
        long total_time=0;
        UsageStatsManager mUsageStatsManager = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String, UsageStats> lUsageStatsMap = mUsageStatsManager.
                queryAndAggregateUsageStats(System.currentTimeMillis()-86400000, System.currentTimeMillis());

        for (String key:lUsageStatsMap.keySet()){
            if (lUsageStatsMap.get(key).getTotalTimeInForeground()/1000/60>0) {
                long minutes = lUsageStatsMap.get(key).getTotalTimeInForeground() / 1000 / 60;
                list += key + " for " + minutes + " minutes\n\n";
                i++;
                total_time += minutes;
            }
        }
        long min_int=total_time%60;
        String min=""+min_int;
        if (min_int<10){
            min="0"+min_int;
        }
        long hour=(total_time-min_int)/60;
        list+="\nLast 24h activity: "+hour+"h"+min;
        list+="\n"+i+" apps used";
        return list;
    }

}
