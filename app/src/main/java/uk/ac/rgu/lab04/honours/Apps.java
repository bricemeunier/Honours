package uk.ac.rgu.lab04.honours;

import android.app.usage.UsageStats;
import android.app.usage.UsageStatsManager;
import android.content.Context;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.util.Log;
import android.widget.TextView;

import java.util.Calendar;
import java.util.Date;
import java.util.GregorianCalendar;
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

        Date dt=new Date();
        Calendar calendar = GregorianCalendar.getInstance();
        calendar.setTime(dt);

        //calculate millisecond between now and last o'clock
        int minu=calendar.get(Calendar.MINUTE);
        int sec=calendar.get(Calendar.SECOND);
        int milliSinceLastHour=minu*60*1000+sec*1000;

        long lastOClock=System.currentTimeMillis()-milliSinceLastHour-3600000-3600000;
        long oClock=System.currentTimeMillis()-milliSinceLastHour-3600000;
        String list1="";
        list1+=""+lastOClock+"\n"+oClock+"\n\n";
        //fetching usage stat from last hour
        UsageStatsManager mUsageStatsManager1 = (UsageStatsManager) this.getSystemService(Context.USAGE_STATS_SERVICE);
        Map<String, UsageStats> lUsageStatsMap1 = mUsageStatsManager1.
                queryAndAggregateUsageStats(lastOClock, oClock);
        for (String key:lUsageStatsMap1.keySet()){
            if (lUsageStatsMap1.get(key).getTotalTimeInForeground()/1000>0) {
                long secondes = lUsageStatsMap1.get(key).getTotalTimeInForeground() / 1000;
                Log.d("12test", key+" : "+lUsageStatsMap1.get(key).getLastTimeUsed());
                list1 += key + " for " + secondes + " secondes\n\n";
            }
        }
        list1+="\n\n\n";

        String list=list1;
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
