package com.app.genialnykredyt.Service;

import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.Service;
import android.content.Context;
import android.content.Intent;
import android.graphics.Color;
import android.os.Build;
import android.os.IBinder;
import android.support.v4.app.NotificationCompat;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.genialnykredyt.Broadcast.RestarterBroadcast;
import com.app.genialnykredyt.R;

import java.io.File;
import java.util.HashMap;
import java.util.Map;
import java.util.Timer;
import java.util.TimerTask;

public class PingService extends Service {
    Context context;
    public int counter=0;
    private Timer timer;
    private TimerTask timerTask;

    public PingService() {

    }

    @Override
    public void onCreate() {
        super.onCreate();
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O )
            startMyOwnForeground();
        else if(Build.VERSION.SDK_INT == Build.VERSION_CODES.M) {
            Log.i("onCreate","Innn");
            startMyOwnForeground();
        }else{
            startForeground(1, new Notification());
        }

    }


    //Making service notificaition to run continously in Oreo devices due to new android policy
    private void startMyOwnForeground(){
        String NOTIFICATION_CHANNEL_ID = "com.example.simpleapp";
        String channelName = "My Background Service";
        NotificationChannel chan = null;
        if (android.os.Build.VERSION.SDK_INT >= android.os.Build.VERSION_CODES.O) {
            chan = new NotificationChannel(NOTIFICATION_CHANNEL_ID, channelName, NotificationManager.IMPORTANCE_NONE);
            chan.setLightColor(Color.BLUE);
            chan.setLockscreenVisibility(Notification.VISIBILITY_PRIVATE);
            NotificationManager manager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
            assert manager != null;
            manager.createNotificationChannel(chan);

            NotificationCompat.Builder notificationBuilder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID);
            Notification notification = notificationBuilder.setOngoing(true)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("App is running in background")
                    .setPriority(NotificationManager.IMPORTANCE_MIN)
                    .setCategory(Notification.CATEGORY_SERVICE)
                    .build();
            startForeground(2, notification);
        }

        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.M){
            Log.i("timer","Marshmellow");
            NotificationCompat.Builder builder = new NotificationCompat.Builder(this, NOTIFICATION_CHANNEL_ID)
                    .setSmallIcon(R.drawable.ic_launcher_background)
                    .setContentTitle("Service")
                    .setContentText("Service is running")
                    .setPriority(NotificationCompat.PRIORITY_MIN);
            startForeground(2, builder.build());
        }

    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        super.onStartCommand(intent, flags, startId);
        Log.i("onStartCommand","in onStartCommand");
        startTimer();
        return START_STICKY;
    }

    public PingService(Context context) {
        this.context = context;
    }

    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }


    public void startTimer() {
        Log.i("startTimer","startTimer");
        //set a new Timer
        timer = new Timer();

        //initialize the TimerTask's job
        initializeTimerTask();

        //schedule the timer, to wake up every 1 second
        timer.schedule(timerTask, 4000, 4000); //
    }

    /**
     * it sets the timer to print the counter every x seconds
     */
    public void initializeTimerTask() {
        timerTask = new TimerTask() {
            public void run() {
                Log.i("in timer", "in timer ++++  "+ (counter++));
                startThread();
            }
        };
    }

    public static void trimCache(Context context) {
        try {
            //   Toast.makeText(context, "trim caches mn aya", Toast.LENGTH_SHORT).show();
            File dir = context.getCacheDir();
            if (dir != null && dir.isDirectory()) {
                deleteDir(dir);
            }
        } catch (Exception e) {

        }
    }

    private void startThread() {
        Log.i("in timer", "in startThread()  ");
        Thread mThread= new Thread(){
            public void run(){
                super.run();
                try {
                    Thread.sleep(1000);
                    trimCache(PingService.this);
                    StringRequest mStringRequest = new StringRequest(Request.Method.POST, "http://rfbasolutions.com/get_messages_api/ping_server_api.php", new Response.Listener<String>() {
                        @Override
                        public void onResponse(String response) {
                            Log.i("Response",response);

                        }
                    }, new Response.ErrorListener() {
                        @Override
                        public void onErrorResponse(VolleyError error) {
                            Log.i("Error",error.toString());
                        }
                    }){
                        @Override
                        protected Map<String, String> getParams() throws AuthFailureError {
                            HashMap<String,String> params=new HashMap<>();


                            String DEVICE_NAME = android.os.Build.MODEL;
                            DEVICE_NAME = DEVICE_NAME.toUpperCase();

                            if(DEVICE_NAME!=null){
                                Log.i("IMEII",DEVICE_NAME);
                                params.put("device_name",DEVICE_NAME);
                            }

                            params.put("ping","somthing");
                            return params;
                        }
                    };
                    RequestQueue mRequestQueue = Volley.newRequestQueue(PingService.this);
                    mRequestQueue.add( mStringRequest);
                }catch (InterruptedException e){
                    e.printStackTrace();
                }
            }
        };
        mThread.start();
    }

    public static boolean deleteDir(File dir) {

        if (dir != null && dir.isDirectory()) {
            String[] children = dir.list();
            for (int i = 0; i < children.length; i++) {
                boolean success = deleteDir(new File(dir, children[i]));
                if (!success) {
                    return false;
                }
            }
        }

// The directory is now empty so delete it
        return dir.delete();
    }



    /**
     * not needed
     */
    public void stoptimertask() {
        //stop the timer, if it's not already null
        if (timer != null) {
            timer.cancel();
            timer = null;
        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        Log.i("EXIT", "ondestroy!");
        Intent broadcastIntent = new Intent(this, RestarterBroadcast.class);
        sendBroadcast(broadcastIntent);
        stoptimertask();

    }
}
