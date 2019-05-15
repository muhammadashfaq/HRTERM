package com.app.genialnykredyt.Broadcast;

import android.annotation.SuppressLint;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.os.Build;
import android.util.Log;

import com.app.genialnykredyt.Service.PingService;

public class RestarterBroadcast extends BroadcastReceiver {
    @SuppressLint("NewApi")
    @Override
    public void onReceive(Context context, Intent intent) {
        Log.i(RestarterBroadcast.class.getSimpleName(), "Service Stops! Oooooooooooooppppssssss!!!!");
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            Log.i(RestarterBroadcast.class.getSimpleName(), "in Oreo");
            context.startForegroundService(new Intent(context, PingService.class));
        } else if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M ) {
            Log.i(RestarterBroadcast.class.getSimpleName(), "in Marshmellow");
            context.startForegroundService(new Intent(context, PingService.class));
        }else{
            Log.i(RestarterBroadcast.class.getSimpleName(), "In Lollipop");
            context.startService(new Intent(context, PingService.class));
        }
    }
}
