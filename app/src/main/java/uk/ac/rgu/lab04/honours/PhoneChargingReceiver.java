package uk.ac.rgu.lab04.honours;

import android.app.PendingIntent;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;
import android.widget.Toast;

public class PhoneChargingReceiver extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        boolean alarmUsageUp = (PendingIntent.getBroadcast(context, 2100,
                new Intent(context,UsageStatService.class),
                PendingIntent.FLAG_NO_CREATE) == null);

        if (!alarmUsageUp) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context,UsageStatService.class));
            }
        }

        boolean alarmLocationUp = (PendingIntent.getBroadcast(context, 2101,
                new Intent(context,LocationService.class),
                PendingIntent.FLAG_NO_CREATE) == null);

        if (!alarmLocationUp) {
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
                context.startForegroundService(new Intent(context,LocationService.class));
            }
        }

    }
}
