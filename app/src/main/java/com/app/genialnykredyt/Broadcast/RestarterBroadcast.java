package com.app.genialnykredyt.Broadcast;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.app.genialnykredyt.Service.PingService;

public class RestarterBroadcast extends BroadcastReceiver {
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(RestarterBroadcast.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            context.startForegroundService(new Intent(context, PingService.class));
        } else {
            Log.i(RestarterBroadcast.class.getSimpleName(), "In");
            context.startService(new Intent(context, PingService.class));
        }
    }
}
